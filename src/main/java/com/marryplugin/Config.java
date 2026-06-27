package com.marryplugin;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Config {
    private FileConfiguration config;
    private JavaPlugin plugin;

    public Config(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        plugin.getDataFolder().mkdirs();
        this.config = plugin.getConfig();
    }

    public long getLong(String key) {
        return config.getLong(key, 60);
    }

    public String getString(String key, String defaultValue) {
        return config.getString(key, defaultValue);
    }

    public String msg(String path) {
        String raw = config.getString("messages." + path, "&cMensaje no configurado: " + path);
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    public void reloadConfig() {
        this.plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
}
