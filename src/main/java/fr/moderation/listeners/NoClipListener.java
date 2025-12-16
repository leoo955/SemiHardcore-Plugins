package fr.moderation.listeners;

import fr.moderation.ModerationSMP;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

public class NoClipListener implements Listener {
    
    private final ModerationSMP plugin;
    
    public NoClipListener(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getNoClipManager().isNoClip(player)) {
            return;
        }
        
        Location loc = player.getLocation();
        
        // Check if player is inside a solid block
        if (isInsideSolidBlock(loc)) {
            // Allow passage through blocks
            player.setInvulnerable(true);
        } else {
            player.setInvulnerable(false);
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Remove noclip when player disconnects
        plugin.getNoClipManager().removeNoClip(event.getPlayer());
    }
    
    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        
        // If player is in noclip and changes gamemode to spectator, disable noclip
        if (plugin.getNoClipManager().isNoClip(player) && event.getNewGameMode() == GameMode.SPECTATOR) {
            plugin.getNoClipManager().removeNoClip(player);
            player.sendMessage(plugin.getMessage("prefix") + "§cNoClip désactivé en mode spectateur.");
        }
    }
    
    private boolean isInsideSolidBlock(Location loc) {
        Block block = loc.getBlock();
        Material type = block.getType();
        
        // Check if block is solid and non-air
        return type.isSolid() && type != Material.AIR;
    }
}
