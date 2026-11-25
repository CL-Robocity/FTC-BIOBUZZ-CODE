package org.firstinspires.ftc.teamcode.robothardware;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Vision {

    ///----------------------------------------* LIMELIGHT *----------------------------------------

    public int currentPipeLine = 1;
    private boolean valid = false;

    private final double ALTEZZA_CAMERA = 29;
    private final double ANGOLO_CAMERA = 21.2;
    private final double ALTEZZA_PALLINA = 0;
    private double tx = 0, ty = 0, ta = 0, distance = Double.NaN;


    /// --------------------------------------------------------------------------------------------


    private final Limelight3A limelight;


    public Vision(HardwareMap hw) {
        Limelight3A limelight1;
        try {
            limelight1 = hw.tryGet(Limelight3A.class, "limelight");
            if (limelight1 != null) {
                limelight1.setPollRateHz(100);
                pipeLineSwitch(1); // 1 = red, 2 = blue, 3 = yellow
            }
        }catch (Exception e){
            limelight1 = null;
        }
        limelight = limelight1;
    }


    public void start() {
        if (limelight == null) return;
        try { limelight.start(); } catch (Exception ignored) {}
    }


    public void stop() {
        if (limelight == null) return;
        try { limelight.stop(); } catch (Exception ignored) {}
    }


    public void pipeLineSwitch(int i) {
        if (limelight == null) return;
        else {
        limelight.pipelineSwitch(i);
        currentPipeLine = i;
        }
    }


    private double computeDistance(double tyDeg){

        double angolo = Math.toRadians(ANGOLO_CAMERA-tyDeg);
        if (angolo <= 0.01) return distance = Double.NaN;
        return (ALTEZZA_CAMERA-ALTEZZA_PALLINA)/Math.tan(angolo);
    }


    public void update(){
        if (limelight == null) return;
        else {
            LLResult result = limelight.getLatestResult();
            valid = result != null && result.isValid();

            if (valid){
                tx = result.getTx();
                ty = result.getTy();
                ta = result.getTa();
                distance = computeDistance(ty);
            } else {
                distance = Double.NaN;
            }
        }
    }


    public boolean isAvailable(){
        return valid && !Double.isNaN(distance);
    }
    public double getTx(){
        return tx;
    }
    public double getTy(){
        return ty;
    }
    public double getTa(){
        return ta;
    }
    public double getDistance (){
        return distance;
    }


    ///----------------------------------------* LOGITECH *----------------------------------------


}