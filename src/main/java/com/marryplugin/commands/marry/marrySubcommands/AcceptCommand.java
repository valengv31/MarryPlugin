package com.marryplugin.commands.marry.marrySubcommands;

import com.marryplugin.Config;
import com.marryplugin.models.Marriage;
import com.marryplugin.services.MarriageManager;
import com.marryplugin.commands.AbstractCommand;
import com.marryplugin.models.Proposal;
import com.marryplugin.utils.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AcceptCommand extends AbstractCommand {

    public AcceptCommand(MarriageManager marriageManager, Config config) {
        super(marriageManager, config);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player player = (Player) commandSender;
        Proposal proposal = manager.getProposal(player.getUniqueId());
        if (proposal == null) {
            player.sendMessage(config.msg("no-pending-proposal"));
            return true;
        }

        Player proposer = Bukkit.getPlayer(proposal.getProposerId());
        manager.removeProposal(player.getUniqueId());

        if (proposer == null || !proposer.isOnline()) {
            player.sendMessage(config.msg("proposer-offline"));
            return true;
        }
        if (manager.isMarried(proposer.getUniqueId()) || manager.isMarried(player.getUniqueId())) {
            player.sendMessage(config.msg("proposal-expired"));
            return true;
        }

        //---------------------------------------------- //async

        Scheduler.runAsync(() -> {
            boolean success = manager.marry(proposer, player);
            if (!success) {
                Scheduler.runSync(() ->{
                    player.sendMessage(config.msg("marriage-db-error"));
                    proposer.sendMessage(config.msg("marriage-db-error"));
                });
            }else {
                Scheduler.runSync(() -> {
                    player.sendMessage(config.msg("marriage-success").replace("%partner%", proposer.getName()));
                    proposer.sendMessage(config.msg("marriage-success").replace("%partner%", player.getName()));

                    Bukkit.broadcastMessage(config.msg("marriage-broadcast")
                            .replace("%player1%", proposer.getName())
                            .replace("%player2%", player.getName()));
                });
            }
        });

        //----------------------------------------------
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player player = (Player) commandSender;
        Proposal proposal = manager.getProposal(player.getUniqueId());
        if (proposal != null) {
            return List.of(proposal.getProposerName());
        }
        return List.of();
    }
}
