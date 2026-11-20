package org.firstinspires.ftc.teamcode.Odometry;

public class MotionProfile {

    /**
     * Dato t in [0,1] restituisce la potenza in [0,1] per setMaxPower().
     *
     * t < brakePoint  -> accelerazione: parte da vStart, cresce con pendenza accel,
     *                    mai sotto vMin (altrimenti con vStart = 0 il robot non parte)
     *                    e mai sopra vMax.
     * t >= brakePoint -> decelerazione: scende con pendenza decel, non sotto vMin.
     */
    public static double compute(double t, MotionConfig cfg) {
        double v;

        if (t < cfg.brakePoint) {
            // rampa su una frazione del percorso, non su tutto t
            double rampEnd = 0.2;                       // sale a vMax nel primo 20%
            double k = Math.min(t / rampEnd, 1.0);
            v = cfg.vMin + (cfg.vMax - cfg.vMin) * k;
        } else {
            double span  = Math.max(1.0 - cfg.brakePoint, 1e-6);
            double tNorm = (t - cfg.brakePoint) / span;
            v = cfg.vMax - (cfg.vMax - cfg.vMin) * tNorm; // scende fino a vMin
        }

        double power = v / MotionConfig.V_PHYSICAL_MAX;
        return Math.min(1.0, Math.max(0.0, power));
    }
}
