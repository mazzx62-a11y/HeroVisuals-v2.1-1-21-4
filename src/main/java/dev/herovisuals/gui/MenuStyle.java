package dev.herovisuals.gui;

import dev.herovisuals.module.Modules;
import dev.herovisuals.util.ColorUtil;
import dev.herovisuals.util.Fonts;
import dev.herovisuals.util.RenderUtil;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Скрывает ванильные кнопки (alpha = 0) и рисует поверх них фиолетовые скруглённые.
 * Работает только вне мира (главное меню и экраны, открытые из него).
 */
public final class MenuStyle {
    private MenuStyle() {}

    private static final Set<Screen> HOOKED = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Map<ClickableWidget, Float> ANIM = new WeakHashMap<>();

    public static void init() {
        ScreenEvents.AFTER_INIT.register((mc, screen, w, h) -> {
            if (!active(mc, screen)) return;
            for (ClickableWidget cw : Screens.getButtons(screen)) {
                if (cw.getClass() == ButtonWidget.class) cw.setAlpha(0f);
            }
            if (HOOKED.add(screen)) {
                ScreenEvents.afterRender(screen).register((s, ctx, mx, my, td) -> overlay(mc, s, ctx));
            }
        });
    }

    private static boolean active(MinecraftClient mc, Screen screen) {
        Modules.InterfaceModule i = Modules.INTERFACE;
        return i.enabled && i.menu.value && mc.world == null && (screen instanceof TitleScreen || i.menuAll.value);
    }

    private static void overlay(MinecraftClient mc, Screen screen, DrawContext ctx) {
        boolean act = active(mc, screen);
        if (act) ctx.fill(0, 0, screen.width, screen.height, 0xFF000000);
        for (ClickableWidget cw : Screens.getButtons(screen)) {
            if (cw.getClass() != ButtonWidget.class) continue;
            cw.setAlpha(act ? 0f : 1f);
            if (!act || !cw.visible) continue;

            int x = cw.getX(), y = cw.getY(), w = cw.getWidth(), h = cw.getHeight();
            boolean hover = cw.isHovered() && cw.active;
            float a = ANIM.getOrDefault(cw, 0f);
            a += ((hover ? 1f : 0f) - a) * 0.25f;
            ANIM.put(cw, a);

            int top, bottom, textColor;
            if (cw.active) {
                top = 0xFF000000 | ColorUtil.lerp(0x171717, 0x303030, a);
                bottom = 0xFF000000 | ColorUtil.lerp(0x080808, 0x191919, a);
                textColor = 0xFFFFFFFF;
            } else {
                top = 0xFF3A3350;
                bottom = 0xFF2A2540;
                textColor = 0xFF8A86A3;
            }
            if (a > 0.03f) {
                RenderUtil.rounded(ctx, x - 2, y - 2, w + 4, h + 4, 9, ((int) (a * 80) << 24) | 0x8B5CF6);
            }
            RenderUtil.roundedGradient(ctx, x, y, w, h, 7, top, bottom);
            // лёгкий блик сверху
            RenderUtil.rounded(ctx, x + 4, y + 1, w - 8, 1, 0, 0x30FFFFFF);

            String label = cw.getMessage().getString();
            Fonts.drawCentered(ctx, label, x + w / 2, y + (h - 8) / 2, textColor);
        }

        if (act && screen instanceof TitleScreen) {
            ctx.getMatrices().push();
            ctx.getMatrices().translate(10, 10, 0);
            ctx.getMatrices().scale(2f, 2f, 1f);
            Fonts.draw(ctx, "HeroVisuals", 0, 0, ColorUtil.get(1, 0f, 255));
            ctx.getMatrices().pop();
        }
    }
}
