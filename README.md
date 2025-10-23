
Note: This app was built entirely using Claude Code. I wanted to see how well Claude Code works for languages / build pipelines I have no familiarity with.

# KeyLearner

**Learn chord progressions by position in any musical key**

KeyLearner is an Android app designed to help musicians master chord progressions by learning the positional relationships within major and minor keys. Whether you're a guitarist, pianist, or any instrumentalist, KeyLearner helps you instantly recall which chords belong to any key and their numerical positions (I-VII).

## Why KeyLearner?

When someone says "Let's play a 1-4-5 progression in Em" or "Let's jam in the key of C", you'll know exactly which chords to play without hesitation. This app trains your muscle memory for chord positions, making you a more versatile and confident musician.

## Features

### 🎯 **Customisable Practice Sessions**
- Select multiple major and/or minor keys to practise (A-G)
- Set the number of questions per key (5-40)
- Adjust the countdown timer (0.5-20 seconds)
- Choose between limited (7 chords) or full (all notes + qualities) answer modes

### 📊 **Comprehensive Score Tracking**
- View current game results or all-time statistics
- Bar charts showing correct vs. wrong answers per chord position
- Response time tracking with scatter charts (2 decimal place precision)
- Progress tracking with multi-line charts showing improvement over time
- Accuracy percentages and practice statistics

### 💾 **Data Backup & Transfer**
- Export your score history as CSV files
- Import CSV data to transfer progress between devices
- Automatic duplicate detection and merge logic
- Timestamped exports for easy organisation

### 🎓 **Smart Learning Modes**
- **Limited Mode**: Select from 7 shuffled chords in the current key (quick practice)
- **Full Mode**: Build chords from note name, quality, and accidental (advanced practice)

### 📈 **Progress Visualisation**
- Track improvement for specific chord positions over time
- Multi-select comparison of up to 7 positions simultaneously
- Response time analysis with scatter charts showing speed trends
- Colour-coded accuracy trends by note (A-G)
- Session-by-session breakdown
- Unified filter system controlling multiple chart views

## Screenshots



### Start Screen
*Configure your practice session with key selection, question count, timer delay, and answer mode.*

<a href="imgs/start_screen.png"><img src="imgs/start_screen.png" alt="Start Screen" height="300"></a>

---

### Game Screen - Limited Mode
*Quick practice: Select the correct chord from 7 shuffled options.*

<a href="imgs/game_limited.png"><img src="imgs/game_limited.png" alt="Game Screen" height="300"></a>

---

### Game Screen - Full Mode
*Advanced practice: Build the chord by selecting note, quality, and accidental.*

<a href="imgs/game_full.png"><img src="imgs/game_full.png" alt="Game Screen" height="300"></a>

---

### Score Screen - Current Game
*Review your performance with detailed statistics and position-by-position breakdown.*

<a href="imgs/score_game.png"><img src="imgs/score_game.png" alt="Score Screen - Game" height="300"></a>

---

### Score Screen - All Time Statistics
*Track your progress over time with cumulative stats and historical data.*

<a href="imgs/score_all_time.png"><img src="imgs/score_all_time.png" alt="Score Screen - History" height="300"></a>

---

### Progress Tracking
*Multi-line charts showing accuracy trends for multiple chord positions over time.*

<a href="imgs/progress_tracking.png"><img src="imgs/progress_tracking.png" alt="Score Screen - Tracking" height="300"></a>

---

## How It Works

