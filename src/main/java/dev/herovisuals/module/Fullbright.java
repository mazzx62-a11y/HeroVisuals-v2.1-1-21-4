package dev.herovisuals.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;

/** Client-side gamma boost. Does not remove server-side or shader-based Darkness effects. */
public final class Fullbright {
    private Fullbright() {}
    private static Double originalGamma;

    public static void tick(MinecraftClient mc) {
        if (mc == null || mc.options == null) return;
        SimpleOption<Double> gamma = mc.options.getGamma();
        if (Modules.FULLBRIGHT.enabled) {
            if (originalGamma == null) originalGamma = gamma.getValue();
            // Vanilla's option range may clamp this value; keep applying it without changing server state.
            if (gamma.getValue() < 1.0) gamma.setValue(1.0);
        } else restore(mc);
    }

    public static void restore(MinecraftClient mc) {
        if (originalGamma != null && mc != null && mc.options != null) {
            mc.options.getGamma().setValue(originalGamma);
            originalGamma = null;
        }
    }
}
