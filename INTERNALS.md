# Vigil — How Everything Works

This document explains the complete internal story of Vigil: every layer, every component, and how they talk to each other. Read top to bottom for the full picture.

---

## What Vigil Actually Is

Vigil is a DNS monitor for Android. It watches every DNS query every app on the device makes, classifies it against a blocklist of ~83,600 tracker/ad domains, and shows you results in real time — entirely on-device, no network calls for analysis, no data leaving the phone.

It does not block anything. It observes and reports. Think of it as a DNS flight recorder.

---

## Module Map

```
:app                       — Activity, NavHost, theme, DI entrypoint
:core:common               — ResolverConfig, RetentionPolicy, DispatcherProvider
:core:ui                   — Reusable Compose components, VerdictChip, MonitoringToggle
:core:database             — Room DB, DAOs, entities, query tuples
:domain                    — Pure Kotlin models + repository interfaces + use cases
:data                      — All implementations: blocklist, repositories, DI modules
:vpn                       — VpnService, packet reading, DNS parsing, forwarder, bus
:feature                   — 5 screens with their ViewModels and UI state
```

No screen knows about the database. No ViewModel touches the VPN. Every layer only talks to the layer directly below it, through interfaces defined in `:domain`.

---

## The VPN Trick

Android has no raw packet sniffing API for third-party apps. The only way to inspect traffic is through `VpnService`, which lets an app create a virtual TUN (tunnel) network interface.

Vigil uses this, but with a very narrow scope: it does not route all traffic through itself. It only intercepts DNS.

**How the TUN is configured:**

```
TUN address : 10.0.0.1/32
DNS server  : 10.0.0.2  (a sentinel IP that doesn't exist anywhere)
Route       : 10.0.0.2/32 only
```

The device is told "send DNS queries to 10.0.0.2", and that IP is routed into the TUN interface, so packets arrive at Vigil's `FileInputStream`. No other traffic enters the tunnel. It's DNS-only interception.

`VpnService.Builder.setBlocking(true)` makes the `FileInputStream.read()` call block until a packet arrives, which is exactly what you want for a read loop.

---

## Startup Sequence

1. `VigilApp` (`@HiltAndroidApp`) is the `Application` class. Hilt sets up the DI graph here.
2. `MainActivity` launches. It hosts the single-activity Compose UI.
3. `VigilNavHost` builds the `NavHost` with 5 destinations and the `NavigationBar`.
4. Hilt creates `ControlViewModel`, which injects `BlocklistRepository`. That triggers `BlocklistRepositoryImpl.init`, which fires a background coroutine to load `hosts.txt` from assets and build the trie. This is async — classification calls before loading completes return `Verdict.Unknown`.
5. The user sees the Home screen. Nothing is running yet.

---

## Starting Monitoring (User Taps Start)

`ControlViewModel.onToggleClick()` calls `StartMonitoringUseCase`, which calls `MonitoringRepositoryImpl.startMonitoring()`. That does:

1. Checks if VPN consent is already granted (`VpnService.prepare()` returns null = already granted).
2. Inserts a new `SessionEntity` into Room to record this session's start time. Room gives back a generated `sessionId` (Long).
3. Sends a `startForegroundService` Intent to `VigilVpnService` with `ACTION_START` and the `sessionId`.
4. Sets `MonitoringState` to `Active(sessionId, startedAt)` in a `MutableStateFlow`.

`VigilVpnService.onStartCommand()` receives the intent. It:

1. Calls `sessionBus.resetReplayCache()` to clear any leftover queries from a previous session.
2. Creates the foreground notification ("Vigil is active").
3. Builds the TUN interface with `VpnService.Builder`.
4. Creates `DnsForwarder`, `ConnectionOwnerResolver`, and `PacketProcessor`.
5. Starts `PacketReadLoop` on a daemon thread named `vigil-packet-reader`.
6. Launches two coroutines: `countQueries()` and `refreshNotificationPeriodically()`.

---

## The Packet Pipeline

This is the hot path. Every DNS query by every app goes through here.

### Step 1 — PacketReadLoop

A single daemon `Thread` sits in a blocking loop:

```
FileInputStream(pfd.fileDescriptor).read(buffer)
```

`buffer` is a 32,767-byte `ByteArray` (maximum IPv4 packet size). When a packet arrives, it does `buffer.copyOf(n)` to create a fresh copy (so the buffer can be reused) and calls the `onPacket` lambda → `PacketProcessor.process()`.

### Step 2 — DnsPacketParser

`DnsPacketParser.parse()` dissects the raw bytes manually:

