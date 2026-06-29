package com.marryplugin.commands.admin.adminSubcommands;

import com.marryplugin.Config;
import com.marryplugin.commands.AbstractCommand;
import com.marryplugin.services.MarriageManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ForceCommand extends AbstractCommand {
    public ForceCommand(MarriageManager marriageManager, Config config) {
        super(marriageManager, config);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length < 2) {
            commandSender.sendMessage("Uso: /marryadmin force <jugador1> <jugador2>");
            return true;
        }
        OfflinePlayer p1 = Bukkit.getOfflinePlayer(strings[0]);
        OfflinePlayer p2 = Bukkit.getOfflinePlayer(strings[1]);

        if (p1.getUniqueId().equals(p2.getUniqueId())) {
            commandSender.sendMessage(config.msg("cannot-marry-self"));
            return true;
        }

        // Si alguno ya tenía pareja, se rompe automáticamente ese matrimonio anterior.
        manager.divorce(p1.getUniqueId());
        manager.divorce(p2.getUniqueId());

        boolean success = manager.marry(p1, p2);

        if (!success) {
            commandSender.sendMessage(config.msg("admin-db-error"));
            return true;
        }

        commandSender.sendMessage(config.msg("admin-force-success")
                .replace("%player1%", strings[0])
                .replace("%player2%", strings[1]));

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 1 || strings.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(strings[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
