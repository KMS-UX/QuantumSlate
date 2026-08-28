# QuantumSlate — Completion Roadmap

**Created:** 2026-08-26, after the first on-device test.
**Governing spec:** `QuantumSlateBible_1.0.md`
**Current state:** Phases A–H delivered. See `progress.md`; device testing is the next step, per `TESTING.md`.

---

## Locked decisions

| Decision | Choice |
|---|---|
| Visual language | **Two modes as of 2026-08-28: Quantum Daily (1950s newspaper) + QuantumEffect.** Minimalist and Data-Dense retired — they differed in skin only, and each extra mode multiplied where every widget and fix had to be wired. Their code stays in the repo, unreferenced. |
| Mascot | **Quantum Boy** (Toddler Edition), from the QuantumOS Design System. Reference art: `C:\Users\cleme\Downloads\QuantumBoy` |
| Calendar | **Local `CalendarContract`** — no Google Sign-In, no Cloud Console. Reads calendars already synced on device. |
| Flight data | **Pluggable `FlightDataSource`** + adaptive polling. aviationstack (100 req/mo) is the first implementation. |
| Delivery | **One APK at the end.** Every phase must still compile + lint clean before the next begins. |
| Progress tracking | **`progress.md` is updated at every phase completion**, not only at handoff. |

### ✅ Art-style gate — RESOLVED 2026-08-27

| Question | Decision |
|---|---|
| 4th mode | **Yes — "QuantumEffect" pixel-art sci-fi fantasy is mode 4.** Swipe order: Minimalist → Data-Dense → Retro → QuantumEffect. Sourced from the *Quantum Effect Design System* (`2d830ed5-c3ae-4368-91f3-19902e2737a7`), whose `components/hud/` (StatBar, Panel, Notification), `system/` (LoadingScreen, Minimap) and `icon-library` map well onto dashboard widgets. |
| Design depth | **Full styling per Bible §3** for all modes — not just token consistency. |
| Mascot art | **Extract frames to app drawables** from the Quantum Boy reference sheets. **User reviews the extracted frames before they are wired to moods.** |

Consequences now in scope:
- `DASHBOARD_MODES` and `ModeIndicator` handle 4 modes, not 3.
- `PreferencesManager.UiMode` gains a `QUANTUM_EFFECT` value (Room/prefs read this by name — existing stored values stay valid).
- Every widget needs a 4th styled variant.
- `ui/theme/` gains a fourth colour scheme + type treatment.

---

## Why the app failed on device — the four structural causes

Everything you saw traces to these. The roadmap is organised around fixing them in dependency order.

1. **There is no navigation.** `SettingsScreen` is dead code — never referenced. `MainActivity` renders one of three dashboards directly with no nav host, no gear icon, no back stack. Settings is physically unreachable.
2. **Therefore no API keys can be entered.** Weather/Flight/Spotify all fail closed. This is a hard dependency: nothing data-driven can work until #1 is fixed.
3. **Widgets are unevenly placed.** Minimalist renders Time + Weather only. Data-Dense adds News/Flight/Spotify. Retro has the only mascot. **No mode has a Calendar widget — the composable does not exist.**
4. **Interactive affordances are text, not controls.** "Tap + to add one" is an `emptyMessage` string; the `onAddData` callback that would render an actual **+** is `null` at every call site.

---

## Phase A — Make the app reachable

*Nothing else can be tested until this lands. Highest priority.*

| # | Task | Files |
|---|---|---|
| A1 | Add a Navigation Compose host: routes `dashboard` and `settings`. | `MainActivity.kt`, new `ui/navigation/QuantumSlateNavHost.kt` |
| A2 | Hoist the swipe-mode state out of `QuantumSlateApp` so it survives navigating to Settings and back. | `MainActivity.kt` |
| A3 | Add a **gear icon, top-right**, on all three dashboards (Bible §3 requires this) and pass a real `onNavigateToSettings`. | all 3 dashboards |
| A4 | Wire `SettingsScreen` into the graph with a back affordance. | `ui/screens/settings/SettingsScreen.kt` |
| A5 | Verify `SettingsViewModel` actually persists — it has never been executed. | `SettingsViewModel.kt` |
| A6 | Add a mode indicator (3 dots) so the swipe position is legible. | new `ui/components/ModeIndicator.kt` |

**Acceptance:** launch → gear icon visible in all 3 modes → Settings opens → key saved → back returns to the same mode.

