package fr.moderation.listeners;

import fr.moderation.ModerationSMP;
import fr.moderation.managers.TeamManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class TeamListener implements Listener {
    
    private final ModerationSMP plugin;
    private final TeamManager teamManager;
    
    public TeamListener(ModerationSMP plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String team = teamManager.getPlayerTeam(player.getUniqueId());
        
        if (team != null) {
            ChatColor color = teamManager.getColor(team);
            
            // Update TabList
            player.setPlayerListName(color + player.getName());
            
            // Update Scoreboard Team (Nametag) - THIS WAS MISSING!
            org.bukkit.scoreboard.Scoreboard board = org.bukkit.Bukkit.getScoreboardManager().getMainScoreboard();
            org.bukkit.scoreboard.Team scoreboardTeam = board.getTeam(team);
            if (scoreboardTeam != null) {
                scoreboardTeam.addEntry(player.getName());
            }
        }
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String team = teamManager.getPlayerTeam(player.getUniqueId());
        
        if (team != null) {
            ChatColor color = teamManager.getColor(team);
            // Format: [TEAM] Username: Message
            // But here we just want the username color
            event.setFormat(color + "%s" + ChatColor.RESET + ": %s");
        }
    }
}
