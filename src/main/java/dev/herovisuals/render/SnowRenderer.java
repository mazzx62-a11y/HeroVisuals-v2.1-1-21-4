package dev.herovisuals.render;

import dev.herovisuals.module.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.Random;

public class SnowRenderer {
    private static final int MAX = 2000;
    private static final float[] BX = new float[MAX], BY = new float[MAX], BZ = new float[MAX], SP = new float[MAX];

    static {
        Random r = new Random(1337);
        for (int i = 0; i < MAX; i++) {
            BX[i] = r.nextFloat(); BY[i] = r.nextFloat(); BZ[i] = r.nextFloat();
            SP[i] = 0.7f + 0.6f * r.nextFloat();
        }
    }

    private static double mod(double a, double b) { return ((a % b) + b) % b; }

    public static void render(MatrixStack ms, VertexConsumerProvider consumers, Vec3d cam) {
        Modules.SnowModule m = Modules.SNOW;
        if (!m.enabled) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;
        if (mc.world.getDimension().hasCeiling()) return;
        if (m.outdoors.value && !mc.world.isSkyVisible(mc.player.getBlockPos())) return;

        double R = m.range.value;
        double S = R * 2;
        double t = System.nanoTime() / 1.0E9;
        int n = Math.min(MAX, m.count.i());

        Matrix4f mat = ms.peek().getPositionMatrix();
        VertexConsumer vc = consumers.getBuffer(RenderLayer.getDebugQuads());

        for (int i = 0; i < n; i++) {
            double sp = m.speed.value * SP[i];
            double x = mod(BX[i] * S + m.wind.value * t * sp * 0.6 + Math.sin(t * 0.8 + i) * 0.3 - cam.x, S) - R;
            double y = mod(BY[i] * S - sp * t - cam.y, S) - R;
            double z = mod(BZ[i] * S + Math.cos(t * 0.6 + i) * 0.3 - cam.z, S) - R;

            double d = Math.sqrt(x * x + y * y + z * z);
            double edge = Math.min(1.0, (R - Math.min(d, R)) / (R * 0.4));
            double near = Math.min(1.0, d / 1.5);
            int a = (int) (230 * edge * near);
            if (a < 6) continue;
            Draw.sprite(vc, mat, x, y, z, m.size.value, (a << 24) | 0xFFFFFF);
        }
    }
}
