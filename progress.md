# QuantumSlate Android Dashboard - Progress Log

## Current Status: 🟢 READY FOR DEVICE TESTING — Phases A–H delivered

**Last verified:** 2026-08-27 — clean build, 46/46 unit tests, lint clean, both APKs produced.

**Plan of record:** [ROADMAP.md](ROADMAP.md) — 8 phases (A–H). This file is updated at each
phase completion. Device test script: [TESTING.md](TESTING.md).

> **Note on earlier versions of this file:** this log once claimed "100% COMPLETE". That was
> not accurate — the project had never been compiled. The first build attempt surfaced 137
> Kotlin errors plus 8 resource-level blockers, and several items listed as delivered did not
> exist at all. Everything below records what was *verified*, not what was planned.

---

## Verified Build Status

| Check | Result |
|---|---|
| `./gradlew testDebugUnitTest` | ✅ 46 tests, 0 failures |
| `./gradlew lintDebug` | ✅ no errors |
| `./gradlew assembleDebug` | ✅ 21.6 MB, debug-signed |
| `./gradlew assembleRelease` | ✅ 4.3 MB (R8 + resource shrinking), unsigned without a keystore |
| Components in APK | ✅ MainActivity, SpotifyRedirectActivity, TimeWeatherWidget, RealtimeSyncService |
| Permissions | ✅ 9, all used (was 12 — see Phase H) |
| Installed & run on device | ❌ **Not yet — this is the next step** |

Build environment: JDK 17, AGP 8.2.0, Gradle 8.2, Kotlin 1.9.20, compileSdk 34, minSdk 24.

---

## Phase Log

### ✅ Phase A — Make the app reachable (complete 2026-08-26)

The root cause of the failed device test: there was no navigation, so Settings was unreachable,
so no API key could be entered, so every data widget failed closed.

| # | Task | Outcome |
|---|---|---|
| A1 | Navigation Compose host (`dashboard` / `settings` routes) | ✅ `ui/navigation/QuantumSlateNavHost.kt` |
| A2 | Swipe-mode state hoisted so it survives navigating to Settings and back | ✅ `rememberSaveable` in the nav host |
| A3 | Gear icon, top-right, on all three dashboards (Bible §3) | ✅ all 3 dashboards |
| A4 | `SettingsScreen` wired into the graph with working back | ✅ was previously dead code |
| A5 | `SettingsViewModel` audited | ✅ sound; all 9 save paths verified against `PreferencesManager` |
| A6 | Mode indicator (3 dots) so swipe position is legible | ✅ `ui/components/ModeIndicator.kt`, tappable, 48dp targets |

**Extra fix found during A5.** `DashboardViewModel` loads its data once in `init`. Entering an API
key and navigating back would have left every widget showing the failure it cached *before* the key
existed — the app would have looked just as broken as before. Settings that invalidate fetched data
(both API keys, location toggle, mascot character) now signal the dashboard to refresh on return.
Update-mode/time changes deliberately do **not** trigger a refetch, because that costs real API
quota against the 100 req/month flight tier.

Also: `MainActivity` now honours the Bible §5 "default UI mode on launch" setting, which was
previously stored but never read.

**Build state:** `assembleDebug` ✅ · `lintDebug` ✅

### 🟡 Phase B — Widget completeness (6 of 7 complete, 2026-08-26)

| # | Task | Outcome |
|---|---|---|
| B1 | Build the Calendar widget | ✅ `ui/components/CalendarWidget.kt` — colour-coded per source calendar, distinct permission state |
| B2 | Place every widget on every mode | ✅ Calendar added to all three; **two hardcoded placeholders removed** (see below) |
| B3 | Wire `onAddData` so "+" actually renders | ✅ flights + news |
| B4 | Add-flight dialog | ✅ `ui/components/AddItemDialogs.kt`, with format + duplicate validation |
| B5 | Add-RSS-feed dialog | ✅ same file; requires `https://` per Bible §12 |
| B6 | Pull-to-refresh on all modes | ✅ hosted once in `DashboardPager` rather than triplicated |
| B7 | Long-press widget config sheet | ⏭️ **Deferred to Phase F**, to be built together with F3 (widget enable/disable) — they share the same per-widget state, and building them separately would mean writing that state twice. |

