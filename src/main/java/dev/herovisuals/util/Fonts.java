package dev.herovisuals.util;

import dev.herovisuals.module.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class Fonts {
    private Fonts() {}

    public static final String[] NAMES = {"Minecraft", "Uniform", "Clean", "Bold", "Mono"};
    private static final Identifier[] IDS = {
            null,
            Identifier.of("minecraft", "uniform"),
            Identifier.of("herovisuals", "clean"),
            Identifier.of("herovisuals", "bold"),
            Identifier.of("herovisuals", "mono")
    };

    public static Text text(String s) {
        Identifier id = Modules.INTERFACE.enabled ? IDS[Modules.INTERFACE.font.index] : null;
        if (id == null) return Text.literal(s);
        return Text.literal(s).setStyle(Style.EMPTY.withFont(id));
    }

    public static int width(String s) {
        return MinecraftClient.getInstance().textRenderer.getWidth(text(s));
    }

    public static void draw(DrawContext c, String s, int x, int y, int color) {
        c.drawText(MinecraftClient.getInstance().textRenderer, text(s), x, y, color, Modules.INTERFACE.shadow.value);
    }

    public static void drawCentered(DrawContext c, String s, int cx, int y, int color) {
        draw(c, s, cx - width(s) / 2, y, color);
    }
}
