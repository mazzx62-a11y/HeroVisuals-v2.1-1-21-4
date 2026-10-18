package dev.herovisuals.util;

import net.minecraft.client.gui.DrawContext;

/** Скруглённые прямоугольники для 2D интерфейса. */
public final class RenderUtil {
    private RenderUtil() {}

    private static int inset(int r, int i) {
        double dy = r - i - 0.5;
        double dx = Math.sqrt(Math.max(0, r * r - dy * dy));
        return (int) Math.round(r - dx);
    }

    public static void rounded(DrawContext c, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) return;
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        if (r == 0) { c.fill(x, y, x + w, y + h, color); return; }
        c.fill(x, y + r, x + w, y + h - r, color);
        for (int i = 0; i < r; i++) {
            int in = inset(r, i);
            c.fill(x + in, y + i, x + w - in, y + i + 1, color);
            c.fill(x + in, y + h - 1 - i, x + w - in, y + h - i, color);
        }
    }

    public static void roundedGradient(DrawContext c, int x, int y, int w, int h, int r, int top, int bottom) {
        if (w <= 0 || h <= 0) return;
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        for (int i = 0; i < h; i++) {
            float t = i / (float) Math.max(1, h - 1);
            int col = ColorUtil.lerpArgb(top, bottom, t);
            int in = 0;
            if (i < r) in = inset(r, i);
            else if (i >= h - r) in = inset(r, h - 1 - i);
            c.fill(x + in, y + i, x + w - in, y + i + 1, col);
        }
    }

    /** Заливка с рамкой: рамка рисуется под заливкой. */
    public static void bordered(DrawContext c, int x, int y, int w, int h, int r, int fill, int border) {
        rounded(c, x, y, w, h, r, border);
        rounded(c, x + 1, y + 1, w - 2, h - 2, Math.max(0, r - 1), fill);
    }
}
