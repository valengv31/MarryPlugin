package com.marryplugin.commands.divorce.divorceSubcommands;

import com.marryplugin.Config;
import com.marryplugin.commands.AbstractCommand;
import com.marryplugin.models.Marriage;
import com.marryplugin.services.MarriageManager;
import com.marryplugin.utils.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class ConfirmCommand extends AbstractCommand {

    public ConfirmCommand(MarriageManager marriageManager, Config config) {
        super(marriageManager, config);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player player =  (Player) commandSender;
        Marriage marriage = manager.getMarriage(player.getUniqueId());
        if (!manager.hasPendingDivorce(player.getUniqueId())) {
            player.sendMessage(config.msg("divorce-confirm-expired"));
            return true;
        }

        String partnerName = marriage.getPartnerName(player.getUniqueId());
        UUID partnerId = marriage.getPartner(player.getUniqueId());


        //---------------------------------------------//async
        Scheduler.runAsync(() -> {
            boolean success = manager.divorce(player.getUniqueId());
            if (!success) {
                Scheduler.runSync(()->{
                    player.sendMessage(config.msg("divorce-db-error"));
                });
            }else{
                Scheduler.runSync(()->{
                    player.sendMessage(config.msg("divorce-success").replace("%partner%", partnerName));

                    Player partner = partnerId != null ? Bukkit.getPlayer(partnerId) : null;
                    if (partner != null) {
                        partner.sendMessage(config.msg("divorce-notice").replace("%partner%", player.getName()));
                    }
                });
            }
        });

        //------------------------------------------------------
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return List.of();
    }
}
