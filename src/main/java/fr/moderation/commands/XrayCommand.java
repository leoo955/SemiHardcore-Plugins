package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class XrayCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public XrayCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("moderation.xray.alerts")) {
            player.sendMessage(plugin.getMessage("no_permission"));
            return true;
        }
        
        if (args.length > 0 && args[0].equalsIgnoreCase("alerts")) {
            plugin.getXrayManager().toggleAlerts(player);
            return true;
        }
        
        player.sendMessage(plugin.getMessage("prefix") + "Usage: /xray alerts");
        return true;
    }
}
