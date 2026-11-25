package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.Movmts.StandardMvmnt;
import org.firstinspires.ftc.teamcode.Movmts.PositionHold;
import org.firstinspires.ftc.teamcode.RobotHardware.RobotHardware;
import org.firstinspires.ftc.teamcode.RobotHardware.RobotVisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
@TeleOp(name = "TeleOp TEST", group = "Test")
public class teleop extends LinearOpMode {

    private static final boolean DEBUG_VISION = true;  // a gara: false
    private static final int     TARGET_TAG   = 20;    // id del tag da cercare

    private static final double  POWER_LOW  = 0.3;
    private static final double  POWER_HIGH = 0.6;

    @Override
    public void runOpMode() throws InterruptedException {

        // Robot FERMO durante l'init: il Pinpoint si calibra qui
        RobotHardware robot = new RobotHardware();
        robot.init(hardwareMap, telemetry);

        RobotVisionPortal vision = new RobotVisionPortal();
        vision.init(hardwareMap, DEBUG_VISION);

        StandardMvmnt movement = new StandardMvmnt(robot, gamepad1);

        PositionHold.HOLD_MAX_POWER = POWER_LOW;

        boolean holdEnabled = true;
        boolean prevY = false, prevA = false, prevB = false, prevX = false;
        boolean prevRB = false;
        boolean prevUp = false, prevDown = false, prevLeft = false, prevRight = false;

        telemetry.addLine("Pronto. Y=hold on/off | A,B,X=inverti segno avanti,lato,rot");
        telemetry.addLine("RB=potenza hold 0.3/0.6 | dpad su/giu=KP_POS | sx/dx=KD_POS");
        telemetry.update();

        waitForStart();

        try {
            while (opModeIsActive()) {

                // ── comandi di test ───────────────────────────────────
                if (gamepad1.y && !prevY) {
                    holdEnabled = !holdEnabled;
                    movement.setHoldEnabled(holdEnabled);
                }
                if (gamepad1.a && !prevA) PositionHold.FORWARD_SIGN = -PositionHold.FORWARD_SIGN;
                if (gamepad1.b && !prevB) PositionHold.STRAFE_SIGN  = -PositionHold.STRAFE_SIGN;
                if (gamepad1.x && !prevX) PositionHold.TURN_SIGN    = -PositionHold.TURN_SIGN;

                if (gamepad1.right_bumper && !prevRB) {
                    PositionHold.HOLD_MAX_POWER =
                            PositionHold.HOLD_MAX_POWER < POWER_HIGH ? POWER_HIGH : POWER_LOW;
                }

                if (gamepad1.dpad_up   && !prevUp)    PositionHold.KP_POS += 0.005;
                if (gamepad1.dpad_down && !prevDown)  PositionHold.KP_POS = Math.max(0, PositionHold.KP_POS - 0.005);
                if (gamepad1.dpad_right && !prevRight) PositionHold.KD_POS += 0.001;
                if (gamepad1.dpad_left  && !prevLeft)  PositionHold.KD_POS = Math.max(0, PositionHold.KD_POS - 0.001);

                prevY = gamepad1.y;   prevA = gamepad1.a;   prevB = gamepad1.b;   prevX = gamepad1.x;
                prevRB = gamepad1.right_bumper;
                prevUp = gamepad1.dpad_up;     prevDown = gamepad1.dpad_down;
                prevLeft = gamepad1.dpad_left; prevRight = gamepad1.dpad_right;

                // ── movimento + blocco posizione ──────────────────────
                movement.move();

                // ── telemetria movimento ──────────────────────────────
                telemetry.addLine("=== MOVIMENTO ===");
                telemetry.addData("Driver muove", movement.isMoving());
                telemetry.addData("Hold", (holdEnabled ? "ON" : "OFF") + " / " + movement.getHoldState());
                telemetry.addData("Errore (cm)", "%.1f", movement.getHoldErrorCm());
                telemetry.addData("Pose x,y (cm)", "%.1f , %.1f",
                        robot.pinpoint.getPosX(DistanceUnit.MM) / 10.0,
                        robot.pinpoint.getPosY(DistanceUnit.MM) / 10.0);
                telemetry.addData("Heading (gradi)", "%.1f",
                        robot.pinpoint.getHeading(AngleUnit.DEGREES));

                telemetry.addLine("--- parametri hold ---");
                telemetry.addData("Potenza max", "%.2f", PositionHold.HOLD_MAX_POWER);
                telemetry.addData("KP_POS / KD_POS", "%.3f / %.3f",
                        PositionHold.KP_POS, PositionHold.KD_POS);
                telemetry.addData("Segni avanti/lato/rot", "%.0f / %.0f / %.0f",
                        PositionHold.FORWARD_SIGN, PositionHold.STRAFE_SIGN, PositionHold.TURN_SIGN);

                // ── telemetria AprilTag ───────────────────────────────
                telemetry.addLine("=== APRILTAG ===");
                telemetry.addData("Camera", vision.isStreaming() ? "OK" : "in avvio...");

                AprilTagDetection target  = vision.getTag(TARGET_TAG);
                AprilTagDetection closest = vision.getClosestTag();

                if (target != null) {
                    telemetry.addLine("Tag " + TARGET_TAG + " visibile");
                    telemetry.addData("x (cm)",   "%.1f", target.ftcPose.x);
                    telemetry.addData("y (cm)",   "%.1f", target.ftcPose.y);
                    telemetry.addData("yaw",      "%.1f", target.ftcPose.yaw);
                    telemetry.addData("bearing",  "%.1f", target.ftcPose.bearing);
                } else if (closest != null) {
                    telemetry.addLine("Piu' vicino: " + vision.describe(closest));
                    telemetry.addData("x (cm)",   "%.1f", closest.ftcPose.x);
                    telemetry.addData("y (cm)",   "%.1f", closest.ftcPose.y);
                    telemetry.addData("yaw",      "%.1f", closest.ftcPose.yaw);
                } else {
                    telemetry.addData("Tag", "nessuno visibile");
                }

                telemetry.update();
            }
        } finally {
            vision.close();
        }
    }
}