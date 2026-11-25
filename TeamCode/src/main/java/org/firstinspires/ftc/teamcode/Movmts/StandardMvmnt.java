package org.firstinspires.ftc.teamcode.Movmts;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.RobotHardware.RobotHardware;

public class StandardMvmnt {
    private RobotHardware Robot;
    private Gamepad gamepad;
    private PositionHold hold;

    double SPEED = 0.5;
    double speed = SPEED;

    private static final double DEADBAND_LEFT  = 0.4;
    private static final double DEADBAND_RIGHT = 0.2;

    public StandardMvmnt(RobotHardware robot, Gamepad GamePad) {
        this.Robot   = robot;
        this.gamepad = GamePad;
        this.hold    = new PositionHold(robot);
    }

    public void move() {
        boolean left_bumper = gamepad.left_bumper;
        double left_trigger = gamepad.left_trigger;

        // gestione velocità movimento
        speedControl(left_bumper, left_trigger);

        // il driver non sta muovendo: tiene la posizione (o la riporta)
        if (!isMoving()) {
            hold.updateIdle();
            return;
        }

        // il driver sta guidando: tutto come prima
        hold.onDriverControl();

        double lX = deadband(gamepad.left_stick_x,  DEADBAND_LEFT);
        double lY = deadband(gamepad.left_stick_y,  DEADBAND_LEFT);
        double rX = deadband(gamepad.right_stick_x, DEADBAND_RIGHT);

        double[] MotArr = MotorOut(lX, lY, rX);
        Robot.lfD.setPower(MotArr[0] * speed);
        Robot.lbD.setPower(MotArr[1] * speed);
        Robot.rfD.setPower(MotArr[2] * speed);
        Robot.rbD.setPower(MotArr[3] * speed);
    }

    //--------------------------------------------
    //       IL DRIVER STA MUOVENDO?
    //--------------------------------------------
    /** true se almeno uno stick supera la sua deadband. */
    public boolean isMoving() {
        return deadband(gamepad.left_stick_x,  DEADBAND_LEFT)  != 0
                || deadband(gamepad.left_stick_y,  DEADBAND_LEFT)  != 0
                || deadband(gamepad.right_stick_x, DEADBAND_RIGHT) != 0;
    }

    private static double deadband(double v, double threshold) {
        return Math.abs(v) < threshold ? 0 : v;
    }

    // ── accesso al blocco posizione (telemetria / toggle) ─────────────
    public void   setHoldEnabled(boolean enabled) { hold.setEnabled(enabled); }
    public boolean isHolding()                    { return hold.isHolding(); }
    public String  getHoldState()                 { return hold.getState(); }
    public double  getHoldErrorCm()               { return hold.getErrorCm(); }

    //--------------------------------------------
    //       CONTROLLO VELOCITA DI MOVIMENTO
    //--------------------------------------------
    private void speedControl(boolean left_bumper, double left_trigger) {
        if (left_bumper) {
            speed = 1; // l1  --> velocità massima --> potenza 1
        } else if (left_trigger >= 0.1) {
            speed = (SPEED - 0.1) * (1 - left_trigger) + 0.1; // l2 --> frenata
        } else {
            speed = SPEED; // velocità standard di sistema
        }
    }

    //--------------------------------------------
    //       CONTROLLO MOTORI MOVIMENTO
    //--------------------------------------------
    private double[] MotorOut(double lX, double lY, double rX) {
        double rot = rX;
        double y = lY, x = lX;
        double lf = y + x + rot;
        double lb = y - x + rot;
        double rf = y - x - rot;
        double rb = y + x - rot;
        double max = Math.max(1, Math.max(Math.abs(lf), Math.max(Math.abs(lb), Math.max(Math.abs(rf), Math.abs(rb)))));
        return new double[]{lf / max, lb / max, rf / max, rb / max};
    }
}