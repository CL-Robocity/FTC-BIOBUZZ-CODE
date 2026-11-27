package org.firstinspires.ftc.teamcode.Odometry.motion;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathBuilder;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Odometry.ActionHandler;
import org.firstinspires.ftc.teamcode.Odometry.AutonomaConfig;

public class MotionUtils {

    private static final double FOLLOW_TIMEOUT_S = 10.0;
    private static final double ALIGN_TIMEOUT_S  = 3.0;
    private static final double ALIGN_TOL_DEG    = 2.0;

    // orologio del match + parcheggio d'emergenza
    private static ElapsedTime matchTimer = null;
    private static double      parkAtS    = Double.POSITIVE_INFINITY;

    // ══════════════════════════════════════════════════════════════════
    //  ENTRY POINT UNICO — chiamato da Autonoma per ogni segmento
    // ══════════════════════════════════════════════════════════════════

    public static void executeSegment(LinearOpMode op,
                                      Follower follower,
                                      AutonomaConfig.Segment seg,
                                      ActionHandler actions,
                                      double prevX, double prevY) {
        switch (seg.type) {
            case "line":
            case "curve": {
                // la differenza tra line e curve e' gestita in buildPath() e followBlocking()
                MotionConfig cfg = new MotionConfig(seg.speed);
                PathChain path = buildPath(follower, prevX, prevY, seg);
                followBlocking(op, follower, path, cfg, seg, actions);
                break;
            }

            default:
                throw new RuntimeException("Tipo segmento sconosciuto: " + seg.type);
        }

        if (!op.opModeIsActive() || parkRequested()) return;
        finishSegment(op, follower, seg, actions);
    }

    /**
     * Fine segmento, nell'ordine dell'HUD: ALIGN -> azioni puntuali del nodo
     * (intake / flywheel / sparo con ritardo e durata, robot fermo) -> action (wait).
     */
    private static void finishSegment(LinearOpMode op,
                                      Follower follower,
                                      AutonomaConfig.Segment seg,
                                      ActionHandler actions) {
        // 1. align: ruota sul posto
        if (seg.align != null) {
            alignHeading(op, follower, seg.align, ALIGN_TOL_DEG);
            if (!op.opModeIsActive() || parkRequested()) return;
        }

        // 2. azioni puntuali con after_segment == id
        actions.runPointActions(seg.id);
        if (!op.opModeIsActive() || parkRequested()) return;

        // 3. action: "move" non fa nulla, {"wait": s} aspetta fermo
        AutonomaConfig.ParsedAction act = AutonomaConfig.parseAction(seg.action);
        if (act.type == AutonomaConfig.ActionType.WAIT) {
            actions.waitSeconds(act.duration);
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
                // Come l'HUD: cubica con 2 punti di controllo. Con un solo control point
                // viene usato due volte; con 2 o piu' si usano il primo e l'ultimo.
                AutonomaConfig.Point cA = seg.controls.get(0);
                AutonomaConfig.Point cB = seg.controls.get(seg.controls.size() - 1);
                Pose c1 = new Pose(cA.x, cA.y);
                Pose c2 = new Pose(cB.x, cB.y);
                PathBuilder builder = follower.pathBuilder()
                        .addPath(new BezierCurve(from, c1, c2, to));
                if (sameHeading)
                    builder.setConstantHeadingInterpolation(headingStartRad);
                else
                    builder.setLinearHeadingInterpolation(headingStartRad, headingEndRad);
                return builder.build();
            }

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
                                      MotionConfig cfg,
                                      AutonomaConfig.Segment seg,
                                      ActionHandler actions) {
        final boolean isCurve = "curve".equals(seg.type);

        follower.followPath(path, true);

        ElapsedTime timer = new ElapsedTime();

        while (op.opModeIsActive()
                && follower.isBusy()
                && !follower.atParametricEnd()
                && !parkRequested()
                && timer.seconds() < FOLLOW_TIMEOUT_S) {

            double t = follower.getCurrentTValue();

            // Posizione lungo il segmento come FRAZIONE DI LUNGHEZZA D'ARCO (e' quella dell'HUD).
            // Sulle linee coincide con t; sulle curve t NON e' lineare: usa getPathCompletion()
            // (verifica che il metodo esista nella tua versione di Pedro).
            double progress = isCurve ? follower.getPathCompletion() : t;

            double power;
            if (isCurve) {
                // curve: niente brake_point / decel, la frenata la gestisce Pedro.
                // (il JSON delle curve ha solo v_start / v_max / accel: v_min, decel e
                // brake_point arrivano a 0 e MotionProfile.compute() frenerebbe dall'inizio)
                power = Math.min(1.0, cfg.vMax / MotionConfig.V_PHYSICAL_MAX);
            } else {
                power = MotionProfile.compute(t, cfg);
            }

            follower.setMaxPower(power);
            actions.updateAtPosition(seg.id + progress);   // tratti intake / flywheel / sparo / barra
            follower.update();

            op.telemetry.addData("t", "%.3f", t);
            op.telemetry.addData("progress", "%.3f", progress);
            op.telemetry.addData("power", "%.2f", power);
            op.telemetry.addData("pose", follower.getPose());
            op.telemetry.update();
        }

        follower.setMaxPower(1.0);

        if (!op.opModeIsActive()) {
            follower.breakFollowing();
            return;
        }
        if (!parkRequested()) {
            actions.updateAtPosition(seg.id + 1.0);   // stato dei meccanismi sul nodo di arrivo
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  PARCHEGGIO D'EMERGENZA
    // ══════════════════════════════════════════════════════════════════

    /** Da chiamare subito dopo waitForStart(): avvia l'orologio del match. */
    public static void startMatchClock(double matchDurationS, AutonomaConfig.Park park) {
        matchTimer = new ElapsedTime();
        parkAtS = (park == null)
                ? Double.POSITIVE_INFINITY
                : matchDurationS - park.trigger_time;
    }

    /** true quando mancano meno di trigger_time secondi (e il parcheggio e' attivo). */
    public static boolean parkRequested() {
        return matchTimer != null && matchTimer.seconds() >= parkAtS;
    }

    /**
     * Lascia tutto e va in linea retta al punto di parcheggio, a potenza costante park.v.
     * Nota: il JSON non contiene le zone vietate, quindi qui non c'e' l'aggiramento
     * che l'HUD mostra nella simulazione.
     */
    public static void goPark(LinearOpMode op,
                              Follower follower,
                              ActionHandler actions,
                              AutonomaConfig.Park park) {
        actions.stopAll();
        follower.breakFollowing();

        Pose cur = follower.getPose();
        PathChain path = follower.pathBuilder()
                .addPath(new BezierLine(
                        new Pose(cur.getX(), cur.getY()),
                        new Pose(park.x, park.y)))
                .setLinearHeadingInterpolation(cur.getHeading(), Math.toRadians(park.heading))
                .build();

        double power = Math.max(0.0, Math.min(1.0, park.v / MotionConfig.V_PHYSICAL_MAX));
        follower.setMaxPower(power);
        follower.followPath(path, true);

        ElapsedTime timer = new ElapsedTime();
        while (op.opModeIsActive()
                && follower.isBusy()
                && !follower.atParametricEnd()
                && timer.seconds() < FOLLOW_TIMEOUT_S) {
            follower.update();
        }
        follower.setMaxPower(1.0);
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