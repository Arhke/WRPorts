package net.johnbrooks.ports;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import net.johnbrooks.ports.listeners.PortUseEvents;
import net.johnbrooks.ports.ports.Port;
import net.johnbrooks.ports.settings.MetricsLite;
import net.johnbrooks.ports.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class Main extends JavaPlugin {
    public static Main plugin;
    public static WorldEditPlugin worldEdit;
    public static Logger logger;

    @Override
    public void onEnable() {
        plugin = this;
        logger = getLogger();
        saveDefaultConfig();
        Settings.load();
        setupMetrics();
        if (bindToWorldEdit())
            getLogger().info("Successfully bound to WorldEdit!");
        else
            getLogger().severe("Could not bind to WorldEdit! Please install WorldEdit before using this plugin. ");

        if (worldEdit != null) {
            getCommand("port").setExecutor(new Commands());
            getServer().getPluginManager().registerEvents(new PortUseEvents(), this);
            Port.loadPorts();
            getLogger().info("Successfully registered events and commands.");
        }
        getLogger().info(getName() + " is now enabled!");
    }

    @Override
    public void onDisable() {
        Port.savePorts();
        getLogger().info(getName() + " is now disabled!");
    }

    private boolean bindToWorldEdit() {
        worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        return worldEdit != null;
    }

    private void setupMetrics() {
        if (Settings.allowMetrics) {
            new MetricsLite(this);
        }
    }
}