**Two hardcoded placeholders were the real reason no calendar appeared.** Data-Dense contained a
literal card reading "No events", and the Retro dashboard a literal "No events scheduled" — neither
was connected to anything. Both now render real data.

**The "+ does nothing" symptom had two independent causes**, both fixed: the flight widget's
`onAddFlight` was an empty lambda `{ /* Add flight dialog */ }`, *and* the empty-state message said
"Tap + to add one" while `onAddData` was null, so no "+" was ever drawn in that state.

**Calendar DAO corrected.** `getUpcomingEvents()` had no time filter — it returned the three
oldest rows in the table, so once any event was cached the widget would have shown expired events
forever. Now filters on `endTime >= now`.

Also pulled forward from Phase C: `CalendarRepository` (C1) and the runtime-permission plumbing
(C2), because the widget could not be built or tested without a data source behind it.

**Build state:** `assembleDebug` ✅ · `lintDebug` ✅

### ✅ Phase C — Data sources (complete 2026-08-26)

| # | Task | Outcome |
|---|---|---|
| C3 | `FlightDataSource` interface + aviationstack implementation | ✅ `data/remote/flight/` — swapping providers is now one DI binding |
| C4 | Adaptive flight polling + monthly request budget | ✅ `FlightPollingPolicy`, `FlightRequestBudget` |
| C5 | Weather path audited end-to-end | ✅ endpoint, DTO and mapping verified against the OpenWeatherMap contract |
| C6 | Spotify OAuth 2.0 | ✅ PKCE flow, Custom Tabs, redirect activity, auto-refresh, Settings UI |
| C7 | RSS path audited | ✅ **found and fixed a guaranteed crash** (below) |

#### ⚠️ aviationstack free tier is HTTP-only — this required a security exception

aviationstack gates HTTPS behind its paid plans. Android has blocked cleartext by default
since API 28, so on a free key **the flight widget could never have connected at all**.

Added `res/xml/network_security_config.xml` permitting cleartext for `api.aviationstack.com`
**and no other host** — every other domain still requires TLS. This is a real, if
provider-imposed, weakening of Bible §12 and should be removed if the account is upgraded or
the app moves to a provider that serves TLS on its free tier.

The existing flight code was also written against **AviationEdge's** response shape, not
aviationstack's, so it would have failed to parse regardless. New DTOs and mapper added.

#### The Bible's 5-minute flight polling is not achievable — and did not need to be

Bible §4 asks for 5-minute polling on active flights: 288 requests/day/flight, ~8,600/month,
against a free tier of 100. `FlightPollingPolicy` preserves the *intent* — timely data when it
matters — by concentrating requests where status actually changes: daily while a flight is far
off, every 15 min from 3h before departure until 2h after arrival, and **nothing at all** once
landed/cancelled/diverted. That is roughly 20 requests across a flight's entire lifecycle.

`FlightRequestBudget` counts requests per calendar month and holds back a 5-request reserve, so
the app stops before the provider does. On quota exhaustion it serves stale cached data rather
than erroring — an old departure time beats an error message, and staleness is already flagged.
Remaining allowance is exposed on the UI state.

#### Two more latent bugs found and fixed

- **RSS would have crashed on first fetch.** `getRssFeed` was annotated `@GET(".")` *and*
  `@Url`. Retrofit rejects that combination outright with "@Url cannot be used with @GET URL".
  The RSS widget could never have loaded a single article.
- **API credentials were being written to logcat.** `ApiClient` used
  `HttpLoggingInterceptor.Level.BODY`, which logs full URLs and bodies — meaning the
  OpenWeatherMap key, the flight `access_key`, and the Spotify bearer token. Bible §12 forbids
  logging sensitive information. Now `BASIC` in debug, `NONE` in release.

#### Spotify notes

