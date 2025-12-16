package fr.moderation.managers;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GodModeManager {
    
    private final Set<UUID> godModePlayers;
    
    public GodModeManager() {
        this.godModePlayers = new HashSet<>();
    }
    
    /**
     * Toggle god mode for a player
     * @param player The player to toggle god mode for
     * @return true if god mode is now enabled, false if disabled
     */
    public boolean toggleGodMode(Player player) {
        UUID uuid = player.getUniqueId();
        if (godModePlayers.contains(uuid)) {
            godModePlayers.remove(uuid);
            return false;
        } else {
            godModePlayers.add(uuid);
            return true;
        }
    }
    
    /**
     * Set god mode for a player
     * @param player The player
     * @param enabled true to enable, false to disable
     */
    public void setGodMode(Player player, boolean enabled) {
        UUID uuid = player.getUniqueId();
        if (enabled) {
            godModePlayers.add(uuid);
        } else {
            godModePlayers.remove(uuid);
        }
    }
    
    /**
     * Check if a player has god mode enabled
     * @param playerId The player's UUID
     * @return true if god mode is enabled
     */
    public boolean isGodMode(UUID playerId) {
        return godModePlayers.contains(playerId);
    }
    
    /**
     * Disable god mode for all players (on server shutdown)
     */
    public void disableAllGodMode() {
        godModePlayers.clear();
    }
}
