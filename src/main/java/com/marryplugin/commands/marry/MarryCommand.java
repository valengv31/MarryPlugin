package com.marryplugin.commands.marry;

import com.marryplugin.Config;
import com.marryplugin.services.MarriageManager;
import com.marryplugin.commands.AbstractCommand;
import com.marryplugin.commands.marry.marrySubcommands.AcceptCommand;
import com.marryplugin.commands.marry.marrySubcommands.CancelCommand;
import com.marryplugin.commands.marry.marrySubcommands.DenyCommand;
import com.marryplugin.commands.marry.marrySubcommands.ProposeCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarryCommand extends AbstractCommand {
    private final Map<String, AbstractCommand> subCommands;

    public MarryCommand(MarriageManager marriageManager, Config config) {
        super(marriageManager, config);

        subCommands = new HashMap<>();
        subCommands.put("accept", new AcceptCommand(marriageManager, config));
        subCommands.put("deny", new DenyCommand(marriageManager, config));
        subCommands.put("cancel", new CancelCommand(marriageManager, config));
        subCommands.put("propose", new ProposeCommand(marriageManager, config));
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

        if (args.length == 0) {
            player.sendMessage(config.msg("marry-usage"));
            return true;
        }

        String sub = args[0].toLowerCase();
        AbstractCommand subCommand = subCommands.get(sub);
        if (subCommand == null) {
            player.sendMessage(config.msg("marry-usage"));
            return true;
        }
        return subCommand.onCommand(sender,command,label, args.length > 1 ? java.util.Arrays.copyOfRange(args, 1, args.length) : new String[0]);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 1) {
            return subCommands.keySet().stream().filter(sub -> sub.startsWith(strings[0].toLowerCase())).toList();
        }

        if (strings.length > 1) {
            String sub = strings[0].toLowerCase();
            AbstractCommand subCommand = subCommands.get(sub);
            if (subCommand != null) {
                return subCommand.onTabComplete(commandSender, command, s, java.util.Arrays.copyOfRange(strings, 1, strings.length));
            }
        }
        return null;
    }
}
