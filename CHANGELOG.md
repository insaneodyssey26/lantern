# Changelog

All notable changes to **Lantern** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased] - Lantern 2.0 (In Development)

### Rebranding & Architecture Overhaul
- **Project Renaming**: Rebranded from **Vigil** to **Lantern**, migrating the core Android application ID and package namespaces from `com.masum.vigil` to `com.masum.lantern`.
- **Dual Build Variant Isolation**: Configured independent debug and release builds:
  - **Debug**: `com.masum.lantern.debug` (`"Lantern (Debug)"`), allowing continuous development and live device testing alongside production builds without overwrite.
  - **Release**: `com.masum.lantern` (`"Lantern"`).
  - Dynamically scoped broadcast intents (`${applicationId}.action.STOP_VPN`, `${applicationId}.action.TOGGLE_SHIELD`) and `FileProvider` authorities to ensure strict component and IPC isolation.
- **Licensing & Privacy**: Formally adopted the [GNU General Public License v3.0 (GPLv3)](LICENSE) and published a zero-telemetry, 100% on-device [Privacy Policy](PRIVACY.md).

### Networking, VPN & DNS Engine
- **Active Sinkhole & Passive Audit**: Dual firewall operation modes:
  - **Block Trackers**: Proactively synthesizes immediate `0.0.0.0` or `NXDOMAIN` responses for known tracking, ad, and malware domains.
  - **Audit Only**: Passively inspects and logs network telemetry without dropping queries.
- **Encrypted DNS Transports**:
  - **DNS-over-HTTPS (DoH - RFC 8484)**: HTTP/2 wire-format payload framing over TLS 443 with custom `Lantern-Android-DoH` User-Agent.
  - **DNS-over-TLS (DoT - RFC 7858)**: 2-byte length-prefixed raw wire format over TLS 853.
  - Supported upstream resolvers: Cloudflare, Quad9, Google DNS, with hardcoded bootstrap IPs to prevent recursive DNS deadlock.
- **TTL Response Cache & Protected Sockets**:
  - Thread-safe 2,048-entry in-memory DNS cache honoring original record TTLs to eliminate redundant upstream round-trips.
  - Protected socket pool isolating upstream queries from entering the local VPN loop.
- **App-Level Socket Attribution**: Real-time `/proc/net/udp` socket mapping linking every DNS packet to its originating application UID, package name, and app icon.
- **OTA Blocklist Synchronizer**: Built-in background rule updater fetching over 80,000+ curated hosts from [StevenBlack/hosts](https://github.com/StevenBlack/hosts) with HTTP ETag caching and atomic Room database persistence.
- **Reverse-Label Suffix Trie**: In-memory radix trie providing sub-millisecond ($O(k)$) domain classification without regex performance overhead.

### UI & Expressive Design
- **Material 3 Expressive Design System**:
  - Custom floating capsule dock navigation with fluid indicator expansion and tactile feedback.
  - Calibrated typography powered by **Google Sans Flex** variable font.
  - AMOLED Pure Black mode and calibrated dark/light palette tokens.
  - Radar-inspired hero telemetry visualizer with dynamic status waves.
  - Connected button switchers and split-button controls.
- **Animated Gate Splash Screen**:
  - Custom hardware-accelerated Animated Vector Drawable (`avd_lantern_splash.xml`) featuring an animated gate opening glyph, settling dot, and smooth brand reveal.
  - System splash integration via `androidx.core:core-splashscreen` with post-reveal hold condition.
  - Density-specific launcher mipmaps, adaptive vector icons, and themed monochrome icon support.
- **Interactive Telemetry Dashboard**:
  - Contextual navigation clicking on blocked trackers, clean queries, and total inquiries navigating directly to pre-filtered traffic stream views.
  - Radial gauges and statistics breakdown pager.
- **Predictive Back Gestures & Motion**:
  - Predictive back animation container tracking 1:1 finger swipe gestures with dynamic corner rounding, scale transforms, and depth elevation.
  - Coordinated container scale-in and slide transitions between tabs and sub-screens.
- **Edge-to-Edge System Bar Synchronization**:
  - Seamless edge-to-edge layout across Android 10 to 16.
  - Disabled navigation bar contrast scrim (`isNavigationBarContrastEnforced = false`) for completely transparent navigation bars in light and dark modes.

### Forensics, Rules & Management
- **Forensic Data Export**: RFC 4180 compliant CSV export of query logs with automated escaping and secure system share sheet integration via Android `FileProvider`.
- **Custom Domain Rules**: Per-domain custom user allowlist and blocklist rules manageable directly from traffic feed inspection sheets or the dedicated rules dashboard.
- **Kernel-Level App Whitelist**: Granular per-application bypass (`VpnService.Builder.addDisallowedApplication`) routing trusted applications (e.g., banking, VoIP) directly through the physical network interface.
- **Persistent Notification with Quick Actions**: Low-overhead notification with live query and block counters, real-time percentage indicators, and quick `Stop` / `Toggle Mode` remote actions using `goAsync()`.
- **Editorial Onboarding & Spotlight Tour**: First-run walkthrough highlighting key firewall controls and onboarding introduction for new users.

### Performance & Testing
- **Compilation & Shrinking**: Full R8 code shrinking and resource optimization rules configured for Android Gradle Plugin 8.9+.
- **Comprehensive Test Suite**:
  - Robolectric and JUnit unit test suites covering ViewModels, repositories, Room DAOs, and domain use cases.
  - On-device instrumentation test harness verifying Compose navigation and backstack flows.

---

## [1.0.0] - 2026-06-23 - Vigil 1.0

### Added
- **Initial Release of Vigil**: First public version of the on-device Android DNS traffic monitor and firewall (`com.masum.vigil`).
- **Core VPN Interception**: Local `VpnService` tunnel intercepting UDP port 53 DNS packets.
- **On-Device Tracker Detection**: Bundled StevenBlack hosts blocklist for detecting tracking and ad queries.
- **Traffic Stream & History**: Real-time DNS query feed with app attribution, installed package icons, and Room database persistence.
- **Statistics Dashboard**: Visual breakdown of total requests, blocked trackers, and top querying applications.
- **Basic Configuration**: Settings screen with theme selection, upstream DNS selection, and log clearing.
