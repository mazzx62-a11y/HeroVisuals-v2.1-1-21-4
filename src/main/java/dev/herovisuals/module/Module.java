package dev.herovisuals.module;

import java.util.ArrayList;
import java.util.List;

public class Module {
    public final String name;
    public final String description;
    public final Category category;
    public boolean enabled;
    public boolean expanded;
    public float anim;
    /** GLFW key code; 0 means unbound. */
    public int bindKeyCode = 0;
    public boolean wasBindDown = false;
    public final List<Setting> settings = new ArrayList<>();

    public Module(String name, String description, Category category, boolean enabled) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.enabled = enabled;
        this.anim = enabled ? 1f : 0f;
    }

    public <T extends Setting> T add(T s) { settings.add(s); return s; }

    public void toggle() {
        enabled = !enabled;
        if (enabled) onEnable(); else onDisable();
        ConfigManager.save();
    }

    public void onEnable() {}
    public void onDisable() {}
}
