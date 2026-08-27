# QuantumSlate — Device Test Script

Work top to bottom. **Order matters**: nothing data-driven can pass until the API keys in
Step 2 are entered.

Mark each row **PASS** / **FAIL** / **N/A** and note anything unexpected. A `FAIL` with the
step number is far more actionable than "almost nothing worked".

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

If the app misbehaves, capture logs alongside the failing step:

```bash
adb logcat -s QuantumSlateHttp:D AndroidRuntime:E
```

---

## Step 1 — Launch and navigation

*No keys needed. If any of these fail, stop and report — later steps depend on them.*

| # | Action | Expected | Result |
|---|---|---|---|
| 1.1 | Launch from the app drawer | Opens without crashing; launcher icon is the blue slate | |
| 1.2 | Look at the screen | Time and date shown, updating live | |
| 1.3 | Look at the bottom | **Four** dots, first one highlighted | |
| 1.4 | Swipe left | Moves to Data-Dense; second dot highlights | |
| 1.5 | Swipe left twice more | Retro Newspaper, then QuantumEffect | |
| 1.6 | Swipe left again at the last mode | Nothing happens (no wrap-around) | |
| 1.7 | Swipe right back to the first | Returns through the modes in reverse | |
| 1.8 | Tap the 4th dot directly | Jumps straight to QuantumEffect | |
| 1.9 | Look top-right of each mode | A **gear icon** on all four | |
| 1.10 | Tap the gear | Settings opens | |
| 1.11 | Press system Back | Returns to the **same mode** you left | |

## Step 2 — Settings and keys

| # | Action | Expected | Result |
|---|---|---|---|
| 2.1 | Scroll Settings | Sections: API Configuration, Spotify, Display, Updates, Mascot, Location | |
| 2.2 | Enter your OpenWeatherMap key, save | Field retains the value | |
| 2.3 | Enter your aviationstack key, save | Retains the value | |
| 2.4 | Set **Location** to a city name (e.g. `Edinburgh`) | Retains the value | |
| 2.5 | Back to dashboard | Weather widget populates **without a manual refresh** | |
| 2.6 | Force-stop the app, relaunch, open Settings | Keys still there | |

> A brand-new OpenWeatherMap key takes **up to 2 hours** to activate. Until then 2.5 fails
> with an error — that is the provider, not the app.

## Step 3 — Weather

| # | Action | Expected | Result |
|---|---|---|---|
| 3.1 | Minimalist mode | Temperature and conditions for your city | |
| 3.2 | Pull down from the top | Refresh spinner appears, then data refreshes | |
| 3.3 | Enable airplane mode, pull to refresh | Cached data stays visible with a staleness indicator — **not** a blank widget | |
| 3.4 | Disable airplane mode, refresh | Recovers | |

## Step 4 — Calendar

| # | Action | Expected | Result |
|---|---|---|---|
| 4.1 | Minimalist mode | Calendar shows **Grant access** | |
| 4.2 | Tap it | System permission dialog | |
| 4.3 | Allow | Widget populates with upcoming events | |
| 4.4 | Check the events | Colour stripe matches the source calendar; times correct | |
| 4.5 | Check labels | Today's events say "Today", tomorrow's say "Tomorrow" | |
| 4.6 | Deny instead (fresh install) | Shows the grant prompt again, not a crash or silent blank | |

## Step 5 — News

| # | Action | Expected | Result |
|---|---|---|---|
| 5.1 | Data-Dense mode, find News | Shows an empty state with a **+** | |
| 5.2 | Tap **+**, enter `http://example.com/feed` | Rejected: "Feed must use https://" | |
| 5.3 | Enter `https://feeds.bbci.co.uk/news/rss.xml` | Accepted | |
| 5.4 | Wait for refresh | Headlines appear | |
| 5.5 | Tap **+** again, re-enter the same URL | Rejected as duplicate | |

## Step 6 — Flights *(spends your monthly quota — 100 total)*

| # | Action | Expected | Result |
|---|---|---|---|
| 6.1 | Find the Flights widget | Empty state with a **+** | |
| 6.2 | Tap **+**, enter `XX` | Rejected: format hint shown | |
| 6.3 | Enter a real flight number departing today, e.g. `BA2490` | Accepted | |
| 6.4 | Wait for refresh | Flight row with route and status | |
| 6.5 | QuantumEffect mode, TRANSIT panel | **API BUDGET** meter showing remaining requests | |
| 6.6 | Add a third flight | Only the two most recent are tracked | |

## Step 7 — Spotify

*Requires the redirect URI registered per SETUP_GUIDE.md.*

| # | Action | Expected | Result |
|---|---|---|---|
| 7.1 | Settings → Spotify | **Connect** is disabled | |
| 7.2 | Enter and save your Client ID | Connect becomes enabled | |
| 7.3 | Tap **Connect** | Browser opens Spotify consent | |
| 7.4 | Approve | Returns to the app; Settings shows **Connected** | |
| 7.5 | Play something on Spotify, refresh | Now-playing shows track and artist | |
| 7.6 | Tap **Disconnect** | Returns to "Not connected" | |

