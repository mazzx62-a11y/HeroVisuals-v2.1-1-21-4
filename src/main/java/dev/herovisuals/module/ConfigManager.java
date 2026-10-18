package dev.herovisuals.module;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Small crash-safe user configuration for module states, binds and settings. */
public final class ConfigManager {
    private static final Path FILE = Path.of("config", "herovisuals.properties");
    private ConfigManager() {}

    public static void load() {
        if (!Files.isRegularFile(FILE)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            p.load(in);
            for (int i = 0; i < Modules.ALL.size(); i++) {
                Module m = Modules.ALL.get(i);
                String prefix = "module." + i + ".";
                m.enabled = Boolean.parseBoolean(p.getProperty(prefix + "enabled", Boolean.toString(m.enabled)));
                m.anim = m.enabled ? 1f : 0f;
                m.bindKeyCode = parseInt(p.getProperty(prefix + "bind"), m.bindKeyCode);
                for (int j = 0; j < m.settings.size(); j++) {
                    Setting s = m.settings.get(j);
                    String value = p.getProperty(prefix + "setting." + j);
                    if (value == null) continue;
                    try {
                        if (s instanceof Setting.Bool b) b.value = Boolean.parseBoolean(value);
                        else if (s instanceof Setting.Num n) n.value = Math.max(n.min, Math.min(n.max, Double.parseDouble(value)));
                        else if (s instanceof Setting.Mode mode) mode.index = Math.max(0, Math.min(mode.modes.length - 1, Integer.parseInt(value)));
                    } catch (NumberFormatException ignored) { }
                }
            }
        } catch (IOException ignored) { }
    }

    public static void save() {
        Properties p = new Properties();
        for (int i = 0; i < Modules.ALL.size(); i++) {
            Module m = Modules.ALL.get(i);
            String prefix = "module." + i + ".";
            p.setProperty(prefix + "enabled", Boolean.toString(m.enabled));
            p.setProperty(prefix + "bind", Integer.toString(m.bindKeyCode));
            for (int j = 0; j < m.settings.size(); j++) {
                Setting s = m.settings.get(j);
                String value = s instanceof Setting.Bool b ? Boolean.toString(b.value)
                        : s instanceof Setting.Num n ? Double.toString(n.value)
                        : s instanceof Setting.Mode mode ? Integer.toString(mode.index) : "";
                p.setProperty(prefix + "setting." + j, value);
            }
        }
        try {
            Files.createDirectories(FILE.getParent());
            Path tmp = FILE.resolveSibling(FILE.getFileName() + ".tmp");
            try (OutputStream out = Files.newOutputStream(tmp)) { p.store(out, "HeroVisuals settings"); }
            try { Files.move(tmp, FILE, java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE); }
            catch (IOException ex) { Files.move(tmp, FILE, java.nio.file.StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException ignored) { }
    }

    private static int parseInt(String s, int fallback) {
        try { return s == null ? fallback : Integer.parseInt(s); } catch (NumberFormatException e) { return fallback; }
    }
}
