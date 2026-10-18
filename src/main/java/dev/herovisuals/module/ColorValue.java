package dev.herovisuals.module;

import java.util.function.Supplier;

/** Цвет из трёх слайдеров R/G/B. */
public class ColorValue {
    public final Setting.Num r, g, b;

    public ColorValue(Module m, String prefix, int rgb, Supplier<Boolean> visible) {
        r = m.add(Setting.when(new Setting.Num(prefix + " R", 0, 255, 1, (rgb >> 16) & 255), visible));
        g = m.add(Setting.when(new Setting.Num(prefix + " G", 0, 255, 1, (rgb >> 8) & 255), visible));
        b = m.add(Setting.when(new Setting.Num(prefix + " B", 0, 255, 1, rgb & 255), visible));
    }

    public int rgb() { return (r.i() << 16) | (g.i() << 8) | b.i(); }
}