Uses **Authorization Code + PKCE**, not the classic flow: a mobile client cannot keep a client
secret, and shipping one in the APK would expose it to anyone who unzips the app. Consent opens
in a **Custom Tab** rather than a WebView, so credentials never enter the app's process. Token
refresh is mutex-guarded so concurrent widget loads cannot each spend the refresh token, and a
rejected refresh clears stored credentials rather than retrying a credential Spotify has already
refused. `clearSpotifyTokens()` added to `PreferencesManager`, plus token-expiry storage.

**Requires setup:** register `quantumslate://spotify-callback` as a Redirect URI on the Spotify
developer dashboard, then enter the Client ID in Settings → Spotify.

**Build state:** clean `assembleDebug` ✅ · `lintDebug` ✅

### ✅ Phase D — Mascot: Quantum Boy (complete 2026-08-27)

Replaced the 20 stub Lottie files (681-byte, single-shape placeholders) with real poses
extracted from the approved Quantum Boy reference sheets.

| Mood | Pose | Source sheet |
|---|---|---|
| NEUTRAL | Ready Stance | Jumping Jacks, row 1 frame 1 |
| HAPPY | Waving | Master Idle Pose |
| CONCERNED | Shoulders drop | Sleepy Sheet frame 2 |
| EXCITED | Peak Spread | Jumping Jacks, row 1 frame 4 |
| SLEEPY | rub eye → yawn → curled | Sleepy Sheet frames 3–5 |

#### Extraction notes (all boundaries measured, not eyeballed)

- **Sleepy frames 4 and 5 merged** into one 294px block — the sleep bubbles bridge the gap.
  Re-scanning at head height (only the standing figure reaches it) gave the true split.
- **Tolerance 26 ate the left arm** of the first NEUTRAL candidate: the pale sleeve is too
  close in value to the cream paper. NEUTRAL was switched to the Ready Stance, which sits on
  a plain panel with no measurement brackets — and whose right hand isn't smudged in the
  source art, unlike the Reference Sheet's front view.
- **Callout leader lines survived keying** as floating dashes. Cleanup removes components by
  *shape* (thin + elongated), not size, specifically so the **sleep bubbles and halo sparkles
  survive** — a "keep the largest component" pass would have deleted them. 4 rules removed,
  bubbles intact.
- **Scale is normalised per source sheet, not per file.** Normalising each pose to identical
  pixel height would inflate the curled sleeping pose to standing height; each sheet gets one
  factor from a standing reference, so the character never changes size when the mood does.
  All poses share a ground line.

#### Rendering

`ui/components/MascotWidget.kt` is **static at rest** — a dashboard sits on display for hours,
so a permanently looping mascot would redraw forever for no informational gain and blow the
Bible's <5%/day battery budget (§6). Motion is functional only: a mood change crossfades the
pose, and SLEEPY steps through its three drawn stages so "getting tired" and "fast asleep"
read differently. Everything else holds one frame. Honours the existing animations toggle,
and every mood carries a TalkBack description (Bible §13).

Mascot now renders on **all three modes** (previously Retro only): 96dp Minimalist, 72dp
Data-Dense, 120dp Retro.

#### Divergences recorded

- **Bible §7 asks for 4 mascot characters; only Quantum Boy ships.** Approved. The Settings
  character picker offered robot/cat/bird/creature — characters that never existed as real
  art — and has been replaced with a static identity row rather than left as a dead control.
- **CONCERNED reuses the "fatigue onset" pose.** The source sheet drew it as tiredness, so it
  reads as unhappy rather than specifically worried. Flagged to the user; pending dedicated art.
- **Bible §7 reactive accessories** (umbrella when raining, sunglasses when sunny) are not
  implemented — the base art has no accessory layers. Not faked.
- Lottie dependency removed; nothing uses it now.

**Build state:** clean `assembleDebug` ✅ · `lintDebug` ✅ · all 7 drawables verified in the APK

### ✅ Phase E — Visual design, 4 modes (complete 2026-08-27)

