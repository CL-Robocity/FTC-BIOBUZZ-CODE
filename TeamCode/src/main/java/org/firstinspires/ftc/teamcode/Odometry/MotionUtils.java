package org.firstinspires.ftc.teamcode.Odometry;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathBuilder;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.util.ArrayList;
import java.util.List;

public class MotionUtils {

    private static final double FOLLOW_TIMEOUT_S = 10.0;
    private static final double ALIGN_TIMEOUT_S  = 3.0;
    private static final int    MAX_BALLS        = 4;
    private static final double SWEEP_WIDTH_IN   = 35.0 / 2.54; // 35 cm in pollici
    private static final double SEARCH_STEP_S    = 0.5;         // ogni step riscansiona
    private static final double ARRIVE_TOL_IN    = 0.5;

    // ══════════════════════════════════════════════════════════════════
    //  ENTRY POINT UNICO — chiamato da Autonoma per ogni segmento
    // ══════════════════════════════════════════════════════════════════

    public static void executeSegment(LinearOpMode op,
                                      Follower follower,
                                      AutonomaConfig.Segment seg,
                                      ActionHandler actions,
                                      int[] collectedBalls,
                                      double prevX, double prevY) {
        switch (seg.type) {
            case "line":
            case "curve": {
                // la differenza tra line e curve e' gestita in buildPath()
                MotionConfig cfg = new MotionConfig(seg.speed);
                AutonomaConfig.ParsedAction act = AutonomaConfig.parseAction(seg.action);

                PathChain path = buildPath(follower, prevX, prevY, seg);

                actions.onSegmentStart(act);
                followBlocking(op, follower, path, cfg);
                if (!op.opModeIsActive()) return;
                actions.onSegmentEnd(act);
                break;
            }

            default:
                throw new RuntimeException("Tipo segmento sconosciuto: " + seg.type);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  BUILD PATH
    // ══════════════════════════════════════════════════════════════════

    public static PathChain buildPath(Follower follower,
                                      double fromX, double fromY,
                                      AutonomaConfig.Segment seg) {
        Pose from = new Pose(fromX, fromY);
        Pose to   = new Pose(seg.to.x, seg.to.y);

        double headingStartRad = Math.toRadians(seg.robot_heading.start);
        double headingEndRad   = Math.toRadians(seg.robot_heading.end);
        boolean sameHeading = Math.abs(headingEndRad - headingStartRad) < Math.toRadians(1.0);

        switch (seg.type) {
            case "line": {
                PathBuilder builder = follower.pathBuilder()
                        .addPath(new BezierLine(from, to));
                if (sameHeading)
                    builder.setConstantHeadingInterpolation(headingStartRad);
                else
                    builder.setLinearHeadingInterpolation(headingStartRad, headingEndRad);
                return builder.build();
            }

            case "curve": {
                if (seg.controls == null || seg.controls.isEmpty())
                    throw new RuntimeException("Segmento " + seg.id + ": curve senza control points");
                Pose ctrl = new Pose(seg.controls.get(0).x, seg.controls.get(0).y);
                PathBuilder builder = follower.pathBuilder()
                        .addPath(new BezierCurve(from, ctrl, to));
                if (sameHeading)
                    builder.setConstantHeadingInterpolation(headingStartRad);
                else
                    builder.setLinearHeadingInterpolation(headingStartRad, headingEndRad);
                return builder.build();
            }

            case "search":
                // il search non ha un path fisso: lo gestisce searchBalls()
                throw new RuntimeException("Segmento " + seg.id
                        + ": i segmenti search vanno eseguiti con executeSegment()/searchBalls()");

            default:
                throw new RuntimeException("Tipo segmento sconosciuto: " + seg.type);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  FOLLOW BLOCKING (line / curve)
    // ══════════════════════════════════════════════════════════════════

    public static void followBlocking(LinearOpMode op,
                                      Follower follower,
                                      PathChain path,
                                      MotionConfig cfg) {
        follower.followPath(path, true);

        ElapsedTime timer = new ElapsedTime();

        while (op.opModeIsActive()
                && follower.isBusy()
                && !follower.atParametricEnd()
                && timer.seconds() < FOLLOW_TIMEOUT_S) {
            double t     = follower.getCurrentTValue();
            double power = MotionProfile.compute(t, cfg);

            follower.setMaxPower(power);
            follower.update();

            op.telemetry.addData("t", "%.3f", t);
            op.telemetry.addData("power", "%.2f", power);
            op.telemetry.addData("pose", follower.getPose());
            op.telemetry.update();
        }

        follower.setMaxPower(1.0);

        if (!op.opModeIsActive()) {
            follower.breakFollowing();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  SEARCH BALLS
    // ══════════════════════════════════════════════════════════════════

    /**
     * Segmento "search".
     *
     * Area di ricerca: rettangolo con lunghezza = distanza tra (startX,startY)
     * e seg.to, larghezza = area_width. Le palline fuori dal rettangolo
     * vengono ignorate.
     *
     * Il robot avanza verso seg.to a velocita' costante search.v, intake
     * sempre attivo. Se vede palline nel rettangolo si sposta a prenderle
     * e poi riprende.
     *
     * Esce dalla ricerca quando ha 4 palline OPPURE scade time_limit
     * (timeout di sicurezza). In entrambi i casi va al punto finale seg.to.
     * Una volta arrivato ferma l'intake e, se shoot_after, spara.
     */

    // ══════════════════════════════════════════════════════════════════
    //  UTILITIES INTERNE
    // ══════════════════════════════════════════════════════════════════

    /**
     * Va da dove si trova il robot verso (toX, toY) a potenza costante.
     * Esce quando: arriva, scade clock>=maxSeconds, scade lo step
     * (se stepSeconds > 0) o viene premuto STOP.
     *
     * @return true se il robot e' arrivato al punto
     */
    private static boolean driveTo(LinearOpMode op,
                                   Follower follower,
                                   double toX, double toY,
                                   double power,
                                   ElapsedTime clock,
                                   double maxSeconds,
                                   double stepSeconds) {
        Pose p = follower.getPose();
        if (dist(p.getX(), p.getY(), toX, toY) < ARRIVE_TOL_IN) return true;

        PathChain path = buildBallPath(follower, p.getX(), p.getY(), toX, toY);
        follower.followPath(path, true);

        ElapsedTime step = new ElapsedTime();

        while (op.opModeIsActive()
                && follower.isBusy()
                && !follower.atParametricEnd()
                && clock.seconds() < maxSeconds
                && (stepSeconds <= 0 || step.seconds() < stepSeconds)) {
            follower.setMaxPower(power);
            follower.update();
        }

        return follower.atParametricEnd();
    }

    /**
     * Palline (coordinate campo) che stanno DENTRO il rettangolo di ricerca.
     * Quelle fuori vengono scartate.
     */


    /**
     * Raggruppa le palline raccoglibili con un solo sweep (<= 35 cm laterali)
     * e restituisce i target {x, y, count} ordinati per distanza dal robot.
     */
    private static List<double[]> groupSweeps(List<double[]> balls,
                                              Follower follower,
                                              int maxBalls) {
        List<double[]> targets   = new ArrayList<>();
        boolean[]      used      = new boolean[balls.size()];
        int            remaining = maxBalls;
        double         robotH    = follower.getPose().getHeading();

        for (int i = 0; i < balls.size(); i++) {
            if (remaining <= 0) break;
            if (used[i]) continue;

            List<double[]> group = new ArrayList<>();
            group.add(balls.get(i));
            used[i] = true;

            for (int j = i + 1; j < balls.size(); j++) {
                if (group.size() >= remaining) break;
                if (used[j]) continue;

                double lat = lateralDist(balls.get(i), balls.get(j), robotH);
                if (lat <= SWEEP_WIDTH_IN) {
                    group.add(balls.get(j));
                    used[j] = true;
                }
            }

            double avgX = 0, avgY = 0;
            for (double[] b : group) { avgX += b[0]; avgY += b[1]; }
            avgX /= group.size();
            avgY /= group.size();

            targets.add(new double[]{avgX, avgY, group.size()});
            remaining -= group.size();
        }

        final double rx = follower.getPose().getX();
        final double ry = follower.getPose().getY();
        targets.sort((a, b) -> Double.compare(
                dist(rx, ry, a[0], a[1]),
                dist(rx, ry, b[0], b[1])
        ));

        return targets;
    }

    private static PathChain buildBallPath(Follower follower,
                                           double fromX, double fromY,
                                           double toX,   double toY) {
        double headingRad = follower.getPose().getHeading();
        return follower.pathBuilder()
                .addPath(new BezierLine(new Pose(fromX, fromY), new Pose(toX, toY)))
                .setConstantHeadingInterpolation(headingRad)
                .build();
    }

    private static double lateralDist(double[] a, double[] b, double robotH) {
        double dx = b[0] - a[0];
        double dy = b[1] - a[1];
        return Math.abs(-dx * Math.sin(robotH) + dy * Math.cos(robotH));
    }

    private static double dist(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    // ══════════════════════════════════════════════════════════════════
    //  ALIGN
    // ══════════════════════════════════════════════════════════════════

    public static void alignHeading(LinearOpMode op,
                                    Follower follower,
                                    double targetDeg,
                                    double toleranceDeg) {
        double targetRad    = Math.toRadians(targetDeg);
        double toleranceRad = Math.toRadians(toleranceDeg);

        Pose current = follower.getPose();
        follower.holdPoint(new Pose(current.getX(), current.getY(), targetRad));

        ElapsedTime timer = new ElapsedTime();

        while (op.opModeIsActive() && timer.seconds() < ALIGN_TIMEOUT_S) {
            follower.update();
            double error = Math.abs(normalizeAngle(targetRad - follower.getPose().getHeading()));
            if (error < toleranceRad) break;
        }

        follower.breakFollowing();
    }

    private static double normalizeAngle(double angle) {
        while (angle >  Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }
}