## Step 8 — Mascot (Quantum Boy)

| # | Action | Expected | Result |
|---|---|---|---|
| 8.1 | Each of the four modes | Quantum Boy visible in all four | |
| 8.2 | Look closely | Clean edges — no cream halo or missing limbs | |
| 8.3 | Watch for ~30 seconds | **Static** unless the mood changes — he should not loop constantly | |
| 8.4 | Settings → Mascot → disable animations | Becomes fully static | |
| 8.5 | Settings → Mascot | Reads "Quantum Boy"; no dead character picker | |

## Step 9 — Appearance

| # | Action | Expected | Result |
|---|---|---|---|
| 9.1 | Retro mode masthead | Serif "The Daily Quantum" — **not** the system default font | |
| 9.2 | Retro folio line | `VOL. I` / date / `PRICE 5¢` between rules | |
| 9.3 | QuantumEffect mode | Near-black background, teal/gold accents, scanlines, corner ticks on panels | |
| 9.4 | Settings → Display → Dark | First three modes switch to dark | |
| 9.5 | Settings → Display → Light | They switch back | |
| 9.6 | QuantumEffect under both | Stays dark either way (by design) | |
| 9.7 | Settings → Default UI Mode → Retro; force-stop; relaunch | Opens **in Retro** | |

## Step 10 — Widget configuration

| # | Action | Expected | Result |
|---|---|---|---|
| 10.1 | **Long-press** anywhere on a dashboard | Config sheet slides up | |
| 10.2 | Toggle News off | Disappears from the dashboard | |
| 10.3 | Move a widget up | Order changes in the list | |
| 10.4 | Force-stop, relaunch, long-press | Your changes persisted | |
| 10.5 | Toggle News back on | Returns **to its previous position**, not the end | |

## Step 11 — Home screen widget

| # | Action | Expected | Result |
|---|---|---|---|
| 11.1 | Add "Time & Weather" from the widget picker | Places on the home screen | |
| 11.2 | Read it | Time, date, and a **real temperature** — not `--°` | |
| 11.3 | Compare to the app | Same temperature | |
| 11.4 | Tap the refresh button | Redraws | |
| 11.5 | Tap the body | Opens the app | |

## Step 12 — Update modes and battery

| # | Action | Expected | Result |
|---|---|---|---|
| 12.1 | Settings → Updates → Real-time | Ongoing notification appears | |
| 12.2 | Read the notification | States it uses **noticeably more battery** | |
| 12.3 | Switch to Ambient | Notification disappears | |
| 12.4 | Leave on Real-time 30 min, check flight budget in QuantumEffect | Budget **unchanged** — flights are excluded from the fast loop | |

## Step 13 — Accessibility *(Bible §13)*

| # | Action | Expected | Result |
|---|---|---|---|
| 13.1 | Enable TalkBack, swipe through a dashboard | Every icon announces a meaningful label | |
| 13.2 | Focus the mascot | Announces mood, e.g. "Mascot is waving, everything looks good" | |
| 13.3 | Focus the mode dots | Announces "Dashboard mode N of 4" | |
| 13.4 | Android Settings → Display → Font size → largest | Text scales; nothing clipped or overlapping | |

## Step 14 — Performance *(the Bible §6 targets, unmeasured so far)*

Run with the app open, then record actuals:

```bash
adb shell dumpsys meminfo com.quantumslate.dashboard | grep TOTAL
adb shell am start -W -n com.quantumslate.dashboard/.MainActivity | grep TotalTime
adb shell dumpsys gfxinfo com.quantumslate.dashboard | grep -A3 "Janky frames"
```

| # | Target | Measured | Result |
|---|---|---|---|
| 14.1 | RAM < 100 MB | | |
| 14.2 | Cold start < 2 s | | |
| 14.3 | Janky frames low (60 fps) | | |
| 14.4 | Battery < 5%/day in Daily mode (leave overnight) | | |

## Step 15 — Robustness

| # | Action | Expected | Result |
|---|---|---|---|
| 15.1 | Rotate the device in each mode | No crash; stays on the same mode | |
| 15.2 | Background 10 min, return | State intact | |
| 15.3 | Airplane mode, cold launch | Opens with cached data; no crash | |
| 15.4 | Clear app data, relaunch | Starts clean at Minimalist, prompts for keys again | |

---

## Reporting

For each failure, note: **step number**, what happened, and whether it's reproducible.

Known-not-implemented — please don't file these as bugs:

- Mascot accessories (umbrella in rain, sunglasses in sun) — base art has no accessory layers
- Only one mascot character — the spec asks for four
- Certificate pinning — deliberately omitted; see progress.md
- Drag-and-drop widget reorder — up/down buttons instead, so it doesn't fight the swipe
- Instrumented UI tests — deferred; they need a device to run
