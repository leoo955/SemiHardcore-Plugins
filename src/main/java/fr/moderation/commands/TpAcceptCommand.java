package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpAcceptCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public TpAcceptCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check if player is in combat
        if (plugin.getCombatManager().isInCombat(player)) {
            player.sendMessage(plugin.getMessage("prefix") + "§cVous ne pouvez pas accepter de téléportation en combat !");
            return true;
        }
        
        if (!player.hasPermission("moderation.tpa")) {
            player.sendMessage(plugin.getMessage("no_permission"));
            return true;
        }
        
        plugin.getTeleportManager().acceptTpa(player);
        return true;
    }
}
