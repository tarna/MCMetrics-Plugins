package net.mcmetrics.fabric;

public class TpsUtils {

    public static final DoubleCircularBuffer mspt = new DoubleCircularBuffer(20 * 60); // Track for 1 minute

    public static double getMspt() {
        return mspt.average();
    }

    public static double getTps() {
        return Math.min(20.0, 1000 /  mspt.average());
    }
}