- Checks IPv4 version bit in byte 0 (`versionIhl ushr 4 != 4` → reject).
- Reads IP header length from the lower nibble of byte 0 (`(versionIhl and 0x0F) * 4`). IP headers can vary between 20–60 bytes due to options.
- Checks protocol byte (offset 9) = 17 (UDP). Non-UDP → null.
- Reads source IP (offset 12, 4 bytes) and dest IP (offset 16, 4 bytes) as big-endian `Int`s using bit-shifting.
- Jumps to the UDP header at `ipHeaderLen`. Reads source port (2 bytes) and dest port (2 bytes). Dest port must be 53 → else null.
- Reads UDP length (2 bytes at offset +4), computes DNS payload offset = `ipHeaderLen + 8`.
- Validates the DNS header: QR bit (bit 15 of flags word) must be 0 (query, not response). QDCOUNT must be > 0.
- Calls `parseDomainName()` which walks the DNS question section. DNS names are encoded as length-prefixed labels: `[len][label][len][label]...[0x00]`. It also handles DNS name compression pointers (0xC0 prefix), with a 10-jump limit to prevent infinite loops.
- Returns a `ParsedDnsQuery(transactionId, domain, queryType, sourceIp, destIp, sourcePort)`.

`extractDnsPayload()` runs separately to grab just the raw DNS bytes (strips IP+UDP headers) — needed to forward the query upstream unchanged.

### Step 3 — PacketProcessor

With the parsed query in hand:

1. `blocklistRepository.classify(parsed.domain)` — hits the trie (explained below). Returns `Verdict.Tracker`, `Verdict.Clean`, or `Verdict.Unknown`.
2. `resolver.getUid(parsed)` — calls `ConnectivityManager.getConnectionOwnerUid(IPPROTO_UDP, srcAddr, dstAddr)`. This is a system API that looks up which process owns the UDP socket matching these endpoints. Returns a Linux UID.
   - `intToAddress(ip)` converts the raw Int IP to `InetAddress` using `ByteBuffer.allocate(4).putInt(ip).array()` — the one NIO usage in the codebase.
3. `resolver.resolveAppInfo(uid)` — calls `PackageManager.getPackagesForUid(uid)` to get the package name(s) for that UID, then `getApplicationLabel()` for the human-readable app name. Results are cached in an `LruCache<Int, AppInfo>(512)` to avoid repeated PM calls.
4. Wraps everything into a `DnsQuery` domain model.
5. `sessionBus.tryEmit(query)` — pushes it onto the real-time bus.
6. `forwarder.forward(parsed, rawDns)` — ships the query to a real DNS server and writes the response back to the TUN.

### Step 4 — DnsForwarder

This runs in a coroutine on `Dispatchers.IO` so it doesn't block the packet reader thread.

- Opens a `DatagramSocket`, calls `VpnService.protect(socket)` so the socket is excluded from the VPN tunnel (otherwise the forwarded query would loop back).
- Sends the raw DNS bytes to `1.1.1.1:53` (Cloudflare, hardcoded in `ResolverConfig`). Timeout is 5 seconds.
- Waits for the UDP response in a 4096-byte buffer.
- Calls `DnsPacketBuilder.wrapResponse()` to re-wrap the DNS response into a proper IPv4+UDP packet, with source/dest IPs and ports swapped (the TUN interface sees it as coming from `10.0.0.2`).
- `synchronized(output) { output.write(wrapped) }` — writes back to the TUN. `synchronized` because multiple forwarder coroutines could be writing concurrently.

`DnsPacketBuilder.wrapResponse()` builds the IPv4 header manually: version+IHL byte `0x45`, total length, TTL `0x40` (64), protocol `0x11` (UDP), computes the IP checksum via ones-complement addition, then appends the UDP header and DNS payload. No NIO, just bit-shifting into a `ByteArray`.

---

## The Blocklist System

### The file

`data/src/main/assets/blocklist/hosts.txt` is the StevenBlack unified hosts list (~90k lines, ~83,600 actual domains after stripping comments and housekeeping entries like `0.0.0.0 localhost`).

Format: `0.0.0.0 doubleclick.net` or `127.0.0.1 ads.example.com`. One domain per line.

### BlocklistLoader

Parses the file line by line. Skips comments (`#`), blank lines, and known non-domain entries (`localhost`, IPv6 housekeeping names). Extracts the domain (everything after the first whitespace), lowercases it, and feeds it to the trie.

### BlocklistTrie

A reverse-label trie for efficient subdomain matching. When inserting `ads.doubleclick.net`, it splits into `["ads", "doubleclick", "net"]`, reverses to `["net", "doubleclick", "ads"]`, and walks/creates nodes. The last node is marked `isEnd = true`.

