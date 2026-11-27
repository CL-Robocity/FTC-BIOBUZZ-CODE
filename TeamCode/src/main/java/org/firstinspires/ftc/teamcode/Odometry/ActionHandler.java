package org.firstinspires.ftc.teamcode.Odometry;

import com.pedropathing.follower.Follower;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Odometry.motion.MotionUtils;
import org.firstinspires.ftc.teamcode.RobotHardware.RobotHardware;

import java.util.ArrayList;
import java.util.List;

/**
 * Esegue i meccanismi del JSON ("mechanisms"):
 *  - TRATTI (intake, flywheel, intake_down, bar_down, shoot): accesi/spenti in base alla
 *    posizione sul percorso, con updateAtPosition() chiamata ad ogni giro di followBlocking.
 *  - AZIONI PUNTUALI ("point"): a fine segmento il robot sta fermo mentre intake / flywheel /
 *    shoot partono dopo `delay` e durano `time` (runPointActions).
 *
 * I comandi all'hardware partono solo quando lo stato CAMBIA (apply), cosi' non si
 * riscrivono i motori ad ogni giro del loop.
 */
public class ActionHandler {

    private final LinearOpMode  op;
    private final RobotHardware robot;
    private final Follower      follower;

    private final List<AutonomaConfig.Mechanism> spans  = new ArrayList<>();
    private final List<AutonomaConfig.Mechanism> points = new ArrayList<>();

    // stato attuale dei meccanismi
    private boolean intakeOn, flywheelOn, intakeDown, barDown, shootOn;

    public ActionHandler(LinearOpMode op, RobotHardware robot, Follower follower) {
        this.op       = op;
        this.robot    = robot;
        this.follower = follower;
    }

    /** Divide "mechanisms" in tratti e azioni puntuali. Chiamala dopo aver caricato il JSON. */
    public void loadMechanisms(List<AutonomaConfig.Mechanism> all) {
        spans.clear();
        points.clear();
        if (all == null) return;
        for (AutonomaConfig.Mechanism m : all) {
            if ("point".equals(m.type)) points.add(m);
            else                        spans.add(m);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  TRATTI: in base alla posizione sul percorso
    // ══════════════════════════════════════════════════════════════════

    /**
     * g = indice segmento + frazione di lunghezza d'arco (es. 1.35 = 35% del segmento 1).
     * Chiamala ad ogni giro del loop di followBlocking.
     */
    public void updateAtPosition(double g) {
        boolean[] s = spanState(g);
        apply(s[0], s[1], s[2], s[3], s[4]);
    }

    /** {intake, flywheel, intake_down, bar_down, shoot} attivi alla posizione g. */
    private boolean[] spanState(double g) {
        boolean[] s = new boolean[5];
        for (AutonomaConfig.Mechanism m : spans) {
            if (m.from == null || m.to == null) continue;
            if (g < m.from.global() || g > m.to.global()) continue;
            switch (m.type) {
                case "intake":      s[0] = true; break;
                case "flywheel":    s[1] = true; break;
                case "intake_down": s[2] = true; break;
                case "bar_down":    s[3] = true; break;
                case "shoot":       s[4] = true; break;
                default: break;
            }
        }
        return s;
    }

    // ══════════════════════════════════════════════════════════════════
    //  AZIONI PUNTUALI: a fine segmento, robot fermo
    // ══════════════════════════════════════════════════════════════════

    /**
     * Esegue le azioni "point" con after_segment == segIndex. Il robot resta fermo fino alla
     * fine dell'ultima (delay + time piu' alto). I tratti che coprono il nodo restano attivi.
     */
    public void runPointActions(int segIndex) {
        List<AutonomaConfig.Mechanism> mine = new ArrayList<>();
        double total = 0;
        for (AutonomaConfig.Mechanism m : points) {
            if (m.after_segment != segIndex) continue;
            mine.add(m);
            total = Math.max(total, m.delay + m.time);
        }
        if (mine.isEmpty()) return;

        boolean[] hold = spanState(segIndex + 1.0);   // tratti che coprono il nodo di arrivo
        ElapsedTime timer = new ElapsedTime();

        while (op.opModeIsActive() && !MotionUtils.parkRequested() && timer.seconds() < total) {
            double s = timer.seconds();
            boolean in = hold[0], fw = hold[1], sh = hold[4];
            for (AutonomaConfig.Mechanism m : mine) {
                if (s < m.delay || s >= m.delay + m.time) continue;
                switch (m.mechanism) {
                    case "intake":   in = true; break;
                    case "flywheel": fw = true; break;
                    case "shoot":    sh = true; break;
                    default: break;
                }
            }
            apply(in, fw, hold[2], hold[3], sh);
            follower.update();
            op.idle();
        }
        apply(hold[0], hold[1], hold[2], hold[3], hold[4]);
    }

    // ══════════════════════════════════════════════════════════════════
    //  Utility
    // ══════════════════════════════════════════════════════════════════

    /** Spegne tutto (parcheggio d'emergenza / fine autonoma). */
    public void stopAll() {
        apply(false, false, false, false, false);
    }

    /** Aspetta fermo (tenendo aggiornato il follower). Esce subito se scatta il parcheggio. */
    public void waitSeconds(double seconds) {
        ElapsedTime timer = new ElapsedTime();
        while (op.opModeIsActive() && !MotionUtils.parkRequested() && timer.seconds() < seconds) {
            follower.update();
            op.idle();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  Stato → hardware (solo quando cambia)
    // ══════════════════════════════════════════════════════════════════

    private void apply(boolean in, boolean fw, boolean low, boolean bar, boolean sh) {
        if (sh) fw = true;   // lo sparo implica flywheel (come nell'HUD)

        if (in  != intakeOn)   { intakeOn   = in;  setIntake(in); }
        if (fw  != flywheelOn) { flywheelOn = fw;  setFlywheel(fw); }
        if (low != intakeDown) { intakeDown = low; setIntakeDown(low); }
        if (bar != barDown)    { barDown    = bar; setBarDown(bar); }
        if (sh  != shootOn)    { shootOn    = sh;  setShoot(sh); }
    }

    private void setIntake(boolean on) {
        // TODO: robot.FrontIntake.setPower(on ? 1.0 : 0);
        // TODO: robot.IntakeRoller.setPower(on ? 1.0 : 0);
    }

    private void setFlywheel(boolean on) {
        // TODO: robot.TopFlyWheel.setPower(on ? 1.0 : 0);
        // TODO: robot.DownFlyWheel.setPower(on ? 1.0 : 0);
    }

    private void setIntakeDown(boolean down) {
        // TODO: servo intake giu' / su
    }

    private void setBarDown(boolean down) {
        // TODO: servo barra giu' (davanti all'intake) / su
    }

    private void setShoot(boolean on) {
        // TODO: alimentazione delle palline verso il flywheel (il flywheel e' gia' acceso da apply)
    }
}
