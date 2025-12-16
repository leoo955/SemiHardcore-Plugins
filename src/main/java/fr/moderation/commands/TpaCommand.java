package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpaCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public TpaCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check if player is in death world
        if (plugin.getDeathWorldManager().isInDeathWorld(player)) {
            if (!player.hasPermission("moderation.deathdim.bypass")) {
                player.sendMessage(plugin.getMessage("prefix") + "§cVous ne pouvez pas vous téléporter depuis le monde de la mort !");
                return true;
            }
        }
        
        // Check if player is in combat (except if ghost)
        if (!plugin.getDeathWorldManager().isGhost(player.getUniqueId()) && plugin.getCombatManager().isInCombat(player)) {
            player.sendMessage(plugin.getMessage("prefix") + "§cVous ne pouvez pas envoyer de demande de téléportation en combat !");
            return true;
        }
        
        if (!player.hasPermission("moderation.tpa")) {
            player.sendMessage(plugin.getMessage("no_permission"));
            return true;
        }
        
        if (args.length != 1) {
            player.sendMessage(plugin.getMessage("prefix") + "Usage: /tpa <joueur>");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(plugin.getMessage("player_not_found"));
            return true;
        }
        
        if (target.equals(player)) {
            player.sendMessage(plugin.getMessage("prefix") + "§cVous ne pouvez pas vous téléporter à vous-même.");
            return true;
        }
        
        plugin.getTeleportManager().sendTpaRequest(player, target);
        return true;
    }
}
