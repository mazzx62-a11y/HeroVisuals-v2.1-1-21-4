package dev.herovisuals.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.client.option.SimpleOption;

/** Меняет настройки графики на более лёгкие и возвращает исходные при выключении. */
public final class FpsBoost {
    private FpsBoost() {}

    private record Snap(ParticlesMode particles, CloudRenderMode clouds, boolean shadows,
                        int blend, double entDist, GraphicsMode gfx, boolean ao) {}

    private static Snap snap;
    private static String lastKey = "";

    public static void tick(MinecraftClient mc) {
        Modules.FpsBoostModule m = Modules.FPS_BOOST;
        GameOptions o = mc.options;
        if (!m.enabled) {
            restore(mc);
            return;
        }
        if (snap == null) snap = capture(o);
        String key = m.key();
        if (!key.equals(lastKey)) {
            apply(o, m);
            lastKey = key;
        }
    }

    public static void restore(MinecraftClient mc) {
        if (snap == null) return;
        GameOptions o = mc.options;
        set(o.getParticles(), snap.particles());
        set(o.getCloudRenderMode(), snap.clouds());
        set(o.getEntityShadows(), snap.shadows());
        set(o.getBiomeBlendRadius(), snap.blend());
        set(o.getEntityDistanceScaling(), snap.entDist());
        set(o.getGraphicsMode(), snap.gfx());
        set(o.getAo(), snap.ao());
        snap = null;
        lastKey = "";
    }

    private static Snap capture(GameOptions o) {
        return new Snap(o.getParticles().getValue(), o.getCloudRenderMode().getValue(),
                o.getEntityShadows().getValue(), o.getBiomeBlendRadius().getValue(),
                o.getEntityDistanceScaling().getValue(), o.getGraphicsMode().getValue(), o.getAo().getValue());
    }

    private static void apply(GameOptions o, Modules.FpsBoostModule m) {
        set(o.getParticles(), m.particles.value ? ParticlesMode.MINIMAL : snap.particles());
        set(o.getCloudRenderMode(), m.clouds.value ? CloudRenderMode.OFF : snap.clouds());
        set(o.getEntityShadows(), m.shadows.value ? Boolean.FALSE : Boolean.valueOf(snap.shadows()));
        set(o.getBiomeBlendRadius(), m.blend.value ? Integer.valueOf(0) : Integer.valueOf(snap.blend()));
        set(o.getEntityDistanceScaling(), m.entityDist.value >= 0.999 ? Double.valueOf(snap.entDist()) : Double.valueOf(m.entityDist.value));
        set(o.getGraphicsMode(), m.fast.value ? GraphicsMode.FAST : snap.gfx());
        set(o.getAo(), m.noAo.value ? Boolean.FALSE : Boolean.valueOf(snap.ao()));
    }

    private static <T> void set(SimpleOption<T> opt, T v) {
        if (!opt.getValue().equals(v)) opt.setValue(v);
    }
}
