package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public HomeCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (plugin.getDeathWorldManager().isInDeathWorld(player)) {
            if (!player.hasPermission("moderation.deathdim.bypass")) {
                player.sendMessage(plugin.getMessage("prefix") + "§cVous ne pouvez pas vous téléporter depuis le monde de la mort !");
                return true;
            }
        }
        
        if (plugin.getCombatManager().isInCombat(player)) {
            player.sendMessage(plugin.getMessage("prefix") + "§cVous ne pouvez pas vous téléporter en combat !");
            return true;
        }
        
        if (!player.hasPermission("moderation.home")) {
            player.sendMessage(plugin.getMessage("no_permission"));
            return true;
        }
        
        String homeName = args.length > 0 ? args[0] : "home";
        Location home = plugin.getHomeManager().getHome(player, homeName);
        
        if (home == null) {
            player.sendMessage(plugin.getMessageRaw("home_not_found").replace("{home}", homeName));
            return true;
        }
        
        player.sendMessage(plugin.getMessage("prefix") + "§aTéléportation vers §e" + homeName + " §adans §e3 secondes§a...");
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.teleport(home);
            player.sendMessage(plugin.getMessageRaw("home_teleport").replace("{home}", homeName));
        }, 60L);
        
        return true;
    }
}
