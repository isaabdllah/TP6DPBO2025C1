import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int frameWidth = 360;
    int frameHeight = 640;

    // image attributes
    Image backgroundImage;
    Image birdImage;
    Image lowerPipeImage;
    Image upperPipeImage;

    // player attributes
    int playerStartPosX = frameWidth / 8;
    int playerStartPosY = frameHeight / 2;
    int playerWidth = 34;
    int playerHeight = 24;
    Player player;

    // pipes attributes
    int pipeStartPosX = frameWidth;
    int pipeStartPosY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;
    ArrayList<Pipe> pipes;

    // game logic attributes
    Timer gameLoop;
    Timer pipesCooldown;
    int gravity = 1;
    private double gravityAcceleration = 0.5; // Percepatan gravitasi
    private boolean gameStarted = false;

    // constructor
    private JLabel scoreLabel;
    private int score = 0;

    public FlappyBird() {
        setPreferredSize(new Dimension(frameWidth, frameHeight));
        setFocusable(true);
        addKeyListener(this);

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
        scoreLabel.setForeground(Color.WHITE);
        add(scoreLabel);

        // load images
        backgroundImage = new ImageIcon(getClass().getResource("assets/background.png")).getImage();
        birdImage = new ImageIcon(getClass().getResource("assets/bird.png")).getImage();
        lowerPipeImage = new ImageIcon(getClass().getResource("assets/lowerPipe.png")).getImage();
        upperPipeImage = new ImageIcon(getClass().getResource("assets/upperPipe.png")).getImage();

        // create player object
        player = new Player(playerStartPosX, playerStartPosY, playerWidth, playerHeight, birdImage);
        pipes = new ArrayList<Pipe>();



        pipesCooldown = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Pipes cooldown");
                placePipes();
            }
        });
        pipesCooldown.start();

        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();
    }

    public void draw (Graphics g) {
        // draw background
        g.drawImage(backgroundImage, 0, 0, frameWidth, frameHeight, null);

        // draw player
        g.drawImage(player.getImage(), player.getPosX(), player.getPosY(), player.getWidth(), player.getHeight(), null);

        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.getImage(), pipe.getPosX(), pipe.getPosY(), pipe.getWidth(), pipe.getHeight(), null);
        }
    }

    public void placePipes() {
        int randomPosY = (int) (pipeStartPosY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = frameHeight / 3; // Tingkatkan jarak antar pipa

        Pipe upperPipe = new Pipe(pipeStartPosX, randomPosY, pipeWidth, pipeHeight, upperPipeImage);
        pipes.add(upperPipe);

        Pipe lowerPipe = new Pipe(pipeStartPosX, (randomPosY + openingSpace + pipeHeight), pipeWidth, pipeHeight, lowerPipeImage);
        pipes.add(lowerPipe);
    }

    public void move() {
        if (!gameStarted) {
            return; // Jangan gerakkan apapun jika permainan belum dimulai
        }

        gravity += gravityAcceleration; // Tingkatkan gravitasi secara bertahap
        player.setVelocityY(player.getVelocityY() + gravity);
        player.setPosY(player.getPosY() + player.getVelocityY());
        player.setPosY(Math.max(player.getPosY(), 0));

        // Check if player hits the ground
        if (player.getPosY() + player.getHeight() >= frameHeight) {
            gameOver();
        }

        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.setPosX(pipe.getPosX() + pipe.getVelocityX());

            // Check collision with pipes
            if (player.getPosX() < pipe.getPosX() + pipe.getWidth() &&
                    player.getPosX() + player.getWidth() > pipe.getPosX() &&
                    player.getPosY() < pipe.getPosY() + pipe.getHeight() &&
                    player.getPosY() + player.getHeight() > pipe.getPosY()) {
                gameOver();
            }

            // Add score when player passes the lower pipe only
            if (!pipe.isPassed() && pipe.getPosX() + pipe.getWidth() < player.getPosX() && pipe.getPosY() > 0) {
                pipe.setPassed(true); // Mark pipe as passed
                score++; // Increment score
                scoreLabel.setText("Score: " + score); // Update score label
            }
        }
    }

    private void gameOver() {
        gameLoop.stop();
        pipesCooldown.stop();
        JOptionPane.showMessageDialog(this, "Game Over! Press 'R' to restart.");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (!gameStarted) {
                gameStarted = true; // Mulai permainan
            }
            player.setVelocityY(-20); // Lompatan burung
        } else if (e.getKeyCode() == KeyEvent.VK_R) {
            restartGame();
        }
    }

    private void restartGame() {
        gameStarted = false; // Reset status permainan
        player.setPosX(playerStartPosX);
        player.setPosY(playerStartPosY);
        player.setVelocityY(0);
        pipes.clear();
        score = 0;
        scoreLabel.setText("Score: 0");
        gameLoop.start();
        pipesCooldown.start();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            player.setVelocityY(0);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }
}
