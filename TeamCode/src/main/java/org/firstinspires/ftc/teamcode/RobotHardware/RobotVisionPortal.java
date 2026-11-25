package org.firstinspires.ftc.teamcode.RobotHardware;

import android.util.Size;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagLibrary;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.apriltag.AprilTagSingleDetection;


import java.util.List;

public class RobotVisionPortal {
    // costruttore di classe
    public RobotVisionPortal() {}

    // variabili dell'apriltag
    public AprilTagLibrary   tagLibrary;
    public AprilTagProcessor tagProcessor;
    public VisionPortal      visionPortal;

    public void init(HardwareMap hwmp, boolean DEBUGGING) throws InterruptedException {
        tagLibrary = new AprilTagLibrary.Builder()
                .addTag(20, "Blu", 41, DistanceUnit.CM)
                .addTag(24, "Red", 41, DistanceUnit.CM)
                .addTag(23, "PPG", 41, DistanceUnit.CM)
                .addTag(21, "GPP", 41, DistanceUnit.CM)
                .build();

        tagProcessor = new AprilTagProcessor.Builder()
                .setTagLibrary(tagLibrary)
                .setOutputUnits(DistanceUnit.CM, AngleUnit.DEGREES)   // x, y, z in cm - yaw in gradi
                .setDrawAxes(DEBUGGING)
                .setDrawTagOutline(DEBUGGING)
                .setDrawTagID(DEBUGGING)
                .setDrawCubeProjection(DEBUGGING)
                .setLensIntrinsics(629.694, 629.694, 358.384, 256.314)
                .setTagFamily(AprilTagProcessor.TagFamily.TAG_36h11)
                .build();

        visionPortal = new VisionPortal.Builder()
                .addProcessor(tagProcessor)
                .setCamera(hwmp.get(WebcamName.class, "Webcam 1"))
                .setCameraResolution(new Size(640, 480))
                .enableLiveView(DEBUGGING)
                .build();

        if (DEBUGGING) FtcDashboard.getInstance().startCameraStream(visionPortal, 24);
    }

    // ══════════════════════════════════════════════════════════════════
    //  LETTURA TAG
    // ══════════════════════════════════════════════════════════════════

    /** Tag con quell'id visibile in questo momento, oppure null. */
    /** Tag singolo con quell'id visibile in questo momento, oppure null. */
    public AprilTagSingleDetection getTag(int id) {
        for (AprilTagDetection d : tagProcessor.getDetections()) {
            if (!(d instanceof AprilTagSingleDetection)) continue;   // salta i cluster
            AprilTagSingleDetection single = (AprilTagSingleDetection) d;
            if (single.metadata != null && single.id == id) return single;
        }
        return null;
    }

    /** La detection piu' vicina (tag singolo o cluster), oppure null. */
    public AprilTagDetection getClosestTag() {
        AprilTagDetection best = null;
        for (AprilTagDetection d : tagProcessor.getDetections()) {
            if (d.ftcPose == null) continue;   // tag senza dimensione in libreria: niente posa
            if (best == null || d.ftcPose.range < best.ftcPose.range) best = d;
        }
        return best;
    }

    /** Etichetta per la telemetria: "ID 20" per un tag singolo, "cluster" per un cluster. */
    public String describe(AprilTagDetection d) {
        if (d instanceof AprilTagSingleDetection) return "ID " + ((AprilTagSingleDetection) d).id;
        return "cluster";
    }

    /** true quando la camera sta mandando frame. */
    public boolean isStreaming() {
        return visionPortal != null
                && visionPortal.getCameraState() == VisionPortal.CameraState.STREAMING;
    }

    /** Chiudi la camera a fine OpMode per liberare la USB. */
    public void close() {
        if (visionPortal != null) visionPortal.close();
    }
}