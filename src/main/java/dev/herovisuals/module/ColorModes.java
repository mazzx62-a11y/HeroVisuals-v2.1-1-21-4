package dev.herovisuals.module;

import dev.herovisuals.util.ColorUtil;

/** Режим цвета + кастомный цвет (показывается при режиме Custom). */
public class ColorModes {
    public final Setting.Mode mode;
    public final ColorValue custom;

    public ColorModes(Module m, int customRgb) {
        this.mode = m.add(new Setting.Mode("Color", "Rainbow", "Hero", "White", "Custom"));
        final Setting.Mode md = this.mode;
        this.custom = new ColorValue(m, "Custom", customRgb, () -> md.index == 3);
    }

    public int get(float offset, int alpha) {
        return ColorUtil.get(mode.index, offset, alpha, custom.rgb());
    }
}
