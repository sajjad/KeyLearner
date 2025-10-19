The goal of this app is to allow the user to:
1. Learn the chords in keys (major, minor) by position - this will allow a user to instantly tell what chords they can use if someone says, "Let's play in the key of Em", or the exact chords if someone says, "Let's play 1-4-5-1 in Em".

## Game Play:

### Start screen: 

1. Allows selecting key(s) to guess from, the choices are in two rows, This section is labelled 'Keys':
	1. First row labelled 'Major', choices: A, B, C, D, E, F, G
	2. Second row labelled 'Minor', choices: A, B, C, D, E, F, G
2. Below key selection, a label called 'Count', followed by buttons to set the number of times different chord numbers are shown per key. 
   Choices: 5, 10, 15, 20, 25, 30, 40
3. Below count number is a label called 'Delay', which allows a user to select the length of time before a chord number changes.
   Choices: 0.5, 1, 1.5, 2, 2.5, 3, 4, 5, 6, 8, 10, 15, 20
4. Below delay is a label called 'Limit Choices to Key'. This decides If the answer choices should be limited to the chords in the selected key. This is a yes/no toggle button. 

### Game screen:

Top row: The currently selected key to guess from. If multiple keys were selected in the start screen, then once the count for the first key has been reached, the next key is shown, and so on until the last selected key has run through all it's chord numbers.
Second row: A number from 1 to 7. This number changes to another random number between 1 and 7 after the value in delay has been reached, or an answer has been selected/submitted.
Bottom section:
If 'Limit Choices to Key' is 'yes' the choices in the game screen will be:
Top row: all 7 chords in the currently selected key, but the order of the chords will be randomized with every new chord number. In this case, clicking on an option will advance the game to the next chord number

If 'Limit Choices to Key' is 'no', the choices in the game screen will be: 
Top row: A, B, C, D, E, F, G
Second row to have 2 sets of 3 buttons:
1. Chord quality - Major, Minor, Dim. The button labels are 'M', 'm', and 'dim'
2. Note type -  Natural, Sharp, Flat, none. The button labels are ' ', #, ♮
Whenever a new chord number shows, these 2 sets of button revert to no button being toggled on.
In this case, the answer is submitted when all three options have been selected.

Once chord numbers for all keys have been shown, the game exits to the score screen.

There should also be a quit game button to go back to the start screen.

### Score screen:

This screen shows:
A drop down labelled 'Keys', with all the keys that were presented to the player in the game. This will be the same list as selected in Keys section of the start screen.
Below the drop down is a graph which on the X-axis shows the chords for the selected key ordered from 1 to 7 including the chord name, e.g. if the Key in the drop down is C, the X-axis labels should be: 1-C, 2-Dm, 3-Em, 4-F, 5-G, 6-Am, 7-B°
The Y-axis shows the number of correct answers per chord in green (#27ae60), and wrong answer in orange (#f39c12)
The top of each bar should show the "right/wrong" count. The total height of a bar should match the total number of times a user was asked to name the chord, e.g. if a user was asked to name the 4th chord 7 time, the height of this bar would be 7

The user should be given a choice to replay with the same settings or exit to the start screen. If the user goes to the start screen, the same settings that the user played with should be pre-selected. 

### Notes:
- The values a user selects should be stored so that they're automatically loaded the next time the user plays the game.
- Any function written to generate the chords of a given major or minor key need to take into account that a flat is only used if the sharp or natural note has already been used in the scale. For instance, in the key of Em, the chords are: Em, F#dim, G, Am, Bm, C, D. They won't be: Em, G♮dim, G, Am, Bm, C, D as G is already in the scale, so G♮dim is replaced by an F#dim. 
  The theory here is that when figuring out the chords, the note names should occur only once, so if G has already been used, we don't use G♮. Rather we use F#.

