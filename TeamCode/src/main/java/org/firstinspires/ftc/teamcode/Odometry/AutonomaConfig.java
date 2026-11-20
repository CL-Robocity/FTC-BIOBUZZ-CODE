package org.firstinspires.ftc.teamcode.Odometry;

import java.util.List;
import java.util.Map;

public class AutonomaConfig {

    public String        units;
    public int           match_duration;
    public Pose          start;
    public List<Segment> segments;

    public static class Pose {
        public double x;
        public double y;
        public double heading;
    }

    public static class Segment {
        public int          id;
        public String       type;         // "line" | "curve" | "search"
        public Point        to;
        public List<Point>  controls;
        public String       heading;
        public RobotHeading robot_heading;
        public SpeedConfig  speed;        // null sui segmenti search
        public SearchConfig search;       // null su line/curve
        public Object       action;
        public boolean      intake;
        public boolean      flywheel;
    }

    public static class Point {
        public double x;
        public double y;
    }

    public static class RobotHeading {
        public double start;
        public double end;
        public double rotation_speed;
    }

    public static class SpeedConfig {
        public double v_start;
        public double v_max;
        public double v_min;
        public double accel;
        public double decel;
        public double brake_point;
    }

    /**
     * Configurazione esclusiva dei segmenti "search".
     * v          → velocità costante durante la ricerca (cm/s poi in/s)
     * area_width → larghezza rettangolo di ricerca (cm poi inch)
     * time_limit → timeout in secondi
     * shoot_after→ se true spara dopo aver raccolto
     * shoot_time → secondi di attesa per lo sparo
     */
    public static class SearchConfig {
        public double  v;
        public double  area_width;
        public double  time_limit;
        public boolean shoot_after;
        public double  shoot_time;
    }

    public enum ActionType {
        MOVE,
        SHOOT_MOVE,
        SHOOT_STOP,
        WAIT
    }

    public static class ParsedAction {
        public ActionType type;
        public double     duration;

        public ParsedAction(ActionType type, double duration) {
            this.type     = type;
            this.duration = duration;
        }
    }

    /**
     * Pedro Pathing lavora in pollici: converte le coordinate del JSON da cm.
     * Gli heading (gradi) e i timeout non vengono toccati.
     */
    public void convertToInches() {
        if (units == null || !units.equalsIgnoreCase("cm")) return;
        final double K = 1.0 / 2.54;

        start.x *= K;
        start.y *= K;

        for (Segment s : segments) {
            s.to.x *= K;
            s.to.y *= K;
            if (s.controls != null) {
                for (Point p : s.controls) {
                    p.x *= K;
                    p.y *= K;
                }
            }
            if ("search".equals(s.type) && s.search != null) {
                s.search.area_width *= K; // cm   → in
                // time_limit e shoot_time sono secondi → non si toccano
            }
        }
        units = "in";
    }

    @SuppressWarnings("unchecked")
    public static ParsedAction parseAction(Object raw) {
        if (raw == null) return new ParsedAction(ActionType.MOVE, 0);

        if (raw instanceof String)
            return new ParsedAction(ActionType.MOVE, 0);

        if (raw instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) raw;

            if (map.containsKey("shoot_move"))
                return new ParsedAction(ActionType.SHOOT_MOVE,
                        ((Number) map.get("shoot_move")).doubleValue());

            if (map.containsKey("shoot_stop"))
                return new ParsedAction(ActionType.SHOOT_STOP,
                        ((Number) map.get("shoot_stop")).doubleValue());

            if (map.containsKey("wait"))
                return new ParsedAction(ActionType.WAIT,
                        ((Number) map.get("wait")).doubleValue());
        }

        return new ParsedAction(ActionType.MOVE, 0);
    }
}