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

public class ProposeCommand extends AbstractCommand {
    public ProposeCommand(MarriageManager marriageManager, Config config) {
        super(marriageManager, config);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player proposer = (Player) commandSender;

        if (strings.length == 0) {
            proposer.sendMessage("/marry propose <player>");
            return true;
        }

        String targetName = strings[0];
        Player target = Bukkit.getPlayerExact(targetName);

        if (target == null) {
            proposer.sendMessage(config.msg("player-not-found").replace("%target%", targetName));
            return true;
        }
        if (target.getUniqueId().equals(proposer.getUniqueId())) {
            proposer.sendMessage(config.msg("cannot-marry-self"));
            return true;
        }
        if (manager.isMarried(proposer.getUniqueId())) {
            proposer.sendMessage(config.msg("proposal-expired"));
            return true;
        }
        if (manager.isMarried(target.getUniqueId())) {
            proposer.sendMessage(config.msg("target-already-marry").replace("%target%", target.getName()));
            return true;
        }
        if (manager.hasPendingProposal(proposer.getUniqueId())) {
            proposer.sendMessage(config.msg("proposal-already-sent"));
            return true;
        }
        if (manager.getProposal(target.getUniqueId()) != null) {
            proposer.sendMessage(config.msg("target-has-pending-proposal").replace("%target%", target.getName()));
            return true;
        }

        Proposal proposal = new Proposal(proposer.getUniqueId(), proposer.getName(), target.getUniqueId(),targetName);
        manager.addProposal(proposal);

        proposer.sendMessage(config.msg("proposal-sent").replace("%target%", target.getName()));
        target.sendMessage(config.msg("proposal-received").replace("%proposer%", proposer.getName()));

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(strings[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