| # | Task | Outcome |
|---|---|---|
| E1 | Minimalist styling | ✅ 96sp thin display type, wider margins, essentials only |
| E2 | Data-Dense styling | ✅ monospace for numerics so columns align; tighter gutters and card padding |
| E3 | Retro Newspaper styling | ✅ Playfair masthead, thin rule + folio line (VOL. I / date / price), double separator rule |
| E4 | Vintage fonts actually applied | ✅ **see below — they never were** |
| E5 | Dark / Light / Auto | ✅ **see below — the setting did nothing** |
| — | QuantumEffect as mode 4 | ✅ new `QuantumEffectDashboard` + `QuantumEffectHud` |

#### The vintage fonts were never reaching the screen

`Typography.kt` declared every Retro style as `FontFamily.Serif` — the generic system serif —
so the six bundled vintage faces were compiled into the APK and never used. On top of that,
`RetroNewspaperDashboard` carried **18 hardcoded `fontFamily = FontFamily.Serif` overrides**
that would have defeated the typography even after it was fixed. Both are gone: real
`FontFamily` definitions now bind Playfair Display (masthead), Old Standard TT (headlines)
and Crimson Text (body), and the per-call overrides are removed.

#### Dark / Light / Auto was stored but never read

Every dashboard called `QuantumSlateTheme(uiMode = ...)` without passing `darkMode`, so the
parameter always fell back to its `AUTO` default. Choosing Light or Dark in Settings changed
nothing on screen. The preference is now carried on the dashboard state and threaded through
the pager into each mode's theme.

#### QuantumEffect (mode 4)

Palette ported **verbatim** from the Quantum Effect Design System's `tokens/colors.css`
(project `2d830ed5-…`) rather than approximated — void surfaces `#04060d`–`#161f36`, the
accent set (quantum-purple / teal / electric-blue / atom-gold / alert-red / success-green),
panel borders and the stat-bar ramps.

`ui/components/QuantumEffectHud.kt` ports two primitives from that system's `components/hud/`:
- **QePanel** — "dark, scanlined, corner-ticked", with `accent` recolouring title and ticks,
  matching the source contract including the `corners` opt-out.
- **QeStatBar** — gradient-filled resource meter over a dark track.

Widgets map onto the game's semantic roles: CHRONO (electric blue), ATMOS (teal), MISSION LOG
(purple), TRANSIT (gold), COMMS (teal), AUDIO (green). Flight status uses the hazard colour
scale. The remaining flight API allowance gets its own StatBar, since on a 100/month tier
that genuinely is operational information.

Swipe order is now Minimalist → Data-Dense → Retro → QuantumEffect. `UiMode` gained
`QUANTUM_EFFECT`; prefs and Room store this by name, so existing saved values stay valid.

#### Known substitution

The source system specifies **Orbitron** (display) and **Share Tech Mono** (UI). Neither is
bundled, so system monospace stands in, carrying the character through uppercase and wide
tracking (`--tracking-caps: 0.14em`) rather than the typeface. Dropping the real fonts into
`res/font` and repointing `QuantumMono` is a drop-in change.

**Build state:** clean `assembleDebug` ✅ · `lintDebug` ✅

### 🟡 Phase F — Bible compliance sweep (5 of 7 complete, 2026-08-27)

| # | Task | Outcome |
|---|---|---|
| F1 | Bind home widget to real weather | ✅ reads the same Room cache as the app; no more `--°` placeholder |
| F2 | Real-time mode + foreground service + battery warning | ✅ `work/RealtimeSyncService.kt` |
| F3 + B7 | Widget enable/disable, reorder, long-press config sheet | ✅ `WidgetConfigSheet`, `WidgetLayout` |
| F4 | Cache auto-expiry at 24h | ✅ **it did not exist at all** — see below |
| F5 | Accessibility | ✅ lint clean; 48dp targets, sp text, TalkBack descriptions |
| F6 | Performance targets | ⏳ **cannot be verified without a device** — see below |
| F7 | Certificate pinning | ⚠️ **deliberately not implemented** — see below |

#### Cache expiry never existed

