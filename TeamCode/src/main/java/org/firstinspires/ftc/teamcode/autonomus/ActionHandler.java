package org.firstinspires.ftc.teamcode.autonomus;

import com.pedropathing.follower.Follower;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.robothardware.Hardware;

public class ActionHandler {

    private final LinearOpMode  op;
    private final Hardware robot;
    private final Follower      follower;

    public ActionHandler(LinearOpMode op, Hardware robot, Follower follower) {
        this.op       = op;
        this.robot    = robot;
        this.follower = follower;
    }

    public void onSegmentStart(AutonomaConfig.ParsedAction action) {
        switch (action.type) {
            case SHOOT_MOVE:
                shootMoveStart(action.duration);
                break;
            default:
                break;
        }
    }

    public void onSegmentEnd(AutonomaConfig.ParsedAction action) {
        switch (action.type) {
            case SHOOT_STOP:
                shootStop(action.duration);
                break;
            case WAIT:
                waitSeconds(action.duration);
                break;
            case SHOOT_MOVE:
                shootMoveStop();
                break;
            default:
                break;
        }
    }

    public void startIntake() {
        // TODO: robot.FrontIntake.setPower(1.0);
        // TODO: robot.IntakeRoller.setPower(1.0);
    }

    public void stopIntake() {
        // TODO: robot.FrontIntake.setPower(0);
        // TODO: robot.IntakeRoller.setPower(0);
    }

    public void shoot(double durationSeconds) {
        // TODO: robot.TopFlyWheel.setPower(1.0);
        // TODO: robot.DownFlyWheel.setPower(1.0);
        waitSeconds(durationSeconds);
        // TODO: robot.TopFlyWheel.setPower(0);
        // TODO: robot.DownFlyWheel.setPower(0);
    }

    // ── Privati ───────────────────────────────────────────────────────

    private void shootMoveStart(double durationSeconds) {
        // TODO: es. robot.TopFlyWheel.setPower(1.0);
    }

    private void shootMoveStop() {
        // TODO: es. robot.TopFlyWheel.setPower(0);
    }

    private void shootStop(double durationSeconds) {
        // TODO: attiva flywheel, aspetta durationSeconds, disattiva
        waitSeconds(durationSeconds);
    }

    private void waitSeconds(double seconds) {
        ElapsedTime timer = new ElapsedTime();
        while (op.opModeIsActive() && timer.seconds() < seconds) {
            follower.update();
            op.idle();
        }
    }
}