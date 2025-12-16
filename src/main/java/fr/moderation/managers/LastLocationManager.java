package fr.moderation.managers;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LastLocationManager {
    
    private final Map<UUID, Location> lastLocations;
    
    public LastLocationManager() {
        this.lastLocations = new HashMap<>();
    }
    
    /**
     * Set the last location for a player
     * @param player The player
     * @param location The location to save
     */
    public void setLastLocation(Player player, Location location) {
        lastLocations.put(player.getUniqueId(), location.clone());
    }
    
    /**
     * Get the last location for a player
     * @param playerId The player's UUID
     * @return The last location, or null if none saved
     */
    public Location getLastLocation(UUID playerId) {
        return lastLocations.get(playerId);
    }
    
    /**
     * Check if a player has a last location saved
     * @param playerId The player's UUID
     * @return true if a last location exists
     */
    public boolean hasLastLocation(UUID playerId) {
        return lastLocations.containsKey(playerId);
    }
    
    /**
     * Remove the last location for a player
     * @param playerId The player's UUID
     */
    public void removeLastLocation(UUID playerId) {
        lastLocations.remove(playerId);
    }
    
    /**
     * Clear all last locations (on server shutdown)
     */
    public void clearAll() {
        lastLocations.clear();
    }
}
