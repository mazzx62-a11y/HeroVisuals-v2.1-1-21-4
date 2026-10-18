package dev.herovisuals.mixin;

import dev.herovisuals.module.Modules;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FogShape;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Client-side, visual-only fog profile. Never changes world or server state. */
@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {
    @Inject(method = "applyFog", at = @At("RETURN"), cancellable = true)
    private static void herovisuals$customFog(Camera camera, @Coerce Object fogType, Vector4f color, float viewDistance, boolean thickenFog, float tickDelta, CallbackInfoReturnable<Fog> cir) {
        if (!Modules.FOG.enabled || !(fogType instanceof Enum<?> fogMode) || !fogMode.name().equals("FOG_TERRAIN")) return;
        Fog original = cir.getReturnValue();
        if (original == null) return;
        Modules.FogModule module = Modules.FOG;
        float end = Math.max(8.0f, original.end());
        float start = Math.max(0.0f, original.start());
        switch (module.style.index) {
            case 0 -> { // Soft: gentle transition, preserve the normal far distance.
                start = Math.min(end - 1.0f, end * (0.58f + module.density.f() * 0.002f));
            }
            case 1 -> { // Dense: nearer but still readable.
                end = Math.max(12.0f, Math.min(end, viewDistance * (0.72f - module.density.f() * 0.003f)));
                start = Math.max(2.0f, end * 0.28f);
            }
            case 2 -> { // Clear: push fog transition toward render distance.
                end = Math.max(end, viewDistance * 0.98f);
                start = Math.max(start, end * 0.82f);
            }
        }
        int rgb = module.color.get(0.0f, 255);
        float r = ((rgb >> 16) & 255) / 255.0f;
        float g = ((rgb >> 8) & 255) / 255.0f;
        float b = (rgb & 255) / 255.0f;
        // Keep the biome's natural fog color in Soft mode; tint Dense/Clear gently.
        if (module.style.index == 0) { r = original.red(); g = original.green(); b = original.blue(); }
        cir.setReturnValue(new Fog(start, end, original.shape() == null ? FogShape.SPHERE : original.shape(), r, g, b, original.alpha()));
    }
}
