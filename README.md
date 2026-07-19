# GOJU Agent

Standalone Android app for financial agents (mobile-money and bank agency-banking tellers).
Replaces manual USSD menu-hunting with clean transaction forms that drive the USSD session
automatically, then syncs every transaction to GOJU Cloud.

This is a **separate app** from `goju-android` (the GOJU Cloud WebView wrapper) — different
package (`com.maangatech.gojuagent`), different purpose, own release cadence.

**Distribution**: enterprise/sideload only (MDM or direct APK), not Google Play — this app's
core feature (Accessibility-Service-driven USSD automation) does not qualify under Play's
Accessibility API policy. See `core/core-ussd`'s module docs for why this approach was
chosen over `TelephonyManager.sendUssdRequest`.

## Module map

```
app/                     application shell, DI graph wiring, nav host
core/
  core-common/           Result wrappers, dispatcher provider, shared Moshi/CoroutineScope
  core-designsystem/      Compose theme, shared components (buttons, PIN keypad, status badge)
  core-database/          Room (SQLCipher-encrypted), entities/DAOs, TransactionRepository
  core-security/          biometric/PIN, root/tamper/emulator detection, encrypted prefs
  core-network/           Retrofit APIs, auth interceptor, DTOs for the GOJU Cloud agent API
  core-ussd/              AccessibilityService, workflow engine, USSD dialer
feature/
  feature-auth/           login, device pairing, PIN setup, unlock
  feature-home/           dashboard + bottom navigation shell
  feature-transactions/   service launcher, transaction form, execution, history
  feature-customers/      search, recent, favorites, quick-repeat
  feature-sync/           WorkManager outbox + workflow-definition sync
```

## First-run setup

1. Open in Android Studio (Koala+), let it sync — the Gradle wrapper jar isn't checked in;
   Android Studio will offer to regenerate it, or run `gradle wrapper` once if you have a
   local Gradle install.
2. Build the `development` flavor first — it points at
   `https://dev.gojucloud.maangatech.com/`, set in `app/build.gradle.kts`.
3. On first launch, the app seeds a **template** M-Pesa wakala workflow from
   `app/src/main/assets/workflows/mpesa_wakala_v1.json` — read that folder's README before
   testing on a real device. The bundled menu text is illustrative, not verified.
4. USSD automation requires manually enabling "GOJU Agent — USSD Automation" under
   Settings → Accessibility on the test device — there is no programmatic grant path, by
   design (Android security model).
5. The Laravel-side API this app talks to (auth/device-pairing, workflow sync, transaction
   ingest) lives in the `goju-saas` repo under `app/Http/Controllers/Api/Agent/`.

## What's built vs. deferred

Phase 1 (this scaffold): M-Pesa withdraw/deposit/float-purchase/balance-inquiry, offline-first
transaction history, customer quick-repeat, device pairing + PIN/biometric login, background
sync.

Deferred to a later phase (not built yet, module boundaries left room for them): analytics
dashboard, scheduler/reminders, PDF/CSV/Excel export, receipt generation, additional
providers beyond M-Pesa, Bluetooth printer/QR/NFC hooks.
