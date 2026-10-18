package dev.herovisuals.gui;

import dev.herovisuals.module.Category;
import dev.herovisuals.module.Module;
import dev.herovisuals.module.Modules;
import dev.herovisuals.module.Setting;
import dev.herovisuals.util.ColorUtil;
import dev.herovisuals.util.Fonts;
import dev.herovisuals.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ClickGuiScreen extends Screen {
    private static final int W = 520, H = 320, HEADER = 34, TAB_W = 120, FOOT = 20;
    private static final int MODULE_H = 28, SETTING_H = 20;

    private static Category selected = Category.RENDER;
    private static float scroll = 0;

    private static class Dot { float x, y, vx, vy; int size; float phase; }
    private static class Row { Module m; Setting s; int y, h; }

    private final List<Dot> dots = new ArrayList<>();
    private final Random rnd = new Random();
    private long lastNanos = System.nanoTime();
    private float openAnim = 0f;
    private int contentH = 0;
    private Setting.Num draggingNum;
    private static Module bindingModule;

    public ClickGuiScreen() { super(Text.literal("HeroVisuals")); }

    @Override public boolean shouldPause() { return false; }

    private int wx() { return (width - W) / 2; }
    private int wy() { return (height - H) / 2; }
    private int cx() { return wx() + TAB_W + 10; }
    private int cw() { return W - TAB_W - 20; }
    private int cTop() { return wy() + HEADER + 8; }
    private int cH() { return H - HEADER - 8 - FOOT; }
    private int sliderX() { return cx() + cw() - 180; }
    private static final int SLIDER_W = 170;

    private List<Row> buildRows() {
        List<Row> out = new ArrayList<>();
        int y = 0;
        for (Module m : Modules.byCategory(selected)) {
            Row r = new Row(); r.m = m; r.y = y; r.h = MODULE_H; out.add(r);
            y += MODULE_H + 3;
            if (m.expanded) {
                for (Setting s : m.settings) {
                    if (!s.isVisible()) continue;
                    Row sr = new Row(); sr.m = m; sr.s = s; sr.y = y; sr.h = SETTING_H; out.add(sr);
                    y += SETTING_H + 2;
                }
                y += 4;
            }
        }
        contentH = y;
        return out;
    }

    private void clampScroll() {
        float max = Math.max(0, contentH - cH());
        scroll = Math.max(0, Math.min(max, scroll));
    }

    private void syncDots() {
        int target = Modules.INTERFACE.dots.i();
        while (dots.size() < target) {
            Dot d = new Dot();
            d.x = rnd.nextFloat() * width;
            d.y = rnd.nextFloat() * height;
            d.vy = 14f + rnd.nextFloat() * 40f;
            d.vx = (rnd.nextFloat() - 0.5f) * 6f;
            d.size = rnd.nextFloat() < 0.8f ? 1 : 2;
            d.phase = rnd.nextFloat() * 6.28f;
            dots.add(d);
        }
        while (dots.size() > target) dots.remove(dots.size() - 1);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        long now = System.nanoTime();
        float dt = Math.min((now - lastNanos) / 1.0E9f, 0.1f);
        lastNanos = now;
        openAnim = Math.min(1f, openAnim + dt * 4f);

        // Clean black canvas; subtle particles remain optional through the dot setting.
        ctx.fill(0, 0, width, height, 0xF5000000);
        syncDots();
        long t = System.currentTimeMillis();
        for (Dot d : dots) {
            d.y += d.vy * dt;
            d.x += d.vx * dt;
            if (d.y > height) { d.y = -2; d.x = rnd.nextFloat() * width; }
            if (d.x < 0) d.x = width; else if (d.x > width) d.x = 0;
            int a = (int) (110 + 100 * Math.sin(t / 700.0 + d.phase));
            ctx.fill((int) d.x, (int) d.y, (int) d.x + d.size, (int) d.y + d.size, (a << 24) | 0xFFFFFF);
        }

        int x = wx();
        int y = wy() + (int) ((1f - openAnim) * 12);

        // окно
        RenderUtil.rounded(ctx, x - 1, y - 1, W + 2, H + 2, 10, ColorUtil.withAlpha(0x8B5CF6, 140));
        RenderUtil.rounded(ctx, x, y, W, H, 9, 0xF0100D1C);
        RenderUtil.roundedGradient(ctx, x, y, W, HEADER, 9, 0xFF221A3D, 0xFF161228);
        RenderUtil.rounded(ctx, x + 10, y + HEADER - 2, W - 20, 2, 1, ColorUtil.ACCENT);

        ctx.getMatrices().push();
        ctx.getMatrices().translate(x + 14, y + 9, 0);
        ctx.getMatrices().scale(1.5f, 1.5f, 1f);
        Fonts.draw(ctx, "HeroVisuals", 0, 0, ColorUtil.get(1, 0f, 255));
        ctx.getMatrices().pop();
        Fonts.draw(ctx, "v1.1", x + W - 32, y + 13, 0xFF8F88B8);

        // вкладки
        RenderUtil.rounded(ctx, x + 6, y + HEADER + 6, TAB_W - 6, H - HEADER - 12, 7, 0xFF0D0A18);
        int ty = y + HEADER + 14;
        for (Category c : Category.values()) {
            boolean sel = c == selected;
            boolean hover = mx >= x + 10 && mx <= x + TAB_W - 4 && my >= ty && my <= ty + 24;
            if (sel) {
                RenderUtil.rounded(ctx, x + 10, ty, TAB_W - 14, 24, 6, ColorUtil.withAlpha(0x8B5CF6, 90));
                RenderUtil.rounded(ctx, x + 10, ty + 5, 3, 14, 1, ColorUtil.ACCENT);
            } else if (hover) {
                RenderUtil.rounded(ctx, x + 10, ty, TAB_W - 14, 24, 6, 0x30FFFFFF);
            }
            Fonts.draw(ctx, c.label, x + 22, ty + 8, sel ? 0xFFFFFFFF : 0xFF9A96BC);
            ty += 28;
        }

        // контент со скроллом
        List<Row> rows = buildRows();
        clampScroll();
        int top = cTop(), ch = cH(), cx = cx(), cw = cw();
        ctx.enableScissor(cx - 2, top, cx + cw + 2, top + ch);
        for (Row r : rows) {
            int ry = top + r.y - (int) scroll;
            if (ry + r.h < top || ry > top + ch) continue;
            if (r.s == null) drawModule(ctx, r.m, cx, ry, cw, mx, my, dt);
            else drawSetting(ctx, r.s, cx, ry, cw, mx, my);
        }
        ctx.disableScissor();

        // полоса прокрутки
        if (contentH > ch) {
            int bh = Math.max(20, ch * ch / contentH);
            int by = top + (int) ((ch - bh) * (scroll / (contentH - ch)));
            RenderUtil.rounded(ctx, cx + cw + 3, by, 3, bh, 1, 0x80B79CFF);
        }

        Fonts.draw(ctx, "LMB toggle  |  RMB settings  |  scroll  |  RShift close", cx, y + H - 15, 0xFF6C6890);
    }

    private void drawModule(DrawContext ctx, Module m, int x, int y, int w, int mx, int my, float dt) {
        m.anim += ((m.enabled ? 1f : 0f) - m.anim) * Math.min(1f, dt * 12f);
        boolean hover = mx >= x && mx <= x + w && my >= y && my <= y + MODULE_H;
        RenderUtil.rounded(ctx, x, y, w, MODULE_H, 6, hover ? 0xFF211C38 : 0xFF181428);
        int bar = ColorUtil.lerp(0x3A3652, 0x8B5CF6, m.anim) | 0xFF000000;
        RenderUtil.rounded(ctx, x + 5, y + 6, 3, MODULE_H - 12, 1, bar);

        Fonts.draw(ctx, m.name, x + 16, y + 5, 0xFFFFFFFF);
        Fonts.draw(ctx, m.description, x + 16, y + 16, 0xFF7C7A99);
        String bindLabel = bindingModule == m ? "Press key..." : (m.bindKeyCode == 0 ? "Bind: -" : "Bind: " + keyName(m.bindKeyCode));
        Fonts.draw(ctx, bindLabel, x + w - 124, y + 16, bindingModule == m ? 0xFFFFD479 : 0xFF8C86A8);

        // переключатель
        int sx = x + w - 46, sy = y + 8;
        RenderUtil.rounded(ctx, sx, sy, 32, 12, 6, ColorUtil.lerp(0x2A2640, 0x8B5CF6, m.anim) | 0xFF000000);
        int knob = sx + 2 + (int) (m.anim * 20);
        RenderUtil.rounded(ctx, knob, sy + 2, 8, 8, 4, 0xFFFFFFFF);

        if (!m.settings.isEmpty()) {
            Fonts.draw(ctx, m.expanded ? "v" : ">", x + w - 62, y + 10, 0xFF8F88B8);
        }
    }

    private void drawSetting(DrawContext ctx, Setting s, int x, int y, int w, int mx, int my) {
        RenderUtil.rounded(ctx, x + 8, y, w - 8, SETTING_H, 5, 0xFF120F20);
        Fonts.draw(ctx, s.name, x + 18, y + 6, 0xFFBDB9DC);
        int sx = sliderX();

        if (s instanceof Setting.Bool b) {
            int px = x + w - 38;
            RenderUtil.rounded(ctx, px, y + 5, 24, 10, 5, b.value ? 0xFF8B5CF6 : 0xFF2A2640);
            RenderUtil.rounded(ctx, px + (b.value ? 14 : 2), y + 7, 6, 6, 3, 0xFFFFFFFF);
        } else if (s instanceof Setting.Mode md) {
            RenderUtil.rounded(ctx, sx, y + 2, SLIDER_W, SETTING_H - 4, 5, 0xFF2A2640);
            Fonts.drawCentered(ctx, "< " + md.get() + " >", sx + SLIDER_W / 2, y + 6, 0xFFD9CCFF);
        } else if (s instanceof Setting.Num n) {
            RenderUtil.rounded(ctx, sx, y + 7, SLIDER_W, 6, 3, 0xFF2A2640);
            int fw = Math.max(6, (int) (SLIDER_W * n.fraction()));
            int col = n.name.endsWith(" R") ? 0xFFE5484D : n.name.endsWith(" G") ? 0xFF46C46A
                    : n.name.endsWith(" B") ? 0xFF4C8DFF : 0xFF8B5CF6;
            RenderUtil.rounded(ctx, sx, y + 7, fw, 6, 3, col);
            RenderUtil.rounded(ctx, sx + fw - 3, y + 5, 6, 10, 3, 0xFFFFFFFF);
            String v = n.display();
            Fonts.draw(ctx, v, sx - 8 - Fonts.width(v), y + 6, 0xFFD9CCFF);
        }
    }

    // ---------------- input ----------------
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int x = wx(), y = wy();

        int ty = y + HEADER + 14;
        for (Category c : Category.values()) {
            if (mx >= x + 10 && mx <= x + TAB_W - 4 && my >= ty && my <= ty + 24) {
                selected = c;
                scroll = 0;
                return true;
            }
            ty += 28;
        }

        int top = cTop(), cx = cx(), cw = cw();
        if (mx >= cx && mx <= cx + cw && my >= top && my <= top + cH()) {
            for (Row r : buildRows()) {
                int ry = top + r.y - (int) scroll;
                if (my < ry || my > ry + r.h) continue;
                if (r.s == null) {
                    if (button == 0) r.m.toggle();
                    else if (button == 1 && !r.m.settings.isEmpty()) r.m.expanded = !r.m.expanded;
                    else if (button == 2) bindingModule = r.m;
                } else if (r.s instanceof Setting.Bool b) {
                    if (button == 0) b.value = !b.value;
                } else if (r.s instanceof Setting.Mode md) {
                    if (button == 0) md.next(); else if (button == 1) md.prev();
                } else if (r.s instanceof Setting.Num n) {
                    if (button == 0) {
                        draggingNum = n;
                        n.setFromFraction((mx - sliderX()) / SLIDER_W);
                    }
                }
                dev.herovisuals.module.ConfigManager.save();
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (draggingNum != null && button == 0) {
            draggingNum.setFromFraction((mx - sliderX()) / SLIDER_W);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (draggingNum != null) dev.herovisuals.module.ConfigManager.save();
        draggingNum = null;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmount, double vAmount) {
        scroll -= (float) (vAmount * 24);
        clampScroll();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (bindingModule != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE)
                bindingModule.bindKeyCode = 0;
            else if (keyCode != GLFW.GLFW_KEY_UNKNOWN && keyCode != GLFW.GLFW_KEY_RIGHT_SHIFT)
                bindingModule.bindKeyCode = keyCode;
            bindingModule = null;
            dev.herovisuals.module.ConfigManager.save();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) { close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private static String keyName(int keyCode) {
        String name = GLFW.glfwGetKeyName(keyCode, 0);
        if (name != null && !name.isBlank()) return name.toUpperCase(java.util.Locale.ROOT);
        return switch (keyCode) {
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "L-SHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "R-SHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "L-CTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "R-CTRL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "L-ALT";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "R-ALT";
            default -> "KEY" + keyCode;
        };
    }
}
