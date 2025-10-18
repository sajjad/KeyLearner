/# KeyLearner Android App Development Plan

## Overview
Building an Android chord learning game using Kotlin, Jetpack Compose, and MPAndroidChart.

---

## Phase 1: Project Configuration & Dependencies
**Status:** ✅ Complete

### Tasks:
- [x] Update `app/build.gradle.kts` to add:
  - Jetpack Compose dependencies (Material3, UI, Navigation)
  - MPAndroidChart library for score charts
  - ViewModel & Lifecycle components
  - DataStore for settings persistence
- [x] Enable Compose build features
- [x] Update AndroidManifest.xml with MainActivity
- [x] Sync project and verify dependencies (sync in Android Studio with "Sync Now" button)

---

## Phase 2: Project Architecture Setup (MVVM)
**Status:** ✅ Complete

### Package Structure:
```
com.example.keylearner/
├── data/
│   ├── model/          # Data classes (Settings, GameState, Score, Chord)
│   ├── repository/     # SettingsRepository (DataStore wrapper)
│   └── MusicTheory.kt  # Chord generation logic
├── ui/
│   ├── screens/        # StartScreen, GameScreen, ScoreScreen
│   ├── components/     # Reusable UI components
│   ├── navigation/     # Navigation setup
│   └── theme/          # Compose theme & colours
└── viewmodel/          # ViewModels for each screen
```

### Tasks:
- [x] Create package structure
- [x] Create MainActivity.kt
- [x] Create basic theme files (Color.kt, Theme.kt, Type.kt)

---

## Phase 3: Data Layer Implementation
**Status:** ✅ Complete

### Tasks:
- [x] **MusicTheory.kt**: Port chord generation logic from reference
  - Implement `getChord()` function with proper enharmonic handling
  - Define note arrays and intervals for major/minor scales
  - Handle sharp/flat key signatures correctly
- [x] **Model classes**:
  - `Settings` data class (majorKeys, minorKeys, count, delay, limitChoices)
  - `GameState` data class (currentKey, isMinor, currentPosition, etc.)
  - `Score` data classes (PositionScore, KeyScore, GameScores)
  - `Chord` data class (note, quality)
  - `SelectedAnswer` data class for user input
- [x] **SettingsRepository**: DataStore implementation
  - Save/load settings asynchronously
  - Provide default values
  - Individual update methods for each setting

---

## Phase 4: UI Theme & Navigation
**Status:** ✅ Complete

