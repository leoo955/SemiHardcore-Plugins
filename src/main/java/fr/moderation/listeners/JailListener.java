package fr.moderation.listeners;

import fr.moderation.ModerationSMP;
import fr.moderation.managers.JailManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class JailListener implements Listener {
    
    private final ModerationSMP plugin;
    
    public JailListener(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getJailManager().isJailed(player)) {
            return;
        }
        
        // Prevent moving too far from jail
        JailManager.JailData data = plugin.getJailManager().getJailData(player);
        if (data != null) {
            org.bukkit.Location jailLoc = plugin.getJailManager().getJailLocation(data.getJailName());
            
            if (jailLoc != null && player.getLocation().distance(jailLoc) > 10) {
                // Teleport back to jail
                player.teleport(jailLoc);
                player.sendMessage(plugin.getMessage("prefix") + "§cVous ne pouvez pas quitter la prison !");
                
                // Afficher le temps restant
                long remaining = plugin.getJailManager().getRemainingTime(player);
                long minutes = remaining / 60;
                long seconds = remaining % 60;
                player.sendMessage(plugin.getMessage("prefix") + "§7Temps restant: §e" + minutes + "m " + seconds + "s");
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getJailManager().isJailed(player)) {
            return;
        }
        
        // Prevent all teleportations
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.PLUGIN) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessage("prefix") + "§cVous ne pouvez pas vous téléporter en prison !");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getJailManager().isJailed(player)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessage("prefix") + "§cVous ne pouvez pas casser de blocs en prison !");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getJailManager().isJailed(player)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessage("prefix") + "§cVous ne pouvez pas placer de blocs en prison !");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getJailManager().isJailed(player)) {
            return;
        }
        
        String command = event.getMessage().toLowerCase();
        
        // List of allowed commands
        String[] allowedCommands = {"/msg", "/r", "/reply", "/w", "/whisper", "/tell"};
        
        boolean allowed = false;
        for (String allowedCmd : allowedCommands) {
            if (command.startsWith(allowedCmd + " ") || command.equals(allowedCmd)) {
                allowed = true;
                break;
            }
        }
        
        if (!allowed && !player.hasPermission("moderation.jail.bypass")) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessage("prefix") + "§cVous ne pouvez pas utiliser cette commande en prison !");
            
            // Afficher le temps restant
            long remaining = plugin.getJailManager().getRemainingTime(player);
            long minutes = remaining / 60;
            long seconds = remaining % 60;
            player.sendMessage(plugin.getMessage("prefix") + "§7Temps restant: §e" + minutes + "m " + seconds + "s");
        }
    }
}
