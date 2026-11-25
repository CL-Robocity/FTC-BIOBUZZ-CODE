package org.firstinspires.ftc.teamcode.automa;

import android.annotation.SuppressLint;

import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Movmts.StandardMvmnt;
import org.firstinspires.ftc.teamcode.RobotHardware.Hardware;
import org.firstinspires.ftc.teamcode.RobotHardware.Vision;

public class Robot{

    public Hardware hardware;
    public Vision vision;
    public StandardMvmnt standardMvmnt;

    @SuppressLint("NotConstructor")

    public void init(HardwareMap hw, Telemetry telemetry) throws InterruptedException{
        hardware.init(hw, telemetry);
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
