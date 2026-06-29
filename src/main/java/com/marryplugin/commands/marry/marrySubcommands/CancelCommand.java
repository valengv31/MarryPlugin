package com.marryplugin.commands.marry.marrySubcommands;

import com.marryplugin.Config;
import com.marryplugin.models.Proposal;
import com.marryplugin.services.MarriageManager;
import com.marryplugin.commands.AbstractCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CancelCommand extends AbstractCommand {
    public CancelCommand(MarriageManager marriageManager, Config config) {
        super(marriageManager, config);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player  player = (Player) commandSender;
        if (!manager.hasPendingProposal(player.getUniqueId())) {
            player.sendMessage(config.msg("no-proposal-to-cancel"));
            return true;
        }
        manager.cancelProposals(player.getUniqueId());
        player.sendMessage(config.msg("proposal-cancelled"));
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
