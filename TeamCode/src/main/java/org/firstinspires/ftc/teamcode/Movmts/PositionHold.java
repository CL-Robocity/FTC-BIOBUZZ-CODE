package org.firstinspires.ftc.teamcode.Movmts;

import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.RobotHardware.RobotHardware;

/**
 * Mantiene la posizione del robot e lo riporta indietro se viene spostato.
 * Indipendente da Pedro: legge robot.pinpoint e scrive direttamente sui 4 motori.
 *
 * Teleop: chiama updateTeleop() ogni ciclo al posto del tuo mix mecanum.
 * Auto:   chiama release() prima di dare i motori a Pedro e updateIdle()
 *         quando il robot e' fermo e nessun path e' attivo.
 * Non chiamare mai questo modulo e follower.update() nello stesso ciclo.
 */
public class PositionHold {

    private enum Mode { DRIVING, SETTLING, HOLDING }
    public static double KP_POS            = 0.06;  // potenza per cm di errore
    public static double KD_POS            = 0.005; // potenza per cm/s (smorza)
    public static double KP_HEAD           = 1.2;   // potenza per rad di errore
    public static double KD_HEAD           = 0.10;  // potenza per rad/s
    public static double K_STATIC          = 0.08;  // spinta minima per vincere l'attrito
    public static double POS_DEADBAND_CM   = 0.8;
    public static double HEAD_DEADBAND_RAD = Math.toRadians(1.0);
    public static double HOLD_MAX_POWER    = 0.6;   // tetto della potenza di tenuta

    public static double STICK_DEADBAND    = 0.05;

    // Attesa che il robot si fermi prima di "bloccare" la posizione
    public static double SETTLE_SPEED_CMS  = 4.0;
    public static double SETTLE_SPEED_RADS = 0.15;
    public static double SETTLE_TIME_S     = 0.12;  // quanto deve restare fermo
    public static double SETTLE_MAX_S      = 0.8;   // oltre questo blocca comunque

    // Se spinge nella direzione sbagliata, metti -1 sull'asse giusto
    public static double FORWARD_SIGN = 1.0;
    public static double STRAFE_SIGN  = 1.0;
    public static double TURN_SIGN    = 1.0;

    private static final double VEL_ALPHA = 0.4;    // filtro sulla velocita'

    private final RobotHardware robot;

    private Mode    mode    = Mode.DRIVING;
    private boolean enabled = true;

    // pose corrente (cm, rad) e velocita' stimata dalle differenze
    private double  x, y, h;
    private double  vx, vy, w;
    private long    lastNanos;
    private boolean hasLast = false;

    // posa da mantenere
    private double tx, ty, th;

    private final ElapsedTime settleTimer = new ElapsedTime();
    private final ElapsedTime calmTimer   = new ElapsedTime();

    public PositionHold(RobotHardware robot) {
        this.robot = robot;
    }

    /*
    * =============================================
    *                API
    * =============================================
    * */
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isHolding()              { return mode == Mode.HOLDING; }
    public String  getState()               { return mode.name(); }

    /** Errore di posizione attuale in cm (0 se non sta tenendo). */
    public double getErrorCm() {
        return mode == Mode.HOLDING ? Math.hypot(tx - x, ty - y) : 0.0;
    }
    /** Teleop: il driver sta guidando. Aggiorna la posa e annulla ogni blocco. */
    public void onDriverControl() {
        readPose();
        mode = Mode.DRIVING;
    }

    /**
     * Teleop. Se gli stick sono oltre la deadband guida il driver,
     * altrimenti il robot si ferma e tiene la posizione.
     *
     * @param forward     avanti positivo
     * @param strafeRight destra positivo
     * @param turnCW      rotazione oraria positiva
     */
    public void updateTeleop(double forward, double strafeRight, double turnCW) {
        readPose();

        boolean commanded = Math.abs(forward)     > STICK_DEADBAND
                || Math.abs(strafeRight) > STICK_DEADBAND
                || Math.abs(turnCW)      > STICK_DEADBAND;

        if (!enabled || commanded) {
            mode = Mode.DRIVING;
            setPowers(forward, strafeRight, turnCW);
            return;
        }
        idleStep();
    }

    /** Auto: robot fermo, nessun path attivo. Chiamare ogni ciclo. */
    public void updateIdle() {
        readPose();
        if (!enabled) { setPowers(0, 0, 0); return; }
        idleStep();
    }

