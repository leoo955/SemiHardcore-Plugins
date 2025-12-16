package fr.moderation.listeners;

import fr.moderation.ModerationSMP;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ScoreboardListener implements Listener {
    
    private final ModerationSMP plugin;
    
    public ScoreboardListener(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Create scoreboard for joining player
        plugin.getScoreboardManager().updateScoreboard(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Remove scoreboard from leaving player
        plugin.getScoreboardManager().removeScoreboard(event.getPlayer());
    }
}
