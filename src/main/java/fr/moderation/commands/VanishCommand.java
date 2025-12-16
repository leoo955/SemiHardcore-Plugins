package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VanishCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public VanishCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("moderation.vanish")) {
            player.sendMessage(plugin.getMessage("no_permission"));
            return true;
        }
        
        boolean isVanished = plugin.getVanishManager().isVanished(player);
        plugin.getVanishManager().setVanish(player, !isVanished);
        
        if (!isVanished) {
            player.sendMessage(plugin.getMessage("vanish_enabled"));
        } else {
            player.sendMessage(plugin.getMessage("vanish_disabled"));
        }
        
        return true;
    }
}