---

## Phase B — Widget completeness

| # | Task | Notes |
|---|---|---|
| B1 | **Build the Calendar widget** — composable does not exist. | `CalendarWidgetWithStatus` in `EnhancedWidgets.kt` |
| B2 | Place every widget on every mode per Bible §3, styled per mode. Minimalist = essentials only; Data-Dense = 3–4 col grid, all widgets; Retro = newspaper columns. | all 3 dashboards |
| B3 | Wire `onAddData` everywhere so the **+** button actually renders. | fixes the "tap does nothing" symptom |
| B4 | **Add-flight dialog** — flight number entry, persists to `PreferencesManager`. | new `ui/components/AddFlightDialog.kt` |
| B5 | **Add-RSS-feed dialog** — URL entry + validation. | new `ui/components/AddFeedDialog.kt` |
| B6 | Pull-to-refresh on all modes (Bible §3). | `PullToRefreshBox` |
| ~~B7~~ | ~~Long-press any widget → config sheet~~ → **moved to Phase F**, alongside F3 (widget enable/disable); they share per-widget state. | `ui/components/WidgetConfigSheet.kt` |

---

## Phase C — Data sources

| # | Task | Notes |
|---|---|---|
| ~~C1~~ | ✅ **Done in Phase B** — `CalendarRepository` via `CalendarContract` — next 7 days, colour-coded, runtime `READ_CALENDAR` permission request. | The only missing data source with zero external dependency — do it first. |
| ~~C2~~ | ✅ **Done in Phase B** — runtime permission flow (calendar + notifications). | new `ui/permissions/PermissionRequester.kt` |
| ~~C3~~ | ✅ **Done** — `FlightDataSource` interface + `AviationStackDataSource` impl. Provider chosen by config, not code. | decouples us from the 100 req/mo ceiling |
| ~~C4~~ | ✅ **Done** — adaptive flight polling — only poll in a window around scheduled departure/arrival; back off hard once landed; respect a monthly budget counter. | ~95% request reduction; makes 100/mo viable for 1–2 flights |
| ~~C5~~ | ✅ **Done** — weather path audited end-to-end now that a key can be entered. | `WeatherRepository` + new `WeatherResponse` DTO are untested at runtime |
| ~~C6~~ | ✅ **Done** — Spotify OAuth 2.0 (PKCE) — auth screen, redirect scheme in manifest, token refresh, secure token storage. | Largest single item in this phase |
| ~~C7~~ | ✅ **Done** — RSS audited; fixed a guaranteed crash. against real feeds; the custom XML converter has never run. | `SimpleXmlConverterFactory` |

---

## Phase D — Mascot (Quantum Boy)

Source art is already approved and, helpfully, already drawn as evenly-spaced loop sheets.

| # | Task | Notes |
|---|---|---|
| ~~D1~~ | ✅ **Done** — extracted per-pose frames from the reference sheets using the `sprite-sheet-extraction` skill. | source: `C:\Users\cleme\Downloads\QuantumBoy` |
| ~~D2~~ | ✅ **Done** — mapped to the 5 mood states (engine already exists in `MascotRepository`). | see mapping below |
| ~~D3~~ | ✅ **Done** — replaced the 20 stub Lottie files with a frame-based `MascotRenderer`. **Static at rest**, stepped frame advance on state change — battery-safe and matches the house style. | `LottieMascotWidget.kt` → `MascotWidget.kt` |
| ~~D4~~ | ✅ **Done** — mascot on all three modes, sized/placed per mode. | Bible §7: 100–150dp |
| ~~D5~~ | ⚠️ **Not implemented** — base art has no accessory layers; recorded, not faked. (umbrella when raining, etc.) — defer if the base art doesn't support it; note as a gap rather than faking it. | |
| ~~D6~~ | ✅ **Done** — Quantum Boy only; dead character picker removed. or reduce it to Quantum Boy + variants — **this is a deliberate divergence from Bible §7 and must be recorded in `progress.md`.** | |

**Mood → pose mapping (all sourced from existing art):**

| Mood | Pose | Source sheet |
|---|---|---|
| HAPPY | Waving | Model Sheet Rev A (centre) / Sleepy frame 1 "Alert Stand" |
| NEUTRAL | Master idle | `QuantumBoyPrototype_MasterIdlePose.png` |
| CONCERNED | Shoulders drop / fatigue onset | Sleepy Sheet frame 2 |
| EXCITED | Jumping jacks | `QuantumBoyJumpingJacks.png` |
| SLEEPY | Head droop → yawn → curled | Sleepy Sheet frames 3–5 |
| *(loading)* | Walk cycle | `QuantumBoyWalk.png` |

