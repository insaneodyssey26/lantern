# Vigil — Implementation Plan

> Privacy-first, on-device DNS monitor. Local `VpnService` TUN, capture-parse-forward
> relay, on-device tracker detection. Zero egress.

## Locked decisions
- **Upstream resolver:** fixed public (Cloudflare `1.1.1.1`, fallback `9.9.9.9`) behind `ResolverConfig`.
- **Architecture:** multi-module clean architecture.
- **Retention:** rolling cap — keep 7 days OR 50k rows (whichever first), both configurable.
- **Boot:** no auto-start; manual re-arm after reboot.
- **Route strategy:** narrow DNS-only route (sentinel DNS IP + /32 route), not full tunnel.
- **Scope:** plaintext Do53 only. DoH/DoT out of scope → detect & surface honestly.
- **Relay model:** TUN is a sink; we read → parse → forward via `protect()`ed socket → write
  response back. "Never blocks" = policy-level (we never refuse a domain).
- **UI:** Jetpack Compose + **Material 3 Expressive** (shape morphing, spring motion,
  expressive type scale, button groups, flexible toolbars).

## Module graph
```
:app  ── wires DI, NavHost, MainActivity, VigilApp
:core:common    (pure Kotlin) dispatchers, Result, ResolverConfig, RetentionPolicy
:core:ui        Material3 Expressive theme, design system, shared components
:core:database  Room: VigilDatabase, DAOs, entities, converters
:domain         (pure JVM) models, repository interfaces, usecases
:data           repo impls, blocklist engine, app attribution data sources, mappers
:vpn            VigilVpnService, tunnel, engine, parser, attribution, session bus
:feature        live / apps / history / stats / control screens + VMs
```
Dependency rule: `:domain` depends on nothing; everyone depends on `:domain`; `:app` wires all.

---

## Phases

Each phase is independently buildable and verifiable. We do NOT do them all at once.
Exit criteria must pass before moving on.

### Phase 0 — Foundation & tooling
**Goal:** project compiles with all modules + DI skeleton.
- Add to version catalog: Hilt, KSP, Room, coroutines, navigation-compose, datastore,
  kotlinx-collections, junit5/turbine/mockk/robolectric for tests.
- Create module structure + `build.gradle.kts` per module with correct dependency edges.
- `VigilApp` (`@HiltAndroidApp`), `MainActivity` (`@AndroidEntryPoint`), empty NavHost.
- `:core:common`: `DispatcherProvider`, `ResolverConfig`, `RetentionPolicy` constants.
- **Exit:** `./gradlew assembleDebug` green; app launches to an empty scaffold.

### Phase 1 — Domain layer (the contracts)
**Goal:** every layer has interfaces to build against.
- Models: `DnsQuery`, `AppInfo`, `Verdict` (Tracker/Clean/Unknown), `SessionSummary`,
  `PerAppStat`, `MonitoringState`.
- Repository interfaces: `MonitoringRepository`, `DnsLogRepository`, `BlocklistRepository`,
  `AppInfoRepository`.
- UseCases (one responsibility each): `StartMonitoring`, `StopMonitoring`,
  `ObserveMonitoringState`, `ObserveLiveFeed`, `ClassifyDomain`, `GetPerAppStats`,
  `GetSessionHistory`, `GetSummaryStats`, `ClearHistory`.
- **Exit:** `:domain` compiles as pure JVM module; unit-test a trivial usecase with fakes.

### Phase 2 — DNS packet parser (riskiest pure logic — TDD first)
**Goal:** bulletproof, well-tested parser before any networking.
- `Ipv4Header` / `Ipv6Header` / `UdpHeader` decoders over `ByteBuffer`.
- `DnsPacketParser`: extract qname (handle **label encoding** + **compression pointers 0xC0**),
  qtype, transaction id, multiple questions, EDNS0; defensive against malformed/hostile input.
- Builder side: rewrite/repackage for the relay (swap addresses/ports, recompute checksums).
- **Exit:** comprehensive unit-test suite (golden packet fixtures, compression pointers,
  truncated packets, IPv6) all green. No Android dependency.

### Phase 3 — Core database (Room)
**Goal:** persistence layer with retention.
- Entities: `DnsQueryEntity`, `SessionEntity`; converters.
- DAOs: insert (batch), paged live query, per-app aggregate, summary aggregate,
  retention delete (`olderThan`, `trimToMaxRows`).
- `:core:database` migration strategy from v1.
- **Exit:** instrumented/Robolectric DAO tests pass, including retention prune query.

### Phase 4 — Blocklist engine
**Goal:** fast on-device classification.
- Bundle Steven Black hosts list in `:data` assets; build/normalize at load.
- `BlocklistMatcher`: reverse-label **trie** with suffix matching (`ads.x.com` → `x.com`),
  loaded once into memory at service start. Report true domain count.
- `BlocklistRepositoryImpl` + `ClassifyDomain` usecase wiring.
- **Exit:** unit tests for suffix matching edge cases; load-time + lookup benchmark recorded.

