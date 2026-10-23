# Privacy Policy for Lantern

**Last Updated:** October 2026

Lantern ("we", "our", or "the app") is an open-source, on-device DNS firewall and network monitor for Android. We believe privacy is a fundamental human right. 

This Privacy Policy explains how Lantern handles data. The short version: **Lantern collects zero personal data, runs zero analytics, operates no remote servers, and processes all network queries entirely on your physical device.**

---

## 1. Zero Data Collection & Zero Remote Servers

- **No Remote Servers:** Lantern does not own, operate, or communicate with any proprietary cloud infrastructure or analytics backend.
- **No Third-Party Trackers or SDKs:** Lantern contains **zero tracking SDKs, zero advertising libraries, and zero crash reporting beacons** (e.g., no Google Analytics, Firebase, Mixpanel, Crashlytics, or Facebook SDK).
- **No Accounts or Identification:** You are never asked to create an account, register an email address, or provide any identifying information.

---

## 2. On-Device Network Interception & VpnService

Lantern uses Android's `VpnService` API exclusively to create a **local-only loopback tunnel** on your device (`10.0.0.2/32`).

- **What Happens Locally:** Outbound DNS queries (port 53) are intercepted on your device to extract queried hostnames, check them against your local blocklist, and identify the originating application via Android's local `/proc/net` tables.
- **No Remote VPN Routing:** Unlike commercial VPN services, **Lantern does not route your internet traffic through a remote proxy server**. Your web browsing, apps, and TCP/UDP payload data travel directly to their intended destinations through your physical Wi-Fi or cellular network interface.

---

## 3. DNS Resolution & Upstream Resolvers

When an app queries a domain that is **not** blocked:
1. Lantern forwards the query directly to the **Upstream DNS Resolver** selected by you in Settings (defaulting to Cloudflare `1.1.1.1`, or Quad9 `9.9.9.9`, Google `8.8.8.8`).
2. When encrypted DNS is enabled (**DoH** or **DoT**), queries are encrypted directly between your device and that provider over TLS.
3. Your queries are subject to the privacy policy of whichever upstream provider you choose. Lantern does not intermediate, intercept, or log your upstream traffic on any server.

---

## 4. Local Data Storage & User Ownership

All DNS query logs, session metrics, custom domain rules, and bypassed application lists are stored **exclusively in a private SQLite database on your local device storage** (`/data/data/com.masum.lantern/databases/`).

- **User Retention Controls:** In Settings, you can configure automatic database pruning (maximum days to keep logs or maximum row cap).
- **Instant Deletion:** Tapping **"Clear Query Log Database"** permanently deletes all historical query records and session analytics from the SQLite database immediately.
- **Forensic Export:** When you use the "Export Logs" feature, Lantern compiles a CSV file in your device's private cache and shares it via Android's `FileProvider` only to the destination app you explicitly select (e.g., Files, Google Drive, email).

---

## 5. App Permissions

Lantern requests the absolute minimum set of Android permissions necessary to function as a network firewall:
- `INTERNET`: Required to communicate with your selected upstream DNS resolvers (Do53, DoH, DoT) and to fetch blocklist rule updates from StevenBlack/hosts.
- `ACCESS_NETWORK_STATE`: Used to detect network connectivity changes (e.g., switching between Wi-Fi and mobile data) to reconnect the local DNS forwarder.
- `FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_SPECIAL_USE`: Required by modern Android OS to keep the local VPN tunnel listener running reliably in the background without being killed by system battery management.
- `POST_NOTIFICATIONS`: Required on Android 13+ to display the ongoing foreground service status notification and quick toggle controls.

Lantern **does not** request location, contacts, camera, microphone, or external storage read/write permissions.

---

## 6. Open Source Transparency

Lantern is 100% open-source software licensed under the **GNU General Public License v3.0 (GPLv3)**. Its complete source code is publicly inspectable and auditable on GitHub:
- Repository: [https://github.com/](https://github.com/)

---

## 7. Changes to this Policy

Because Lantern collects no data, updates to this policy are rare. If changes occur (such as adding new optional local network features), the updated policy will be committed directly to the open-source repository.

---

## 8. Contact

For technical questions, vulnerability disclosures, or privacy inquiries:
- Open an issue on GitHub
- Contact the developer: Masum
