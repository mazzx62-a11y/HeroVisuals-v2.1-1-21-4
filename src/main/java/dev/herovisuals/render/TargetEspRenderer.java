package dev.herovisuals.render;

import dev.herovisuals.module.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class TargetEspRenderer {
    private static class State { Entity e; Vec3d smooth; }

    private static final State MAIN = new State();
    private static final State SELF = new State();
    private static LivingEntity target;
    private static long targetTime;
    private static long lastNanos = System.nanoTime();

    public static void onAttack(LivingEntity e) {
        target = e;
        targetTime = System.currentTimeMillis();
    }

    public static void render(MatrixStack ms, VertexConsumerProvider consumers, Vec3d cam, float td) {
        Modules.TargetEspModule m = Modules.TARGET_ESP;
        if (!m.enabled) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        long now = System.nanoTime();
        float dt = Math.min((now - lastNanos) / 1.0E9f, 0.1f);
        lastNanos = now;

        LivingEntity e = null;
        if (target != null && target.isAlive() && System.currentTimeMillis() - targetTime < 4000) e = target;
        else if (m.crosshair.value && mc.targetedEntity instanceof LivingEntity le && le != mc.player) e = le;

        VertexConsumer vc = consumers.getBuffer(RenderLayer.getDebugQuads());
        Matrix4f mat = ms.peek().getPositionMatrix();

        if (e != null) draw(MAIN, e, m, vc, mat, cam, td, dt);
        if (m.self.value && !mc.options.getPerspective().isFirstPerson()) {
            draw(SELF, mc.player, m, vc, mat, cam, td, dt);
        }
    }

    private static void draw(State st, LivingEntity e, Modules.TargetEspModule m, VertexConsumer vc, Matrix4f mat,
                             Vec3d cam, float td, float dt) {
        Vec3d pos = new Vec3d(MathHelper.lerp(td, e.prevX, e.getX()),
                MathHelper.lerp(td, e.prevY, e.getY()),
                MathHelper.lerp(td, e.prevZ, e.getZ()));
        if (st.e != e || st.smooth == null) { st.e = e; st.smooth = pos; }
        double k = 1.0 - Math.exp(-dt * 9.0);
        st.smooth = st.smooth.add(pos.subtract(st.smooth).multiply(k));
        Vec3d lag = st.smooth.subtract(pos); // указывает назад, откуда пришёл игрок

        float H = e.getHeight();
        double t = (System.nanoTime() / 1.0E9) * m.speed.value;
        double R = m.radius.value;
        int alpha = m.alpha.i();

        if (m.style.index == 0) {
            int count = m.count.i();
            int n = m.tail.i();
            for (int g = 0; g < count; g++) {
                double base = t + g * Math.PI * 2 / count;
                Vec3d[] pts = new Vec3d[n];
                float[] w = new float[n];
                int[] col = new int[n];
                for (int j = 0; j < n; j++) {
                    double f = j / (double) (n - 1);
                    double th = base - j * 0.11;
                    Vec3d c = st.smooth.add(lag.multiply(f));
                    double ox = Math.cos(th) * R;
                    double oz = Math.sin(th) * R;
                    double oy = H * 0.5 + Math.sin(th * 1.3 + g * 2.1) * H * 0.42;
                    pts[j] = new Vec3d(c.x + ox - cam.x, c.y + oy - cam.y, c.z + oz - cam.z);
                    w[j] = (float) (m.size.value * (1.0 - f * 0.85));
                    col[j] = m.color.get((float) f * 0.5f + g / (float) count, (int) (alpha * (1 - f) * (1 - f)));
                }
                Draw.ribbon(vc, mat, pts, w, col, true);
                // светящаяся «голова»
                Draw.sprite(vc, mat, pts[0].x, pts[0].y, pts[0].z, m.size.value * 1.7,
                        m.color.get(g / (float) count, Math.min(255, alpha)));
            }
        } else {
            double y = H * (0.5 + 0.5 * Math.sin(t * 0.9));
            double dir = Math.signum(Math.cos(t * 0.9));
            int segs = 40;
            double half = Math.max(0.03, m.size.value * 0.6);
            for (int j = 0; j < 7; j++) {
                double yy = y - dir * j * 0.045;
                int a = (int) (alpha * Math.pow(1 - j / 7.0, 1.6));
                for (int s = 0; s < segs; s++) {
                    double a0 = s * Math.PI * 2 / segs, a1 = (s + 1) * Math.PI * 2 / segs;
                    int c0 = m.color.get(s / (float) segs, a);
                    int c1 = m.color.get((s + 1) / (float) segs, a);
                    double cx = st.smooth.x - cam.x, cy = st.smooth.y + yy - cam.y, cz = st.smooth.z - cam.z;
                    double ri = R - half, ro = R + half;
                    Draw.quadRaw(vc, mat,
                            cx + Math.cos(a0) * ri, cy, cz + Math.sin(a0) * ri, c0,
                            cx + Math.cos(a0) * ro, cy, cz + Math.sin(a0) * ro, c0,
                            cx + Math.cos(a1) * ro, cy, cz + Math.sin(a1) * ro, c1,
                            cx + Math.cos(a1) * ri, cy, cz + Math.sin(a1) * ri, c1);
                }
            }
        }
    }
}
