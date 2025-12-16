package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BackCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public BackCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("moderation.back")) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Seul un joueur peut utiliser cette commande.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (plugin.getDeathWorldManager().isInDeathWorld(player)) {
            if (!player.hasPermission("moderation.deathdim.bypass")) {
                player.sendMessage(ChatColor.RED + "Vous ne pouvez pas vous téléporter depuis le monde de la mort !");
                return true;
            }
        }
        
        if (!plugin.getLastLocationManager().hasLastLocation(player.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Aucune position précédente enregistrée.");
            return true;
        }
        
        Location lastLocation = plugin.getLastLocationManager().getLastLocation(player.getUniqueId());
        
        if (lastLocation == null || lastLocation.getWorld() == null) {
            sender.sendMessage(ChatColor.RED + "Impossible de retourner à la position précédente.");
            return true;
        }
        
        Location currentLocation = player.getLocation();
        player.teleport(lastLocation);
        
        plugin.getLastLocationManager().setLastLocation(player, currentLocation);
        
        sender.sendMessage(ChatColor.GREEN + "Vous avez été téléporté à votre position précédente !");
        
        return true;
    }
}