    /** Auto: chiamare PRIMA di followPath(); da quel momento i motori sono di Pedro. */
    public void release() {
        mode    = Mode.DRIVING;
        hasLast = false;
        vx = vy = w = 0;
    }

    // ══════════════════════════════════════════════════════════════════
    //  LOGICA INTERNA
    // ══════════════════════════════════════════════════════════════════

    private void idleStep() {
        if (mode == Mode.DRIVING) {
            mode = Mode.SETTLING;
            settleTimer.reset();
            calmTimer.reset();
        }

        if (mode == Mode.SETTLING) {
            setPowers(0, 0, 0); // lascia frenare, i motori sono in BRAKE

            boolean moving = Math.hypot(vx, vy) > SETTLE_SPEED_CMS
                    || Math.abs(w) > SETTLE_SPEED_RADS;
            if (moving) calmTimer.reset();

            if (calmTimer.seconds() >= SETTLE_TIME_S
                    || settleTimer.seconds() >= SETTLE_MAX_S) {
                tx = x; ty = y; th = h;   // blocca QUI
                mode = Mode.HOLDING;
            } else {
                return;
            }
        }

        holdStep();
    }

    private void holdStep() {
        double ex = tx - x;
        double ey = ty - y;
        double c  = Math.cos(h);
        double s  = Math.sin(h);

        // errore e velocita' dal sistema campo al sistema robot (avanti / sinistra)
        double errF = ex * c + ey * s;
        double errL = -ex * s + ey * c;
        double vF   = vx * c + vy * s;
        double vL   = -vx * s + vy * c;
        double errH = normalize(th - h);

        double dist = Math.hypot(errF, errL);
        double f = 0, l = 0, t = 0;

        if (dist > POS_DEADBAND_CM) {
            f = KP_POS * errF - KD_POS * vF + K_STATIC * errF / dist;
            l = KP_POS * errL - KD_POS * vL + K_STATIC * errL / dist;
        }
        if (Math.abs(errH) > HEAD_DEADBAND_RAD) {
            t = KP_HEAD * errH - KD_HEAD * w + K_STATIC * Math.signum(errH);
        }

        // tetto di potenza
        double m = Math.hypot(f, l);
        if (m > HOLD_MAX_POWER) { f *= HOLD_MAX_POWER / m; l *= HOLD_MAX_POWER / m; }
        t = Math.max(-HOLD_MAX_POWER, Math.min(HOLD_MAX_POWER, t));

        // l e' "sinistra" e t e' antiorario (convenzione Pinpoint):
        // il mix vuole "destra" e "orario", quindi si invertono
        setPowers(FORWARD_SIGN * f, STRAFE_SIGN * -l, TURN_SIGN * -t);
    }

    private void readPose() {
        robot.pinpoint.update();

        double nx = robot.pinpoint.getPosX(DistanceUnit.MM) / 10.0;
        double ny = robot.pinpoint.getPosY(DistanceUnit.MM) / 10.0;
        double nh = robot.pinpoint.getHeading(AngleUnit.RADIANS);
        long   now = System.nanoTime();

        if (hasLast) {
            double dt = (now - lastNanos) / 1e9;
            if (dt > 1e-3) {
                vx = VEL_ALPHA * ((nx - x) / dt)             + (1 - VEL_ALPHA) * vx;
                vy = VEL_ALPHA * ((ny - y) / dt)             + (1 - VEL_ALPHA) * vy;
                w  = VEL_ALPHA * (normalize(nh - h) / dt)    + (1 - VEL_ALPHA) * w;
            }
        }
        x = nx; y = ny; h = nh;
        lastNanos = now;
        hasLast   = true;
    }

    /** Mix mecanum standard: con le direzioni di RobotHardware potenza positiva = avanti. */
    private void setPowers(double forward, double strafeRight, double turnCW) {
        double lf = forward + strafeRight + turnCW;
        double rf = forward - strafeRight - turnCW;
        double lb = forward - strafeRight + turnCW;
        double rb = forward + strafeRight - turnCW;

        double max = Math.max(1.0, Math.max(Math.max(Math.abs(lf), Math.abs(rf)),
                Math.max(Math.abs(lb), Math.abs(rb))));
        robot.lfD.setPower(lf / max);
        robot.rfD.setPower(rf / max);
        robot.lbD.setPower(lb / max);
        robot.rbD.setPower(rb / max);
    }

    private static double normalize(double a) {
        while (a >  Math.PI) a -= 2 * Math.PI;
        while (a < -Math.PI) a += 2 * Math.PI;
        return a;
    }
}