`CacheManager` classified data as FRESH/STALE/EXPIRED for *display*, but nothing ever
deleted anything. The database grew without bound and expired rows could resurface
indefinitely. `data/local/CacheExpiry.kt` now purges on app start and before every
background sync. Weather and Spotify expire by age (24h); calendar events and flights expire
by their own end/arrival time plus a 6h grace, because an event fetched an hour ago but
finished yesterday is useless however fresh the fetch was. News is kept 3 days — a day-old
headline is still readable content.

#### Another credential leak, caught while verifying F7

`HttpLoggingInterceptor.Level.BASIC` (set during Phase C as the "safe" level) logs the full
request line — **including the query string**. Both aviationstack's `access_key` and
OpenWeatherMap's `appid` travel as query parameters, so debug builds would have written live
credentials to logcat on every call. Replaced with `RedactingLogInterceptor`, which logs
method/host/path and timing while replacing sensitive parameter values with `***`.

#### Real-time mode

WorkManager cannot poll faster than 15 minutes, so Bible §4's 1–5 minute cadence requires a
foreground service. It is opt-in and its ongoing notification states the battery cost plainly
rather than hiding it. **Flights are deliberately excluded from the fast loop** — the free
tier is 100 requests/month, so minute-level polling would exhaust it in under two hours;
`FlightPollingPolicy` continues to govern flights regardless of update mode. Registered with
`foregroundServiceType="dataSync"` and the Android 14+ permission.

#### Reordering is buttons, not drag-and-drop

Bible §5 says "drag-and-drop to reorder". The config sheet is opened by long-press on a
dashboard that is itself listening for horizontal swipes, and a drag handle would compete
with that gesture. Explicit up/down controls also work with TalkBack, which a custom drag
reorder does not without extra work. Deliberate divergence.

#### F6 — performance cannot be verified here

Bible §6 sets <100MB RAM, <2s cold start, 60fps and <5%/day battery. **None of these can be
measured without running on a device**, so none are claimed. Statically: the mascot drawables
add ~514KB, the APK is ~21.6MB (largely `material-icons-extended` and the six vintage fonts),
and the mascot is static at rest so it contributes no idle redraw. Real measurement belongs
in the Phase H test pass.

#### F7 — certificate pinning deliberately not implemented

Bible §12 asks for pinning on critical APIs. **Recommending against it here**, and flagging
rather than silently skipping: pins break when a provider rotates its certificate, and this
app has no remote kill switch or update channel to recover — a rotation would simply brick
weather, flights or Spotify until a new APK shipped. The providers rotate on their own
schedule with no notice to us. Given the data involved (public weather/news, the user's own
calendar) the risk pinning removes is much smaller than the outage risk it introduces.

What *is* in place: HTTPS enforced by default for every host, the single cleartext exception
scoped to `api.aviationstack.com` alone, all keys in `EncryptedSharedPreferences`, no
credential logging, and no `Log`/`println` calls anywhere in the codebase. **Open decision for
the user** — say the word and pinning goes in with a documented rotation procedure.

**Build state:** clean `assembleDebug` ✅ · `lintDebug` ✅ (was 9 errors from my own gating
edits; fixed by properly bracing the guards)

### ✅ Phase G — Tests, CI and docs (complete 2026-08-27)

Every item here was previously marked delivered in this file and did not exist.

| # | Task | Outcome |
|---|---|---|
| G1 | Unit tests | ✅ **46 tests, 0 failures** across 5 classes |
| G2 | Instrumented tests | ⏳ deferred — needs a device/emulator; see below |
| G3 | CI workflow | ✅ `.github/workflows/android-ci.yml` (the directory was empty) |
| G4 | README + SETUP_GUIDE | ✅ both written, scoped to verified claims only |
| G5 | Release signing + minification | ✅ signing config, R8 enabled, resource shrinking |

#### Test coverage

