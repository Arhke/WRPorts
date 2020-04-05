package net.johnbrooks.ports.settings;

import net.johnbrooks.ports.Main;
import org.bukkit.configuration.file.FileConfiguration;

public class Settings {
    public static boolean fireworks, particles, allowMetrics;

    public static void load() {
        FileConfiguration config = Main.plugin.getConfig();
        fireworks = config.getBoolean("Fireworks");
        particles = config.getBoolean("Particles");
        allowMetrics = !config.isBoolean("Allow Metrics") || config.getBoolean("Allow Metrics");
    }
}
