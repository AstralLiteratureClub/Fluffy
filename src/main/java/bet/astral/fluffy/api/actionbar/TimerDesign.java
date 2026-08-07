package bet.astral.fluffy.api.actionbar;

import java.text.DecimalFormat;

public enum TimerDesign implements Designable{
    MINUTES_AND_SECONDS_ROUNDED_DECIMAL("%m, %s", "%.0"),
    MINUTES_AND_SECONDS_ROUNDED("%m%s", "%.0"),
    MINUTES_AND_SECONDS_DECIMAL("%m, %s", "%"),
    MINUTES_AND_SECONDS("%m%s", "%"),
    SECONDS_ROUNDED("%s", "%.0"),
    SECONDS("%s", "%"),

    ;
    private final DecimalFormat formatter;
    private final String timeFormat;
    private final String secondsFormat;

    TimerDesign(String timeFormat, String secondsFormat) {
        this.timeFormat = timeFormat;
        this.secondsFormat = secondsFormat;
        this.formatter = new DecimalFormat(secondsFormat);
    }

    public String format(int ticks) {
        double seconds = ticks * 0.050;

        switch (this) {
            case SECONDS, SECONDS_ROUNDED -> {
                String formatted = formatter.format(seconds);
                return this.timeFormat.replace("%s", formatted);
            }
            default -> {
                int minutes = (int) (seconds / 60);
                double remainingSeconds = seconds % 60;
                return this.timeFormat.replace("%m", String.valueOf(minutes)).replace("%s", formatter.format(remainingSeconds));
            }
        }
    }

    @Override
    public String design() {
        return format(410/*15.5s*/);
    }
}
