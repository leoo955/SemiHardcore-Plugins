package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public SetSpawnCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("moderation.setspawn")) {
            player.sendMessage(plugin.getMessage("no_permission"));
            return true;
        }
        
        plugin.getTeleportManager().setSpawn(player.getLocation());
        player.sendMessage(plugin.getMessage("spawn_set"));
        
        return true;
    }
}
