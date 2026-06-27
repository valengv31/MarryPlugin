package com.marryplugin.commands;

import com.marryplugin.MarriageManager;
import com.marryplugin.MarryPlugin;
import com.marryplugin.model.Marriage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DivorceCommand implements CommandExecutor {

    private final MarryPlugin plugin;

    public DivorceCommand(MarryPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo puede usarse en el juego.");
            return true;
        }

        if (!player.hasPermission("marry.use")) {
            player.sendMessage(plugin.msg("no-permission"));
            return true;
        }

        MarriageManager manager = plugin.getMarriageManager();
        Marriage marriage = manager.getMarriage(player.getUniqueId());

        if (marriage == null) {
            player.sendMessage(plugin.msg("not-married"));
            return true;
        }

        long confirmSeconds = plugin.getConfig().getLong("divorce-confirm-seconds", 30);

        if (args.length > 0 && args[0].equalsIgnoreCase("confirm")) {
            if (!manager.hasDivorceConfirmPending(player.getUniqueId(), confirmSeconds * 1000L)) {
                player.sendMessage(plugin.msg("divorce-confirm-expired"));
                return true;
            }
            manager.clearDivorceConfirm(player.getUniqueId());

            String partnerName = marriage.getPartnerName(player.getUniqueId());
            UUID partnerId = marriage.getPartner(player.getUniqueId());
            boolean success = manager.divorce(player.getUniqueId());

            if (!success) {
                player.sendMessage(plugin.msg("divorce-db-error"));
                return true;
            }

            player.sendMessage(plugin.msg("divorce-success").replace("%partner%", partnerName));

            Player partner = partnerId != null ? Bukkit.getPlayer(partnerId) : null;
            if (partner != null) {
                partner.sendMessage(plugin.msg("divorce-notice").replace("%partner%", player.getName()));
            }
            return true;
        }

        manager.requestDivorceConfirm(player.getUniqueId());
        player.sendMessage(plugin.msg("divorce-confirm-request")
                .replace("%partner%", marriage.getPartnerName(player.getUniqueId()))
                .replace("%seconds%", String.valueOf(confirmSeconds)));
        return true;
    }
}