---

## Phase E — Visual design

Currently there is effectively no styling — this is the "no design" you saw.

| # | Task |
|---|---|
| ~~E1~~ | ✅ **Minimalist:** large type, generous whitespace, essentials only, B/W + subtle accent (Bible §3 Mode A). |
| ~~E2~~ | ✅ **Data-Dense:** true 3–4 column grid, compact rows, colour-coded sections (Mode B). |
| ~~E3~~ | ✅ **Retro Newspaper:** masthead, column rules, drop caps, "forecast box", "social calendar", sepia ground. The vintage fonts are wired but barely used. Quantum Boy's atomic-age palette is a natural fit here. |
| ~~E4~~ | ✅ **Done** — fonts were never actually applied; fixed. Apply the 3 font families properly across the type scale. |
| ~~E5~~ | ✅ **Done** — the setting was stored but never read; fixed. |

---

## Phase F — Bible compliance sweep

| # | Task | Bible § |
|---|---|---|
| ~~F1~~ | ✅ **Done** — bound `TimeWeatherWidget` to `WeatherRepository` — it still shows placeholder data. | §4 |
| ~~F2~~ | ✅ **Done** — real-time foreground service with battery warning. | §4 |
| ~~F3~~ | ✅ **Done** (with B7) — enable/disable + up/down reorder; drag-and-drop deliberately not used. | §5 |
| ~~F4~~ | ✅ **Done** — expiry did not exist at all. Cache auto-expiry; "last updated" on every widget. | §6 |
| ~~F5~~ | ✅ **Done** — accessibility: TalkBack labels, 48dp targets, scalable text, contrast. | §13 |
| ⏳ F6 | **Needs a device** — performance: <100MB RAM, <2s cold start, 60fps — measure, don't assume. | §6 |
| ~~F7~~ | ✅ **Closed — user approved not implementing it.** Certificate pinning; confirm no key material is ever logged. | §12 |

---

## Phase G — The falsely-claimed Phase 4

These were previously marked complete. None exist.

| # | Task |
|---|---|
| ~~G1~~ | ✅ **Done** — 46 tests, 0 failures. Unit tests: mood engine, cache expiry, API parsing, RSS parsing. |
| ~~G2~~ | ✅ **Done** — 16 instrumented tests + emulator CI job. Instrumented tests: swipe navigation, settings persistence. |
| ~~G3~~ | ✅ **Done** — `.github/workflows/android-ci.yml` — build + test + lint. |
| ~~G4~~ | ✅ **Done** — `README.md` and `SETUP_GUIDE.md` (how to obtain each API key). |
| ~~G5~~ | ✅ **Done** — release 4.3MB vs debug 21.6MB. Signing config; enable `minifyEnabled` + ProGuard rules. |

---

## ~~Phase H~~ — ✅ Build and hand off (complete 2026-08-27)

1. Clean `assembleDebug` + `lintDebug`.
2. Produce the APK.
3. Hand over with a **structured test script**: every widget, every mode, permission flows, offline behaviour, background sync — so the next test pass produces a checklist rather than "almost nothing worked".

---

## Known divergences from the Bible

Recorded deliberately, to be re-confirmed before release:

- **Google Calendar API → local `CalendarContract`** (approved).
- **4 mascot characters → Quantum Boy only** (approved; Bible §7 asks for 4).
- **Flight polling every 5 min → adaptive windowed polling** (forced by free-tier limits; the Bible's cadence is not achievable on any free plan).
- **Lottie → frame-based mascot rendering** (the Lottie assets were never real).

---

## Risk register

| Risk | Impact | Mitigation |
|---|---|---|
| Spotify OAuth is the heaviest item and can block on app registration | Phase C stalls | Build it last in Phase C; everything else ships without it |
| aviationstack 100/mo could be exhausted during testing | Flight widget untestable | Budget counter + a fixture/replay mode for development |
| "Everything, then one test pass" means a large untested surface lands at once | Repeat of this session | Compile + lint gate per phase; detailed test script at handoff |
| Reference-sheet extraction may need manual review | Phase D slips | Sheets are evenly spaced and "smartwatch optimized"; review frames before wiring |
