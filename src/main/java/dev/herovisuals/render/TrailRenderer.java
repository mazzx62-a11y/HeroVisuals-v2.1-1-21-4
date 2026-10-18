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

import java.util.ArrayList;
import java.util.List;

public class TrailRenderer {
    private static final List<Vec3d> POINTS = new ArrayList<>();

    public static void clear() { POINTS.clear(); }

    public static void tick(MinecraftClient mc) {
        Modules.TrailModule m = Modules.TRAIL;
        if (mc.player == null || mc.world == null || !m.enabled) { POINTS.clear(); return; }

        Vec3d pos = mc.player.getPos();
        boolean moved = POINTS.isEmpty() || POINTS.get(POINTS.size() - 1).squaredDistanceTo(pos) > 0.01;
        if (moved) POINTS.add(pos);
        else if (!POINTS.isEmpty()) POINTS.remove(0); // стоим - след плавно сокращается

        int max = m.length.i();
        while (POINTS.size() > max) POINTS.remove(0);
    }

    public static void render(MatrixStack ms, VertexConsumerProvider consumers, Vec3d cam, float td) {
        Modules.TrailModule m = Modules.TRAIL;
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity p = mc.player;
        if (!m.enabled || p == null || POINTS.isEmpty()) return;

        Vec3d cur = new Vec3d(MathHelper.lerp(td, p.prevX, p.getX()),
                MathHelper.lerp(td, p.prevY, p.getY()),
                MathHelper.lerp(td, p.prevZ, p.getZ()));

        List<Vec3d> pts = new ArrayList<>(POINTS);
        pts.add(cur);
        int n = pts.size();
        if (n < 2) return;

        boolean fp = mc.options.getPerspective().isFirstPerson();
        double oy = m.offsetY.value;
        Matrix4f mat = ms.peek().getPositionMatrix();
        VertexConsumer vc = consumers.getBuffer(RenderLayer.getDebugQuads());

        Vec3d[] rel = new Vec3d[n];
        float[] w = new float[n];
        int[] col = new int[n];
        for (int i = 0; i < n; i++) {
            Vec3d q = pts.get(i);
            rel[i] = new Vec3d(q.x - cam.x, q.y + oy - cam.y, q.z - cam.z);
            float f = i / (float) (n - 1); // 0 = хвост, 1 = голова
            w[i] = (float) (m.width.value * (0.15 + 0.85 * f));
            float a = m.alpha.f() * f * f;
            if (fp) { // чтобы не закрывало экран вблизи камеры
                double d = rel[i].length();
                a *= MathHelper.clamp((d - 0.7) / 1.5, 0.0, 1.0);
            }
            col[i] = m.color.get(f, (int) a);
        }

        if (m.style.index == 0) {
            Draw.ribbon(vc, mat, rel, w, col, false);
        } else {
            for (int i = 0; i < n; i += 2) {
                if ((col[i] >>> 24) < 4) continue;
                double h = m.width.value * (0.3 + 0.7 * (i / (double) (n - 1)));
                Draw.cube(vc, mat, rel[i].x, rel[i].y + h, rel[i].z, h, col[i]);
            }
        }
    }
}
