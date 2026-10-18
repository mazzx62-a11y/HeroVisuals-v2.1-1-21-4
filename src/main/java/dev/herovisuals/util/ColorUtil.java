package dev.herovisuals.util;

import java.awt.Color;

public class ColorUtil {
    public static final int ACCENT = 0xFF8B5CF6;

    /** mode: 0 Rainbow, 1 Hero, 2 White, 3 Custom. Возвращает ARGB. */
    public static int get(int mode, float offset, int alpha, int custom) {
        offset = ((offset % 1f) + 1f) % 1f;
        int rgb;
        switch (mode) {
            case 0 -> rgb = Color.HSBtoRGB(((System.currentTimeMillis() % 4000L) / 4000f + offset) % 1f, 0.55f, 1f);
            case 1 -> {
                float t = (float) (Math.sin(System.currentTimeMillis() / 600.0 + offset * Math.PI * 2) * 0.5 + 0.5);
                rgb = lerp(0x8B5CF6, 0xFFFFFF, t);
            }
            case 3 -> rgb = custom;
            default -> rgb = 0xFFFFFF;
        }
        return (alpha << 24) | (rgb & 0xFFFFFF);
    }

    public static int get(int mode, float offset, int alpha) { return get(mode, offset, alpha, 0xFFFFFF); }

    /** Линейная интерполяция RGB (без альфы). */
    public static int lerp(int a, int b, float t) {
        int r = (int) (((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * t);
        int g = (int) (((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * t);
        int bl = (int) ((a & 255) + ((b & 255) - (a & 255)) * t);
        return (r << 16) | (g << 8) | bl;
    }

    /** Линейная интерполяция ARGB (с альфой). */
    public static int lerpArgb(int a, int b, float t) {
        int al = (int) (((a >>> 24) & 255) + (((b >>> 24) & 255) - ((a >>> 24) & 255)) * t);
        return (al << 24) | lerp(a, b, t);
    }

    public static int withAlpha(int rgb, int alpha) { return (alpha << 24) | (rgb & 0xFFFFFF); }
}
