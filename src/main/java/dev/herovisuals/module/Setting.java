package dev.herovisuals.module;

import java.util.Locale;
import java.util.function.Supplier;

public abstract class Setting {
    public final String name;
    public Supplier<Boolean> visible = () -> true;

    protected Setting(String name) { this.name = name; }

    public boolean isVisible() { return visible.get(); }

    public static <T extends Setting> T when(T s, Supplier<Boolean> v) { s.visible = v; return s; }

    public static class Bool extends Setting {
        public boolean value;
        public Bool(String name, boolean def) { super(name); this.value = def; }
    }

    public static class Num extends Setting {
        public double value;
        public final double min, max, step;

        public Num(String name, double min, double max, double step, double def) {
            super(name);
            this.min = min; this.max = max; this.step = step; this.value = def;
        }

        public float f() { return (float) value; }
        public int i() { return (int) Math.round(value); }
        public double fraction() { return (value - min) / (max - min); }

        public void setFromFraction(double fr) {
            double v = min + (max - min) * Math.max(0, Math.min(1, fr));
            if (step > 0) v = Math.round(v / step) * step;
            value = Math.max(min, Math.min(max, v));
        }

        public String display() {
            return step >= 1 ? String.valueOf(i()) : String.format(Locale.ROOT, "%.2f", value);
        }
    }

    public static class Mode extends Setting {
        public final String[] modes;
        public int index;
        public Mode(String name, String... modes) { super(name); this.modes = modes; }
        public String get() { return modes[index]; }
        public void next() { index = (index + 1) % modes.length; }
        public void prev() { index = (index - 1 + modes.length) % modes.length; }
    }
}
