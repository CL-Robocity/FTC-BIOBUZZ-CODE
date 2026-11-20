package org.firstinspires.ftc.teamcode.Odometry;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.Pose;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.RobotHardware.RobotHardware;

@Autonomous(name = "Autonoma")
public class Autonoma extends LinearOpMode {

    private Follower       follower;
    private RobotHardware  robot;
    private AutonomaConfig config;
    private ActionHandler  actions;

    private static final double ALIGN_TOLERANCE_DEG = 2.0;

    // contatore palline raccolte — array per passarlo by reference
    private final int[] collectedBalls = {0};

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


        telemetry.addData("Status", "Pronto");
        telemetry.addData("Segmenti", config.segments.size());
        telemetry.update();

        waitForStart();
        if (isStopRequested()) return;

        double prevX = config.start.x;
        double prevY = config.start.y;

        try {
            for (AutonomaConfig.Segment seg : config.segments) {
                if (!opModeIsActive()) break;

                // Allineamento prima del segmento se necessario
                double expectedStart = seg.robot_heading.start;
                double currentDeg    = Math.toDegrees(follower.getPose().getHeading());
                if (Math.abs(normalizeAngleDeg(expectedStart - currentDeg)) > ALIGN_TOLERANCE_DEG) {
                    MotionUtils.alignHeading(this, follower, expectedStart, ALIGN_TOLERANCE_DEG);
                    if (!opModeIsActive()) break;
                }

                // line / curve / search: lo smista MotionUtils

            }
        } finally {
            // Fermo sempre il follower, anche con STOP o eccezione
            follower.breakFollowing();
        }
    }

    private boolean hasSearchSegments() {
        if (config.segments == null) return false;
        for (AutonomaConfig.Segment seg : config.segments) {
            if ("search".equals(seg.type)) return true;
        }
        return false;
    }

    private double normalizeAngleDeg(double angle) {
        while (angle >  180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }
}