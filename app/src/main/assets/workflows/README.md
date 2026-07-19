# Bundled workflow seeds — PLACEHOLDER, not verified against a live menu

`mpesa_wakala_v1.json` is a **template**, shipped only so the app has something to run on
day one before its first successful sync with GOJU Cloud. The `match_pattern` regexes are
generic, plausible USSD banking phrasing (numbered menu prompts, PIN prompts, common
Swahili success/failure keywords) — they are **not** a verified transcript of Vodacom
M-Pesa's actual wakala menu, which changes over time and by region.

Before this workflow is used on a real device with a real SIM:

1. Dial `*150*00#` on the target device/SIM and record the exact text of every screen for
   Withdraw, Deposit, Float Purchase, and Balance Inquiry, including menu option numbers.
2. Replace each step's `match_pattern` with a regex anchored to that exact captured text
   (keep it as tight as possible while tolerating minor whitespace/case differences —
   `RegexOption.IGNORE_CASE` is already applied by the engine).
3. Confirm the `reference_capture_group` / `failure_capture_group` indices against the real
   parenthesized groups in your updated patterns.
4. Test on at least one device per OEM skin listed in `DialerPackageRegistry` — the dialog
   text extraction depends on how that OEM renders the USSD response dialog.
5. Once field-verified, this JSON becomes the authoritative source that gets uploaded to
   GOJU Cloud's workflow-definition admin screen (not shipped in the APK) — see the
   `ussd_workflows` table and `AgentWorkflowController` on the Laravel side. The APK-bundled
   copy should then only need to change for a true "seed a brand new install offline" case.
