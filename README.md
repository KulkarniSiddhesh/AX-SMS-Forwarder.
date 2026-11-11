# AX SMS Forwarder

A minimal Android app intended for personal sideloading on a compatible Android phone.

## What it does
- Matches incoming SMS by exact sender ID (default `AX-FNTESA-S`, normalized for case and punctuation).
- Sends matching message text to the destination number configured in the app.
- Stores a small local activity log.
- Does not use a backend, analytics, or cloud storage.

## Build an APK using GitHub Actions (phone-friendly)
1. Create a **private** GitHub repository in your browser.
2. Upload the contents of this project ZIP to the repository root (extract the ZIP first; upload the files/folders, not the ZIP itself).
3. Ensure the default branch is named `main`.
4. Open **Actions** → **Build Android APK** → **Run workflow** (or wait for the first push-triggered run).
5. When the workflow succeeds, open the run → **Artifacts** → download `AX-SMS-Forwarder-debug`.
6. Extract the artifact ZIP on your phone and tap `app-debug.apk` to install. Android may ask you to allow installs from that browser/file manager.

## First run
1. Open AX SMS Forwarder.
2. Confirm sender ID and enter the destination phone number in international format (for India, e.g. `+91XXXXXXXXXX`).
3. Tap **Save settings and grant permissions**.
4. Grant Receive SMS and Send SMS permissions.
5. Turn forwarding on only after verifying the destination.

## Important limitations and safety
- This is a personal sideload build, not a Play Store release.
- Android/OEM background restrictions, carrier behavior, dual-SIM settings, and OS updates can affect delivery. Test carefully.
- Sending SMS can incur carrier charges. Large messages may be split into multiple outgoing SMS parts.
- Do not use for OTPs, banking alerts, or other sensitive messages unless you fully understand the privacy and security implications and control the destination.
- This starter build does not provide encryption at rest for its local activity log.
- Android restricts SMS permissions and distribution. Do not publish this app on Google Play without reviewing current Google Play SMS permission policies.
- The APK built by GitHub Actions is a debug-signed APK suitable for personal testing; it is not a production release signing configuration.
