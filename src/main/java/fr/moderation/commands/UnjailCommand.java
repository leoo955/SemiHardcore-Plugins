package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnjailCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public UnjailCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("moderation.jail")) {
            sender.sendMessage(plugin.getMessage("no_permission"));
            return true;
        }
        
        // /unjail <joueur>
        if (args.length < 1) {
            sender.sendMessage(plugin.getMessage("prefix") + "§cUsage: /unjail <joueur>");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(plugin.getMessage("player_not_found"));
            return true;
        }
        
        if (!plugin.getJailManager().isJailed(target)) {
            sender.sendMessage(plugin.getMessage("prefix") + "§cCe joueur n'est pas en prison.");
            return true;
        }
        
        // Free the player
        plugin.getJailManager().unjailPlayer(target);
        
        // Message au staff
        sender.sendMessage(plugin.getMessage("prefix") + "§aVous avez libéré §e" + target.getName() + " §ade prison.");
        
        // Broadcast aux admins
        String broadcastMessage = plugin.getMessage("prefix") + "§e" + sender.getName() + 
                                 " §aa libéré §e" + target.getName() + " §ade prison.";
        
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("moderation.jail.notify") && !online.equals(sender)) {
                online.sendMessage(broadcastMessage);
            }
        }
        
        return true;
    }
}
