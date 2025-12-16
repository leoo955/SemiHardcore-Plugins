package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkinCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public SkinCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("moderation.skin")) {
            player.sendMessage(plugin.getMessage("no_permission"));
            return true;
        }
        
        if (args.length != 1) {
            player.sendMessage(plugin.getMessage("prefix") + "Usage: /skin <pseudo>");
            return true;
        }
        
        plugin.getSkinManager().setSkin(player, args[0]);
        player.sendMessage(plugin.getMessage("prefix") + "§7Changement de skin en cours...");
        
        return true;
    }
}
