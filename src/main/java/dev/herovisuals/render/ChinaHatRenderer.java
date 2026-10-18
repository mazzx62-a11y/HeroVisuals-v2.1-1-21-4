package dev.herovisuals.render;

import dev.herovisuals.module.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class ChinaHatRenderer {
    private static final int SEGMENTS = 48;

    public static void render(MatrixStack ms, VertexConsumerProvider consumers, Vec3d cam, float td) {
        Modules.ChinaHatModule m = Modules.CHINA_HAT;
        if (!m.enabled) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity p = mc.player;
        if (p == null) return;
        if (mc.options.getPerspective().isFirstPerson() && !m.firstPerson.value) return;

        double x = MathHelper.lerp(td, p.prevX, p.getX()) - cam.x;
        double y = MathHelper.lerp(td, p.prevY, p.getY()) - cam.y + p.getHeight() - 0.02;
        double z = MathHelper.lerp(td, p.prevZ, p.getZ()) - cam.z;

        float radius = m.size.f();
        float height = m.height.f();
        int a = m.alpha.i();

        ms.push();
        ms.translate(x, y, z);
        Matrix4f mat = ms.peek().getPositionMatrix();
        VertexConsumer vc = consumers.getBuffer(RenderLayer.getDebugQuads());

        for (int i = 0; i < SEGMENTS; i++) {
            float a0 = (float) (i * Math.PI * 2 / SEGMENTS);
            float a1 = (float) ((i + 1) * Math.PI * 2 / SEGMENTS);
            float x0 = MathHelper.cos(a0) * radius, z0 = MathHelper.sin(a0) * radius;
            float x1 = MathHelper.cos(a1) * radius, z1 = MathHelper.sin(a1) * radius;

            int cA = m.color.get(i / (float) SEGMENTS, a);
            int cB = m.color.get((i + 1) / (float) SEGMENTS, a);
            int apex = m.color.get(i / (float) SEGMENTS, Math.min(255, a + 70));

            vc.vertex(mat, 0, height, 0).color(apex);
            vc.vertex(mat, 0, height, 0).color(apex);
            vc.vertex(mat, x1, 0, z1).color(cB);
            vc.vertex(mat, x0, 0, z0).color(cA);

            if (m.outline.value) {
                float k = 0.94f;
                int e0 = m.color.get(i / (float) SEGMENTS, 255);
                int e1 = m.color.get((i + 1) / (float) SEGMENTS, 255);
                vc.vertex(mat, x0, 0, z0).color(e0);
                vc.vertex(mat, x1, 0, z1).color(e1);
                vc.vertex(mat, x1 * k, 0.012f, z1 * k).color(e1);
                vc.vertex(mat, x0 * k, 0.012f, z0 * k).color(e0);
            }
        }
        ms.pop();
    }
}