### Tasks:
- [x] Create Compose theme in `ui/theme/`:
  - `Color.kt` - Define colours (#27ae60 green, #f39c12 orange, purple/blue gradients)
  - `Theme.kt` - Material3 theme setup
  - `Type.kt` - Typography if needed
- [x] Create `AppNavigation.kt`:
  - Define 3 navigation destinations (Start, Game, Score)
  - Set up NavHost with composable routes
  - Create placeholder screens for navigation testing
- [x] Create `Routes.kt` with sealed class navigation routes
- [x] Update MainActivity to use navigation

---

## Phase 5: Start Screen Implementation
**Status:** ✅ Complete

### Tasks:
- [x] **StartScreenViewModel**:
  - Manage settings state
  - Load persisted settings on init
  - Validate selections (at least one key selected)
  - Toggle methods for major/minor keys
  - Update methods for count, delay, and limit choices
- [x] **StartScreen Composable**:
  - Key selection section (Major/Minor rows with A-G buttons)
  - Count selection (5, 10, 15, 20, 25, 30, 40)
  - Delay selection (0.5 to 20 seconds)
  - "Limit Choices to Key" toggle
  - Start Game button with validation
- [x] Style with gradient backgrounds and proper spacing
- [x] Integrate into AppNavigation

---

## Phase 6: Game Screen Implementation
**Status:** ✅ Complete

### Tasks:
- [x] **GameViewModel**:
  - Initialise game state from settings
  - Implement countdown timer using Kotlin Coroutines/Flow
  - Generate random chord positions
  - Shuffle chords for limited mode
  - Handle answer checking with enharmonic equivalence
  - Track progress and advance through keys
  - Update scores in real-time
  - Methods for both limited and full choice modes
- [x] **GameScreen Composable**:
  - Current key display (top)
  - Chord position number (large, centre)
  - Countdown timer display
  - Question progress counter
  - Two answer modes:
    - **Limited Mode**: Grid of 7 shuffled chords from current key
    - **Full Mode**: Note selection (A-G), Quality (M/m/dim), Accidental (♮/#/♭), Submit button
  - Quit button to return to start
- [x] Handle answer submission and state updates
- [x] Integrate GameScreen into AppNavigation with shared settings

---

## Phase 7: Score Screen Implementation
**Status:** ⏳ Pending

### Tasks:
- [ ] **ScoreRepository**: DataStore/Room implementation for historical scores
  - Save game scores after each session
  - Load historical scores by key
  - Aggregate scores across all games for cumulative view
  - Store timestamp with each game session
- [ ] **ScoreViewModel**:
  - Process current game scores into chart data format
  - Load and aggregate historical scores for all-time view
  - Toggle between "Current Game" and "All Time" modes
  - Generate chord labels (e.g., "1-C", "2-Dm")
  - Handle key selection for chart display
  - Provide cumulative statistics (total games played, overall accuracy, etc.)
- [ ] **ScoreScreen Composable**:
  - **View Mode Toggle**: Calendar icon button to switch between:
    - **Current Game** (default): Shows scores from just-completed game
    - **All Time**: Shows cumulative scores across all previous games
  - Key dropdown/selector showing all played keys (filtered by view mode)
  - MPAndroidChart integration:
    - Stacked bar chart component
    - X-axis: Chord positions with labels
    - Y-axis: Count
    - Green bars for correct (#27ae60)
    - Orange bars for wrong (#f39c12)
    - Labels showing "correct/wrong" counts on bars
  - Statistics panel:
    - Current game: Accuracy percentage, total questions
    - All time: Total games played, overall accuracy, most practised keys
  - "Replay with Same Settings" button
  - "Back to Start Screen" button
- [ ] Create `BarChartComposable` wrapper for MPAndroidChart
- [ ] Save current game scores to repository when entering Score screen

---

## Phase 8: Settings Persistence
**Status:** ⏳ Pending

### Tasks:
- [ ] Implement DataStore preferences
- [ ] Save settings when changed
- [ ] Load settings on app start
- [ ] Ensure settings persist after replay
- [ ] Handle first-time launch (default settings)

---

## Phase 9: Testing & Validation
**Status:** ⏳ Pending

### Tasks:
- [ ] **Music Theory Tests**:
  - Test chord generation for all 7 major keys
  - Test chord generation for all 7 minor keys
  - Verify enharmonic handling (e.g., F# vs Gb)
  - Test edge cases (B major, F# minor, etc.)
- [ ] **Game Logic Tests**:
  - Test timer at various delays
  - Verify answer checking (correct/wrong)
  - Test enharmonic answer acceptance
  - Verify score tracking accuracy
  - Test key transitions
- [ ] **UI Tests**:
  - Test navigation flow
  - Verify settings persistence
  - Test both answer modes
  - Verify chart displays correctly

---

## Phase 10: Polish & Final Touches
**Status:** ⏳ Pending

### Tasks:
- [ ] Ensure all text uses British spelling (colour, organisation, etc.)
- [ ] Add proper screen transitions/animations
- [ ] Optimise performance
- [ ] Test on different screen sizes
- [ ] Add app icon if needed
- [ ] Review and clean up code
- [ ] Add code documentation

---

## Status Legend:
- ⏳ **Pending** - Not started
- 🚧 **In Progress** - Currently working on
- ✅ **Complete** - Finished and tested
- ⚠️ **Blocked** - Waiting on something

---

## Notes:
- Reference implementation: `reference/chord-learning-app.tsx`
- Package name: `com.example.keylearner`
- Min SDK: 33
- Target SDK: 36
- Kotlin version: As per project defaults
