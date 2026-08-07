package bet.astral.fluffy.api.actionbar;

import lombok.Getter;

import java.text.DecimalFormat;

public enum HealthDesign implements Designable{
    PRECISE("%.00"),
    ROUNDED("%.0"),
    INTEGER("%")

    ;
    @Getter
    private final DecimalFormat formatter;
    @Getter
    private final String format;

    HealthDesign(String format) {
        this.formatter = new DecimalFormat(format);
        this.format = format;
    }

    public String format(int v) {
        return formatter.format(v);
    }
    public String format(double v) {
        return formatter.format(v);
    }
    public String format(float v) {
        return formatter.format(v);
    }

    @Override
    public String design() {
        return format(18.74);
    }
}
