package com.marryplugin.commands;

import com.marryplugin.Config;
import com.marryplugin.services.MarriageManager;
import com.marryplugin.models.Marriage;
import com.marryplugin.utils.DateTimeFormatter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class MarriedCommand extends AbstractCommand {

    public MarriedCommand(MarriageManager  manager, Config config) {
        super(manager, config);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo puede ser usado por jugadores.");
            return true;
        }

        if (!player.hasPermission("marry.use")) {
            player.sendMessage(config.msg("no-permission"));
            return true;
        }

        UUID targetId;
        String targetLabel;

        if (args.length > 0) {
            OfflinePlayer off = Bukkit.getOfflinePlayer(args[0]);
            if(!off.hasPlayedBefore()){
                player.sendMessage(config.msg("player-not-found").replace("%player%", args[0]));
                return true;
            }
            targetId = off.getUniqueId();
            targetLabel = args[0];
        } else {
            targetId = player.getUniqueId();
            targetLabel = player.getName();
        }

        Marriage marriage = manager.getMarriage(targetId);
        if (marriage == null) {
            sender.sendMessage(config.msg("status-not-married").replace("%player%", targetLabel));
            return true;
        }

        String partnerName = marriage.getPartnerName(targetId);
        String date = DateTimeFormatter.format(marriage.getMarriedAt());

        sender.sendMessage(config.msg("status-married")
                .replace("%player%", targetLabel)
                .replace("%partner%", partnerName)
                .replace("%date%", date));
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
        return null;
    }
}
