package fr.moderation.managers;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {
    
    private final ModerationSMP plugin;
    private final Map<UUID, Scoreboard> playerBoards;
    private int taskId;
    
    public ScoreboardManager(ModerationSMP plugin) {
        this.plugin = plugin;
        this.playerBoards = new HashMap<>();
    }
    
    /**
     * Starts the scoreboard system
     */
    public void start() {
        // Update scoreboard every second (20 ticks)
        taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateScoreboard(player);
            }
        }, 0L, 20L).getTaskId();
        
        plugin.getLogger().info("ScoreboardManager started");
    }
    
    /**
     * Stops the scoreboard system
     */
    public void stop() {
        Bukkit.getScheduler().cancelTask(taskId);
        
        // Remove all scoreboards
        for (Player player : Bukkit.getOnlinePlayers()) {
            removeScoreboard(player);
        }
        
        playerBoards.clear();
        plugin.getLogger().info("ScoreboardManager stopped");
    }
    
    /**
     * Creates or updates a player's scoreboard
     */
    public void updateScoreboard(Player player) {
        Scoreboard board = playerBoards.get(player.getUniqueId());
        
        if (board == null) {
            board = createScoreboard(player);
            playerBoards.put(player.getUniqueId(), board);
            player.setScoreboard(board);
        }
        
        // Update scoreboard lines
        Objective objective = board.getObjective("main");
        if (objective == null) return;
        
        // Clear old lines
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }
        
        // Informations du joueur
        String team = plugin.getTeamManager().getPlayerTeam(player.getUniqueId());
        int playersOnline = Bukkit.getOnlinePlayers().size();
        String world = getWorldName(player.getWorld().getName());
        
        // Build scoreboard (WITHOUT coordinates)
        int line = 15;
        
        // Empty line
        setLine(objective, " ", line--);
        
        // Team
        if (team != null) {
            setLine(objective, ChatColor.GRAY + "Équipe: " + ChatColor.translateAlternateColorCodes('&', team), line--);
        } else {
            setLine(objective, ChatColor.GRAY + "Équipe: " + ChatColor.RED + "Aucune", line--);
        }
        
        // Ligne vide
        setLine(objective, "  ", line--);
        
        // Argent
        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        setLine(objective, ChatColor.GRAY + "Argent: " + ChatColor.GOLD + plugin.getEconomyManager().format(balance), line--);
        
        // Ligne vide
        setLine(objective, "   ", line--);
        
        // Monde
        setLine(objective, ChatColor.GRAY + "Monde: " + ChatColor.WHITE + world, line--);
        
        // Ligne vide
        setLine(objective, "    ", line--);
        
        // Joueurs en ligne
        setLine(objective, ChatColor.GRAY + "Joueurs: " + ChatColor.GREEN + playersOnline, line--);
        
        // Ligne vide
        setLine(objective, "     ", line--);
        
        // Footer
        setLine(objective, ChatColor.GRAY + "" + ChatColor.ITALIC + "fakeunstable.mine.fun", line--);
    }
    
    /**
     * Creates a new scoreboard for a player
     */
    private Scoreboard createScoreboard(Player player) {
        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        
        Objective objective = board.registerNewObjective("main", "dummy", 
            ChatColor.GOLD + "" + ChatColor.BOLD + "FAKE UNSTABLE SMP");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        return board;
    }
    
    /**
     * Sets a scoreboard line
     */
    private void setLine(Objective objective, String text, int score) {
        // Add invisible spaces to avoid duplicates
        String uniqueText = text + ChatColor.RESET;
        
        Score s = objective.getScore(uniqueText);
        s.setScore(score);
    }
    
    /**
     * Removes a player's scoreboard
     */
    public void removeScoreboard(Player player) {
        playerBoards.remove(player.getUniqueId());
        
        // Reset to default scoreboard
        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        player.setScoreboard(manager.getMainScoreboard());
    }
    
    /**
     * Translates world name to English
     */
    private String getWorldName(String worldName) {
        switch (worldName.toLowerCase()) {
            case "world":
                return "Overworld";
            case "world_nether":
                return "Nether";
            case "world_the_end":
                return "End";
            case "death_world":
                return "Death World";
            default:
                return worldName;
        }
    }
    
    /**
     * Refreshes a specific player's scoreboard immediately
     */
    public void refreshPlayer(Player player) {
        updateScoreboard(player);
    }
}
