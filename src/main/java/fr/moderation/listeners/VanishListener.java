package fr.moderation.listeners;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class VanishListener implements Listener {
    
    private final ModerationSMP plugin;
    
    public VanishListener(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Hide vanished players from the new player
        plugin.getVanishManager().onJoin(event.getPlayer());
        
        // Update player count by removing vanished players from counter
        updatePlayerList();
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Update after departure
        Bukkit.getScheduler().runTaskLater(plugin, this::updatePlayerList, 1L);
    }
    
    /**
     * Met à jour le player list footer pour montrer le bon nombre de joueurs
     */
    private void updatePlayerList() {
        int visibleCount = plugin.getVanishManager().getVisiblePlayerCount();
        int maxPlayers = Bukkit.getMaxPlayers();
        
        // Create footer with correct count
        String footer = org.bukkit.ChatColor.GRAY + "Joueurs: " + 
                       org.bukkit.ChatColor.GREEN + visibleCount + 
                       org.bukkit.ChatColor.GRAY + "/" + 
                       org.bukkit.ChatColor.GREEN + maxPlayers;
        
        // Apply to all players
        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            // Players with bypass see the real count
            if (player.hasPermission("moderation.vanish.see")) {
                int realCount = Bukkit.getOnlinePlayers().size();
                String staffFooter = org.bukkit.ChatColor.GRAY + "Joueurs: " + 
                                   org.bukkit.ChatColor.YELLOW + visibleCount + 
                                   org.bukkit.ChatColor.GRAY + " (" + 
                                   org.bukkit.ChatColor.RED + realCount + 
                                   org.bukkit.ChatColor.GRAY + ")/" + 
                                   org.bukkit.ChatColor.GREEN + maxPlayers;
                player.setPlayerListFooter(staffFooter);
            } else {
                player.setPlayerListFooter(footer);
            }
        }
    }
}
