package fr.moderation.listeners;

import fr.moderation.ModerationSMP;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.entity.Player;

public class TeleportTrackingListener implements Listener {
    
    private final ModerationSMP plugin;
    
    public TeleportTrackingListener(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        
        // Don't track if it's an ender pearl or chorus fruit (short distance)
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL ||
            event.getCause() == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT) {
            return;
        }
        
        // Don't save location if player is in death world (prevent escape)
        if (plugin.getDeathWorldManager().isInDeathWorld(player)) {
            return;
        }
        
        // Save the location they're teleporting FROM
        if (event.getFrom() != null && event.getFrom().getWorld() != null) {
            plugin.getLastLocationManager().setLastLocation(player, event.getFrom());
        }
    }
}
