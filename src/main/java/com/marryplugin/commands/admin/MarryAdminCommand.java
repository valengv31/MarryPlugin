package com.marryplugin.commands.admin;

import com.marryplugin.Config;
import com.marryplugin.commands.admin.adminSubcommands.*;
import com.marryplugin.services.MarriageManager;
import com.marryplugin.commands.AbstractCommand;
import com.marryplugin.models.Marriage;
import com.marryplugin.utils.DateTimeFormatter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MarryAdminCommand extends AbstractCommand {
    private final Map<String, AbstractCommand> subCommands = new HashMap<>();

    public MarryAdminCommand(MarriageManager marriageManager, Config config) {
        super(marriageManager, config);

        this.subCommands.put("divorce", new DivorceCommand(marriageManager, config));
        this.subCommands.put("force", new ForceCommand(marriageManager, config));
        this.subCommands.put("list", new ListCommand(marriageManager, config));
        this.subCommands.put("info", new InfoCommand(marriageManager, config));
        this.subCommands.put("history",new HistoryCommand(marriageManager, config));
        this.subCommands.put("reload", new ReloadCommand(marriageManager, config));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("marry.admin")) {
            sender.sendMessage(config.msg("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        AbstractCommand subCommand = subCommands.get(sub);
        if (subCommand == null) {
            sendHelp(sender);
        }
        return subCommand.onCommand(sender, command, label, args.length > 1 ? java.util.Arrays.copyOfRange(args, 1, args.length) : new String[0]);
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
