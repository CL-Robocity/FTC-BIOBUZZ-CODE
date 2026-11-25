package org.firstinspires.ftc.teamcode.general;

import android.annotation.SuppressLint;

import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.movmts.StandardMvmnt;
import org.firstinspires.ftc.teamcode.robothardware.Hardware;
import org.firstinspires.ftc.teamcode.robothardware.Vision;

public class Robot{

    public Hardware hardware;
    public Vision vision;
    public StandardMvmnt standardMvmnt = new StandardMvmnt();

    @SuppressLint("NotConstructor")

    public Robot(HardwareMap hw, Telemetry telemetry) throws InterruptedException{
        hardware = new Hardware(hw, telemetry);
        vision = new Vision(hw);
    }

    public void Update(){
        vision.update();
    }

    public void Start(){
        vision.start();
    }

    public void Stop(){
        vision.stop();
    }
}
