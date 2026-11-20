package org.firstinspires.ftc.teamcode.Odometry;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;

import java.util.ArrayList;
import java.util.List;

public class LimelightReader {

    private final Limelight3A limelight;

    // ── Offset Limelight rispetto al centro robot in cm ───────────────
    // positivo = avanti, negativo = indietro/sinistra
    public static double LIMELIGHT_FORWARD_OFFSET = 20.0; // da misurare
    public static double LIMELIGHT_SIDE_OFFSET    = 0.0;  // da misurare
    public static double LIMELIGHT_HEIGHT         = 30.0; // altezza dal suolo in cm

    // Altezza centro pallina dal suolo in cm
    public static double BALL_HEIGHT = 3.0; // da misurare

    public LimelightReader(Limelight3A limelight) {
        this.limelight = limelight;
        // Pipeline 0 = HSV giallo configurata su Limelight
        limelight.pipelineSwitch(0);
        limelight.start();
    }

    /**
     * Restituisce le palline visibili come posizioni relative al robot in cm.
     * [0] = x laterale (positivo = destra)
     * [1] = y avanti   (positivo = avanti)
     *
     * Usa getColorResults() — pipeline HSV, non Neural Network.
     */
    public List<double[]> getVisibleBalls() {
        List<double[]> balls = new ArrayList<>();

        LLResult result = limelight.getLatestResult();
        if (result == null || !result.isValid()) return balls;

        List<LLResultTypes.ColorResult> colors = result.getColorResults();
        if (colors == null || colors.isEmpty()) return balls;

        for (LLResultTypes.ColorResult color : colors) {
            double tx = color.getTargetXDegrees();
            double ty = color.getTargetYDegrees();
            double[] pos = txTyToRelative(tx, ty);
            if (pos != null) balls.add(pos);
        }

        return balls;
    }

    /**
     * Converte tx/ty angoli in posizione relativa al robot in cm.
     * Restituisce null se ty è positivo (pallina dietro la camera — impossibile).
     */
    private double[] txTyToRelative(double tx, double ty) {
        double txRad = Math.toRadians(tx);
        double tyRad = Math.toRadians(ty);

        double heightDiff = LIMELIGHT_HEIGHT - BALL_HEIGHT;
        if (Math.tan(-tyRad) <= 0) return null; // pallina non valida

        double distance = heightDiff / Math.tan(-tyRad);
        double camX     = distance * Math.tan(txRad);
        double camY     = distance;

        double robotX = camX + LIMELIGHT_SIDE_OFFSET;
        double robotY = camY + LIMELIGHT_FORWARD_OFFSET;

        return new double[]{robotX, robotY};
    }

    public void stop() {
        limelight.stop();
    }
}