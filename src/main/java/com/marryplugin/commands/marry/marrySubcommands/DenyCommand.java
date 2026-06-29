package com.marryplugin.commands.marry.marrySubcommands;

import com.marryplugin.Config;
import com.marryplugin.services.MarriageManager;
import com.marryplugin.commands.AbstractCommand;
import com.marryplugin.models.Proposal;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DenyCommand extends AbstractCommand {
    public DenyCommand(MarriageManager marriageManager, Config config) {
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
        manager.removeProposal(player.getUniqueId());

        player.sendMessage(config.msg("proposal-denied-self").replace("%proposer%", proposal.getProposerName()));

        Player proposer = Bukkit.getPlayer(proposal.getProposerId());
        if (proposer != null) {
            proposer.sendMessage(config.msg("proposal-denied-other").replace("%target%", player.getName()));
        }
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
