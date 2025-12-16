package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SuperVanishCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public SuperVanishCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("moderation.supervanish")) {
            player.sendMessage(plugin.getMessage("no_permission"));
            return true;
        }
        
        boolean isSuperVanished = plugin.getVanishManager().isSuperVanished(player);
        plugin.getVanishManager().setSuperVanish(player, !isSuperVanished);
        
        if (!isSuperVanished) {
            player.sendMessage(plugin.getMessage("supervanish_enabled"));
        } else {
            player.sendMessage(plugin.getMessage("supervanish_disabled"));
        }
        
        return true;
    }
}
