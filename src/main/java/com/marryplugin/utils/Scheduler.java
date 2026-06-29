package com.marryplugin.utils;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Scheduler {
    private static BukkitScheduler scheduler;
    private static JavaPlugin plugin;

    public static void init(JavaPlugin plugin) {
        Scheduler.plugin = plugin;
        Scheduler.scheduler = plugin.getServer().getScheduler();
    }

    public static void runAsync(Runnable task) {
        scheduler.runTaskAsynchronously(plugin, task);
    }

    public static void runSync(Runnable task) {
        scheduler.runTask(plugin, task);
    }
}
