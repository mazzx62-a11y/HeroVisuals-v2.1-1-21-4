package dev.herovisuals.module;

import dev.herovisuals.render.TrailRenderer;
import dev.herovisuals.util.Fonts;
import dev.herovisuals.render.StarHitRenderer;

import java.util.ArrayList;
import java.util.List;

public final class Modules {
    private Modules() {}

    // ---------------- RENDER ----------------
    public static class ChinaHatModule extends Module {
        public final ColorModes color = new ColorModes(this, 0xFF66CC);
        public final Setting.Num size = add(new Setting.Num("Size", 0.3, 1.0, 0.01, 0.62));
        public final Setting.Num height = add(new Setting.Num("Height", 0.1, 0.7, 0.01, 0.30));
        public final Setting.Num alpha = add(new Setting.Num("Alpha", 20, 255, 1, 120));
        public final Setting.Bool outline = add(new Setting.Bool("Outline", true));
        public final Setting.Bool firstPerson = add(new Setting.Bool("First person", false));
        public ChinaHatModule() { super("China Hat", "Китайская шляпа над головой", Category.RENDER, true); }
    }

    public static class TrailModule extends Module {
        public final Setting.Mode style = add(new Setting.Mode("Style", "Ribbon", "Cubes"));
        public final ColorModes color = new ColorModes(this, 0x8B5CF6);
        public final Setting.Num length = add(new Setting.Num("Length", 10, 120, 1, 50));
        public final Setting.Num width = add(new Setting.Num("Width", 0.04, 0.4, 0.01, 0.12));
        public final Setting.Num alpha = add(new Setting.Num("Alpha", 30, 255, 1, 190));
        public final Setting.Num offsetY = add(new Setting.Num("Offset Y", 0.0, 1.8, 0.05, 0.1));
        public TrailModule() { super("Trail", "Плавный след за игроком", Category.RENDER, true); }
        @Override public void onDisable() { TrailRenderer.clear(); }
    }

    public static class TargetEspModule extends Module {
        public final Setting.Mode style = add(new Setting.Mode("Style", "Ghost", "Ring"));
        public final ColorModes color = new ColorModes(this, 0x8B5CF6);
        public final Setting.Num count = add(new Setting.Num("Ghosts", 1, 6, 1, 3));
        public final Setting.Num radius = add(new Setting.Num("Radius", 0.4, 1.6, 0.05, 0.8));
        public final Setting.Num tail = add(new Setting.Num("Tail", 8, 60, 1, 28));
        public final Setting.Num speed = add(new Setting.Num("Speed", 0.5, 6, 0.1, 2.2));
        public final Setting.Num size = add(new Setting.Num("Size", 0.03, 0.25, 0.01, 0.09));
        public final Setting.Num alpha = add(new Setting.Num("Alpha", 30, 255, 1, 220));
        public final Setting.Bool self = add(new Setting.Bool("Around me", false));
        public final Setting.Bool crosshair = add(new Setting.Bool("Crosshair target", true));
        public TargetEspModule() { super("Target ESP", "Ghost / кольцо вокруг цели", Category.RENDER, true); }
    }

    // ---------------- WORLD ----------------
    public static class SnowModule extends Module {
        public final Setting.Num count = add(new Setting.Num("Count", 100, 2000, 50, 600));
        public final Setting.Num speed = add(new Setting.Num("Speed", 0.5, 6, 0.1, 1.6));
        public final Setting.Num size = add(new Setting.Num("Size", 0.02, 0.15, 0.01, 0.05));
        public final Setting.Num wind = add(new Setting.Num("Wind", -1, 1, 0.1, 0.2));
        public final Setting.Num range = add(new Setting.Num("Range", 6, 24, 1, 14));
        public final Setting.Bool outdoors = add(new Setting.Bool("Only outdoors", true));
        public SnowModule() { super("Snow", "Снег вокруг игрока", Category.WORLD, false); }
    }

    public static class SkyModule extends Module {
        public final Setting.Mode style = add(new Setting.Mode("Sky style", "Gradient", "Galaxy"));
        public final Setting.Num stars = add(new Setting.Num("Galaxy stars", 40, 360, 20, 180));
        public final Setting.Num starSize = add(new Setting.Num("Star size", 0.02, 0.12, 0.01, 0.05));
        public final ColorValue color;
        public final Setting.Num alpha = add(new Setting.Num("Opacity", 0, 255, 1, 140));
        public final Setting.Bool gradient = add(new Setting.Bool("Gradient", true));
        public final ColorValue horizon;
        public SkyModule() {
            super("Sky", "Gradient sky or galaxy dome", Category.WORLD, false);
            color = new ColorValue(this, "Sky", 0x7B4DFF, () -> true);
            horizon = new ColorValue(this, "Horizon", 0xFF8AD8, () -> gradient.value);
        }
    }

