package fr.moderation.managers;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FreezeManager {
    
    private final Set<UUID> frozenPlayers;
    
    public FreezeManager() {
        this.frozenPlayers = new HashSet<>();
    }
    
    /**
     * Toggle freeze status for a player
     * @param player The player to toggle freeze for
     * @return true if now frozen, false if unfrozen
     */
    public boolean toggleFreeze(Player player) {
        UUID uuid = player.getUniqueId();
        if (frozenPlayers.contains(uuid)) {
            frozenPlayers.remove(uuid);
            return false;
        } else {
            frozenPlayers.add(uuid);
            return true;
        }
    }
    
    /**
     * Set freeze status for a player
     * @param player The player
     * @param frozen true to freeze, false to unfreeze
     */
    public void setFrozen(Player player, boolean frozen) {
        UUID uuid = player.getUniqueId();
        if (frozen) {
            frozenPlayers.add(uuid);
        } else {
            frozenPlayers.remove(uuid);
        }
    }
    
    /**
     * Check if a player is frozen
     * @param playerId The player's UUID
     * @return true if frozen
     */
    public boolean isFrozen(UUID playerId) {
        return frozenPlayers.contains(playerId);
    }
    
    /**
     * Unfreeze all players (on server shutdown)
     */
    public void unfreezeAll() {
        frozenPlayers.clear();
    }
}
