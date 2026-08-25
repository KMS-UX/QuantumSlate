# PROJECT: "QuantumSlate" - Android Information Dashboard App

## 🎯 PROJECT OVERVIEW
Build a customizable Android dashboard app for tablets that displays ambient information with multiple visual themes. The app should be lightweight, battery-efficient, and support swappable UI modes.

## 📋 CORE REQUIREMENTS

### 1. APP ARCHITECTURE
- **Platform:** Android Tablet (API 24+)
- **Language:** Kotlin
- **Architecture:** MVVM (Model-View-ViewModel)
- **UI Framework:** Jetpack Compose (preferred) OR XML Layouts
- **Database:** Room (for caching widget data offline)
- **Dependency Injection:** Hilt or Koin

### 2. MANDATORY WIDGETS (Phase 1)

**A. Time & Date Widget**
- Large digital clock (24h or 12h format)
- Date display with day of week
- Timezone support
- Optional: Sunrise/sunset times

**B. Weather Widget**
- Current temperature and conditions
- High/low for the day
- 3-day forecast
- Location-based (GPS or manual input)
- API: OpenWeatherMap (free tier)

**C. Calendar Widget**
- Next 3 upcoming events
- Event title, time, and calendar name
- Integration with Google Calendar API
- Color-coded by calendar

**D. RSS News Feed Widget**
- Fetch from user-defined RSS URLs
- Display 5-10 headlines
- Tap to open full article in browser
- Auto-refresh on schedule

**E. Flight Status Widget**
- Track 1-2 flights by flight number
- Display: departure/arrival times, gate, status
- API: AviationEdge or FlightAware
- Manual entry of flight numbers

**F. Spotify "Now Playing" Widget**
- Display current track (title, artist, album art)
- Requires Spotify API integration
- Show playback status (playing/paused)
- Optional: simple controls (play/pause/skip)

**G. Virtual Mascot Widget**
- Animated or static character
- Reacts to data (e.g., happy when weather is good, waving when flight is on time)
- Pixel art or simple vector style
- Positioned in corner of screen

### 3. UI/UX DESIGN MODES

**Mode A: Ultra-Minimalist (Default Dashboard)**
- Clean, lots of whitespace
- Large typography
- Only essential info visible
- Black/white or subtle color accents
- Swipe left to access Mode B

**Mode B: Data-Dense (Information Panel)**
- All widgets visible simultaneously
- Grid layout (3-4 columns)
- Compact information display
- Color-coded sections
- Swipe right to return to Mode A

**Mode C: Retro 1950s Newspaper/Magazine**
- Vintage aesthetic (serif fonts, sepia tones)
- Layout mimics old newspaper columns
- Mascot prominently featured as "mascot character"
- Decorative borders and flourishes
- Weather shown as "forecast box"
- News as "headlines"
- Calendar as "social calendar"

**UI Requirements:**
- Smooth swipe gestures to switch modes
- Settings accessible via gear icon (top-right)
- Pull-to-refresh on all modes
- Long-press on any widget to configure

### 4. UPDATE & REFRESH LOGIC

**Default Mode (Set & Forget):**
- Updates once per day at user-defined time
- OR manual refresh via pull-down or tap
- Minimal battery usage

**Ambient Mode (Toggle in Settings):**
- Updates every 15-30 minutes
- Background sync using WorkManager
- Moderate battery usage

**Real-Time Mode (Toggle in Settings):**
- Updates every 1-5 minutes
- WebSocket or frequent polling for live data
- High battery usage (show warning)

**Smart Update Rules:**
- Weather: Update every 30 min max
- Calendar: Update every 15 min or on event change
- Flight status: Update every 5 min when flight is active
- Spotify: Update every 30 sec when playing
- RSS: Update every 2 hours

### 5. SETTINGS & CONFIGURATION

**General Settings:**
- Select default UI mode on launch
- Update frequency toggle (Daily/Ambient/Real-time)
- Auto-update time (for daily mode)
- Location services toggle
- Dark mode / Light mode / Auto

**Widget Configuration:**
- Enable/disable individual widgets
- Drag-and-drop to reorder widgets
- Widget-specific settings (e.g., which RSS feeds, which flights to track)

**API Configuration:**
- Input fields for API keys:
  - OpenWeatherMap API key
  - AviationEdge/FlightAware API key
  - Spotify Client ID & Secret
- Store keys securely in EncryptedSharedPreferences

**Mascot Settings:**
- Choose mascot character (3-4 options)
- Toggle animations on/off
- Mascot personality (reactive vs static)

### 6. TECHNICAL SPECIFICATIONS

**Permissions Required:**
- INTERNET (for API calls)
- ACCESS_FINE_LOCATION (for weather)
- READ_CALENDAR (for calendar events)
- WAKE_LOCK (for background updates)
- RECEIVE_BOOT_COMPLETED (to restart service after reboot)

**Background Processing:**
- Use WorkManager for scheduled updates
- Use Foreground Service for real-time mode (with notification)
- Implement exponential backoff for failed API calls

**Data Caching:**
- Cache all API responses in Room database
- Display cached data when offline
- Show "last updated" timestamp
- Auto-expire cache after 24 hours

**Error Handling:**
- Graceful degradation if API fails
- Show cached data with warning indicator
- User-friendly error messages
- Retry logic with backoff

**Performance:**
- App should use < 100MB RAM
- Cold start time < 2 seconds
- Smooth 60fps animations
- Battery usage < 5% per day (in daily mode)

### 7. VIRTUAL MASCOT SYSTEM

