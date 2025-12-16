package fr.moderation.listeners;

import fr.moderation.ModerationSMP;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class RollbackListener implements Listener {
    
    private final ModerationSMP plugin;
    
    public RollbackListener(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        // Logger la casse de bloc
        plugin.getRollbackManager().logBlockBreak(event.getPlayer(), event.getBlock());
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        // Logger la pose de bloc
        plugin.getRollbackManager().logBlockPlace(
            event.getPlayer(), 
            event.getBlock(),
            event.getBlockReplacedState().getType()
        );
    }
}
