package org.firstinspires.ftc.teamcode.Odometry;

import java.util.List;
import java.util.Map;

public class AutonomaConfig {

    public String          units;
    public int             match_duration;
    public Pose            start;
    public List<Segment>   segments;
    public List<Mechanism> mechanisms;   // tratti e azioni puntuali (puo' mancare nei JSON vecchi)
    public Park            park;         // null = parcheggio d'emergenza disattivato

    public static class Pose {
        public double x;
        public double y;
        public double heading;
    }

    public static class Segment {
        public int          id;           // == indice del segmento (0, 1, 2...)
        public String       type;         // "line" | "curve"
        public Point        to;
        public List<Point>  controls;     // solo curve
        public RobotHeading robot_heading;
        public SpeedConfig  speed;        // line: completo; curve: solo v_start/v_max/accel
        public Object       action;       // "move" oppure {"wait": s}
        public Double       align;        // gradi a cui ruotare sul posto a fine segmento (null = niente)
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
        public double v_min;        // 0 sulle curve (non presente nel JSON)
        public double accel;
        public double decel;        // 0 sulle curve
        public double brake_point;  // 0 sulle curve
    }

    /**
     * Posizione lungo il percorso: indice segmento + frazione di LUNGHEZZA D'ARCO (non il t
     * parametrico della Bezier). global() = segment + t, es. 1.35 = 35% del segmento 1.
     */
    public static class PathPos {
        public int    segment;
        public double t;

        public double global() { return segment + t; }
    }

    /**
     * Voce di "mechanisms".
     *
     * Tratto (type = intake | flywheel | intake_down | bar_down | shoot):
     *     attivo mentre la posizione e' tra `from` e `to`.
     * Azione puntuale (type = "point"):
     *     finito il segmento `after_segment` il robot si ferma; `mechanism`
     *     (intake | flywheel | shoot) parte dopo `delay` s e dura `time` s.
     */
    public static class Mechanism {
        public String  type;
        public PathPos from;            // solo tratti
        public PathPos to;              // solo tratti
        public String  mechanism;       // solo point
        public int     after_segment;   // solo point
        public double  delay;           // solo point (s)
        public double  time;            // solo point (s)
    }

    /** Parcheggio d'emergenza: negli ultimi trigger_time secondi lascia tutto e va qui. */
    public static class Park {
        public double x;
        public double y;
        public double heading;
        public double trigger_time;
        public double v;                // cm/s
    }

    public enum ActionType {
        MOVE,
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
     * Gli heading (gradi), le velocita' (cm/s, confrontate con V_PHYSICAL_MAX) e i
     * tempi non vengono toccati.
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
        }

        if (park != null) {
            park.x *= K;
            park.y *= K;
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

            if (map.containsKey("wait"))
                return new ParsedAction(ActionType.WAIT,
                        ((Number) map.get("wait")).doubleValue());
        }

        return new ParsedAction(ActionType.MOVE, 0);
    }
}
