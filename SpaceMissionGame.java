import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.stream.*;

public class SpaceMissionGame extends JPanel implements ActionListener, KeyListener {

    // Record for storing resource snapshots
    record ResourceSnapshot(double fuel, double oxygen, double energy, double equipment) {}

    private final Timer timer = new Timer(16, this);
    private final Random random = new Random();

    private static final int READY = 0;
    private static final int BOARDING = 1;
    private static final int COUNTDOWN = 2;
    private static final int LAUNCHING = 3;
    private static final int SPACE = 4;
    private static final int WIN = 5;
    private static final int LOSE = 6;

    private int stage = READY;
    private int stageTimer = 0;
    private int score = 0;

    private double rocketX = 770;
    private double rocketY = 545;

    private double astronautX = 350;
    private double astronautY = 610;

    private double fuel = 100;
    private double oxygen = 100;
    private double energy = 100;
    private double equipment = 100;

    private boolean up, down, left, right;
    private boolean equipmentBroken = false;

    private String message = "Press ENTER to begin the Mars mission";

    private final int[] starX = new int[90];
    private final int[] starY = new int[90];

    private final int[] asteroidX = new int[7];
    private final int[] asteroidY = new int[7];
    private final int[] asteroidSize = new int[7];

    private double cloud1 = -100;
    private double cloud2 = 340;
    private double cloud3 = 760;

    // NEW: Resource history + second counter
    private final List<ResourceSnapshot> resourceHistory = new ArrayList<>();
    private int secondCounter = 0;

    // NEW: Custom collector for averages
    private final Collector<ResourceSnapshot, double[], double[]> resourceAverages =
            Collector.of(
                    () -> new double[4],
                    (acc, r) -> {
                        acc[0] += r.fuel();
                        acc[1] += r.oxygen();
                        acc[2] += r.energy();
                        acc[3] += r.equipment();
                    },
                    (a, b) -> {
                        for (int i = 0; i < 4; i++) a[i] += b[i];
                        return a;
                    },
                    acc -> new double[]{
                            acc[0] / resourceHistory.size(),
                            acc[1] / resourceHistory.size(),
                            acc[2] / resourceHistory.size(),
                            acc[3] / resourceHistory.size()
                    }
            );