### Music Theory Foundation
KeyLearner uses proper music theory to generate chords:
- Each note name (A-G) appears exactly once in a scale
- Enharmonic equivalents are handled correctly (F# vs G♭)
- Major scale pattern: M-m-m-M-M-m-dim (I-ii-iii-IV-V-vi-vii°)
- Minor scale pattern: m-dim-M-m-m-M-M (i-ii°-III-iv-v-VI-VII)

### Example: Key of C Major
1. C (Major)
2. Dm (Minor)
3. Em (Minor)
4. F (Major)
5. G (Major)
6. Am (Minor)
7. B° (Diminished)

### Example: Key of Em (E Minor)
1. Em (Minor)
2. F#° (Diminished) - *Note: F# not G♭, because G already appears at position 3*
3. G (Major)
4. Am (Minor)
5. Bm (Minor)
6. C (Major)
7. D (Major)

## Technical Details

### Architecture
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Charts**: MPAndroidChart
- **Data Persistence**: DataStore Preferences
- **Min SDK**: 33 (Android 13)
- **Target SDK**: 36

### Project Structure
```
com.example.keylearner/
├── data/
│   ├── model/          # Data classes (Settings, GameState, Score, Chord)
│   ├── repository/     # ScoreRepository (DataStore wrapper)
│   └── MusicTheory.kt  # Core chord generation logic
├── ui/
│   ├── screens/        # StartScreen, GameScreen, ScoreScreen
│   ├── components/     # Reusable UI components
│   ├── navigation/     # Compose Navigation setup
│   └── theme/          # Material 3 theme
├── viewmodel/          # ViewModels for each screen
└── util/               # Helper utilities (FileExportHelper)
```

### Key Features Implementation
- ✅ Enharmonic chord name handling
- ✅ Circular countdown timer animation
- ✅ Real-time score tracking
- ✅ Response time tracking (2 decimal place precision)
- ✅ CSV import/export with validation and response time data
- ✅ Progress charts with multi-select positions
- ✅ Unified filter system for performance analysis
- ✅ Scatter charts for response time visualisation
- ✅ Independent X-axis numbering per position in historical view
- ✅ Responsive landscape/portrait layouts
- ✅ Smooth navigation transitions

## Building the Project

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Android SDK with API level 33+

### Build Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/KeyLearner.git
   ```

2. Open in Android Studio:
   - File → Open → Select the KeyLearner directory

3. Sync Gradle:
   - Android Studio will prompt you to sync Gradle files
   - Click "Sync Now"

4. Run the app:
   - Connect an Android device or start an emulator
   - Click the "Run" button (▶️) or press Shift+F10

### Running Tests
```bash
# Unit tests (115+ tests)
./gradlew test

# Or run in Android Studio:
# Right-click on test package → Run Tests
```

## Testing Coverage
- **Music Theory**: 50+ tests for chord generation and enharmonic handling
- **Game Logic**: 25+ tests for answer validation and scoring
- **Data Models**: 40+ tests for score calculations and aggregations
- **CSV Import/Export**: 18+ tests for validation and merge logic

**Total: 133+ unit tests** ✅

## CSV Export Format

Score history is exported in a simple CSV format for easy backup and analysis:

```csv
Timestamp,Key,Position,Correct,Wrong,TimeSeconds
2025-10-23T14:30:00Z,C,1,5,2,2.34;1.87;3.45;2.12;1.95;2.67;2.51
2025-10-23T14:30:00Z,C,2,3,4,3.21;4.56;2.89;1.98;3.12;2.45;2.78
2025-10-23T14:30:00Z,Em,1,7,1,1.56;2.34;1.89;2.45;1.78;2.12;1.93;2.67
```

- **Timestamp**: ISO 8601 format (UTC)
- **Key**: Musical key (e.g., C, Em, F#m)
- **Position**: Chord position 1-7
- **Correct**: Number of correct answers
- **Wrong**: Number of wrong answers
- **TimeSeconds**: Semicolon-separated response times in seconds (2 decimal places)
  - One time value per question asked
  - Backward compatible: missing column defaults to empty list

## Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

### Development Guidelines
- Follow MVVM architecture patterns
- Write unit tests for new features
- Use British spelling in code and documentation
- Maintain Material 3 design consistency

## Licence

This project is licensed under the MIT Licence - see the [LICENCE](LICENCE) file for details.

## Acknowledgements

- Built with [Jetpack Compose](https://developer.android.com/jetpack/compose)
- Charts powered by [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)
- Icons from [Material Design Icons](https://fonts.google.com/icons)

## Contact

For questions, suggestions, or feedback, please open an issue on GitHub.

---

**Happy practising! 🎵🎸🎹**
