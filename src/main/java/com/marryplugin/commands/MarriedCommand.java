package com.marryplugin.commands;

import com.marryplugin.MarriageManager;
import com.marryplugin.MarryPlugin;
import com.marryplugin.model.Marriage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class MarriedCommand implements CommandExecutor {

    private final MarryPlugin plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public MarriedCommand(MarryPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        MarriageManager manager = plugin.getMarriageManager();
        UUID targetId;
        String targetLabel;

        if (args.length > 0) {
            OfflinePlayer off = Bukkit.getOfflinePlayer(args[0]);
            targetId = off.getUniqueId();
            targetLabel = off.getName() != null ? off.getName() : args[0];
        } else {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Indicá un jugador: /married <jugador>");
                return true;
            }
            targetId = player.getUniqueId();
            targetLabel = sender.getName();
        }

        Marriage marriage = manager.getMarriage(targetId);
        if (marriage == null) {
            sender.sendMessage(plugin.msg("status-not-married").replace("%player%", targetLabel));
            return true;
        }

        String partnerName = marriage.getPartnerName(targetId);
        String date = dateFormat.format(new Date(marriage.getMarriedAt()));

        sender.sendMessage(plugin.msg("status-married")
                .replace("%player%", targetLabel)
                .replace("%partner%", partnerName)
                .replace("%date%", date));
        return true;
    }
}
