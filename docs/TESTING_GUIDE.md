# GOJU Agent — Build & Testing Guide

This covers compiling the app to an APK and what can realistically be tested at each stage.
**Nothing in this repo has been compiled yet** — it was written without a Kotlin/Android
compiler in the loop, so expect to fix a handful of small build errors (missing import, a
typo, a version mismatch) on the first sync/build. That's normal for a first compile of a
project this size, not a sign the architecture is wrong.

## 0. Fastest path: let GitHub Actions build the APK for you

No local Android Studio/JDK setup needed. Go to the repo's **Actions** tab →
**Build Debug APK** → **Run workflow** → wait a few minutes → download the
`goju-agent-development-debug` artifact from the finished run and install it on a device
(`adb install` or transfer the file and open it — enable "install unknown apps" for
whatever app you use to open it). A separate `unit-tests` job in the same run reports the
JVM unit test results without blocking the APK.

This is CI building against the `development` flavor, which points at
`https://dev.gojucloud.maangatech.com/` — see §3 below for pointing it at your own backend
instead (requires editing `app/build.gradle.kts` and re-pushing, since CI builds from
whatever's on `main`).

If this is your first time compiling this project, expect the very first Actions run to
surface small build errors (missing import, a version mismatch) — the project was written
without a compiler in the loop. Report the error log back and they're quick to fix.

## 1. Prerequisites (if building locally in Android Studio instead)

- **Android Studio** (Ladybug/2024.2 or newer) — bundles its own JDK 17, so you don't need
  to install Java separately.
- A **physical Android device** if you want to test the actual USSD automation — an
  emulator has no cellular radio/SIM and cannot dial real USSD codes. Emulator is fine for
  everything else (login, forms, navigation, history, sync UI).
- The **goju-saas** Laravel backend, reachable from wherever the app runs (see §3).

## 2. Opening and building the project

1. `File → Open` → select the `goju-agent-android` folder.
2. Let Gradle sync. The wrapper JAR isn't checked in (see `.gitignore`) — Android Studio
   will offer to regenerate it automatically on first open; accept that.
3. Pick the **Build Variant** panel (bottom-left) → choose `developmentDebug` for local
   testing. **Do not** try to build a `release` variant yet — it references
   `../release.keystore`, which doesn't exist in this repo (deliberately; a real signing key
   isn't something to commit). Debug builds don't need signing.
4. `Build → Build App Bundle(s) / APK(s) → Build APK(s)`, or from a terminal:
   ```
   ./gradlew assembleDevelopmentDebug
   ```
   The APK lands in `app/build/outputs/apk/development/debug/`. Install it with
   `adb install app-development-debug.apk` or drag it onto a running emulator.

### If the build fails
Most likely causes, in order of probability:
- A small typo/import error somewhere (search the error text, it'll point at the exact
  file/line).
- A dependency version in `gradle/libs.versions.toml` that's since been superseded —
  Android Studio's "Suggested fix" for version conflicts is usually correct.
- KSP/Hilt codegen ordering — a clean rebuild (`Build → Clean Project` then `Rebuild`) fixes
  most first-time annotation-processing hiccups.

## 3. Backend setup

The app's `development` flavor points at `https://dev.gojucloud.maangatech.com/`
(`app/build.gradle.kts`). Unless that's a real, reachable deployment with today's backend
changes on it, you'll want to point the app at your local `goju-saas` instead:

- **Emulator only**: change the `development` flavor's `DEFAULT_SERVER_URL` in
  `app/build.gradle.kts` to `http://10.0.2.2/goju-saas/public/` (the emulator's alias for
  your host machine's `localhost`), matching your Laragon setup — keep the trailing slash,
  Retrofit requires it.
- **Physical device**: it needs real network access to your Laravel server — either your
  PC's LAN IP (`http://192.168.x.x/goju-saas/public/`, same Wi-Fi network) or an
  [ngrok](https://ngrok.com) tunnel if the device is on a different network.

### Database migrations
Run the three new migrations against whichever database your backend is pointed at:
```
php artisan migrate
```
**Known pre-existing issue**: this repo's dev MySQL database has an older, unrelated
migration (`2026_07_15_..._fix_transactions_archive_missing_columns`) that fails with a
duplicate-column error — not caused by this feature. If `migrate` stops on that one, run
just the three new migrations directly instead:
```
php artisan migrate --path=database/migrations/2026_07_19_090000_create_agent_devices_table.php --path=database/migrations/2026_07_19_090001_create_ussd_workflows_table.php --path=database/migrations/2026_07_19_090002_create_agent_transactions_table.php
```
Then seed the new permissions: `php artisan db:seed --class=RolesAndPermissionsSeeder`.

### Test data you'll need
1. A tenant with the **Agency Desk** plan feature enabled (`PlanFeature::AGENCY_MODULE`) —
   without it, every `/api/agent/v1/*` call except login/device-status returns 403.
2. A **tenant-staff** user (not Owner — see `AgentAuthController`'s docs for why) with a
   `branch_id` set, in that tenant.
3. A `CommissionOperator` row with `code = 'MPESA'`, `type = 'mobile_money'`, `is_active =
   true` — the transaction endpoint validates the provider code against this.
4. Log in to GOJU Cloud as that tenant and go to **Agency → GOJU Agent Devices** — after the
   phone's first login attempt, approve the device there.
5. Optionally, **Agency → GOJU Agent Workflows** → paste the contents of
   `app/src/main/assets/workflows/mpesa_wakala_v1.json` (one service at a time — the form
   takes one provider+service per submission) so the server has something to serve to
   `/api/agent/v1/workflows`. Without this, the app still works using its bundled local
   copy — you just won't be testing the server-sync path for workflows.

## 4. What you can actually test, and where

| Flow | Emulator | Physical device |
|---|---|---|
| Login, device pairing/approval, PIN setup, biometric unlock | ✅ | ✅ |
| Navigation, forms, customer list, transaction history, sync tab | ✅ | ✅ |
| Transaction sync to GOJU Cloud (once a transaction exists) | ✅ | ✅ |
| **Actual USSD dialing/automation** | ❌ dials into the emulator's fake dialer, no carrier response — will just time out after ~90s, that's expected, not a bug | ✅ but only after you've re-verified the real menu text — see below |

### Before testing real USSD automation on a device
The bundled `mpesa_wakala_v1.json` workflow is a **template** — its `match_pattern` regexes
are plausible, generic USSD banking phrasing, not a verified transcript of the real M-Pesa
wakala menu on your test SIM. Read
`app/src/main/assets/workflows/README.md` first: dial `*150*00#` by hand, record the exact
screen text for each step, and update the patterns before expecting the automation to
actually complete a transaction. Until then, expect it to get partway through the menu (or
not match at all) and time out — that's the workflow JSON needing real data, not an app bug.

Also required on the device, once, before any of this works:
- Settings → Accessibility → enable "GOJU Agent — USSD Automation" (the app prompts for
  this on first transaction attempt).
- On Samsung/Xiaomi/Tecno/Infinix devices: disable battery optimization for GOJU Agent
  (Settings → Battery → App battery usage), since aggressive OEM battery managers are known
  to kill background accessibility services — this is flagged in the architecture plan as a
  real risk on budget devices.

## 5. Suggested test sequence

1. Install the debug APK on an emulator first — confirm login → pairing → PIN setup → home
   screen all work, and that a test transaction (Balance Inquiry is simplest, no amount/PIN
   needed until the dial itself) reaches the "Dialing…" screen and times out gracefully
   after ~90s (proves the whole pipeline up to the point only a real carrier can complete).
2. Check the transaction landed in `agent_transactions` on the backend as `pending`, and
   that **Tenant → Agency → GOJU Agent Transactions** shows it.
3. Move to a physical device with a real SIM for the actual USSD dial-through, after
   capturing real menu text per §4.
4. Try device revoke (**Agency → Devices → Revoke**) and confirm the app can no longer sync
   (the Sanctum token is deleted server-side immediately).

## 6. Known gaps (not bugs — see the main README's "What's built vs. deferred")
No analytics dashboard, scheduler, receipts, export, or additional providers yet. Only
M-Pesa is wired up. Unit tests (`./gradlew test`) cover the workflow engine and repository
logic and don't need a device/emulator at all — run those first if you just want a fast
sanity check that the core logic is intact.
