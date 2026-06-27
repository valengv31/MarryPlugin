package com.marryplugin;

import com.marryplugin.commands.DivorceCommand;
import com.marryplugin.commands.MarriedCommand;
import com.marryplugin.commands.MarryAdminCommand;
import com.marryplugin.commands.MarryCommand;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class MarryPlugin extends JavaPlugin {

    private static MarryPlugin instance;
    private MarriageManager marriageManager;
    private MarryPlaceholders placeholders;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        getDataFolder().mkdirs();

        this.marriageManager = new MarriageManager(this);

        getCommand("marry").setExecutor(new MarryCommand(this));
        getCommand("divorce").setExecutor(new DivorceCommand(this));
        getCommand("married").setExecutor(new MarriedCommand(this));
        getCommand("marryadmin").setExecutor(new MarryAdminCommand(this));

        // Limpia propuestas vencidas cada 10 segundos.
        long expireSeconds = getConfig().getLong("proposal-expire-seconds", 60);
        getServer().getScheduler().runTaskTimer(this,
                () -> marriageManager.cleanExpiredProposals(expireSeconds * 1000L),
                20L * 10, 20L * 10);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.placeholders = new MarryPlaceholders(this);
            this.placeholders.register();
            getLogger().info("Integración con PlaceholderAPI activada (%marry_partner%, %marry_status%, %marry_since%).");
        } else {
            getLogger().info("PlaceholderAPI no está instalado. El plugin funciona igual, pero esos placeholders no van a estar disponibles.");
        }

        getLogger().info("MarryPlugin habilitado correctamente.");
    }

    @Override
    public void onDisable() {
        if (placeholders != null) {
            placeholders.unregister();
        }
        if (marriageManager != null) {
            marriageManager.close();
        }
        getLogger().info("MarryPlugin deshabilitado correctamente.");
    }

    public static MarryPlugin getInstance() {
        return instance;
    }

    public MarriageManager getMarriageManager() {
        return marriageManager;
    }

    /**
     * Obtiene un mensaje del config.yml ya traducido (códigos de color '&').
     */
    public String msg(String path) {
        String raw = getConfig().getString("messages." + path, "&cMensaje no configurado: " + path);
        return ChatColor.translateAlternateColorCodes('&', raw);
    }
}
