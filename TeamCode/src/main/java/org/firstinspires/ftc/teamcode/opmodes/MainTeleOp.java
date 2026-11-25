package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.automa.Robot;

@TeleOp(name = "TeleOp - FTC",group = "OpModes")
public class MainTeleOp extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException{

        Robot robot = new Robot();

        try {
            robot.init(hardwareMap, telemetry);
            telemetry.addLine("La vespa è in moto e pronta a partire!");
            telemetry.update();
        } catch (Exception a) {
            telemetry.addData("Errore durante l'inizializzazione: ", a.getMessage());
            telemetry.update();

            sleep(5000);
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
