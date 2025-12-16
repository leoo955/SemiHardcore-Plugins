package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class HomesCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public HomesCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("moderation.homes")) {
            player.sendMessage(plugin.getMessage("no_permission"));
            return true;
        }
        
        Map<String, Location> homes = plugin.getHomeManager().getHomes(player);
        int maxHomes = plugin.getConfig().getInt("settings.max_homes", 3);
        
        if (homes.isEmpty()) {
            player.sendMessage(plugin.getMessage("homes_none"));
            return true;
        }
        
        player.sendMessage(plugin.getMessageRaw("homes_list")
            .replace("{count}", String.valueOf(homes.size()))
            .replace("{limit}", String.valueOf(maxHomes)));
            
        for (Map.Entry<String, Location> entry : homes.entrySet()) {
            Location loc = entry.getValue();
            player.sendMessage(plugin.getMessageRaw("homes_entry")
                .replace("{home}", entry.getKey())
                .replace("{x}", String.valueOf(loc.getBlockX()))
                .replace("{y}", String.valueOf(loc.getBlockY()))
                .replace("{z}", String.valueOf(loc.getBlockZ()))
                .replace("{world}", loc.getWorld().getName()));
        }
        
        return true;
    }
}