When matching `sub.ads.doubleclick.net`:
- Walks `net → doubleclick → ads` — `isEnd = true` at `ads` → match! Returns early.

This means if `ads.doubleclick.net` is blocked, so is any subdomain like `sub.ads.doubleclick.net`. Parent wildcard blocking for free.

`TrieNode` uses a `HashMap<String, TrieNode>` for O(1) average child lookups. The whole trie lives in memory (~83,600 nodes).

### BlocklistMatcher

`@Singleton`. Holds a `@Volatile` reference to the trie (null until loaded). `load()` is called once at startup on IO. If a classification call arrives before loading completes, it returns `Verdict.Unknown`. `classify()` is called on the packet reader thread — it's a pure read after the volatile write, so no locking is needed.

`Verdict` is a sealed class with three states: `Tracker(category: String)`, `Clean`, `Unknown`. Category is currently always empty (the StevenBlack list doesn't carry category metadata).

---

## The Real-Time Bus

`MonitoringSessionBus` is a `@Singleton` that wraps a `MutableSharedFlow<DnsQuery>`:

```
replay              = 100
extraBufferCapacity = 512
onBufferOverflow    = DROP_OLDEST
```

`replay = 100` means the last 100 queries are cached. A new subscriber (e.g., when the user navigates to the Live Feed tab for the first time) immediately receives those 100 rather than starting empty.

`extraBufferCapacity = 512` is a slack buffer for when consumers are slow. `DROP_OLDEST` ensures `tryEmit()` never fails — if the buffer is full, the oldest unseen item is evicted. `tryEmit()` is called from the packet reader thread (plain `Thread`) and is thread-safe.

`resetReplayCache()` is called at the start of each new monitoring session so the notification counter and a fresh Live Feed start clean.

Three things subscribe to this bus:
1. `DnsLogRepositoryImpl.collectAndBatchWrite()` — always running, writes to Room.
2. `VigilVpnService.countQueries()` — running while VPN is active, increments the notification counter.
3. `LiveFeedViewModel` — running while the Live Feed tab is alive, updates the UI list.

All three receive every emission concurrently. `SharedFlow` multicasts.

---

## Persistence (Room)

### Database

`VigilDatabase` is a Room database with two tables:

**`sessions`** (`SessionEntity`):
- `id` (autoGenerate PK)
- `startedAt` (Long, epoch ms)
- `endedAt` (Long?, null while active)

**`dns_queries`** (`DnsQueryEntity`):
- `id` (autoGenerate PK)
- `sessionId` (Long, FK to sessions)
- `domain` (String), `timestamp` (Long)
- `verdictType` (Int: 0=Clean, 1=Tracker, 2=Unknown), `verdictCategory` (String)
- `appUid` (Int), `appPackageName` (String), `appName` (String)
- Indices on `sessionId` and `domain` for fast filtering.

### Batch Writing

`DnsLogRepositoryImpl` does not write every query to Room immediately — that would cause write storms at high query rates.

Instead it uses a `Mutex`-guarded `ArrayList<DnsQueryEntity>` as a pending batch:
- Whenever a query arrives from the bus, it's added to the batch.
- If the batch hits 50 items → immediate `insertAll()`.
- A separate periodic coroutine flushes whatever's pending every 2 seconds regardless of size.

This keeps Room writes off the hot path and batched for efficiency. The Live Feed sees queries instantly via the bus; Room sees them up to 2 seconds later.

### Retention

`RetentionScheduler` runs hourly on IO. It does two passes:
1. `deleteOlderThan(cutoff)` — removes rows older than 7 days.
2. `trimToMaxRows(50_000)` — if the table exceeds 50,000 rows, deletes the oldest.

Both are SQL `DELETE` statements, not application-layer loops.

---

## Data Flow From ViewModel to UI

All ViewModels follow the same pattern: a `MutableStateFlow<UiState>` updated by coroutines, exposed as `StateFlow<UiState>`, collected in Compose with `collectAsStateWithLifecycle()`.

### ControlViewModel (Home screen)

- Observes `MonitoringState` (idle vs. active) from `MonitoringRepositoryImpl._state`.
- Observes `SummaryStats` from `DnsQueryDao.observeSummaryTotals()` + `observeTopDomains()` via Room `Flow` — updates whenever new rows are committed. Drives the "queries captured" counter and tracker ratio bar.
- Reads blocklist count once at init from `BlocklistRepository.domainCount()`.
- Checks Private DNS status via `ConnectivityManager.getLinkProperties()` to show a warning banner.

### LiveFeedViewModel (Live Feed screen)

- Subscribes to `bus.queries` via `ObserveLiveFeedUseCase → DnsLogRepository.observeLiveFeed()`.
- Maintains an in-memory `ArrayDeque<DnsQueryUiModel>` capped at 100 items, newest first.
- Each `DnsQuery` from the bus gets a local monotonic ID from `AtomicLong` — this replaces the Room-assigned `id` (which is always 0 for in-flight queries) to give `LazyColumn` stable unique keys.
- Also observes `MonitoringState` to show "Not monitoring" vs "No queries yet" in the empty state.

### HistoryViewModel (History screen)

- Observes `sessionDao.observeAllWithCounts()` — a Room `Flow` over a JOIN query that returns sessions with their total and tracker query counts.

### PerAppViewModel (Per-App screen)

- Observes `dnsQueryDao.observePerAppStats(sessionId)` — grouped by package name, with total and tracker counts. `sessionId = null` means all sessions combined.

### StatsViewModel (Stats screen)

- Combines `observeSummaryTotals()` and `observeTopDomains()` into a `SummaryStats` domain model.
- Also exposes blocklist count for the footer.
- Uses `stateIn(viewModelScope, WhileSubscribed(5_000), initialValue)` — the flow is only active while there's a subscriber (screen is visible), with a 5-second grace period before cancelling.

---

## Screen Structure

The outer `Scaffold` in `VigilNavHost` owns the `NavigationBar`. It applies only `calculateBottomPadding()` (navigation bar height) to the `NavHost` — not top padding — so inner `Scaffold`s in each screen can manage their own `TopAppBar` without double-counting the status bar inset.

Navigation uses `launchSingleTop = true` + `restoreState = true` so tapping a tab you're already on doesn't stack duplicates, and returning to a tab restores its scroll position and ViewModel state.

### Control (Home)

Two states via `Crossfade`: idle shows a shield icon + "Privacy at the network layer" copy, active shows a `PulsingCounter` (two expanding rings via `infiniteTransition` staggered 1 second apart) and a `TrackerRatioBar`. Elapsed session time is updated by a `LaunchedEffect` + `delay(1000)` loop.

### Live Feed

A `LazyColumn` of `QueryRow` items, newest at top. Each row shows domain, app name + timestamp, and a `VerdictChip` (red for Tracker, teal for Clean, grey for Unknown). Shows "LIVE" indicator in the TopAppBar while monitoring is active.

### Per-App

`LazyColumn` of `AppRow`s sorted by query count. Each shows app icon (placeholder), app name, and tracker/total counts. Can filter to a specific session via a session picker.

### History

`LazyColumn` of session cards. Active session shown in `primaryContainer`, past sessions in `surfaceVariant`. Each card shows start time, duration, and query counts.

### Stats

A non-scrolling (well, `verticalScroll` + `Column`) stats page. The centrepiece is a Canvas-drawn donut ring chart (220dp, 30dp stroke): red arc for tracker fraction, teal/primary arc for clean, grey background ring. Below it are three `StatPill` surfaces (Total, Trackers, Clean). Then a "Top domains" section with `DomainBar` — a spring-animated horizontal bar per domain, coloured red or teal by verdict.

---

## Dependency Injection

Hilt with `@Singleton` scope for all repositories, the bus, the blocklist matcher, and the database. `@HiltViewModel` for all ViewModels.

`DispatcherProvider` is an interface (`main`, `io`, `default`) injected everywhere that needs a dispatcher. In tests, a `TestDispatcherProvider` replaces it so coroutines are controllable.

DI modules live in `:data`:
- `DatabaseModule` — provides `VigilDatabase`, `DnsQueryDao`, `SessionDao`.
- `RepositoryModule` — `@Binds` interface → implementation for all four repositories.
- `BlocklistModule` — provides `BlocklistMatcher` (Hilt can inject it directly since it has `@Inject constructor`; this module exists for any extra config).
- `DispatcherModule` — provides `DispatcherProvider` bound to `DefaultDispatcherProvider`.

---

## What Does NOT Work (Known Limits)

- **DNS-over-TLS / DNS-over-HTTPS**: These are encrypted, go to port 853 or 443, not port 53. Vigil does not see them. If Private DNS is configured on the device, Vigil shows a warning banner but cannot intercept those queries.
- **IPv6**: The TUN only has an IPv4 address. IPv6 DNS is not intercepted.
- **Boot persistence**: The VPN service is `START_NOT_STICKY`. If the process is killed or the device reboots, monitoring does not resume automatically.
- **Blocking**: Vigil classifies but never drops packets. It always forwards to `1.1.1.1` and always writes the response back to the app. It is read-only.
