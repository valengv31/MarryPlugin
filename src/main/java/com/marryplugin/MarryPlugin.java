package com.marryplugin;

import com.marryplugin.commands.divorce.DivorceCommand;
import com.marryplugin.commands.MarriedCommand;
import com.marryplugin.commands.admin.MarryAdminCommand;
import com.marryplugin.commands.marry.MarryCommand;
import com.marryplugin.integrations.MarryPlaceholders;
import com.marryplugin.services.MarriageManager;
import com.marryplugin.utils.Scheduler;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class MarryPlugin extends JavaPlugin {

    private MarriageManager marriageManager;
    private MarryPlaceholders placeholders;
    private Config config;
    private Logger logger;

    @Override
    public void onEnable() {
        this.logger = getLogger();
        this.config = new Config(this);
        this.marriageManager = new MarriageManager(this,config);
        Scheduler.init(this);

        registerCommands();
        registerIntegrations();
        logger.info("MarryPlugin habilitado correctamente.");
    }

    private void registerCommands(){
        getCommand("marry").setExecutor(new MarryCommand(this.marriageManager,config));
        getCommand("divorce").setExecutor(new DivorceCommand(this.marriageManager,config));
        getCommand("married").setExecutor(new MarriedCommand(this.marriageManager,config));
        getCommand("marryadmin").setExecutor(new MarryAdminCommand(this.marriageManager,config));
    }

    private void registerIntegrations(){
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.placeholders = new MarryPlaceholders(this.marriageManager, this.config);
            this.placeholders.register();
            logger.info("Integración con PlaceholderAPI activada (%marry_partner%, %marry_status%, %marry_since%).");
        } else {
            logger.info("PlaceholderAPI no está instalado. El plugin funciona igual, pero esos placeholders no van a estar disponibles.");
        }
    }

    @Override
    public void onDisable() {
        if (placeholders != null) {
            placeholders.unregister();
        }
        if (marriageManager != null) {
            marriageManager.stop();
        }
        logger.info("MarryPlugin deshabilitado correctamente.");
    }
}
