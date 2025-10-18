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
  - View History button to access historical scores
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
  - Shuffle note names (A-G) in full mode to prevent position counting
- [x] **GameScreen Composable**:
  - Current key display (top)
  - Chord position number (large, centre)
  - Countdown timer display
  - Question progress counter
  - Two answer modes:
    - **Limited Mode**: Grid of 7 shuffled chords from current key
    - **Full Mode**: Note selection (A-G shuffled), Quality (M/m/dim), Accidental (♮/#/♭), Submit button
  - Quit button to return to start
- [x] Handle answer submission and state updates
- [x] Integrate GameScreen into AppNavigation with shared settings
- [x] **Note Shuffling Enhancement**: In full choice mode, shuffle the A-G note buttons to prevent users from counting positions from the key note

---

## Phase 7: Score Screen Implementation
**Status:** ✅ Complete

### Tasks:
- [x] **ScoreRepository**: DataStore implementation for historical scores
  - Save game scores after each session
  - Load historical scores by key
  - Aggregate scores across all games for cumulative view
  - Store timestamp with each game session
  - JSON serialization for persistence
  - Keep last 100 game sessions
- [x] **ScoreViewModel**:
  - Process current game scores into chart data format
  - Load and aggregate historical scores for all-time view
  - Toggle between "Current Game" and "All Time" modes
  - Generate chord labels (e.g., "1-C", "2-Dm")
  - Handle key selection for chart display
  - Provide cumulative statistics (total games played, overall accuracy, most practised keys)
- [x] **ScoreScreen Composable**:
  - **View Mode Toggle**: Calendar icon button to switch between:
    - **Current Game** (default): Shows scores from just-completed game
    - **All Time**: Shows cumulative scores across all previous games
  - Key dropdown/selector showing all played keys (filtered by view mode)
  - MPAndroidChart integration:
    - Grouped bar chart component (correct + wrong side-by-side)
    - X-axis: Chord positions with labels
    - Y-axis: Count
    - Green bars for correct (#27ae60)
    - Orange bars for wrong (#f39c12)
    - Value labels on bars
  - Statistics panel:
    - Current game: Accuracy percentage, total questions, correct/wrong counts
    - All time: Total games played, overall accuracy, correct/wrong counts, most practised keys
  - "Replay with Same Settings" button
  - "Back to Start Screen" button
- [x] Create `BarChartComposable` wrapper for MPAndroidChart
- [x] Save current game scores to repository when entering Score screen
- [x] Integrate ScoreScreen into AppNavigation with shared scores
- [x] Add "View History" button on Start Screen to access historical statistics

---

## Phase 7.5: Progress Tracking (Multi-Select Dual-Chart View)
**Status:** ✅ Complete

### Tasks:
- [x] **Data Models**:
  - Create `PositionProgressPoint` data class (timestamp, correct, wrong, accuracy, sessionIndex)
  - Create `ProgressStats` data class (first/latest accuracy, improvement, trend, encouragement message)
- [x] **ScoreRepository**:
  - Add `getProgressDataForPosition(keyName, position)` method
  - Returns chronological accuracy data for specific key/position across all sessions
  - Calculate session-by-session accuracy percentages
- [x] **ScoreViewModel**:
  - Multi-select position state using `Set<Int>` (supports selecting 1-7 positions)
  - Progress data stored as `Map<Int, List<PositionProgressPoint>>` for multiple positions
  - Implement `togglePosition()` for multi-select behavior
  - Load progress data for all selected positions concurrently
  - Auto-select position 1 on first view
- [x] **ScoreScreen UI** (All Time view only):
  - Position selector: 7 FilterChip buttons with chord labels (multi-select enabled)
  - Multi-line chart card showing accuracy trends for all selected positions
  - Progress summary card with compact metrics table
- [x] **Multi-Line Chart Components**:
  - Update `LineChartComposable` to support multiple lines with distinct colors
  - 7 position colors: Green, Blue, Purple, Orange, Red, Teal, Indigo
  - Line style variations: solid, dashed, dotted (cycles through for distinction)
  - Legend positioned below chart with chord labels
  - Disable individual value labels (prevents clutter)
- [x] **Progress Summary Card**:
  - Color indicator for each selected position
  - Latest accuracy and improvement metrics
  - Color-coded improvement arrows (↑/↓/→)
- [x] Handle edge cases (no positions selected, all 7 selected, different session counts)

---

## Phase 8: Settings Persistence
**Status:** ✅ Complete

### Tasks:
- [x] Implement DataStore preferences
- [x] Save settings when changed
- [x] Load settings on app start
- [x] Ensure settings persist after replay
- [x] Handle first-time launch (default settings)

---

## Phase 9: Testing & Validation
**Status:** ✅ Complete (Unit Tests) / ⏳ Pending (UI Tests)

### Tasks:
- [x] **Music Theory Tests** (`MusicTheoryTest.kt` - 50+ tests):
  - Test chord generation for all 7 major keys
  - Test chord generation for all 7 minor keys
  - Verify enharmonic handling (e.g., F# vs Gb)
  - Test edge cases (B major, F# minor, etc.)
  - Test scale consistency (each letter A-G appears once)
  - Test quality patterns (major: M-m-m-M-M-m-dim, minor: m-dim-M-m-m-M-M)
  - Test display names and key validation
- [x] **Game Logic Tests** (`GameLogicTest.kt` - 25+ tests):
  - Verify answer checking (correct/wrong)
  - Test enharmonic answer acceptance (C# vs Db, F# vs Gb, etc.)
  - Test all position validation
  - Test quality patterns across all keys
  - Real game scenario simulations
- [x] **Model Tests** (`ModelTest.kt` - 40+ tests):
  - PositionScore (accuracy calculations, totals)
  - KeyScore (aggregation, overall accuracy)
  - GameScores (multi-key scoring, overall accuracy)
  - Settings (key selection, validation)
  - GameState (key display, navigation)
  - SelectedAnswer (composition, validation)
  - Chord display names
- [ ] **ViewModel Tests** (requires instrumented testing):
  - Test timer at various delays
  - Verify score tracking accuracy
  - Test key transitions
- [ ] **Repository Tests** (requires instrumented testing):
  - Settings persistence via DataStore
  - Score persistence and retrieval
- [ ] **UI Tests** (requires instrumented testing):
  - Test navigation flow
  - Verify settings persistence
  - Test both answer modes
  - Verify chart displays correctly

---

## Phase 10: Polish & Final Touches
**Status:** ✅ Complete

### Tasks:
- [x] **Ensure all text uses British spelling**:
  - Fixed "visualization" → "visualisation" in comments (ScoreScreen.kt, build.gradle.kts, CLAUDE.md)
  - Verified all user-facing text uses British spelling
  - Framework API names (e.g., `color` parameter) correctly left as-is
- [x] **Add proper screen transitions/animations**:
  - Added smooth fade-in/fade-out transitions (300-400ms)
  - Added slide animations for forward/backward navigation
  - Start → Game → Score navigation flows smoothly
  - Proper pop animations when navigating back
- [x] **Responsive design verified**:
  - Landscape mode: Side-by-side layout (key/position left, choices right)
  - Portrait mode: Vertical layout with scrolling support
  - No scrolling required in landscape mode
  - Compact layouts adapt to smaller screens
- [x] **Code quality**:
  - Well-structured MVVM architecture maintained
  - ViewModels properly scoped
  - Navigation properly configured with shared state
  - Comprehensive test coverage (115+ unit tests)
- [x] **Documentation**:
  - KDoc comments on key classes and functions
  - README.md with game mechanics
  - CLAUDE.md with architecture and guidelines
  - TEST_SUMMARY.md with comprehensive testing documentation

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