### Phase 5 — VPN engine
**Goal:** the capture-parse-forward core (headless, testable pieces).
- `TunnelManager`: `Builder` config (sentinel DNS, /32 route, MTU), PFD lifecycle,
  teardown, `onRevoke`, reconnect/backoff, handle `prepare()` consent.
- `engine/`: `PacketReadLoop` (dedicated thread, blocking read off PFD),
  `DnsForwarder` (`protect()`ed `DatagramSocket` → resolver → response back to TUN),
  `InFlightTable` (txid + src-port keyed, timeouts).
- `attribution/ConnectionOwnerResolver`: `getConnectionOwnerUid` + UID→package cache.
- `session/MonitoringSessionBus`: `@Singleton` `SharedFlow<DnsQuery>` (bounded, overflow policy).
- **Exit:** on-device smoke test — DNS resolves normally while running; queries observed
  in logs; no DNS breakage; CPU sane.

### Phase 6 — Data repositories
**Goal:** connect engine → DB → domain.
- `DnsLogRepositoryImpl`: observe bus, classify, attribute, **batch-write** Room
  (time/size windowed), expose paged + realtime Flows. Backpressure: `conflate`/`buffer`.
- `MonitoringRepositoryImpl`: start/stop service, expose `MonitoringState` Flow.
- `AppInfoRepositoryImpl`: PackageManager + icon/label cache.
- Retention scheduler (periodic prune within process lifetime).
- **Exit:** end-to-end with a fake UI collector; turbine tests on repository Flows.

### Phase 7 — Foreground service + permissions
**Goal:** stable, compliant always-on service.
- `VigilVpnService` (`@AndroidEntryPoint`): `startForeground` w/ correct FGS type,
  persistent notification (status, query count, stop action).
- Manifest: `BIND_VPN_SERVICE` service, FGS type + perms, `POST_NOTIFICATIONS`.
- Permission/consent flows: VPN `prepare()` consent, notification runtime perm,
  battery-optimization exemption prompt (user-initiated).
- **Exit:** survives Doze/app-swipe; notification accurate; consent + revocation handled.

### Phase 8 — Design system (Material 3 Expressive)
**Goal:** the visual foundation that makes screens shine.
- Migrate theme into `:core:ui`. Dynamic color + expressive color roles; light/dark.
- Expressive type scale; `MaterialShapes` morphing shapes; spring `MotionScheme`.
- Reusable components: monitoring toggle (animated, shape-morph state),
  verdict chips (tracker=error tone, clean=tertiary), app row w/ icon,
  stat cards, empty states, animated counters.
- Navigation: bottom nav / flexible toolbar per Expressive guidance.
- **Exit:** component gallery / previews render; light+dark; consistent tokens.

### Phase 9 — Feature screens
**Goal:** the five screens, UI-state-only VMs (StateFlow), no business logic.
- **Control / Home:** big expressive start/stop with state morph + status, Private DNS banner.
- **Live Feed:** real-time list (conflated), verdict labels, per-row app, smooth item animation.
- **Per-app breakdown:** ranked apps by tracker connections, expandable, charts.
- **Session history:** browse/clear, grouped by session/time.
- **Summary stats:** total queries, tracker %, top domains — expressive cards + animated viz.
- **Exit:** each VM exposes `StateFlow` only; previews + UI tests for state mapping.

### Phase 10 — Polish & measurement (portfolio goals)
**Goal:** hit and *document* the measurable targets.
- Private DNS detection banner (`isPrivateDnsActive`).
- Measure & record: blocklist coverage count, CPU %, per-query latency overhead,
  battery/hr. Add a lightweight in-app diagnostics screen or document method.
- Accessibility pass (content descriptions, touch targets, contrast, dynamic type).
- Edge cases: rapid start/stop, no network, IPv6-only, resolver unreachable fallback.
- **Exit:** README with architecture diagram + measured numbers; goals met or honestly noted.

### Phase 11 — Tests & docs
**Goal:** portfolio-grade quality bar.
- Unit: parser, matcher, usecases, mappers, VMs (turbine). 
- Instrumented: DAO, service smoke, permission flows.
- README: philosophy, architecture, module graph, limitations (DoH/DoT), screenshots, metrics.
- **Exit:** CI-green test suite; documented limitations; demo flow recorded.

---

## Ordering rationale
1. Foundation → contracts → **parser (highest pure-logic risk, TDD)** → DB → blocklist:
   all headless and unit-testable before touching the OS VPN stack.
2. VPN engine + service: the integration-risk core, validated on-device before UI.
3. Design system before screens: so screens are assembled from a finished, expressive kit.
4. Polish + measurement last: tune against a working app; produce portfolio artifacts.

## Cross-cutting principles
- Zero business logic in Composables/VMs beyond UI-state mapping.
- VpnService → `MonitoringSessionBus` → repository → usecase → VM → Compose. Never service→VM.
- ViewModels expose `StateFlow` only.
- All async via coroutines/Flow; reader loop on a dedicated thread, never a shared dispatcher.
- Every component independently testable; `:domain` SDK-free.