    public SpaceMissionGame() {
        setPreferredSize(new Dimension(1100, 700));
        setFocusable(true);
        addKeyListener(this);

        for (int i = 0; i < starX.length; i++) {
            starX[i] = random.nextInt(1100);
            starY[i] = random.nextInt(700);
        }

        resetAsteroids();
        timer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Mars Mission Simulation");
            SpaceMissionGame game = new SpaceMissionGame();
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.add(game);
            window.pack();
            window.setLocationRelativeTo(null);
            window.setResizable(false);
            window.setVisible(true);
            game.requestFocusInWindow();
        });
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        updateGame();
        repaint();
    }

    private void updateGame() {

        cloud1 += 0.35;
        cloud2 += 0.22;
        cloud3 += 0.28;

        if (cloud1 > 1200) cloud1 = -170;
        if (cloud2 > 1200) cloud2 = -170;
        if (cloud3 > 1200) cloud3 = -170;

        if (stage == BOARDING) {
            astronautX += 2.2;
            astronautY += (560 - astronautY) * 0.02;
            message = "Astronaut is boarding the rocket";

            if (astronautX >= 675) {
                stage = COUNTDOWN;
                stageTimer = 180;
            }
        }

        else if (stage == COUNTDOWN) {
            stageTimer--;
            message = "Launch in " + Math.max(1, (int)Math.ceil(stageTimer / 60.0));

            if (stageTimer <= 0) {
                stage = LAUNCHING;
                message = "LIFTOFF!";
            }
        }

        else if (stage == LAUNCHING) {
            rocketY -= 10.0;
            fuel -= 0.15;

            if (rocketY < -280) {
                stage = SPACE;
                rocketX = 120;
                rocketY = 350;
                message = "Fly to Mars and avoid the asteroids";
            }
        }

        else if (stage == SPACE) {
            updateSpaceMission();
        }

        if ((fuel <= 0 || oxygen <= 0 || energy <= 0 || equipment <= 0)
                && stage != WIN && stage != READY) {
            stage = LOSE;
            message = "MISSION FAILED - Press N to restart";
        }

        // NEW: Record resources every second
        secondCounter++;
        if (secondCounter >= 60) {
            resourceHistory.add(new ResourceSnapshot(fuel, oxygen, energy, equipment));
            secondCounter = 0;
        }
    }

    private void updateSpaceMission() {
        double speed = equipmentBroken ? 1.8 : 4.0;

        if (up && rocketY > 95) rocketY -= speed;
        if (down && rocketY < 640) rocketY += speed;
        if (left && rocketX > 45) rocketX -= speed;
        if (right && rocketX < 1030) rocketX += speed;

        if (up || down || left || right) fuel -= 0.025;

        oxygen -= 0.006;
        energy -= 0.004;

        if (!equipmentBroken && random.nextInt(2400) == 0) {
            equipmentBroken = true;
            equipment -= 25;
            message = "Equipment failure! Press R to repair";
        }

        for (int i = 0; i < asteroidX.length; i++) {
            asteroidX[i] -= 2 + i % 3;

            if (asteroidX[i] < -60) {
                asteroidX[i] = 1120 + random.nextInt(400);
                asteroidY[i] = 110 + random.nextInt(500);
            }

            double distance = Math.hypot(rocketX - asteroidX[i], rocketY - asteroidY[i]);

            if (distance < asteroidSize[i] / 2.0 + 25) {
                equipment -= 0.8;
                energy -= 0.4;
                asteroidX[i] = 1150 + random.nextInt(300);
                message = "Asteroid collision! Equipment damaged";
            }
        }

        if (rocketX > 940) {
            stage = WIN;

            // NEW: Calculate averages using collector
            double[] avg = resourceHistory.stream().collect(resourceAverages);

            score = (int)(fuel + oxygen + energy + equipment);

            message = "MARS REACHED! Score: " + score +
                    " | Avg Fuel: " + (int)avg[0] +
                    "% Avg Oxygen: " + (int)avg[1] +
                    "% Avg Energy: " + (int)avg[2] +
                    "% Avg Equipment: " + (int)avg[3];
        }
    }

    private void resetAsteroids() {
        for (int i = 0; i < asteroidX.length; i++) {
            asteroidX[i] = 420 + i * 145 + random.nextInt(100);
            asteroidY[i] = 120 + random.nextInt(500);
            asteroidSize[i] = 30 + random.nextInt(35);
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (stage <= LAUNCHING) drawEarth(g);
        else drawSpace(g);

        drawDashboard(g);
    }

    // (Drawing methods unchanged — omitted for brevity)
    // You keep all your drawEarth, drawSpace, drawRocket, drawDashboard etc.

    private void restartGame() {
        stage = READY;
        stageTimer = 0;
        score = 0;
        rocketX = 770;
        rocketY = 545;
        astronautX = 350;
        astronautY = 610;
        fuel = 100;
        oxygen = 100;
        energy = 100;
        equipment = 100;
        equipmentBroken = false;
        message = "Press ENTER to begin the Mars mission";
        resourceHistory.clear();
        secondCounter = 0;
        resetAsteroids();
    }

    @Override
    public void keyPressed(KeyEvent event) {
        int key = event.getKeyCode();
        if (key == KeyEvent.VK_ENTER && stage == READY) stage = BOARDING;
        if (key == KeyEvent.VK_UP) up = true;
        if (key == KeyEvent.VK_DOWN) down = true;
        if (key == KeyEvent.VK_LEFT) left = true;
        if (key == KeyEvent.VK_RIGHT) right = true;

        if (key == KeyEvent.VK_R && stage == SPACE && equipmentBroken) {
            if (energy >= 15) {
                energy -= 15;
                equipment = Math.min(100, equipment + 35);
                equipmentBroken = false;
                message = "Engineer repaired the equipment";
            } else {
                message = "Not enough energy to repair equipment";
            }
        }

        if (key == KeyEvent.VK_N) restartGame();
    }

    @Override
    public void keyReleased(KeyEvent event) {
        int key = event.getKeyCode();
        if (key == KeyEvent.VK_UP) up = false;
        if (key == KeyEvent.VK_DOWN) down = false;
        if (key == KeyEvent.VK_LEFT) left = false;
        if (key == KeyEvent.VK_RIGHT) right = false;
    }

    @Override
    public void keyTyped(KeyEvent event) {}
}