| Suite | Tests | Covers |
|---|---|---|
| `FlightPollingPolicyTest` | 15 | Window edges, terminal statuses, unknown schedules, and a full-lifecycle simulation asserting a flight costs <30 requests |
| `AviationStackMappingTest` | 9 | The real aviationstack payload shape, ISO-8601 colon offsets, actual-over-estimated, error bodies |
| `WeatherResponseTest` | 8 | Full and sparse payloads, seconds→millis conversion, icon URLs |
| `WidgetLayoutTest` | 8 | Toggle/reorder invariants, key round-trip |
| `CacheExpiryTest` | 6 | Per-cache cutoffs via fake DAOs |

**A test immediately caught a real bug.** `WeatherResponse.tempMin`/`tempMax` were non-null
with a `0.0` default, so `main?.tempMax ?: main?.temp` could never fall back — any payload
without `temp_min`/`temp_max` would have displayed a high/low of **0°**. Fields are now
nullable so absence is distinguishable from a genuine zero.

G2 is deferred rather than faked: instrumented tests need a device or emulator, and writing
them without ever running them would repeat exactly the mistake this log documents.

#### Release build

R8 and resource shrinking are on. Required rules were added for Gson field reflection
(without which DTOs parse to nulls **in release builds only** — a classic ship-breaker), for
Retrofit interfaces, and for the manifest-resolved widget provider, foreground service and
OAuth activity. Tink's optional `KeysDownloader` references Google HTTP client and Joda-Time
that this app never calls; suppressed rather than shipping two unused libraries.

| Build | Size |
|---|---|
| Debug | 21.6 MB |
| Release (minified + shrunk) | **4.3 MB** |

Signing reads a gitignored `keystore.properties` (template committed as
`keystore.properties.example`). Without it `assembleRelease` still succeeds and simply
produces an unsigned APK, so CI never breaks on a missing credential.

**Build state:** clean `testDebugUnitTest` ✅ 46/46 · `lintDebug` ✅ · `assembleDebug` ✅ ·
`assembleRelease` ✅

### ✅ Phase H — Build and handoff (complete 2026-08-27)

Final verification run clean from `clean`: 46/46 tests, lint clean, both APKs produced and
the debug APK signature verified. Every component we declare was confirmed present in the
built APK, not just in source.

#### Three unused permissions removed

Auditing the shipped manifest against actual code found three permissions declared but never
exercised — Bible §12 asks for the minimum necessary:

| Removed | Why it was unnecessary |
|---|---|
| `ACCESS_FINE_LOCATION` | **Device location is never read.** The user types a place name in Settings and it is resolved with `Geocoder`, which needs no permission |
| `ACCESS_COARSE_LOCATION` | Same |
| `WRITE_CALENDAR` | `CalendarRepository` only ever reads |

This matters beyond tidiness: an unused location permission shows users a prompt for a
capability the app does not have, and would require a Play Store data-safety declaration for
data never collected. Both are documented in the manifest as re-addable if GPS-based weather
is ever implemented. **12 permissions → 9.**

#### Handoff

[TESTING.md](TESTING.md) is a 15-step, ~90-check device script ordered by dependency —
navigation first, then keys, then each widget, then appearance, accessibility, performance
and robustness. It ends with a "known not implemented" list so gaps already recorded here
are not re-filed as bugs.

Step 14 carries the Bible §6 performance targets with `adb` commands to measure them, since
those are the only claims that still cannot be made from this machine.

---

## Repairs Applied (2026-08-26)

### Resource / build-config blockers (8)
1. **`gradle.properties` did not exist** → created; `android.useAndroidX=true` was the hard stop.
2. `time_weather_widget_info.xml` sat in `app/src/main/xml/` → moved to `app/src/main/res/xml/`.
3. `res/font/about.md` — a Markdown file in a resource folder → removed.
4. All 13 font files were CamelCase (invalid resource names) → renamed to lowercase snake_case.
5. `<font-family>` blocks lived in `res/values/font_certs.xml` → moved to three `res/font/*.xml` files.
6. **No launcher icon existed** (manifest referenced a nonexistent `@mipmap/ic_launcher`) → adaptive icon + pre-API-26 fallback authored.
7. Unescaped `&` in `strings.xml` → escaped.
8. `local.properties` created and added to `.gitignore`.

