package com.marryplugin.commands;

import com.marryplugin.Config;
import com.marryplugin.services.MarriageManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;

public abstract class AbstractCommand implements CommandExecutor, TabCompleter {
    protected MarriageManager manager;
    protected Config config;

    public AbstractCommand(MarriageManager marriageManager, Config config) {
        this.manager = marriageManager;
        this.config = config;
    }

}
