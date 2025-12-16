package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetHomeCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public SetHomeCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("moderation.sethome")) {
            player.sendMessage(plugin.getMessage("no_permission"));
            return true;
        }
        
        String homeName = args.length > 0 ? args[0] : "home";
        int maxHomes = plugin.getConfig().getInt("settings.max_homes", 3);
        
        // Check if player already has this home or reached limit
        if (plugin.getHomeManager().getHome(player, homeName) == null) {
            if (plugin.getHomeManager().getHomeCount(player) >= maxHomes && !player.hasPermission("moderation.homes.unlimited")) {
                player.sendMessage(plugin.getMessageRaw("home_limit_reached").replace("{limit}", String.valueOf(maxHomes)));
                return true;
            }
        }
        
        plugin.getHomeManager().setHome(player, homeName, player.getLocation());
        
        if (plugin.getHomeManager().getHome(player, homeName) != null) {
            player.sendMessage(plugin.getMessageRaw("home_updated").replace("{home}", homeName));
        } else {
            player.sendMessage(plugin.getMessageRaw("home_set").replace("{home}", homeName));
        }
        
        return true;
    }
}
