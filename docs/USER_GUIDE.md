# GOJU Agent — User Guide

For financial agents (mobile-money and bank agency-banking tellers) using the GOJU Agent
Android app day to day.

## What this app does

Instead of manually dialing `*150*00#` and clicking through USSD menus for every
withdrawal, deposit, or balance check, you fill in a simple form and GOJU Agent drives the
USSD session for you — dialing, navigating the menu, and typing your inputs automatically.
You only step in when the provider asks for your PIN. Every completed transaction is saved
on your phone and synced to GOJU Cloud automatically.

---

## First-time setup

### 1. Sign in
Open the app and enter the email and password your branch supervisor gave you. This is the
same type of account used on GOJU Cloud, just for the mobile app.

### 2. Wait for device approval
The first time you sign in on a new phone, your account works but *this specific device*
doesn't yet — a screen will tell you it's "Waiting for approval." Contact your branch
supervisor or a system administrator and ask them to approve your device from GOJU Cloud
(**Agency → GOJU Agent Devices**). The app checks automatically every few seconds — you
don't need to do anything else, just keep the screen open.

### 3. Create your PIN
Once approved, you'll be asked to create a 4–6 digit PIN. This unlocks the app on this
phone going forward (e.g. after it's been idle a couple of minutes) — it is **not** your
mobile-money PIN, and it never leaves your phone.

### 4. Turn on USSD Automation (one-time, per phone)
The app will prompt you to enable an Accessibility Service called **"GOJU Agent — USSD
Automation"**. This is what lets the app read and respond to the USSD screens automatically.
Tap **Open Accessibility Settings**, find GOJU Agent in the list, and turn it on. Come back
to the app afterward — it detects this automatically, you don't need to restart anything.

You only do steps 1–4 once per phone. After that, you just unlock with your PIN or
fingerprint each day.

---

## Daily use

### Home screen
Shows today's transaction count, how many succeeded/failed, and the total amount moved.
Tap **New Transaction** to start.

### Starting a transaction
1. Pick a service (Cash Withdrawal, Cash Deposit, Float Purchase, Balance Inquiry).
2. Fill in the customer's number and the amount (not needed for Balance Inquiry).
3. Tap **Continue**.
4. Watch the screen — it shows what's happening ("Dialing…", "Processing…") automatically.
5. **When asked for your PIN**, a screen appears asking you to type it — this goes straight
   to the provider and is never saved by GOJU Agent. Enter it and tap Submit.
6. You'll see **Transaction Successful** with a reference code, or a clear failure/timeout
   message if something went wrong.

You can tap **Cancel** at any point before the PIN step if you need to stop.

### Repeating a transaction for a known customer
Go to the **Customers** tab, find the customer (recent or favorites), and tap them. If
they've transacted with you before, the app jumps straight to the right form with their
number and last amount pre-filled — just confirm or change the amount and continue. You
always get to check the amount before anything is dialed.

### Checking history
The **History** tab lists every transaction you've run on this phone, searchable by
customer, reference, amount, or provider. Tap any row for full details (reference, duration,
sync status, notes).

### Sync status
The **Sync** tab shows how many transactions are pending/failed/already synced to GOJU
Cloud. Sync happens automatically in the background every ~15 minutes when you have a
connection; tap **Sync Now** if you need it immediately (e.g. before handing over a shift).

---

## Security notes

- The app locks itself after about 2 minutes of inactivity — unlock with your PIN or
  fingerprint.
- Screenshots and screen recording are blocked everywhere in the app.
- Your mobile-money PIN is only ever typed directly into the live USSD prompt — GOJU Agent
  never stores it, not even temporarily.
- If your phone is lost or you're leaving the agency, ask your supervisor to **revoke** the
  device from GOJU Cloud immediately — this signs it out everywhere, instantly.

---

## Troubleshooting

| Problem | What to do |
|---|---|
| Stuck on "Waiting for approval" | Ask your supervisor to approve the device in GOJU Cloud → Agency → Devices |
| "One-time setup needed" keeps reappearing | The Accessibility Service got turned off (some phones do this automatically to save battery) — re-enable it and, on Samsung/Xiaomi/Tecno phones, also turn off battery optimization for GOJU Agent in phone Settings → Battery |
| Transaction says "Failed" with a message from the provider | That's the provider's own decline reason (e.g. insufficient balance) — no money moved, safe to check with the customer and retry |
| Transaction times out | The USSD session didn't respond in time — check your balance/history with the provider before retrying, to make sure it didn't actually go through |
| A transaction won't sync | Check you have a data/Wi-Fi connection, then use **Sync Now** on the Sync tab |
