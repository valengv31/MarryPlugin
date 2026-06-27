package com.marryplugin.commands.divorce;

import com.marryplugin.Config;
import com.marryplugin.commands.divorce.divorceSubcommands.ConfirmCommand;
import com.marryplugin.models.Divorce;
import com.marryplugin.services.MarriageManager;
import com.marryplugin.commands.AbstractCommand;
import com.marryplugin.models.Marriage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DivorceCommand extends AbstractCommand {
    private Map<String,AbstractCommand> subCommands = new HashMap<>();

    public DivorceCommand(MarriageManager manager, Config  config) {
        super(manager, config);
        this.subCommands.put("confirm", new ConfirmCommand(manager, config));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo puede usarse en el juego.");
            return true;
        }

        if (!player.hasPermission("marry.use")) {
            player.sendMessage(config.msg("no-permission"));
            return true;
        }

        if (!manager.isMarried(player.getUniqueId())) {
            player.sendMessage(config.msg("not-married"));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("confirm")) {
            return subCommands.get("confirm").onCommand(sender, command, label, args);
        }

        Marriage marriage = manager.getMarriage(player.getUniqueId());
        manager.addDivorce(new Divorce(marriage));

        player.sendMessage(config.msg("divorce-confirm-request")
                .replace("%partner%", marriage.getPartnerName(player.getUniqueId()))
                .replace("%seconds%", "60"));
        return true;
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return List.of("confirm");
    }
}
