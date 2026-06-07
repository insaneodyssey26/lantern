# Vigil

Privacy-first, on-device DNS monitor for Android. Captures every DNS query your apps make, classifies them against a bundled tracker blocklist, and surfaces the results in real time — without sending any data off your device.

## What it does

- Creates a local VPN tunnel (no external routing) to intercept DNS traffic
- Parses raw UDP/IP packets, extracts domain names, forwards to a protected upstream resolver (Cloudflare 1.1.1.1)
- Classifies each query against an 80k-domain blocklist using an in-memory reverse-label trie
- Shows live query feed, per-app breakdown, session history, and aggregate stats
- Detects Private DNS (DoT/DoH) and surfaces a warning banner when active

## Architecture

Clean architecture across seven Gradle modules with strict dependency rules.

```
:app
  └─ wires DI, NavHost, MainActivity, VigilApp

:feature
  └─ ControlScreen · LiveFeedScreen · PerAppScreen · HistoryScreen · StatsScreen
     ViewModels (StateFlow only, no business logic)

:domain          ← pure JVM, no Android deps
  └─ Models · Repository interfaces · UseCases

:data
  └─ DnsLogRepositoryImpl · MonitoringRepositoryImpl · AppInfoRepositoryImpl
     BlocklistRepositoryImpl · RetentionScheduler · Mappers

:vpn
  └─ VigilVpnService · TunnelManager · PacketReadLoop · DnsForwarder
     DnsPacketParser · DnsPacketBuilder · ConnectionOwnerResolver
     MonitoringSessionBus

:core:database
  └─ Room: VigilDatabase · DnsQueryDao · SessionDao · entities

:core:ui
  └─ Material 3 Expressive theme · MonitoringToggle · VerdictChip
     StatCard · AppRow · EmptyState · AnimatedCounter

:core:common
  └─ DispatcherProvider · ResolverConfig · RetentionPolicy
```

Dependency rule: `:domain` depends on nothing. Every module depends on `:domain`. `:app` wires all.

## Data flow

```
App makes DNS query
  → OS routes to TUN interface (VPN)
  → PacketReadLoop reads raw IP packet
  → DnsPacketParser extracts domain, txId, source port/IP
  → ConnectionOwnerResolver resolves UID → AppInfo
  → DnsForwarder sends query via protect()ed DatagramSocket → 1.1.1.1
  → Response written back to TUN
  → MonitoringSessionBus emits DnsQuery
  → DnsLogRepositoryImpl classifies + batch-writes to Room
  → ViewModels collect via StateFlow/Flow
```

The VPN is a sink only — it never blocks or modifies any query. Every domain resolves normally.

## Key design decisions

| Decision | Rationale |
|---|---|
| Local VPN only (sentinel DNS /32 route) | Zero packet routing outside device |
| Plaintext Do53 only | DoT/DoH operate outside the TUN layer; surfaced honestly via banner |
| Reverse-label trie for blocklist | O(labels) lookup, ~80k domains load in <100ms |
| Batch writes with Mutex + 2s/50-item flush | Reduces Room contention on high-traffic devices |
| `MonitoringSessionBus` SharedFlow with DROP_OLDEST | Backpressure-safe; UI never blocks the capture loop |
| No auto-start on boot | Requires manual re-arm; avoids always-on battery drain |
| Canvas-drawn icons | Avoids `material-icons-core` dependency size |

## Blocklist

Bundled Steven Black unified hosts list — ~80,000 tracker/ad domains. Loaded once at service start into a reverse-label trie. Suffix matching: inserting `example.com` blocks `sub.example.com` automatically.

## Limitations

- **Private DNS (DoT/DoH):** queries bypass the TUN layer entirely. Vigil detects this and shows a warning, but cannot monitor these queries.
- **HTTPS/DNS-over-HTTPS:** same as above.
- **IPv6-only networks:** relay uses IPv4 DatagramSocket to Cloudflare; falls back gracefully.
- **No auto-start after reboot:** by design.
- **Tracking category detail:** blocklist provides binary tracker/clean only; no per-category breakdown.

## Build

```bash
./gradlew assembleDebug --no-configuration-cache
```

Requires Android Studio Hedgehog+ or AGP 9+. Min SDK 29 (Android 10).

## Tests

```bash
./gradlew :vpn:test :data:testDebugUnitTest :domain:test :feature:testDebugUnitTest
```

- DNS packet parser: label encoding, compression pointers (including circular/chained), truncated packets, malformed headers
- Blocklist trie: exact match, suffix match, partial-suffix rejection, case normalisation
- Blocklist loader: hosts-format parsing, comment/reserved-entry filtering
- DAO: insert, query ordering, summary aggregates, per-app grouping, retention prune
- ViewModels: state mapping, buffer cap, delegation to use cases
- Use cases: delegation + failure propagation
