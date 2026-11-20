package org.firstinspires.ftc.teamcode.Odometry;

import com.pedropathing.geometry.Pose;
import com.pedropathing.follower.Follower;

import java.util.ArrayList;
import java.util.List;

public class BallLocalizer {

    private final Follower       follower;
    private final LimelightReader reader;

    public BallLocalizer(Follower follower, LimelightReader reader) {
        this.follower = follower;
        this.reader   = reader;
    }

    /**
     * Restituisce le posizioni assolute delle palline visibili
     * nel sistema di riferimento del campo in cm.
     * [0] = x campo
     * [1] = y campo
     */
    public List<double[]> getAbsoluteBallPositions() {
        List<double[]> relative  = reader.getVisibleBalls();
        List<double[]> absolute  = new ArrayList<>();

        Pose robotPose = follower.getPose();
        double robotX   = robotPose.getX();
        double robotY   = robotPose.getY();
        double robotH   = robotPose.getHeading(); // in radianti

        for (double[] rel : relative) {
            double relX = rel[0];
            double relY = rel[1];

            // Ruota dal sistema robot al sistema campo
            double fieldX = robotX + relX * Math.cos(robotH) - relY * Math.sin(robotH);
            double fieldY = robotY + relX * Math.sin(robotH) + relY * Math.cos(robotH);

            absolute.add(new double[]{fieldX, fieldY});
        }

        return absolute;
    }
    public void stop(){
        reader.stop();
    }
}