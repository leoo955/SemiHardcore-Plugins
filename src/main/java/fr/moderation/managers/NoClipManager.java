package fr.moderation.managers;

import org.bukkit.entity.Player;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NoClipManager {
    
    private final Set<UUID> noClipPlayers;
    
    public NoClipManager() {
        this.noClipPlayers = new HashSet<>();
    }
    
    public boolean toggleNoClip(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (noClipPlayers.contains(uuid)) {
            // Disable noclip
            noClipPlayers.remove(uuid);
            player.setAllowFlight(false);
            player.setFlying(false);
            return false;
        } else {
            // Enable noclip
            noClipPlayers.add(uuid);
            player.setAllowFlight(true);
            player.setFlying(true);
            return true;
        }
    }
    
    public boolean isNoClip(Player player) {
        return noClipPlayers.contains(player.getUniqueId());
    }
    
    public void removeNoClip(Player player) {
        UUID uuid = player.getUniqueId();
        if (noClipPlayers.contains(uuid)) {
            noClipPlayers.remove(uuid);
            player.setAllowFlight(false);
            player.setFlying(false);
        }
    }
    
    public void disableAllNoClip() {
        noClipPlayers.clear();
    }
}
