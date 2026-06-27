package com.marryplugin.commands.admin.adminSubcommands;

import com.marryplugin.Config;
import com.marryplugin.commands.AbstractCommand;
import com.marryplugin.models.Marriage;
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

public class InfoCommand extends AbstractCommand {
    public InfoCommand(MarriageManager marriageManager, Config config) {
        super(marriageManager, config);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length < 1) {
            commandSender.sendMessage("Uso: /marryadmin info <jugador>");
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(strings[0]);
        Marriage marriage = manager.getMarriage(target.getUniqueId());
        if (marriage == null) {
            commandSender.sendMessage(config.msg("admin-target-not-marry").replace("%target%", strings[0]));
            return true;
        }

        commandSender.sendMessage(config.msg("admin-info")
                .replace("%player1%", marriage.getPlayer1Name())
                .replace("%player2%", marriage.getPlayer2Name())
                .replace("%date%", DateTimeFormatter.format(marriage.getMarriedAt())));
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
