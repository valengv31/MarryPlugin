package com.marryplugin.commands;

import com.marryplugin.MarriageManager;
import com.marryplugin.MarryPlugin;
import com.marryplugin.model.Proposal;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MarryCommand implements CommandExecutor {

    private final MarryPlugin plugin;

    public MarryCommand(MarryPlugin plugin) {
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

        if (args.length == 0) {
            player.sendMessage(plugin.msg("marry-usage"));
            return true;
        }

        MarriageManager manager = plugin.getMarriageManager();
        String sub = args[0].toLowerCase();

        switch (sub) {
            case "accept" -> handleAccept(player, manager);
            case "deny" -> handleDeny(player, manager);
            case "cancel" -> handleCancel(player, manager);
            default -> handlePropose(player, args[0], manager);
        }
        return true;
    }

    private void handlePropose(Player proposer, String targetName, MarriageManager manager) {
        Player target = Bukkit.getPlayerExact(targetName);

        if (target == null) {
            proposer.sendMessage(plugin.msg("player-not-found").replace("%target%", targetName));
            return;
        }
        if (target.getUniqueId().equals(proposer.getUniqueId())) {
            proposer.sendMessage(plugin.msg("cannot-marry-self"));
            return;
        }
        if (manager.isMarried(proposer.getUniqueId())) {
            proposer.sendMessage(plugin.msg("already-married-self"));
            return;
        }
        if (manager.isMarried(target.getUniqueId())) {
            proposer.sendMessage(plugin.msg("target-already-married").replace("%target%", target.getName()));
            return;
        }
        if (manager.hasPendingProposalFrom(proposer.getUniqueId())) {
            proposer.sendMessage(plugin.msg("proposal-already-sent"));
            return;
        }
        if (manager.getProposal(target.getUniqueId()) != null) {
            proposer.sendMessage(plugin.msg("target-has-pending-proposal").replace("%target%", target.getName()));
            return;
        }

        Proposal proposal = new Proposal(proposer.getUniqueId(), proposer.getName(),
                target.getUniqueId(), System.currentTimeMillis());
        manager.addProposal(proposal);

        proposer.sendMessage(plugin.msg("proposal-sent").replace("%target%", target.getName()));
        target.sendMessage(plugin.msg("proposal-received").replace("%proposer%", proposer.getName()));
    }

    private void handleAccept(Player player, MarriageManager manager) {
        Proposal proposal = manager.getProposal(player.getUniqueId());
        if (proposal == null) {
            player.sendMessage(plugin.msg("no-pending-proposal"));
            return;
        }

        Player proposer = Bukkit.getPlayer(proposal.getProposerId());
        manager.removeProposal(player.getUniqueId());

        if (proposer == null || !proposer.isOnline()) {
            player.sendMessage(plugin.msg("proposer-offline"));
            return;
        }
        if (manager.isMarried(proposer.getUniqueId()) || manager.isMarried(player.getUniqueId())) {
            player.sendMessage(plugin.msg("proposal-expired"));
            return;
        }

        boolean success = manager.marry(proposer, player);
        if (!success) {
            player.sendMessage(plugin.msg("marriage-db-error"));
            proposer.sendMessage(plugin.msg("marriage-db-error"));
            return;
        }

        player.sendMessage(plugin.msg("marriage-success").replace("%partner%", proposer.getName()));
        proposer.sendMessage(plugin.msg("marriage-success").replace("%partner%", player.getName()));

        Bukkit.broadcastMessage(plugin.msg("marriage-broadcast")
                .replace("%player1%", proposer.getName())
                .replace("%player2%", player.getName()));
    }

    private void handleDeny(Player player, MarriageManager manager) {
        Proposal proposal = manager.getProposal(player.getUniqueId());
        if (proposal == null) {
            player.sendMessage(plugin.msg("no-pending-proposal"));
            return;
        }
        manager.removeProposal(player.getUniqueId());

        player.sendMessage(plugin.msg("proposal-denied-self").replace("%proposer%", proposal.getProposerName()));

        Player proposer = Bukkit.getPlayer(proposal.getProposerId());
        if (proposer != null) {
            proposer.sendMessage(plugin.msg("proposal-denied-other").replace("%target%", player.getName()));
        }
    }

    private void handleCancel(Player player, MarriageManager manager) {
        if (!manager.hasPendingProposalFrom(player.getUniqueId())) {
            player.sendMessage(plugin.msg("no-proposal-to-cancel"));
            return;
        }
        manager.cancelProposalsFrom(player.getUniqueId());
        player.sendMessage(plugin.msg("proposal-cancelled"));
    }
}
