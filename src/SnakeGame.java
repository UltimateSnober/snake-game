
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.*;
import javax.swing.*;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {

    private final int GAME_WIDTH = 600;
    private final int GAME_HEIGHT = 600;
    private final int UNIT = 25;
    private final Timer timer;  // Game timer
    private final ArrayList<Point> snake;
    private Point food;
    private char direction = 'R';
    private boolean running = false;
    private boolean paused = false;
    private final JFrame frame;
    private final Random rand = new Random();

    // Sound clips
    private Clip eatSound;
    private Clip gameOverSound;
    private Clip startSound;
    private Clip moveSound;
    private Clip collisionSound;
    private boolean soundEnabled = true;

    // Add time tracking variables
    private long startTime;
    private long pauseStartTime;
    private long totalPausedTime = 0;

    public SnakeGame() {
        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);

        frame = new JFrame("Snake Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        snake = new ArrayList<>();

        // Load sound effects
        loadSounds();

        // Initialize timer before calling initGame()
        timer = new Timer(100, this);

        initGame();
    }

    // Load game sound effects
    private void loadSounds() {
        try {
            // Create sound directory if it doesn't exist
            File soundDir = new File("sounds");
            if (!soundDir.exists()) {
                soundDir.mkdir();
                System.out.println("Created sounds directory. Please add sound files there.");
            }

            // Load eat sound
            File eatFile = new File("sounds/eat.wav");
            if (eatFile.exists()) {
                AudioInputStream eatStream = AudioSystem.getAudioInputStream(eatFile);
                eatSound = AudioSystem.getClip();
                eatSound.open(eatStream);
            }

            // Load game over sound
            File gameOverFile = new File("sounds/gameover.wav");
            if (gameOverFile.exists()) {
                AudioInputStream gameOverStream = AudioSystem.getAudioInputStream(gameOverFile);
                gameOverSound = AudioSystem.getClip();
                gameOverSound.open(gameOverStream);
            }

            // Load start sound
            File startFile = new File("sounds/start.wav");
            if (startFile.exists()) {
                AudioInputStream startStream = AudioSystem.getAudioInputStream(startFile);
                startSound = AudioSystem.getClip();
                startSound.open(startStream);
            }

            // Load move sound
            File moveFile = new File("sounds/move.wav");
            if (moveFile.exists()) {
                AudioInputStream moveStream = AudioSystem.getAudioInputStream(moveFile);
                moveSound = AudioSystem.getClip();
                moveSound.open(moveStream);
            }

            // Load collision sound
            File collisionFile = new File("sounds/collision.wav");
            if (collisionFile.exists()) {
                AudioInputStream collisionStream = AudioSystem.getAudioInputStream(collisionFile);
                collisionSound = AudioSystem.getClip();
                collisionSound.open(collisionStream);
            }

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("Error loading sounds: " + e.getMessage());
            // Continue game without sounds if there's an error
            soundEnabled = false;
        }
    }

    // Play a sound if enabled
    private void playSound(Clip clip) {
        if (soundEnabled && clip != null) {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            clip.start();
        }
    }

    private void initGame() {
        snake.clear();
        snake.add(new Point(UNIT * 4, UNIT * 4)); // initial head
        direction = 'R';
        running = true;
        paused = false;
        totalPausedTime = 0;
        startTime = System.currentTimeMillis();
        placeFood();

        // Play start sound
        playSound(startSound);

        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    // Fix the snake eyes when facing right in the draw method
    private void draw(Graphics g) {
        if (!running) {
            gameOver(g);
            return;
        }

        // draw grid
        g.setColor(Color.DARK_GRAY);
        for (int i = 0; i < GAME_HEIGHT / UNIT; i++) {
            g.drawLine(i * UNIT, 0, i * UNIT, GAME_HEIGHT);
            g.drawLine(0, i * UNIT, GAME_WIDTH, i * UNIT);
        }

        // draw food
        g.setColor(Color.RED);
        g.fillOval(food.x, food.y, UNIT, UNIT);

        // draw snake
        for (int i = 0; i < snake.size(); i++) {
            Point p = snake.get(i);
            if (i == 0) {
                g.setColor(new Color(0, 180, 0)); // Brighter green for head
            } else {
                g.setColor(new Color(0, 100, 0)); // Darker green for body
            }
            g.fillRect(p.x, p.y, UNIT, UNIT);

            // Draw eyes on the head
            if (i == 0) {
                g.setColor(Color.WHITE);
                switch (direction) {
                    case 'U' -> {
                        g.fillOval(p.x + 7, p.y + 5, 4, 4);
                        g.fillOval(p.x + 14, p.y + 5, 4, 4);
                    }
                    case 'D' -> {
                        g.fillOval(p.x + 7, p.y + 16, 4, 4);
                        g.fillOval(p.x + 14, p.y + 16, 4, 4);
                    }
                    case 'L' -> {
                        g.fillOval(p.x + 5, p.y + 7, 4, 4);
                        g.fillOval(p.x + 5, p.y + 14, 4, 4);
                    }
                    case 'R' -> {
                        g.fillOval(p.x + 16, p.y + 7, 4, 4);
                        g.fillOval(p.x + 16, p.y + 14, 4, 4);
                    }
                }
            }
        }

        // draw score and timer
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + (snake.size() - 1), 10, 30);

        // Add timer display
        String timeString = formatTime(getPlayTime());
        g.drawString("Time: " + timeString, GAME_WIDTH - 150, 30);

        // Draw sound toggle indicator
        g.setFont(new Font("Arial", Font.BOLD, 14));
        if (soundEnabled) {
            g.drawString("Sound: ON (M)", GAME_WIDTH - 150, GAME_HEIGHT - 20);
        } else {
            g.drawString("Sound: OFF (M)", GAME_WIDTH - 150, GAME_HEIGHT - 20);
        }

        // draw pause message
        if (paused) {
            g.setColor(new Color(255, 255, 255, 200));
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("PAUSED", GAME_WIDTH / 2 - 80, GAME_HEIGHT / 2);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press P to continue", GAME_WIDTH / 2 - 80, GAME_HEIGHT / 2 + 40);
        }
    }

    // Calculate elapsed play time (excluding paused time)
    private long getPlayTime() {
        long currentTime = System.currentTimeMillis();
        if (paused) {
            return pauseStartTime - startTime - totalPausedTime;
        } else {
            return currentTime - startTime - totalPausedTime;
        }
    }

    // Format time as MM:SS
    private String formatTime(long timeInMillis) {
        long seconds = timeInMillis / 1000;
        long minutes = seconds / 60;
        seconds %= 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void gameOver(Graphics g) {
        // Play game over sound
        playSound(gameOverSound);

        // Add semi-transparent overlay
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

        // Draw bordered Game Over text
        drawBorderedText(g, "Game Over", GAME_WIDTH / 2, GAME_HEIGHT / 2 - 70,
                new Font("Arial", Font.BOLD, 50), Color.RED, Color.WHITE, 3);

        // Draw final score with animation effect
        int score = snake.size() - 1;
        String scoreText = "Final Score: " + score;
        drawBorderedText(g, scoreText, GAME_WIDTH / 2, GAME_HEIGHT / 2 - 10,
                new Font("Arial", Font.BOLD, 30), Color.YELLOW, Color.BLACK, 2);

        // Display time survived
        String timeText = "Time Survived: " + formatTime(getPlayTime());
        drawBorderedText(g, timeText, GAME_WIDTH / 2, GAME_HEIGHT / 2 + 30,
                new Font("Arial", Font.BOLD, 30), Color.CYAN, Color.BLACK, 2);

        // Draw high score if applicable
        int highScore = getHighScore();
        if (score > highScore) {
            saveHighScore(score);
            drawBorderedText(g, "NEW HIGH SCORE!", GAME_WIDTH / 2, GAME_HEIGHT / 2 + 70,
                    new Font("Arial", Font.BOLD, 25), Color.GREEN, Color.BLACK, 2);
        } else {
            drawBorderedText(g, "High Score: " + highScore, GAME_WIDTH / 2, GAME_HEIGHT / 2 + 70,
                    new Font("Arial", Font.PLAIN, 20), Color.WHITE, Color.BLACK, 1);
        }

        // Create an animated, glowing button effect
        drawPlayAgainButton(g);

        timer.stop();
    }

    // Improved play again button for Game Over screen
    private void drawPlayAgainButton(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int buttonY = GAME_HEIGHT / 2 + 120;
        int buttonWidth = 300;
        int buttonHeight = 50;
        int buttonX = GAME_WIDTH / 2 - buttonWidth / 2;

        // Use a try-catch block to ensure rendering doesn't fail
        try {
            // Set up nice gradients for button - using more vibrant colors
            GradientPaint gradient = new GradientPaint(
                    buttonX, buttonY - 25, new Color(30, 150, 30),
                    buttonX, buttonY + buttonHeight - 25, new Color(10, 80, 10)
            );
            g2d.setPaint(gradient);

            // Draw button with rounded corners
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D roundRect = new RoundRectangle2D.Float(
                    buttonX, buttonY - 25, buttonWidth, buttonHeight, 15, 15);
            g2d.fill(roundRect);

            // Create simpler glow effect (to avoid potential rendering issues)
            g2d.setColor(new Color(100, 255, 100, 70));
            g2d.setStroke(new BasicStroke(3.0f));
            g2d.draw(roundRect);

            g2d.setColor(new Color(100, 255, 100, 40));
            g2d.setStroke(new BasicStroke(6.0f));
            g2d.draw(roundRect);

            // Draw border
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.draw(roundRect);

            // Add button text with shadow
            Font buttonFont = new Font("Arial", Font.BOLD, 22);
            String buttonText = "PRESS SPACE TO PLAY AGAIN";

            FontMetrics fm = g2d.getFontMetrics(buttonFont);
            int textWidth = fm.stringWidth(buttonText);
            int textX = GAME_WIDTH / 2 - textWidth / 2;
            int textY = buttonY + 5;

            // Text shadow
            g2d.setFont(buttonFont);
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.drawString(buttonText, textX + 2, textY + 2);

            // Main text
            g2d.setColor(Color.WHITE);
            g2d.drawString(buttonText, textX, textY);

        } catch (Exception e) {
            // Fallback simple button rendering if advanced graphics fail
            g2d.setColor(new Color(0, 100, 0));
            g2d.fillRect(buttonX, buttonY - 25, buttonWidth, buttonHeight);
            g2d.setColor(Color.WHITE);
            g2d.drawRect(buttonX, buttonY - 25, buttonWidth, buttonHeight);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            String fallbackText = "PRESS SPACE TO PLAY AGAIN";
            g2d.drawString(fallbackText,
                    GAME_WIDTH / 2 - g2d.getFontMetrics().stringWidth(fallbackText) / 2,
                    buttonY + 5);
        }
    }

    // Helper method for drawing outlined text
    private void drawBorderedText(Graphics g, String text, int x, int y, Font font, Color textColor, Color borderColor, int thickness) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(font);

        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        x = x - textWidth / 2; // Center text

        // Draw border
        g2d.setColor(borderColor);
        for (int i = -thickness; i <= thickness; i++) {
            for (int j = -thickness; j <= thickness; j++) {
                if (i != 0 || j != 0) {
                    g2d.drawString(text, x + i, y + j);
                }
            }
        }

        // Draw text
        g2d.setColor(textColor);
        g2d.drawString(text, x, y);
    }

    // Methods to handle high score
    private int getHighScore() {
        try {
            java.io.File file = new java.io.File("snake_high_score.dat");
            if (!file.exists()) {
                return 0;
            }
            int score;
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file))) {
                score = Integer.parseInt(reader.readLine());
            }
            return score;
        } catch (IOException | NumberFormatException e) {
            return 0;
        }
    }

    private void saveHighScore(int score) {
        try {
            java.io.File file = new java.io.File("snake_high_score.dat");
            try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(file))) {
                writer.write("" + score);
            }
        } catch (IOException e) {
            // Handle exception silently - it's just a high score
        }
    }

    private void placeFood() {
        // Make sure food doesn't spawn on the snake
        boolean validPosition = false;

        while (!validPosition) {
            int x = rand.nextInt(GAME_WIDTH / UNIT) * UNIT;
            int y = rand.nextInt(GAME_HEIGHT / UNIT) * UNIT;
            food = new Point(x, y);

            validPosition = true;
            for (Point p : snake) {
                if (p.equals(food)) {
                    validPosition = false;
                    break;
                }
            }
        }
    }

    private void move() {
        if (!running || paused) {
            return;
        }

        Point head = snake.get(0);
        Point newPoint = new Point(head);

        switch (direction) {
            case 'U' ->
                newPoint.y -= UNIT;
            case 'D' ->
                newPoint.y += UNIT;
            case 'L' ->
                newPoint.x -= UNIT;
            case 'R' ->
                newPoint.x += UNIT;
        }

        // check collisions
        if (newPoint.x < 0 || newPoint.y < 0 || newPoint.x >= GAME_WIDTH || newPoint.y >= GAME_HEIGHT || checkSelfCollision(newPoint)) {
            running = false;
            playSound(collisionSound);
            return;
        }

        // Play move sound occasionally (not every move to avoid sound spam)
        if (snake.size() % 5 == 0) {
            playSound(moveSound);
        }

        snake.add(0, newPoint);

        if (newPoint.x == food.x && newPoint.y == food.y) {
            placeFood(); // eat food
            playSound(eatSound);
        } else {
            snake.remove(snake.size() - 1); // move forward
        }
    }

    private boolean checkSelfCollision(Point head) {
        // Check if head collides with any part of the body
        for (int i = 1; i < snake.size(); i++) {
            if (head.equals(snake.get(i))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            // Direction controls (arrow keys and WASD)
            case KeyEvent.VK_UP, KeyEvent.VK_W -> {
                if (direction != 'D') {
                    direction = 'U';
                }
            }
            case KeyEvent.VK_DOWN, KeyEvent.VK_S -> {
                if (direction != 'U') {
                    direction = 'D';
                }
            }
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> {
                if (direction != 'R') {
                    direction = 'L';
                }
            }
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> {
                if (direction != 'L') {
                    direction = 'R';
                }
            }

            // Restart game with SPACE
            case KeyEvent.VK_SPACE -> {
                if (!running) {
                    initGame();
                }
            }

            // Pause game with P
            case KeyEvent.VK_P -> {
                if (running) {
                    paused = !paused;
                    if (paused) {
                        pauseStartTime = System.currentTimeMillis();
                    } else {
                        // Add paused time to total
                        totalPausedTime += System.currentTimeMillis() - pauseStartTime;
                    }
                }
            }

            // Toggle sound with M
            case KeyEvent.VK_M -> {
                soundEnabled = !soundEnabled;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SnakeGame game = new SnakeGame();
            game.frame.add(game);
            game.frame.pack();
            game.frame.setLocationRelativeTo(null);
            game.frame.setVisible(true);
            game.addKeyListener(game);
        });
    }
}
