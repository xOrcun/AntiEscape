package org.xorcun.antiescape.FileManager;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class ConfigFileManager {

    private final JavaPlugin plugin;

    public ConfigFileManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public String getConfigMessage(String key) {
        FileConfiguration config = plugin.getConfig();
        String value = config.getString(key);
        return value != null ? value : "Config value not found: " + key;
    }

    public String getConfigMessageWithColors(String key) {
        String value = getConfigMessage(key);
        return ChatColor.translateAlternateColorCodes('&', value);
    }

    public File getDataFolder() {
        return plugin.getDataFolder();
    }
    public void saveResource(String resourcePath, boolean replace) {
        plugin.saveResource(resourcePath, replace);
    }
}
