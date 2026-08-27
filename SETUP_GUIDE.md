# QuantumSlate — Setup Guide

How to get the app building, and how to obtain each API key. Nothing here is required to
*launch* the app — Time & Date works with no configuration — but every network widget stays
empty until its key is entered.

---

## 1. Build the app

**Prerequisites:** JDK 17, Android SDK with platform 34, and a device or emulator on API 24+.

```bash
git clone <your-repo-url>
cd QuantumSlate
```

Create `local.properties` in the repo root (gitignored):

```
sdk.dir=C:/Users/you/AppData/Local/Android/Sdk
```

Use forward slashes, or escape backslashes as `\\`. A single backslash is a Java properties
escape character and will produce a confusing "SDK location not found" failure.

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

If `./gradlew` picks the wrong JDK, set `JAVA_HOME` to a JDK 17 install for the command.
Gradle 8.2 and AGP 8.2 do not support newer JDKs.

---

## 2. Where settings live

Tap the **gear icon, top-right** on any dashboard mode. All keys are stored in
`EncryptedSharedPreferences` and are redacted from network logs.

---

## 3. Weather — OpenWeatherMap

1. Create a free account at <https://openweathermap.org/api>
2. Open **My API keys** and copy the default key
3. Paste it into **Settings → API Configuration → OpenWeatherMap API Key**

**A new key takes up to a couple of hours to activate.** Until then requests return 401 and
the weather widget shows an error — this is normal and not a bug in the app.

Also set **Settings → Location** or the app falls back to a default coordinate.

---

## 4. Flights — aviationstack

1. Sign up at <https://aviationstack.com/signup/free>
2. Copy the **API Access Key** from your dashboard
3. Paste it into **Settings → API Configuration → Flight API Key**
4. Add a flight from any dashboard: tap **+** on the flight widget and enter an IATA flight
   number such as `BA2490`

### Understand the free tier before you test

| | |
|---|---|
| Requests | **100 per month** |
| Transport | **HTTP only** — HTTPS is a paid feature |

Two consequences:

- The app permits cleartext **only** for `api.aviationstack.com`, via
  `res/xml/network_security_config.xml`. Every other host still requires TLS. Delete that
  block if you upgrade to a paid plan.
- The app counts your requests locally and holds back a 5-request reserve, so it stops
  before aviationstack does. The QuantumEffect mode shows the remaining budget as a meter.
  When the budget runs out the widget serves cached data rather than erroring.

Flights are polled adaptively — nothing while a flight is days away, every 15 minutes near
departure, nothing once landed. A whole flight costs roughly 20 requests.

**Switching providers** is a new `FlightDataSource` implementation plus one binding in
`di/RepositoryModule.kt`. Nothing else changes.

---

## 5. Spotify — OAuth

Spotify needs a registered app, not just a key.

1. Go to <https://developer.spotify.com/dashboard> and **Create app**
2. Name it anything; for **Redirect URI** enter **exactly**:

   ```
   quantumslate://spotify-callback
   ```

3. Select **Android** as the platform and save
4. Copy the **Client ID** (you do **not** need the Client Secret — the app uses
   Authorization Code + PKCE, because a mobile app cannot keep a secret)
5. In **Settings → Spotify**, paste the Client ID and press **Save**
6. Press **Connect**. A browser tab opens for consent; approving returns you to the app

If Connect does nothing, the Client ID has not been saved yet — the button stays disabled
until it is. If the browser reports an invalid redirect URI, it does not match the dashboard
entry character for character.

Scopes requested are read-only: `user-read-currently-playing`, `user-read-playback-state`.

---

## 6. Calendar

No account setup. The app reads calendars already synced to the device — including Google
ones — through Android's `CalendarContract`.

The calendar widget shows a **Grant access** button on first run. Accepting the system
prompt is all that is required. If you decline and change your mind, grant it under
**Android Settings → Apps → QuantumSlate → Permissions → Calendar**.

---

## 7. News

No key. Tap **+** on the news widget and paste any RSS or Atom feed URL. It must start with
`https://` — cleartext is blocked for every host except the flight API.

Example: `https://feeds.bbci.co.uk/news/rss.xml`

---

## 8. Update frequency

**Settings → Updates**:

- **Daily** — one sync at your chosen time. Lowest battery.
- **Ambient** — every 30 minutes.
- **Real-time** — every minute via a foreground service. A permanent notification appears
  stating the battery cost. Flights are excluded from this loop to protect your quota.

---

## Troubleshooting

| Symptom | Cause |
|---|---|
| Weather shows an error with a valid key | New OpenWeatherMap keys take up to 2 hours to activate |
| Flight widget empty with a valid key | Monthly budget exhausted, or the flight number is not currently in aviationstack's data |
| Spotify **Connect** does nothing | Client ID not saved yet |
| Spotify returns "invalid redirect URI" | Dashboard entry does not exactly match `quantumslate://spotify-callback` |
| Calendar always empty | Permission not granted, or there are genuinely no events in the next 7 days |
| News widget empty | No feeds added, or the URL is not `https://` |
| Everything empty on a fresh install | Expected — no keys ship with the app |
