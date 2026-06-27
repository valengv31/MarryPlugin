package com.marryplugin.commands;

import com.marryplugin.MarriageManager;
import com.marryplugin.MarryPlugin;
import com.marryplugin.model.Marriage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MarryAdminCommand implements CommandExecutor {

    private final MarryPlugin plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public MarryAdminCommand(MarryPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("marry.admin")) {
            sender.sendMessage(plugin.msg("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        MarriageManager manager = plugin.getMarriageManager();
        String sub = args[0].toLowerCase();

        switch (sub) {
            case "divorce" -> handleDivorce(sender, args, manager);
            case "force" -> handleForce(sender, args, manager);
            case "list" -> handleList(sender, manager);
            case "info" -> handleInfo(sender, args, manager);
            case "history" -> handleHistory(sender, args, manager);
            case "reload" -> {
                plugin.reloadConfig();
                manager.loadFromDatabase();
                sender.sendMessage(plugin.msg("admin-reload"));
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void handleDivorce(CommandSender sender, String[] args, MarriageManager manager) {
        if (args.length < 2) {
            sender.sendMessage("Uso: /marryadmin divorce <jugador>");
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        Marriage marriage = manager.getMarriage(target.getUniqueId());
        if (marriage == null) {
            sender.sendMessage(plugin.msg("admin-target-not-married").replace("%target%", args[1]));
            return;
        }

        String partnerName = marriage.getPartnerName(target.getUniqueId());
        UUID targetId = target.getUniqueId();
        boolean success = manager.divorce(targetId);

        if (!success) {
            sender.sendMessage(plugin.msg("admin-db-error"));
            return;
        }

        sender.sendMessage(plugin.msg("admin-divorce-success")
                .replace("%player1%", args[1])
                .replace("%player2%", partnerName));

        notifyIfOnline(targetId, plugin.msg("admin-divorce-notice"));
    }

    private void handleForce(CommandSender sender, String[] args, MarriageManager manager) {
        if (args.length < 3) {
            sender.sendMessage("Uso: /marryadmin force <jugador1> <jugador2>");
            return;
        }
        OfflinePlayer p1 = Bukkit.getOfflinePlayer(args[1]);
        OfflinePlayer p2 = Bukkit.getOfflinePlayer(args[2]);

        if (p1.getUniqueId().equals(p2.getUniqueId())) {
            sender.sendMessage(plugin.msg("cannot-marry-self"));
            return;
        }

        // Si alguno ya tenía pareja, se rompe automáticamente ese matrimonio anterior.
        manager.divorce(p1.getUniqueId());
        manager.divorce(p2.getUniqueId());

        boolean success = manager.marry(p1.getUniqueId(), args[1], p2.getUniqueId(), args[2]);

        if (!success) {
            sender.sendMessage(plugin.msg("admin-db-error"));
            return;
        }

        sender.sendMessage(plugin.msg("admin-force-success")
                .replace("%player1%", args[1])
                .replace("%player2%", args[2]));
    }

    private void handleList(CommandSender sender, MarriageManager manager) {
        List<Marriage> all = manager.getAllMarriages();
        if (all.isEmpty()) {
            sender.sendMessage(plugin.msg("admin-list-empty"));
            return;
        }

        sender.sendMessage(plugin.msg("admin-list-header").replace("%count%", String.valueOf(all.size())));
        for (Marriage m : all) {
            sender.sendMessage("§7- §f" + m.getPlayer1Name() + " §7+ §f" + m.getPlayer2Name()
                    + " §7(" + dateFormat.format(new Date(m.getMarriedAt())) + ")");
        }
    }

    private void handleInfo(CommandSender sender, String[] args, MarriageManager manager) {
        if (args.length < 2) {
            sender.sendMessage("Uso: /marryadmin info <jugador>");
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        Marriage marriage = manager.getMarriage(target.getUniqueId());
        if (marriage == null) {
            sender.sendMessage(plugin.msg("admin-target-not-married").replace("%target%", args[1]));
            return;
        }

        sender.sendMessage(plugin.msg("admin-info")
                .replace("%player1%", marriage.getPlayer1Name())
                .replace("%player2%", marriage.getPlayer2Name())
                .replace("%date%", dateFormat.format(new Date(marriage.getMarriedAt()))));
    }

    private void handleHistory(CommandSender sender, String[] args, MarriageManager manager) {
        if (args.length < 2) {
            sender.sendMessage("Uso: /marryadmin history <jugador>");
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        List<String> history = manager.getHistory(target.getUniqueId(), 10);

        if (history.isEmpty()) {
            sender.sendMessage("§7" + args[1] + " no tiene historial de matrimonios registrado.");
            return;
        }

        sender.sendMessage("§6Historial de " + args[1] + " (últimos " + history.size() + " eventos):");
        for (String line : history) {
            String[] parts = line.split("\\|");
            String type = parts[0];
            String partner = parts[1];
            long timestamp = Long.parseLong(parts[2]);

            String label = type.equals("MARRY") ? "§aSe casó con" : "§cSe divorció de";
            sender.sendMessage("§7- " + label + " §f" + partner + " §7(" + dateFormat.format(new Date(timestamp)) + ")");
        }
    }

    private void notifyIfOnline(UUID id, String message) {
        var p = Bukkit.getPlayer(id);
        if (p != null) p.sendMessage(message);
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6--- MarryPlugin Admin ---");
        sender.sendMessage("§e/marryadmin divorce <jugador> §7- Separa a un jugador de su pareja");
        sender.sendMessage("§e/marryadmin force <j1> <j2> §7- Casa a dos jugadores a la fuerza");
        sender.sendMessage("§e/marryadmin list §7- Lista todos los matrimonios registrados");
        sender.sendMessage("§e/marryadmin info <jugador> §7- Muestra info del matrimonio de un jugador");
        sender.sendMessage("§e/marryadmin history <jugador> §7- Muestra el historial completo (casamientos y divorcios)");
        sender.sendMessage("§e/marryadmin reload §7- Recarga la configuración");
    }
}
