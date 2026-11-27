package org.firstinspires.ftc.teamcode.robothardware;

import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;


import com.qualcomm.robotcore.hardware.HardwareMap;
public class Hardware {
    //costruttore di classe
    public Hardware(){}

    // variabile motori di movimento
    public DcMotor lfD; // sinistro avanti
    public DcMotor lbD; // sinistro dietro
    public DcMotor rfD; // destro avanti
    public DcMotor rbD; // destro dietro
    public GoBildaPinpointDriver pinpoint;



    public Hardware(HardwareMap hwmp, Telemetry telemetry) throws InterruptedException{

        // ================= PINPOINT =================
        pinpoint = hwmp.get(
                GoBildaPinpointDriver.class,
                "pinpoint"
        );

// Configurazione dead wheels goBILDA 4-Bar
        pinpoint.setEncoderResolution(
                GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD
        );

// Offset rispetto al centro geometrico del robot
        pinpoint.setOffsets(
                -102.5,
                -57.5,
                DistanceUnit.MM
        );

// Direzione iniziale degli encoder
        pinpoint.setEncoderDirections(
                GoBildaPinpointDriver.EncoderDirection.FORWARD,
                GoBildaPinpointDriver.EncoderDirection.REVERSED
        );

// Il robot deve rimanere fermo durante questa operazione
        pinpoint.resetPosAndIMU();

// aspetta la fine della calibrazione IMU (robot fermo!)
        Thread.sleep(300);
        long t0 = System.currentTimeMillis();
        while (System.currentTimeMillis() - t0 < 3000) {
            pinpoint.update();
            if (pinpoint.getDeviceStatus() == GoBildaPinpointDriver.DeviceStatus.READY) break;
            Thread.sleep(10);
        }

        telemetry.addData("Pinpoint", pinpoint.getDeviceStatus());

        telemetry.addData("Pinpoint", "Configured");

        // riconoscimento motori di movimento
        lfD = hwmp.get(DcMotor.class, "lf");
        lbD = hwmp.get(DcMotor.class, "lb");
        rfD = hwmp.get(DcMotor.class, "rf");
        rbD = hwmp.get(DcMotor.class, "rb");


        // set direzione motori
        lfD.setDirection(DcMotor.Direction.REVERSE);
        lbD.setDirection(DcMotor.Direction.REVERSE);
        rfD.setDirection(DcMotor.Direction.FORWARD);
        rbD.setDirection(DcMotor.Direction.FORWARD);

        // set per runnare i motori
        lfD.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        lbD.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rbD.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rfD.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // set movimento a potenza 0
        lfD.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        lbD.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rfD.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rbD.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }


}