package org.firstinspires.ftc.teamcode.autonomus;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name = "TestMotori")
public class TestMotori extends LinearOpMode {
    @Override
    public void runOpMode() {
        String[] names = {"lf", "lb", "rf", "rb"};
        DcMotor[] motors = new DcMotor[4];
        for (int i = 0; i < 4; i++) motors[i] = hardwareMap.get(DcMotor.class, names[i]);

        waitForStart();

        for (int i = 0; i < 4 && opModeIsActive(); i++) {
            telemetry.addData("Motore in test", names[i]);
            telemetry.update();
            motors[i].setPower(0.5);
            sleep(1000);
            motors[i].setPower(0);
            sleep(500);
        }
    }
}