### Kotlin compile errors (137 → 0)
- **Missing imports / wrong qualifiers**: `CacheLevel`, `MascotStateEntity`, `asStateFlow`, `org.w3c.dom.Element`, and the `Icons.Default.*` filled extension properties across 6 files.
- **Missing dependency**: added `androidx.compose.material:material-icons-extended`.
- **`CacheManager` used as if static**: added a stateless top-level `cacheLevelFor(timestamp, maxAgeMs)` helper and repointed ~10 call sites.
- **Data-class field drift**: `MascotStateEntity` uses `animation`/`lastUpdate` (call sites said `lastUpdated`); `NewsArticle` uses `pubDate` (call sites said `publishedAt`).
- **Component signature drift**: `WidgetStateHandler` call sites used `isError`/`loadingMessage`; reconciled and added `loadingMessage` passthrough.
- **`WeatherResponse` DTO did not exist** → written (`data/remote/WeatherResponse.kt`) with `toDomainModel()`; `WeatherEntity` gained `humidity`/`windSpeed` (schema v2 → v3, destructive migration — all tables are API caches).
- **Missing repository methods**: `MascotRepository.updateMascotState()` added; wrong call names corrected (`getRssFeedUrls`→`getRssFeeds`, `getCurrentTrack`→`fetchAndCachePlayback`, `getMascotState`→`getCachedMascotState`).
- **Real API misuse**: `NotificationManagerCompat.notify/cancel` called statically; `NotificationCompat.CATEGORY_WEATHER`/`CATEGORY_NEWS` do not exist; `Converter.Factory.responseBodyConverter` takes `Type` not `Class<*>`; a broken hand-rolled `ViewModel.async` extension; `PreferencesManager` accessors treated as `Flow`s.
- **`UpdateScheduler`** enqueued a `PeriodicWorkRequest` through `enqueueUniqueWork` → split to `enqueueUniquePeriodicWork` for ambient/realtime.

### Runtime correctness fixes
- **Swipe navigation never worked**: `detectHorizontalDragGestures` was called without `onHorizontalDrag`, so mode switching — the app's headline interaction — was inert. Implemented with drag accumulation and a commit-on-release threshold.
- **Background sync would have failed on every run**: `DashboardUpdateWorker` is a `@HiltWorker`, but the app never supplied a `HiltWorkerFactory`. `QuantumSlateApplication` now implements `Configuration.Provider`, and the manifest removes WorkManager's default startup initializer (it was set to `merge`, which is wrong for a custom factory).
- **Hilt `MissingBinding`**: `CacheManager`, `PreferencesManager`, and `WeatherRepository` requested a bare `Context`; qualified with `@ApplicationContext`.
- **Notifications**: all posts now route through a permission-guarded helper (Android 13+ `POST_NOTIFICATIONS`), fixing a lint error and a potential `SecurityException`.
- `.gitignore` had stray markdown ``` fences from a paste → removed.

---

## Next Steps

### Immediate
1. **Install and run on a device/emulator** — nothing here has been exercised at runtime yet.
2. Enter API keys in Settings (OpenWeatherMap, flight API, Spotify) and confirm each widget fetches.
3. Verify swipe navigation across all three modes.
4. Confirm WorkManager sync actually runs now that the Hilt factory is wired.

### Known gaps to close
5. Bind `TimeWeatherWidget` to `WeatherRepository` (currently placeholder data).
6. Write the test suite (`src/test`) — none exists.
7. Add the CI workflow — none exists.
8. Write README.md / SETUP_GUIDE.md.
9. Add a release signing config (`assembleRelease` currently produces an unsigned APK; `minifyEnabled` is false).
10. **Calendar widget has no data source.** Entity, DAO, domain model and UI all exist, but
    there is no `CalendarRepository`, no `CalendarContract` query, and no Google Sign-In —
    nothing ever populates the table, so the widget will always render empty.

---

*Status reflects a verified build, not a verified product.*
