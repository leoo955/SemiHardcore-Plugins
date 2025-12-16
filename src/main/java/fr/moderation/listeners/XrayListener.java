package fr.moderation.listeners;

import fr.moderation.ModerationSMP;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class XrayListener implements Listener {
    
    private final ModerationSMP plugin;
    
    public XrayListener(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        plugin.getXrayManager().handleBlockBreak(event.getPlayer(), event.getBlock().getType());
    }
}
