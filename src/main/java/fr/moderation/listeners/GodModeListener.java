package fr.moderation.listeners;

import fr.moderation.ModerationSMP;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.entity.Player;

public class GodModeListener implements Listener {
    
    private final ModerationSMP plugin;
    
    public GodModeListener(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        // Cancel damage if player has god mode
        if (plugin.getGodModeManager().isGodMode(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
