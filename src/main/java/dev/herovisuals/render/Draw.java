package dev.herovisuals.render;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/** Хелперы 3D-рисования. Все координаты относительно камеры. */
public final class Draw {
    private Draw() {}

    private static void v(VertexConsumer vc, Matrix4f m, Vec3d p, int c) {
        vc.vertex(m, (float) p.x, (float) p.y, (float) p.z).color(c);
    }

    private static void v(VertexConsumer vc, Matrix4f m, double x, double y, double z, int c) {
        vc.vertex(m, (float) x, (float) y, (float) z).color(c);
    }

    /** Непрерывная лента с общими вершинами между сегментами (без разрывов). */
    public static void ribbon(VertexConsumer vc, Matrix4f m, Vec3d[] p, float[] w, int[] col, boolean billboard) {
        int n = p.length;
        if (n < 2) return;
        Vec3d[] l = new Vec3d[n];
        Vec3d[] r = new Vec3d[n];
        Vec3d lastPerp = null;
        for (int i = 0; i < n; i++) {
            Vec3d prev = p[Math.max(0, i - 1)];
            Vec3d next = p[Math.min(n - 1, i + 1)];
            Vec3d t = next.subtract(prev);
            Vec3d ref;
            if (billboard) {
                ref = p[i];
            } else {
                t = new Vec3d(t.x, 0, t.z);
                ref = new Vec3d(0, 1, 0);
            }
            Vec3d perp = t.crossProduct(ref);
            if (perp.lengthSquared() < 1.0E-10) {
                perp = lastPerp != null ? lastPerp : new Vec3d(1, 0, 0);
            } else {
                perp = perp.normalize();
            }
            lastPerp = perp;
            l[i] = p[i].add(perp.multiply(w[i]));
            r[i] = p[i].subtract(perp.multiply(w[i]));
        }
        for (int i = 0; i < n - 1; i++) {
            if ((col[i] >>> 24) == 0 && (col[i + 1] >>> 24) == 0) continue;
            v(vc, m, l[i], col[i]);
            v(vc, m, r[i], col[i]);
            v(vc, m, r[i + 1], col[i + 1]);
            v(vc, m, l[i + 1], col[i + 1]);
        }
    }

    /** Маленький квад, всегда повёрнутый к камере (камера в точке 0,0,0). */
    public static void sprite(VertexConsumer vc, Matrix4f m, double x, double y, double z, double s, int color) {
        double len = Math.sqrt(x * x + z * z);
        double rx, rz;
        if (len < 1.0E-6) { rx = 1; rz = 0; } else { rx = z / len; rz = -x / len; }
        double ux = y * rz;
        double uy = z * rx - x * rz;
        double uz = -y * rx;
        double ul = Math.sqrt(ux * ux + uy * uy + uz * uz);
        if (ul < 1.0E-9) { ux = 0; uy = 1; uz = 0; ul = 1; }
        ux /= ul; uy /= ul; uz /= ul;
        rx *= s; rz *= s; ux *= s; uy *= s; uz *= s;
        v(vc, m, x - rx - ux, y - uy, z - rz - uz, color);
        v(vc, m, x + rx - ux, y - uy, z + rz - uz, color);
        v(vc, m, x + rx + ux, y + uy, z + rz + uz, color);
        v(vc, m, x - rx + ux, y + uy, z - rz + uz, color);
    }

    public static void cube(VertexConsumer vc, Matrix4f m, double cx, double cy, double cz, double h, int c) {
        double x0 = cx - h, x1 = cx + h, y0 = cy - h, y1 = cy + h, z0 = cz - h, z1 = cz + h;
        // низ / верх
        v(vc, m, x0, y0, z0, c); v(vc, m, x1, y0, z0, c); v(vc, m, x1, y0, z1, c); v(vc, m, x0, y0, z1, c);
        v(vc, m, x0, y1, z0, c); v(vc, m, x0, y1, z1, c); v(vc, m, x1, y1, z1, c); v(vc, m, x1, y1, z0, c);
        // север / юг
        v(vc, m, x0, y0, z0, c); v(vc, m, x0, y1, z0, c); v(vc, m, x1, y1, z0, c); v(vc, m, x1, y0, z0, c);
        v(vc, m, x0, y0, z1, c); v(vc, m, x1, y0, z1, c); v(vc, m, x1, y1, z1, c); v(vc, m, x0, y1, z1, c);
        // запад / восток
        v(vc, m, x0, y0, z0, c); v(vc, m, x0, y0, z1, c); v(vc, m, x0, y1, z1, c); v(vc, m, x0, y1, z0, c);
        v(vc, m, x1, y0, z0, c); v(vc, m, x1, y1, z0, c); v(vc, m, x1, y1, z1, c); v(vc, m, x1, y0, z1, c);
    }

    public static void quadRaw(VertexConsumer vc, Matrix4f m, double x1, double y1, double z1, int c1,
                               double x2, double y2, double z2, int c2,
                               double x3, double y3, double z3, int c3,
                               double x4, double y4, double z4, int c4) {
        v(vc, m, x1, y1, z1, c1);
        v(vc, m, x2, y2, z2, c2);
        v(vc, m, x3, y3, z3, c3);
        v(vc, m, x4, y4, z4, c4);
    }
}
