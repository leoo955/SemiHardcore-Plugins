package fr.moderation.listeners;

import fr.moderation.ModerationSMP;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PvpListener implements Listener {
    
    private final ModerationSMP plugin;
    
    public PvpListener(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Check if PVP is disabled
        if (!plugin.getConfig().getBoolean("pvp.enabled", true)) {
            // Check if both entities are players
            if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
                event.setCancelled(true);
            }
        }
    }
}