**Mascot Requirements:**
- 4 character options (e.g., robot, cat, bird, abstract creature)
- Simple animations (idle, happy, thinking, waving)
- React to data:
  - Weather: Umbrella when raining, sunglasses when sunny
  - Calendar: Excited when event is soon, relaxed when free
  - Flight: Waving when on-time, worried when delayed
  - Spotify: Dancing or bobbing head to music
- Lottie animations or AnimatedVectorDrawable
- Size: 100x100dp to 150x150dp

**Mascot Logic Engine:**
- Create a "Mood State" system
- Mood calculated from widget data every update cycle
- Mood states: Happy, Neutral, Concerned, Excited, Sleepy
- Mascot animation changes based on mood

### 8. API INTEGRATIONS

**OpenWeatherMap:**
- Endpoint: `api.openweathermap.org/data/2.5/weather`
- Free tier: 1000 calls/day
- Cache responses for 30 minutes

**Google Calendar API:**
- Use Google Sign-In for authentication
- Read-only access to primary calendar
- Fetch next 7 days of events

**RSS Feeds:**
- Use Rome or ROME library for RSS parsing
- Support standard RSS 2.0 and Atom feeds
- Allow user to add custom feed URLs

**AviationEdge/FlightAware:**
- Flight tracking by flight number
- Cache flight data for 10 minutes
- Auto-detect when flight is no longer active

**Spotify API:**
- OAuth 2.0 authentication
- Endpoint: `api.spotify.com/v1/me/player`
- Poll every 30 seconds when active
- Cache for 5 minutes when paused

### 9. TESTING REQUIREMENTS

**Unit Tests:**
- Widget data parsing logic
- Mood calculation engine
- API response handling
- Cache expiration logic

**Integration Tests:**
- API calls with mock servers
- Room database operations
- WorkManager scheduling

**UI Tests:**
- Swipe gestures between modes
- Widget configuration screens
- Settings persistence
- Mascot animation triggers

### 10. DELIVERABLES

**Phase 1 (MVP - Week 1-2):**
- [ ] Basic app structure with 3 UI modes
- [ ] Time/Date widget
- [ ] Weather widget (with OpenWeatherMap)
- [ ] Calendar widget (read-only)
- [ ] Settings screen
- [ ] Daily update mode

**Phase 2 (Core Features - Week 3-4):**
- [ ] RSS News widget
- [ ] Flight status widget
- [ ] Spotify widget (basic)
- [ ] Virtual mascot (1 character, static)
- [ ] Ambient update mode
- [ ] Widget configuration UI

**Phase 3 (Polish - Week 5-6):**
- [ ] Real-time update mode
- [ ] Mascot animations & mood system
- [ ] All 3 mascot characters
- [ ] Retro newspaper theme polish
- [ ] Offline mode & caching
- [ ] Error handling
- [ ] Performance optimization

**Phase 4 (Extras - Week 7+):**
- [ ] Additional mascot characters
- [ ] Custom widget creator
- [ ] Widget marketplace (download layouts)
- [ ] Cloud sync for settings
- [ ] Tablet-optimized landscape mode

### 11. CODE QUALITY STANDARDS

- Follow Kotlin coding conventions
- Use meaningful variable/function names
- Comment complex logic
- Separate UI from business logic
- Use sealed classes for state management
- Implement proper error handling
- Write modular, reusable components

### 12. SECURITY CONSIDERATIONS

- Store API keys in EncryptedSharedPreferences
- Use HTTPS for all API calls
- Validate all user inputs
- Don't log sensitive information
- Request minimum necessary permissions
- Implement certificate pinning for critical APIs

### 13. ACCESSIBILITY

- Support TalkBack screen reader
- Minimum touch target size: 48dp
- High contrast mode support
- Scalable text (respect system font size)
- Content descriptions for all icons

---

## 🚀 STARTING INSTRUCTIONS FOR AI

**Step 1:** Create the project structure with:
- MVVM architecture
- Hilt dependency injection
- Room database setup
- Navigation component

**Step 2:** Implement the 3 UI mode screens using Jetpack Compose:
- MinimalistDashboard.kt
- DataDenseDashboard.kt  
- RetroNewspaperDashboard.kt

**Step 3:** Build the Time and Weather widgets first (simplest)

**Step 4:** Add swipe navigation between modes

**Step 5:** Implement settings screen and persistence

**Step 6:** Add remaining widgets one by one

**Step 7:** Implement mascot system

**Step 8:** Add background update logic with WorkManager

---

## 📦 DEPENDENCIES TO INCLUDE

```gradle
// Core
implementation "androidx.core:core-ktx:1.12.0"
implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.7.0"

// Compose
implementation "androidx.compose.ui:ui:1.6.0"
implementation "androidx.compose.material3:material3:1.2.0"
implementation "androidx.activity:activity-compose:1.8.2"

// Navigation
implementation "androidx.navigation:navigation-compose:2.7.6"

// Room Database
implementation "androidx.room:room-runtime:2.6.1"
implementation "androidx.room:room-ktx:2.6.1"
kapt "androidx.room:room-compiler:2.6.1"

// Hilt
implementation "com.google.dagger:hilt-android:2.50"
kapt "com.google.dagger:hilt-compiler:2.50"

// WorkManager
implementation "androidx.work:work-runtime-ktx:2.9.0"

// Networking
implementation "com.squareup.retrofit2:retrofit:2.9.0"
implementation "com.squareup.retrofit2:converter-gson:2.9.0"
implementation "com.squareup.okhttp3:okhttp:4.12.0"

// Image Loading
implementation "io.coil-kt:coil-compose:2.5.0"

// Lottie Animations (for mascot)
implementation "com.airbnb.android:lottie-compose:6.3.0"

// RSS Parsing
implementation "com.rometools:rome:2.1.0"

// Encrypted Preferences
implementation "androidx.security:security-crypto:1.1.0-alpha06"