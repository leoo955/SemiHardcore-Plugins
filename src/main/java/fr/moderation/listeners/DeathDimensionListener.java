package fr.moderation.listeners;

import fr.moderation.ModerationSMP;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class DeathDimensionListener implements Listener {
    
    private final ModerationSMP plugin;
    
    public DeathDimensionListener(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Empêche la création de portails Nether dans le Death Dimension
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is in Death Dimension
        if (!plugin.getDeathWorldManager().isInDeathWorld(player)) {
            return;
        }
        
        // Block Obsidian placement (to create portal)
        if (event.getBlock().getType() == Material.OBSIDIAN) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Vous ne pouvez pas créer de portail dans le Paradis !");
        }
    }
    
    /**
     * Empêche l'utilisation de portails Nether depuis le Death Dimension
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is in Death Dimension
        if (!plugin.getDeathWorldManager().isInDeathWorld(player)) {
            return;
        }
        
        // Bloquer TOUT portail (Nether ou End)
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL ||
            event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Les portails sont désactivés dans le Paradis !");
        }
    }
    
    /**
     * Empêche la téléportation vers le Nether depuis le Death Dimension
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is in Death Dimension
        if (!plugin.getDeathWorldManager().isInDeathWorld(player)) {
            return;
        }
        
        // Block Nether/End teleportation
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL ||
            event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Vous ne pouvez pas utiliser de portail dans le Paradis !");
        }
    }
}
