import json

body = """## Battery Saver as an action\n- **Battery Saver (ON / OFF)** is now available in the **THEN** section — pair it with a Time Window condition (e.g. 00:00–08:00 → Battery Saver ON, 08:00–12:00 → OFF) and the OS power-save mode follows your schedule\n- Works on every device; it uses Android's elevated power-save setting, so it needs a **one-time ADB grant** — the exact command is shown (and easy to copy) right inside the action config\n\n## Wait / Delay steps\n- A new **Wait / Delay** action pauses a routine between steps: \"Turn on DND → wait 30 s → mute media\" now works. Configurable from 1 second to 10 minutes with a formatted preview\n\n## Modes are complete now\n- **Mode action editor** — every mode (custom or built-in) has an **Add Action** editor on its detail screen: pick any action, configure it, remove it with the X. Custom modes are no longer empty shells\n- Tested end to end: created a \"Gym\" mode, added a Set Media Volume action, removed actions — all persisted\n\n## Share routines as QR / links\n- A **Share this Routine** card on every routine detail: shows a **scannable QR code**, copies a share code, or opens the Android share sheet\n- Opening a share link (`automatrix://import/…`) on a device with AutoMatrix imports the routine **disabled**, ready to review and enable\n- Share codes also work pasted into the existing Import Backup dialog\n- Very large routines fall back to copy/share only (QR has a size limit)\n\n## Widget, smarter\n- The home-screen widget's three mode buttons now **follow your actual mode library** (first three modes) instead of hardcoded Work/Study/Sleep — and the active mode gets a highlighted state\n\n## Under the hood\n- The periodic engine sweep is now **armed only while at least one routine is enabled** — no idle battery wakeups on a fresh install\n- Unified icon palette across routine and mode creation; fixed a misleading template category\n\n## Assets\n| File | Purpose |\n|---|---|\n| `AutoMatrix-v1.4.0.apk` | Install directly on any Android 7.0+ device |\n| `AutoMatrix-v1.4.0.aab` | Play Store bundle |\n\nSee `checksums.txt` for SHA-256 digests.\n\nDeveloped by [SHISHIR](https://github.com/Shishir-ip)"""

payload = {
    "tag_name": "v1.4.0",
    "target_commitish": "main",
    "name": "AutoMatrix 1.4.0",
    "body": body,
    "draft": False,
    "prerelease": False,
}
with open("/tmp/release_body_140.json", "w", encoding="utf-8") as f:
    json.dump(payload, f)
print("written")
