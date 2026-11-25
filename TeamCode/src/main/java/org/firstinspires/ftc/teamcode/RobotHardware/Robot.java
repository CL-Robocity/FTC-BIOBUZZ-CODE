package org.firstinspires.ftc.teamcode.RobotHardware;

import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Robot{

    public Hardware hardware;
    public Vision vision;

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
