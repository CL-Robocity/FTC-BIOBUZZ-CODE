package org.firstinspires.ftc.teamcode.Odometry.motion;

import org.firstinspires.ftc.teamcode.Odometry.AutonomaConfig;

public class MotionConfig {

    public final double vStart;
    public final double vMax;
    public final double vMin;
    public final double accel;
    public final double decel;
    public final double brakePoint;

    /**
     * Velocità fisica massima del robot in cm/s.
     * Modifica questo valore una volta misurato sul campo.
     */
    public static final double V_PHYSICAL_MAX = 80.0;

    public MotionConfig(AutonomaConfig.SpeedConfig s) {
        this.vStart     = s.v_start;
        this.vMax       = s.v_max;
        this.vMin       = s.v_min;
        this.accel      = s.accel;
        this.decel      = s.decel;
        this.brakePoint = s.brake_point;
    }
}