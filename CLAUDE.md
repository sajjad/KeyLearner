# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

KeyLearner is an Android music education app that helps users learn chord progressions by position in different keys. Users select keys (major/minor), practice identifying chords by their position (1-7), and receive scored feedback.

**Tech Stack:**
- Kotlin
- Jetpack Compose (Material3)
- MVVM Architecture
- MPAndroidChart for score visualisation
- DataStore for settings persistence

**Package:** `com.example.keylearner`
**Min SDK:** 33
**Target SDK:** 36

## Build & Run Commands

```bash
# Build the project
./gradlew build

# Run on connected device/emulator
./gradlew installDebug

# Run tests
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # Instrumented tests

# Clean build
./gradlew clean
```

## Architecture

### MVVM Structure
```
com.example.keylearner/
├── data/
│   ├── model/          # Data classes: Settings, GameState, Score, Chord
│   ├── repository/     # SettingsRepository (DataStore wrapper)
│   └── MusicTheory.kt  # Core chord generation logic
├── ui/
│   ├── screens/        # StartScreen, GameScreen, ScoreScreen composables
│   ├── components/     # Reusable UI components
│   ├── navigation/     # Compose Navigation setup
│   └── theme/          # Colours, Theme, Typography
└── viewmodel/          # ViewModels for each screen
```

### Navigation Flow
Start Screen → Game Screen → Score Screen → (back to Start or replay)

### State Management
- ViewModels hold UI state and business logic
- SettingsRepository manages persistent settings via DataStore
- GameViewModel manages game state, timer (Kotlin Coroutines/Flow), and score tracking
- Each screen observes ViewModel state via Compose State

## Critical Music Theory Logic

**Chord Generation (`MusicTheory.kt`):**
- Generates chords for positions 1-7 in any major or minor key
- **Critical Rule:** Each note name (A-G) must appear only once in a scale
- Enharmonic handling: If a note name is already used, use the sharp/flat equivalent
  - Example: In Em, position 2 is F#dim, NOT G♮dim (because G already appears at position 3)
- Major scale intervals: `[0, 2, 4, 5, 7, 9, 11]` with qualities `['M', 'm', 'm', 'M', 'M', 'm', 'dim']`
- Minor scale intervals: `[0, 2, 3, 5, 7, 8, 10]` with qualities `['m', 'dim', 'M', 'm', 'm', 'M', 'M']`

**Answer Checking:**
- Must accept enharmonic equivalents (F# === Gb, C# === Db, etc.)
- Compare note + accidental + quality for correctness

## Game Mechanics

### Two Answer Modes
1. **Limited Choices (limitChoices = true):**
   - Display all 7 chords from current key in randomized order
   - Clicking an answer immediately advances to next question
   - Re-shuffle chord order after each correct answer

2. **Full Choices (limitChoices = false):**
   - User selects: Note (A-G), Quality (M/m/dim), Accidental (♮/#/♭)
   - Submit button enabled only when all three selected
   - Reset selections after each question

### Game Progression
- Timer countdown triggers new random position (1-7) after delay expires
- Track questions asked vs count setting
- When count reached for current key, advance to next selected key
- When all keys completed, navigate to Score Screen

### Score Tracking
- Store correct/wrong counts per position (1-7) per key
- Format: `Map<KeyName, Map<Position, Score>>` where KeyName is "C", "Em", etc.
- Persist scores throughout game session for chart display

## UI Theme & Colours

**Specific Colours:**
- Correct answers: `#27ae60` (green)
- Wrong answers: `#f39c12` (orange)
- Gradient backgrounds: Purple/blue combinations

**Chart Requirements (MPAndroidChart):**
- Stacked bar chart with green (correct) and orange (wrong) segments
- X-axis labels: "1-C", "2-Dm", "3-Em", etc. (position-chordname)
- Display "correct/wrong" count labels on bars
- Bar height = total questions asked for that position

## Settings Persistence

Use DataStore Preferences to persist:
- Selected major keys (List<String>)
- Selected minor keys (List<String>)
- Count (Int): 5, 10, 15, 20, 25, 30, 40
- Delay (Float): 0.5 to 20 seconds
- Limit choices (Boolean)

Load settings on app start and restore after navigating back from Score Screen.

## Reference Implementation

A React/TypeScript reference implementation exists at `reference/chord-learning-app.tsx`. Use it to verify:
- Chord generation algorithm
- Enharmonic handling logic
- Game flow and state transitions
- Answer validation logic

## Language Style

**Use British spelling in all user-facing text:**
- "colour" not "color"
- "organisation" not "organization"
- "realise" not "realize"

## Development Plan

See `dev_plan.md` for the phased implementation plan with task tracking and status updates.
