package com.marryplugin.commands.admin.adminSubcommands;

import com.marryplugin.Config;
import com.marryplugin.commands.AbstractCommand;
import com.marryplugin.models.Marriage;
import com.marryplugin.services.MarriageManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class DivorceCommand extends AbstractCommand {
    public DivorceCommand(MarriageManager marriageManager, Config config) {
        super(marriageManager, config);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length < 1) {
            sender.sendMessage("Uso: /marryadmin divorce <jugador>");
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(strings[0]);
        Marriage marriage = manager.getMarriage(target.getUniqueId());
        if (marriage == null) {
            sender.sendMessage(config.msg("admin-target-not-marry").replace("%target%", strings[0]));
            return true;
        }

        String partnerName = marriage.getPartnerName(target.getUniqueId());
        UUID targetId = target.getUniqueId();
        boolean success = manager.divorce(targetId);

        if (!success) {
            sender.sendMessage(config.msg("admin-db-error"));
            return true;
        }

        sender.sendMessage(config.msg("admin-divorce-success")
                .replace("%player1%", strings[0])
                .replace("%player2%", partnerName));


        var p = Bukkit.getPlayer(targetId);
        if (p != null) p.sendMessage(config.msg("admin-divorce-notice"));

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
