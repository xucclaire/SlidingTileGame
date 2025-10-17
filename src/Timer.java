import java.lang.management.ThreadMXBean;
import java.lang.management.ManagementFactory;

public class Timer {

    private static final double NANOSECONDS_PER_SECOND = 1_000_000_000;

    private final ThreadMXBean threadTimer;
    private final long start;

    public Timer() {
        threadTimer = ManagementFactory.getThreadMXBean();
        start = threadTimer.getCurrentThreadCpuTime();
    }

    public double elapsedTime() {
        long now = threadTimer.getCurrentThreadCpuTime();
        return (now - start) / NANOSECONDS_PER_SECOND;
    }

    public static String toString(double time) {
        if (time < 0.000_000_010) {
            return String.format("%.2f nanoseconds", 1_000_000_000 * time);
        } else if (time < 0.000_000_100) {
            return String.format("%.1f nanoseconds", 1_000_000_000 * time);
        } else if (time < 0.000_001_000) {
            return String.format("%.0f nanoseconds", 1_000_000_000 * time);

        } else if (time < 0.000_010) {
            return String.format("%.2f microseconds", 1_000_000 * time);
        } else if (time < 0.000_100) {
            return String.format("%.1f microseconds", 1_000_000 * time);
        } else if (time < 0.001_000) {
            return String.format("%.0f microseconds", 1_000_000 * time);

        } else if (time < 0.010) {
            return String.format("%.2f milliseconds", 1_000 * time);
        } else if (time < 0.100) {
            return String.format("%.1f milliseconds", 1_000 * time);
        } else if (time < 1.000) {
            return String.format("%.0f milliseconds", 1_000 * time);

        } else if (time < 10.0) {
            return String.format("%.2f seconds", time);
        } else if (time < 100.0) {
            return String.format("%.1f seconds", time);

        } else if (time < 3600.0) {
            int minutes = (int) (time / 60.0);
            time %= 60.0;
            double seconds = time / 60.0;
            return String.format("%d minutes %.1f seconds", minutes, seconds);

        } else {
            int hours = (int) (time / 3600.0);
            time %= 3600.0;
            int minutes = (int) (time / 60.0);
            time %= 60.0;
            double seconds = time;
            return String.format("%d hours %d minutes %.0f seconds", hours, minutes, seconds);
        }
    }

    public static void main(String[] args) {
        for (String arg : args) {
            try {
                double time = Double.parseDouble(arg);
                System.out.printf("%f: %s\n", time, toString(time));
            } catch (NumberFormatException e) {
                System.err.println("Invalid time: " + arg);
            }
        }
    }
}
