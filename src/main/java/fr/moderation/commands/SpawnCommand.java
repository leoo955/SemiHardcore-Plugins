package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public SpawnCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check if player is in death world
        if (plugin.getDeathWorldManager().isInDeathWorld(player)) {
            if (!player.hasPermission("moderation.deathdim.bypass")) {
                player.sendMessage(plugin.getMessage("prefix") + "§cVous ne pouvez pas vous téléporter depuis le monde de la mort !");
                return true;
            }
        }
        
        // Check if player is in combat
        if (plugin.getCombatManager().isInCombat(player)) {
            player.sendMessage(plugin.getMessage("prefix") + "§cVous ne pouvez pas vous téléporter en combat !");
            return true;
        }
        
        if (!player.hasPermission("moderation.spawn")) {
            player.sendMessage(plugin.getMessage("no_permission"));
            return true;
        }

        Location spawn = plugin.getTeleportManager().getSpawn();
        
        if (spawn != null) {
            // OPs teleport instantly
            if (player.isOp()) {
                player.teleport(spawn);
                // FORCE survival mode FOR EVERYONE (even OPs)
                player.setGameMode(GameMode.SURVIVAL);
                player.sendMessage(plugin.getMessage("spawn_teleported"));
            } else {
                player.sendMessage(plugin.getMessage("prefix") + "§aTéléportation au spawn dans §e3 secondes§a...");
                
                // 3 second delay (60 ticks) before teleportation
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.teleport(spawn);
                    // FORCER SURVIVAL MODE
                    player.setGameMode(GameMode.SURVIVAL);
                    player.sendMessage(plugin.getMessage("spawn_teleported"));
                }, 60L); // 60 ticks = 3 secondes
            }
        } else {
            player.sendMessage(plugin.getMessage("spawn_not_set"));
        }
        
        return true;
    }
}
