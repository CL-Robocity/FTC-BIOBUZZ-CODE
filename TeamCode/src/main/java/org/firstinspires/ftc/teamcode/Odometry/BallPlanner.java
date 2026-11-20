package org.firstinspires.ftc.teamcode.Odometry;

import com.pedropathing.geometry.Pose;
import com.pedropathing.follower.Follower;

import java.util.ArrayList;
import java.util.List;

public class BallPlanner {

    private static final double SWEEP_WIDTH   = 35.0; // larghezza intake in cm
    private static final int    MAX_BALLS      = 4;    // massimo palloni raccoglibili

    private final Follower      follower;
    private final BallLocalizer localizer;

    private int collectedBalls = 0;

    public BallPlanner(Follower follower, BallLocalizer localizer) {
        this.follower  = follower;
        this.localizer = localizer;
    }

    public int getCollectedBalls() { return collectedBalls; }
    public boolean isFull()        { return collectedBalls >= MAX_BALLS; }

    /**
     * Raggruppa le palline visibili in sweep:
     * due palline sono nello stesso sweep se la loro distanza laterale
     * rispetto alla direzione di avanzamento è <= SWEEP_WIDTH.
     *
     * Restituisce una lista di target — ogni target è il punto medio
     * del gruppo di palline da raccogliere in un singolo passaggio,
     * in coordinate assolute del campo.
     *
     * I target sono ordinati per distanza crescente dal robot.
     */
    public List<double[]> planSweeps() {
        List<double[]> balls   = localizer.getAbsoluteBallPositions();
        List<double[]> targets = new ArrayList<>();

        if (balls.isEmpty()) return targets;

        // quante palline possiamo ancora raccogliere
        int remaining = MAX_BALLS - collectedBalls;

        List<boolean[]> used = new ArrayList<>();
        used.add(new boolean[balls.size()]);

        boolean[] usedArr = new boolean[balls.size()];

        for (int i = 0; i < balls.size(); i++) {
            if (usedArr[i]) continue;
            if (remaining <= 0) break;

            List<double[]> group = new ArrayList<>();
            group.add(balls.get(i));
            usedArr[i] = true;

            // cerca altre palline entro SWEEP_WIDTH lateralmente
            for (int j = i + 1; j < balls.size(); j++) {
                if (usedArr[j]) continue;
                if (group.size() >= remaining) break;

                double lateralDist = lateralDistance(balls.get(i), balls.get(j));
                if (lateralDist <= SWEEP_WIDTH) {
                    group.add(balls.get(j));
                    usedArr[j] = true;
                }
            }

            // punto medio del gruppo come target
            double avgX = 0, avgY = 0;
            for (double[] b : group) {
                avgX += b[0];
                avgY += b[1];
            }
            avgX /= group.size();
            avgY /= group.size();

            targets.add(new double[]{avgX, avgY, group.size()});
            remaining -= group.size();
        }

        // ordina per distanza dal robot
        Pose pose = follower.getPose();
        targets.sort((a, b) -> {
            double da = distanceTo(pose, a);
            double db = distanceTo(pose, b);
            return Double.compare(da, db);
        });

        return targets;
    }

    /**
     * Chiamato dopo ogni sweep completato per aggiornare il contatore.
     */
    public void onSweepCompleted(int ballsCollected) {
        collectedBalls += ballsCollected;
    }

    // ── Utilities ─────────────────────────────────────────────────────

    /**
     * Distanza laterale tra due palline rispetto all'asse avanti del robot.
     * Proietta la differenza sull'asse perpendicolare all'heading del robot.
     */
    private double lateralDistance(double[] a, double[] b) {
        double robotH   = follower.getPose().getHeading();
        double dx       = b[0] - a[0];
        double dy       = b[1] - a[1];
        // componente perpendicolare all'heading
        return Math.abs(-dx * Math.sin(robotH) + dy * Math.cos(robotH));
    }

    private double distanceTo(Pose pose, double[] target) {
        double dx = target[0] - pose.getX();
        double dy = target[1] - pose.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
}