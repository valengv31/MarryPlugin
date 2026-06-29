package com.marryplugin.commands.admin.adminSubcommands;

import com.marryplugin.Config;
import com.marryplugin.commands.AbstractCommand;
import com.marryplugin.models.Marriage;
import com.marryplugin.services.MarriageManager;
import com.marryplugin.utils.DateTimeFormatter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ListCommand extends AbstractCommand {
    public ListCommand(MarriageManager marriageManager, Config config) {
        super(marriageManager, config);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<Marriage> all = manager.getAllMarriages();
        if (all.isEmpty()) {
            commandSender.sendMessage(config.msg("admin-list-empty"));
            return true;
        }

        commandSender.sendMessage(config.msg("admin-list-header").replace("%count%", String.valueOf(all.size())));
        for (Marriage m : all) {
            commandSender.sendMessage("§7- §f" + m.getPlayer1Name() + " §7+ §f" + m.getPlayer2Name()
                    + " §7(" + DateTimeFormatter.format(m.getMarriedAt()) + ")");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return List.of();
    }
}
