package dev.herovisuals.render;

import dev.herovisuals.module.Modules;
import dev.herovisuals.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/** Small screen-space hit sparks; critical hits create a denser burst. */
public final class StarHitRenderer {
    private StarHitRenderer() {}
    private static final Random RANDOM = new Random();
    private static final List<Star> STARS = new ArrayList<>();
    private static class Star { float x,y,vx,vy,size,rot; long born; int color; }

    public static void onAttack(boolean critical) {
        if (!Modules.HIT_STARS.enabled) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getWindow() == null) return;
        int sw = mc.getWindow().getScaledWidth(), sh = mc.getWindow().getScaledHeight();
        int count = Modules.HIT_STARS.count.i() * (critical ? 2 : 1);
        long now = System.currentTimeMillis();
        for (int i=0;i<count;i++) {
            Star s = new Star();
            s.x = sw/2f + (RANDOM.nextFloat()-0.5f)*28;
            s.y = sh/2f + (RANDOM.nextFloat()-0.5f)*20;
            double a = RANDOM.nextDouble()*Math.PI*2;
            float speed = 18 + RANDOM.nextFloat()*(critical?95:60);
            s.vx=(float)Math.cos(a)*speed; s.vy=(float)Math.sin(a)*speed-18;
            s.size=(float)Modules.HIT_STARS.size.value*(critical?1.15f:1f);
            s.rot=RANDOM.nextFloat()*(float)Math.PI;
            s.born=now; s.color=Modules.HIT_STARS.color.get(RANDOM.nextFloat(),255); STARS.add(s);
        }
        if (STARS.size()>180) STARS.subList(0,STARS.size()-180).clear();
    }

    public static void render(DrawContext ctx) {
        if (!Modules.HIT_STARS.enabled || STARS.isEmpty()) return;
        long now=System.currentTimeMillis();
        float dt=1f/60f;
        Iterator<Star> it=STARS.iterator();
        while(it.hasNext()) {
            Star s=it.next(); long age=now-s.born; long duration=(long)Modules.HIT_STARS.duration.value;
            if(age>=duration){it.remove();continue;}
            float f=age/(float)duration; s.x+=s.vx*dt; s.y+=s.vy*dt; s.vy+=55*dt;
            float size=s.size*(1f-f*0.55f); int alpha=(int)(255*(1f-f)); int col=(alpha<<24)|(s.color&0x00FFFFFF);
            // Four-point star made from two slim diamonds.
            float r=size*(1.4f-f*0.3f), q=Math.max(1,size*0.28f);
            ctx.fill((int)(s.x-q),(int)(s.y-r),(int)(s.x+q),(int)(s.y+r),col);
            ctx.fill((int)(s.x-r),(int)(s.y-q),(int)(s.x+r),(int)(s.y+q),col);
            ctx.fill((int)(s.x-1),(int)(s.y-1),(int)(s.x+2),(int)(s.y+2),0xCCFFFFFF);
        }
    }
}
