package dev.herovisuals.render;

import dev.herovisuals.module.Modules;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/** Lightweight world overlays. Container checks are range-limited and require direct line of sight. */
public final class WorldExtrasRenderer {
    private WorldExtrasRenderer() {}
    private static int scanTicker;
    private static java.util.List<BlockPos> visibleContainers = java.util.List.of();

    public static double nearestVisibleContainerDistance() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || visibleContainers.isEmpty()) return -1;
        double best = Double.MAX_VALUE;
        for (BlockPos p : visibleContainers) best = Math.min(best, mc.player.getPos().squaredDistanceTo(Vec3d.ofCenter(p)));
        return best == Double.MAX_VALUE ? -1 : Math.sqrt(best);
    }

    public static void render(MatrixStack matrices, VertexConsumerProvider providers, Vec3d camera, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        VertexConsumer vc = providers.getBuffer(RenderLayer.getDebugQuads());
        Matrix4f mat = matrices.peek().getPositionMatrix();

        if (Modules.BLOCK_OVERLAY.enabled && mc.crosshairTarget instanceof BlockHitResult hit
                && hit.getType() == HitResult.Type.BLOCK) {
            BlockPos p = hit.getBlockPos();
            int col = Modules.BLOCK_OVERLAY.color.get(0, Modules.BLOCK_OVERLAY.alpha.i());
            double x = p.getX() - camera.x, y = p.getY() - camera.y, z = p.getZ() - camera.z;
            // Thin inset faces: deliberately no through-wall rendering layer.
            double e = 0.002;
            Draw.quadRaw(vc, mat, x+e,y+e,z+e,col, x+1-e,y+e,z+e,col, x+1-e,y+1-e,z+e,col, x+e,y+1-e,z+e,col);
            Draw.quadRaw(vc, mat, x+e,y+e,z+1-e,col, x+e,y+1-e,z+1-e,col, x+1-e,y+1-e,z+1-e,col, x+1-e,y+e,z+1-e,col);
            Draw.quadRaw(vc, mat, x+e,y+1-e,z+e,col, x+1-e,y+1-e,z+e,col, x+1-e,y+1-e,z+1-e,col, x+e,y+1-e,z+1-e,col);
            Draw.quadRaw(vc, mat, x+e,y+e,z+e,col, x+e,y+e,z+1-e,col, x+1-e,y+e,z+1-e,col, x+1-e,y+e,z+e,col);
            Draw.quadRaw(vc, mat, x+e,y+e,z+e,col, x+e,y+1-e,z+e,col, x+e,y+1-e,z+1-e,col, x+e,y+e,z+1-e,col);
            Draw.quadRaw(vc, mat, x+1-e,y+e,z+e,col, x+1-e,y+e,z+1-e,col, x+1-e,y+1-e,z+1-e,col, x+1-e,y+1-e,z+e,col);
        }

        if (Modules.JUMP_CIRCLE.enabled && mc.player.isOnGround()) {
            double t = (System.nanoTime() / 1.0E9) % 2.0;
            double radius = Modules.JUMP_CIRCLE.radius.value * (0.88 + 0.12 * Math.sin(t * Math.PI));
            Vec3d center = new Vec3d(mc.player.getX(), mc.player.getY() + 0.03, mc.player.getZ());
            int n = 64;
            Vec3d[] pts = new Vec3d[n + 1]; float[] widths = new float[n + 1]; int[] colors = new int[n + 1];
            for (int i = 0; i <= n; i++) {
                double a = Math.PI * 2 * i / n;
                pts[i] = new Vec3d(center.x + Math.cos(a)*radius-camera.x, center.y-camera.y, center.z + Math.sin(a)*radius-camera.z);
                widths[i] = (float) Modules.JUMP_CIRCLE.width.value;
                colors[i] = Modules.JUMP_CIRCLE.color.get(i/(float)n, Modules.JUMP_CIRCLE.alpha.i());
            }
            Draw.ribbon(vc, mat, pts, widths, colors, false);
        }

        if (Modules.WARDEN_HELPER.enabled) {
            var settings = Modules.WARDEN_HELPER;
            if (scanTicker++ % 5 == 0) {
                java.util.ArrayList<BlockPos> found = new java.util.ArrayList<>();
                int r = settings.radius.i();
                BlockPos origin = mc.player.getBlockPos();
                BlockPos.Mutable pos = new BlockPos.Mutable();
                for (int x = -r; x <= r; x++) for (int y = -Math.min(4, r); y <= Math.min(4, r); y++) for (int z = -r; z <= r; z++) {
                    if (x*x + y*y + z*z > r*r) continue;
                    pos.set(origin.getX()+x, origin.getY()+y, origin.getZ()+z);
                    BlockState state = mc.world.getBlockState(pos);
                    boolean chest = settings.chests.value && (state.isOf(Blocks.CHEST) || state.isOf(Blocks.TRAPPED_CHEST));
                    boolean barrel = settings.barrels.value && state.isOf(Blocks.BARREL);
                    if (!chest && !barrel) continue;
                    Vec3d center = Vec3d.ofCenter(pos);
                    var ray = mc.world.raycast(new net.minecraft.world.RaycastContext(mc.player.getEyePos(), center,
                            net.minecraft.world.RaycastContext.ShapeType.COLLIDER,
                            net.minecraft.world.RaycastContext.FluidHandling.NONE, mc.player));
                    if (ray.getType() == HitResult.Type.BLOCK && ray.getBlockPos().equals(pos)) found.add(pos.toImmutable());
                }
                visibleContainers = found;
            }
            for (BlockPos pos : visibleContainers) {
                if (mc.player.squaredDistanceTo(Vec3d.ofCenter(pos)) > settings.radius.value * settings.radius.value) continue;
                double bx = pos.getX()-camera.x, by = pos.getY()-camera.y, bz = pos.getZ()-camera.z;
                int c = settings.color.get(0.1f, 210);
                double e=0.015;
                Draw.quadRaw(vc,mat,bx+e,by+e,bz+e,c,bx+1-e,by+e,bz+e,c,bx+1-e,by+1-e,bz+e,c,bx+e,by+1-e,bz+e,c);
                Draw.quadRaw(vc,mat,bx+e,by+e,bz+1-e,c,bx+e,by+1-e,bz+1-e,c,bx+1-e,by+1-e,bz+1-e,c,bx+1-e,by+e,bz+1-e,c);
                Draw.quadRaw(vc,mat,bx+e,by+1-e,bz+e,c,bx+1-e,by+1-e,bz+e,c,bx+1-e,by+1-e,bz+1-e,c,bx+e,by+1-e,bz+1-e,c);
                Draw.quadRaw(vc,mat,bx+e,by+e,bz+e,c,bx+e,by+e,bz+1-e,c,bx+1-e,by+e,bz+1-e,c,bx+1-e,by+e,bz+e,c);
            }
        }
    }
}
