package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.general.Robot;

@TeleOp(name = "TeleOp - FTC",group = "OpModes")
public class MainTeleOp extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException{

        Robot robot;

        try {
            robot = new Robot(hardwareMap,telemetry);
            telemetry.addLine("La vespa è in moto e pronta a partire!");
            telemetry.update();
        } catch (Exception a) {
            telemetry.addData("Errore durante l'inizializzazione: ", a.getMessage());
            telemetry.update();

            waitForStart();
            return; // Termina l'op mode
        }

        waitForStart();
        robot.Start();
        while (opModeIsActive()){
            robot.Update();

            robot.standardMvmnt.move(gamepad1, robot.hardware);


            robot.vision.pipeLineSwitch(1);
            telemetry.addData("Colore palla riconosciuto",(robot.vision.currentPipeLine==1) ? "rosso"
                    : (robot.vision.currentPipeLine==2) ? "Blu"
                    : "Giallo");
            telemetry.update();
        }

    }
}