    // ---------------- HUD ----------------
    public static class HudModule extends Module {
        public int x, y, w, h;
        public boolean placed;
        public HudModule(String n, String d) { super(n, d, Category.HUD, true); }
    }

    public static class WatermarkModule extends HudModule {
        public final Setting.Bool fps = add(new Setting.Bool("Show FPS", true));
        public WatermarkModule() { super("Watermark", "Название клиента и FPS"); }
    }

    public static class ModuleListModule extends HudModule {
        public final ColorModes color = new ColorModes(this, 0x8B5CF6);
        public final Setting.Bool background = add(new Setting.Bool("Background", true));
        public ModuleListModule() { super("Module List", "Список включённых модулей"); }
    }

    public static class InfoModule extends HudModule {
        public final Setting.Bool coords = add(new Setting.Bool("Coordinates", true));
        public final Setting.Bool speed = add(new Setting.Bool("Speed", true));
        public InfoModule() { super("Info", "Координаты и скорость"); }
    }

    // ---------------- CLIENT ----------------
    public static class InterfaceModule extends Module {
        public final Setting.Mode font = add(new Setting.Mode("Font", Fonts.NAMES));
        public final Setting.Bool shadow = add(new Setting.Bool("Text shadow", true));
        public final Setting.Bool menu = add(new Setting.Bool("Dark menu", true));
        public final Setting.Bool menuAll = add(new Setting.Bool("Style all menus", true));
        public final Setting.Num dots = add(new Setting.Num("GUI dots", 0, 300, 10, 140));
        public InterfaceModule() { super("Interface", "Шрифт, меню, GUI", Category.CLIENT, true); }
    }

    public static class FpsBoostModule extends Module {
        public final Setting.Bool particles = add(new Setting.Bool("Minimal particles", true));
        public final Setting.Bool clouds = add(new Setting.Bool("Clouds off", true));
        public final Setting.Bool shadows = add(new Setting.Bool("Entity shadows off", true));
        public final Setting.Bool fast = add(new Setting.Bool("Fast graphics", false));
        public final Setting.Bool noAo = add(new Setting.Bool("Smooth lighting off", false));
        public final Setting.Bool blend = add(new Setting.Bool("Biome blend off", false));
        public final Setting.Num entityDist = add(new Setting.Num("Entity distance", 0.5, 1.0, 0.05, 0.7));
        public FpsBoostModule() { super("FPS Boost", "Оптимизация настроек графики", Category.CLIENT, false); }

        public String key() {
            return enabled + "|" + particles.value + clouds.value + shadows.value + fast.value + noAo.value + blend.value + entityDist.value;
        }
    }


    // ---------------- WORLD HELPERS ----------------
    public static class WardenHelperModule extends Module {
        public final Setting.Num radius = add(new Setting.Num("Radius", 2, 12, 1, 6));
        public final Setting.Bool chests = add(new Setting.Bool("Chests", true));
        public final Setting.Bool barrels = add(new Setting.Bool("Barrels", false));
        public final Setting.Bool showDistance = add(new Setting.Bool("Show distance", true));
        public final Setting.Bool showChestTimer = add(new Setting.Bool("Show server chest timer", true));
        public final ColorModes color = new ColorModes(this, 0xE7B85C);
        public WardenHelperModule() { super("Warden Helper", "Visible containers in range; no through-wall highlights", Category.WORLD, false); }
    }

    public static class JumpCircleModule extends Module {
        public final Setting.Num radius = add(new Setting.Num("Radius", 0.3, 2.5, 0.05, 0.8));
        public final Setting.Num width = add(new Setting.Num("Width", 0.01, 0.12, 0.005, 0.035));
        public final Setting.Num alpha = add(new Setting.Num("Opacity", 30, 255, 1, 210));
        public final ColorModes color = new ColorModes(this, 0x8B5CF6);
        public JumpCircleModule() { super("Jump Circle", "Animated circle under the player", Category.RENDER, false); }
    }

    public static class BlockOverlayModule extends Module {
        public final Setting.Num alpha = add(new Setting.Num("Opacity", 30, 255, 1, 180));
        public final ColorModes color = new ColorModes(this, 0x8B5CF6);
        public BlockOverlayModule() { super("Block Overlay", "Outline the block currently targeted", Category.RENDER, true); }
    }

    public static class FullbrightModule extends Module {
        public FullbrightModule() { super("Fullbright", "Raise gamma; shader darkness may still apply", Category.WORLD, false); }
    }

    public static class StreamerModeModule extends Module {
        public final Setting.Bool hideCoords = add(new Setting.Bool("Hide coordinates", true));
        public final Setting.Bool hideServer = add(new Setting.Bool("Hide server address/name", true));
        public final Setting.Bool hideNamesInHud = add(new Setting.Bool("Hide names in client HUD", true));
        public StreamerModeModule() { super("Streamer Mode", "Hide sensitive info in HeroVisuals overlays", Category.CLIENT, false); }
    }

