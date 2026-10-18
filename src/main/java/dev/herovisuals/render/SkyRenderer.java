package dev.herovisuals.render;

import dev.herovisuals.module.Modules;
import dev.herovisuals.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

/**
 * Рисует полупрозрачный купол цвета неба поверх ванильного неба.
 * Купол проходит depth-тест, поэтому земля и объекты его перекрывают.
 */
public class SkyRenderer {
    private static final int STACKS = 14;
    private static final int SLICES = 36;
    private static final double MIN_ELEV = Math.toRadians(-8);
    private static final double MAX_ELEV = Math.toRadians(90);

    public static void render(MatrixStack ms, VertexConsumerProvider consumers) {
        Modules.SkyModule m = Modules.SKY;
        if (!m.enabled) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return;

        double R = Math.min(600, mc.options.getViewDistance().getValue() * 16.0 + 32.0);
        int alpha = m.alpha.i();
        if (alpha <= 0) return;

        int top = m.color.rgb();
        int hor = m.gradient.value ? m.horizon.rgb() : top;

        Matrix4f mat = ms.peek().getPositionMatrix();
        VertexConsumer vc = consumers.getBuffer(RenderLayer.getDebugQuads());

        for (int i = 0; i < STACKS; i++) {
            double p0 = MIN_ELEV + (MAX_ELEV - MIN_ELEV) * i / STACKS;
            double p1 = MIN_ELEV + (MAX_ELEV - MIN_ELEV) * (i + 1) / STACKS;
            int c0 = col(p0, hor, top, alpha);
            int c1 = col(p1, hor, top, alpha);
            double y0 = Math.sin(p0) * R, y1 = Math.sin(p1) * R;
            double r0 = Math.cos(p0) * R, r1 = Math.cos(p1) * R;
            for (int s = 0; s < SLICES; s++) {
                double a0 = s * Math.PI * 2 / SLICES, a1 = (s + 1) * Math.PI * 2 / SLICES;
                double x00 = Math.cos(a0) * r0, z00 = Math.sin(a0) * r0;
                double x01 = Math.cos(a1) * r0, z01 = Math.sin(a1) * r0;
                double x10 = Math.cos(a0) * r1, z10 = Math.sin(a0) * r1;
                double x11 = Math.cos(a1) * r1, z11 = Math.sin(a1) * r1;
                // обе стороны, чтобы не зависеть от culling
                Draw.quadRaw(vc, mat, x00, y0, z00, c0, x01, y0, z01, c0, x11, y1, z11, c1, x10, y1, z10, c1);
                Draw.quadRaw(vc, mat, x10, y1, z10, c1, x11, y1, z11, c1, x01, y0, z01, c0, x00, y0, z00, c0);
            }
        }

        if (m.style.index == 1) {
            // Deterministic star field on the dome, regenerated identically each frame.
            java.util.Random random = new java.util.Random(0x4845524F);
            int count = m.stars.i();
            for (int i = 0; i < count; i++) {
                double y = random.nextDouble() * 1.65 + 0.05;
                double angle = random.nextDouble() * Math.PI * 2;
                double ring = Math.sqrt(Math.max(0.0, 1.0 - y*y));
                double x = Math.cos(angle) * ring * R * 0.94;
                double z = Math.sin(angle) * ring * R * 0.94;
                double yy = y * R * 0.94;
                int starAlpha = Math.min(255, alpha + 45);
                int starColor = ColorUtil.withAlpha(i % 7 == 0 ? 0xFFC9D9FF : 0xFFFFFFFF, starAlpha);
                Draw.sprite(vc, mat, x, yy, z, m.starSize.value * (i % 9 == 0 ? 1.7 : 1.0), starColor);
            }
        }
    }

    private static int col(double elev, int horizon, int top, int alpha) {
        double t = Math.max(0, Math.min(1, Math.sin(Math.max(0, elev)) / 0.65));
        return ColorUtil.withAlpha(ColorUtil.lerp(horizon, top, (float) t), alpha);
    }
}
