# Snake Game

A simple yet feature-rich implementation of the classic Snake game written in Java using Swing.

## Features
- Smooth snake movement with directional controls (arrow keys or WASD)
- Score tracking and high score saving
- Timer display showing gameplay duration
- Sound effects (can be toggled with M key)
- Pause functionality (P key)
- Game over screen with play again option
- Visual enhancements including:
  - Snake with eyes that change direction
  - Animated buttons
  - Grid background
  - Text with borders for better visibility

## Controls
- **Movement:** Arrow keys or WASD
- **Pause:** P
- **Toggle Sound:** M
- **Restart after Game Over:** Space

## Game Structure
The game is implemented in a single Java class `SnakeGame.java` which extends JPanel and implements event listeners for keyboard input and game timing.

## Sounds
The game supports the following sound effects:

- Eating food
- Game over
- Game start
- Movement
- Collision

Sound files should be placed in the `sounds` directory as WAV files:

- `eat.wav`
- `gameover.wav`
- `start.wav` (optional)
- `move.wav` (optional)
- `collision.wav` (optional)

## High Score
The highest score is saved in `snake_high_score.dat` in the game directory.

## How to Run
1. Ensure you have Java installed
2. Compile the game: `javac SnakeGame.java`
3. Run the game: `java SnakeGame`

Alternatively, open the project in Visual Studio Code and run it using the Java extension's run button.

## Future Improvements
- Multiple difficulty levels
- Power-ups
- Multi-level gameplay
- Two-player mode

**Enjoy the game!**