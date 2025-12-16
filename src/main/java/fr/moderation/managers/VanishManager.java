package fr.moderation.managers;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class VanishManager {
    
    private final ModerationSMP plugin;
    private final Set<UUID> vanishedPlayers;
    private final Set<UUID> superVanishedPlayers;
    
    public VanishManager(ModerationSMP plugin) {
        this.plugin = plugin;
        this.vanishedPlayers = new HashSet<>();
        this.superVanishedPlayers = new HashSet<>();
    }
    
    public boolean isVanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId()) || 
               superVanishedPlayers.contains(player.getUniqueId());
    }
    
    public boolean isSuperVanished(Player player) {
        return superVanishedPlayers.contains(player.getUniqueId());
    }
    
    public void setVanish(Player player, boolean vanish) {
        UUID uuid = player.getUniqueId();
        
        if (vanish) {
            vanishedPlayers.add(uuid);
            superVanishedPlayers.remove(uuid);
            hideFromAll(player);
        } else {
            vanishedPlayers.remove(uuid);
            showToAll(player);
        }
    }
    
    public void setSuperVanish(Player player, boolean vanish) {
        UUID uuid = player.getUniqueId();
        
        if (vanish) {
            superVanishedPlayers.add(uuid);
            vanishedPlayers.remove(uuid);
            hideFromAll(player);
            
            // Enable flight for supervanish
            player.setAllowFlight(true);
            player.setFlying(true);
        } else {
            superVanishedPlayers.remove(uuid);
            showToAll(player);
            
            // Disable flight if not in creative/spectator
            if (player.getGameMode() != org.bukkit.GameMode.CREATIVE && 
                player.getGameMode() != org.bukkit.GameMode.SPECTATOR) {
                player.setAllowFlight(false);
                player.setFlying(false);
            }
        }
    }
    
    private void hideFromAll(Player player) {
        // Hide from all players
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.hidePlayer(plugin, player);
        }
    }
    
    private void showToAll(Player player) {
        // Show to all players
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showPlayer(plugin, player);
        }
    }
    
    public void onJoin(Player player) {
        // Hide all vanished players from the new player
        for (UUID uuid : vanishedPlayers) {
            Player vanished = Bukkit.getPlayer(uuid);
            if (vanished != null && vanished.isOnline()) {
                player.hidePlayer(plugin, vanished);
            }
        }
        
        for (UUID uuid : superVanishedPlayers) {
            Player vanished = Bukkit.getPlayer(uuid);
            if (vanished != null && vanished.isOnline()) {
                player.hidePlayer(plugin, vanished);
            }
        }
    }
    
    public void disableAllVanish() {
        // Disable all vanish
        for (UUID uuid : new HashSet<>(vanishedPlayers)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                setVanish(player, false);
            }
        }
        
        for (UUID uuid : new HashSet<>(superVanishedPlayers)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                setSuperVanish(player, false);
            }
        }
        
        vanishedPlayers.clear();
        superVanishedPlayers.clear();
    }
    
    /**
     * Returns the number of VISIBLE players (non-vanished)
     */
    public int getVisiblePlayerCount() {
        return Bukkit.getOnlinePlayers().size() - vanishedPlayers.size() - superVanishedPlayers.size();
    }
}
