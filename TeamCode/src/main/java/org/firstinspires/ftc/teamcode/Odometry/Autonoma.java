package org.firstinspires.ftc.teamcode.Odometry;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Odometry.motion.MotionUtils;
import org.firstinspires.ftc.teamcode.RobotHardware.RobotHardware;

@Autonomous(name = "Autonoma")
public class Autonoma extends LinearOpMode {

    private Follower       follower;
    private RobotHardware  robot;
    private AutonomaConfig config;
    private ActionHandler  actions;

    private static final double ALIGN_TOLERANCE_DEG = 2.0;

    @Override
    public void runOpMode() throws InterruptedException {

        robot = new RobotHardware();
        robot.init(hardwareMap, telemetry);

        config = JsonLoader.load(hardwareMap.appContext, "path.json");
        config.convertToInches();   // il JSON e' in cm, Pedro usa i pollici

        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(new Pose(
                config.start.x,
                config.start.y,
                Math.toRadians(config.start.heading)
        ));

        actions = new ActionHandler(this, robot, follower);
        actions.loadMechanisms(config.mechanisms);   // tratti + azioni puntuali

        telemetry.addData("Status", "Pronto");
        telemetry.addData("Segmenti", config.segments.size());
        telemetry.update();

        waitForStart();
        if (isStopRequested()) return;

        // orologio del match: serve al parcheggio d'emergenza (config.park puo' essere null)
        MotionUtils.startMatchClock(config.match_duration, config.park);

        double prevX = config.start.x;
        double prevY = config.start.y;

        try {
            for (AutonomaConfig.Segment seg : config.segments) {
                if (!opModeIsActive() || MotionUtils.parkRequested()) break;

                // Allineamento prima del segmento se necessario
                double expectedStart = seg.robot_heading.start;
                double currentDeg    = Math.toDegrees(follower.getPose().getHeading());
                if (Math.abs(normalizeAngleDeg(expectedStart - currentDeg)) > ALIGN_TOLERANCE_DEG) {
                    MotionUtils.alignHeading(this, follower, expectedStart, ALIGN_TOLERANCE_DEG);
                    if (!opModeIsActive()) break;
                }

                // line / curve: lo smista MotionUtils
                MotionUtils.executeSegment(
                        this, follower,
                        seg, actions,
                        prevX, prevY
                );
                if (!opModeIsActive() || MotionUtils.parkRequested()) break;

                prevX = seg.to.x;
                prevY = seg.to.y;
            }

            // Ultimi secondi e percorso ancora in corso: lascia tutto e vai al parcheggio
            if (opModeIsActive() && config.park != null && MotionUtils.parkRequested()) {
                MotionUtils.goPark(this, follower, actions, config.park);
            }
        } finally {
            // Fermo sempre il follower, anche con STOP o eccezione
            follower.breakFollowing();
        }
    }

    private double normalizeAngleDeg(double angle) {
        while (angle >  180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }
}