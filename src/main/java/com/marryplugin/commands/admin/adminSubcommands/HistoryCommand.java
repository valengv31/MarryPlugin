package com.marryplugin.commands.admin.adminSubcommands;

import com.marryplugin.Config;
import com.marryplugin.commands.AbstractCommand;
import com.marryplugin.services.MarriageManager;
import com.marryplugin.utils.DateTimeFormatter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HistoryCommand extends AbstractCommand {
    public HistoryCommand(MarriageManager marriageManager, Config config) {
        super(marriageManager, config);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length < 1) {
            commandSender.sendMessage("Uso: /marryadmin history <jugador>");
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(strings[0]);
        List<String> history = manager.getHistory(target.getUniqueId(), 10);

        if (history.isEmpty()) {
            commandSender.sendMessage("§7" + strings[0] + " no tiene historial de matrimonios registrado.");
            return true;
        }

        commandSender.sendMessage("§6Historial de " + strings[0] + " (últimos " + history.size() + " eventos):");
        for (String line : history) {
            String[] parts = line.split("\\|");
            String type = parts[0];
            String partner = parts[1];
            long timestamp = Long.parseLong(parts[2]);

            String label = type.equals("MARRY") ? "§aSe casó con" : "§cSe divorció de";
            commandSender.sendMessage("§7- " + label + " §f" + partner + " §7(" + DateTimeFormatter.format(timestamp) + ")");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(strings[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
