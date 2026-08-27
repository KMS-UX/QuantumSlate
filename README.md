# QuantumSlate

An Android tablet dashboard that displays ambient information — time, weather, calendar,
news, flights and now-playing — across four swappable visual modes, with a reactive mascot.

> **Status: builds, installs, and is not yet verified on a device.**
> See [progress.md](progress.md) for what is actually verified and
> [ROADMAP.md](ROADMAP.md) for what remains. Claims in this README are limited to things
> that have been checked.

---

## What it does

**Four dashboard modes**, switched by horizontal swipe or by tapping the mode dots:

| Mode | Character |
|---|---|
| **Minimalist** | Large type, generous whitespace, essentials only |
| **Data-Dense** | Compact grid, monospace numerics, colour-coded sections |
| **Retro Newspaper** | 1950s front page — Playfair masthead, folio line, column rules |
| **QuantumEffect** | Pixel-art sci-fi HUD — scanlined panels, corner ticks, stat bars |

**Widgets:** Time & Date · Weather · Calendar · News (RSS/Atom) · Flight status ·
Spotify now-playing · Quantum Boy mascot.

**Mascot.** Quantum Boy reacts to your data through a five-state mood engine
(happy / neutral / concerned / excited / sleepy). He is **static at rest** — a dashboard is
on display for hours, so he only animates when he has something to report.

**Offline.** Every widget renders from a Room cache and shows how stale it is. Nothing
requires a network connection to display.

---

## Requirements

- Android **7.0 (API 24)** or newer — built and tested against API 34
- Tablet or phone; layouts are tablet-oriented
- JDK **17** and the Android SDK to build

## Building

```bash
./gradlew assembleDebug
```

The APK lands at `app/build/outputs/apk/debug/app-debug.apk`.

Create a `local.properties` in the repo root pointing at your SDK (this file is gitignored):

```
sdk.dir=/path/to/Android/Sdk
```

Run the checks:

```bash
./gradlew testDebugUnitTest lintDebug
```

---

## API keys

The app ships with **no keys**. Every network widget stays empty until you add your own in
**Settings** (gear icon, top-right of any mode). Keys are stored in
`EncryptedSharedPreferences` and are never written to logs.

See **[SETUP_GUIDE.md](SETUP_GUIDE.md)** for how to obtain each one.

| Widget | Needs | Notes |
|---|---|---|
| Weather | OpenWeatherMap API key | Free tier is generous |
| Flights | aviationstack access key | **Free tier: 100 requests/month, HTTP only** |
| Spotify | Spotify Client ID | OAuth via browser; no secret needed (PKCE) |
| Calendar | `READ_CALENDAR` permission | No account setup — reads calendars already on the device |
| News | Nothing | Add any RSS/Atom feed URL in the widget |

---

## Architecture

```
ui/          Compose screens, four dashboards, theme, navigation
domain/      Plain models (Weather, CalendarEvent, MascotState, …)
data/local/  Room database, DAOs, preferences, cache expiry
data/remote/ Retrofit services, DTOs, flight provider abstraction
data/repository/  Repositories, one per data source
work/        WorkManager sync + the real-time foreground service
```

MVVM with Hilt. A single `DashboardViewModel` is scoped to the dashboard back-stack entry,
so all four modes share one instance and one set of loaded data.

**Flight providers are pluggable.** `FlightDataSource` is an interface; aviationstack is one
implementation. Switching providers is a new implementation plus one Hilt binding — nothing
in the repository, view model or UI changes.

---

## Update modes

| Mode | Cadence | Cost |
|---|---|---|
| Daily | Once, at a time you choose | Minimal |
| Ambient | Every 30 minutes | Moderate |
| Real-time | Every minute, foreground service | High — the notification says so |

Flights ignore all of this and follow `FlightPollingPolicy` instead: nothing while a flight
is far off, every 15 minutes from 3h before departure until 2h after arrival, and nothing at
all once landed. On a 100-request/month tier, minute-level polling would exhaust the quota in
under two hours.

---

## Known limitations

- **Not verified on a device.** The build is clean and unit-tested; runtime behaviour is not
  yet confirmed.
- **Performance targets unmeasured** — Bible §6 sets <100MB RAM, <2s cold start, 60fps.
  None of these can be checked without a device, so none are claimed.
- **Certificate pinning is deliberately not implemented.** See `progress.md` for the
  reasoning; the short version is that a provider rotating its certificate would brick the
  feature with no way to recover short of shipping a new APK.
- **aviationstack's free tier is HTTP-only**, so a cleartext exception is scoped to that one
  host. Every other host requires TLS.
- **One mascot character.** The spec asks for four; the art exists for one.
- **Reordering uses up/down buttons**, not drag-and-drop, so it does not fight the swipe
  gesture and works with TalkBack.

---

## Licence

Personal project. Quantum Boy artwork and the QuantumEffect palette come from the author's
own design systems.