    public static class ExtraInfoModule extends HudModule {
        public final Setting.Bool ping = add(new Setting.Bool("Show ping", true));
        public final Setting.Bool server = add(new Setting.Bool("Show server", true));
       public ExtraInfoModule() { super("Server Info", "Ping and current server"); }
    }

    public static class HitStarsModule extends Module {
        public final Setting.Num count = add(new Setting.Num("Stars per hit", 2, 18, 1, 7));
        public final Setting.Num size = add(new Setting.Num("Star size", 2, 12, 1, 5));
        public final Setting.Num duration = add(new Setting.Num("Duration ms", 150, 1200, 50, 650));
        public final ColorModes color = new ColorModes(this, 0xD8C7FF);
        public HitStarsModule() { super("Hit Stars", "Screen-space stars; extra particles on critical hits", Category.COMBAT, false); }
    }

    public static class TargetHudModule extends Module {
        public final Setting.Bool showHealth = add(new Setting.Bool("Health bar", true));
        public final Setting.Bool showDistance = add(new Setting.Bool("Distance", true));
        public final Setting.Num width = add(new Setting.Num("Width", 120, 240, 5, 150));
        public final Setting.Num fade = add(new Setting.Num("Fade ms", 100, 1200, 50, 300));
        public final ColorModes color = new ColorModes(this, 0x8B5CF6);
        public TargetHudModule() { super("Target HUD", "Animated target card", Category.HUD, true); }
    }

    public static class FogModule extends Module {
        public final Setting.Mode style = add(new Setting.Mode("Fog style", "Soft", "Dense", "Clear"));
        public final Setting.Num density = add(new Setting.Num("Density", 0, 100, 5, 35));
        public final ColorModes color = new ColorModes(this, 0x19142A);
        public FogModule() { super("Fog", "Fog profile settings (render hook required for custom fog)", Category.WORLD, false); }
    }

    public static class HandViewModule extends Module {
        public final Setting.Num x = add(new Setting.Num("Horizontal", -100, 100, 5, 0));
        public final Setting.Num y = add(new Setting.Num("Vertical", -100, 100, 5, 0));
        public final Setting.Num scale = add(new Setting.Num("Scale %", 50, 150, 5, 100));
        public final Setting.Mode side = add(new Setting.Mode("Hand side", "Vanilla", "Left", "Right"));
        public HandViewModule() { super("Hand View", "First-person hand and item placement settings", Category.RENDER, false); }
    }

    public static final ChinaHatModule CHINA_HAT = new ChinaHatModule();
    public static final TrailModule TRAIL = new TrailModule();
    public static final TargetEspModule TARGET_ESP = new TargetEspModule();
    public static final SnowModule SNOW = new SnowModule();
    public static final SkyModule SKY = new SkyModule();
    public static final WatermarkModule WATERMARK = new WatermarkModule();
    public static final ModuleListModule MODULE_LIST = new ModuleListModule();
    public static final InfoModule INFO = new InfoModule();
    public static final InterfaceModule INTERFACE = new InterfaceModule();
    public static final FpsBoostModule FPS_BOOST = new FpsBoostModule();
    public static final WardenHelperModule WARDEN_HELPER = new WardenHelperModule();
    public static final JumpCircleModule JUMP_CIRCLE = new JumpCircleModule();
    public static final BlockOverlayModule BLOCK_OVERLAY = new BlockOverlayModule();
    public static final FullbrightModule FULLBRIGHT = new FullbrightModule();
    public static final StreamerModeModule STREAMER_MODE = new StreamerModeModule();
    public static final ExtraInfoModule EXTRA_INFO = new ExtraInfoModule();
    public static final HitStarsModule HIT_STARS = new HitStarsModule();
    public static final TargetHudModule TARGET_HUD = new TargetHudModule();
    public static final FogModule FOG = new FogModule();
    public static final HandViewModule HAND_VIEW = new HandViewModule();

    public static final List<Module> ALL = List.of(CHINA_HAT, TRAIL, TARGET_ESP, SNOW, SKY,
            WATERMARK, MODULE_LIST, INFO, INTERFACE, FPS_BOOST, WARDEN_HELPER, JUMP_CIRCLE, BLOCK_OVERLAY, FULLBRIGHT, STREAMER_MODE, EXTRA_INFO, HIT_STARS, TARGET_HUD, FOG, HAND_VIEW);

    public static List<Module> byCategory(Category c) {
        List<Module> out = new ArrayList<>();
        for (Module m : ALL) if (m.category == c) out.add(m);
        return out;
    }
}
