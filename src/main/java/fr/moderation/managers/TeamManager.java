package fr.moderation.managers;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TeamManager {
    
    private final ModerationSMP plugin;
    private final File teamsFile;
    private final YamlConfiguration teamsConfig;
    
    // Structure: TeamColor -> List of UUIDs
    private final Map<String, Set<UUID>> teams;
    private final Map<String, UUID> teamLeaders; // TeamColor -> Leader UUID
    private final Map<String, org.bukkit.Location> teamHomes; // TeamColor -> Home Location
    private final Map<UUID, UUID> pendingInvites; // Target UUID -> Inviter UUID
    
    public static final String TEAM_GREEN = "GREEN";
    public static final String TEAM_BLUE = "BLUE";
    public static final String TEAM_RED = "RED";
    
    public TeamManager(ModerationSMP plugin) {
        this.plugin = plugin;
        this.teams = new HashMap<>();
        this.teamLeaders = new HashMap<>();
        this.teamHomes = new HashMap<>();
        this.pendingInvites = new HashMap<>();
        
        teams.put(TEAM_GREEN, new HashSet<>());
        teams.put(TEAM_BLUE, new HashSet<>());
        teams.put(TEAM_RED, new HashSet<>());
        
        this.teamsFile = new File(plugin.getDataFolder(), "teams.yml");
        if (!teamsFile.exists()) {
            try {
                teamsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Impossible de créer teams.yml");
            }
        }
        this.teamsConfig = YamlConfiguration.loadConfiguration(teamsFile);
        
        loadTeams();
        setupScoreboardTeams();
    }
    
    private void loadTeams() {
        for (String color : teams.keySet()) {
            // Load members
            List<String> members = teamsConfig.getStringList("teams." + color + ".members");
            for (String uuidStr : members) {
                try {
                    teams.get(color).add(UUID.fromString(uuidStr));
                } catch (IllegalArgumentException ignored) {}
            }
            
            // Load leader
            String leaderStr = teamsConfig.getString("teams." + color + ".leader");
            if (leaderStr != null && !leaderStr.isEmpty()) {
                try {
                    teamLeaders.put(color, UUID.fromString(leaderStr));
                } catch (IllegalArgumentException ignored) {}
            }
            
            // Load home
            if (teamsConfig.contains("teams." + color + ".home")) {
                String worldName = teamsConfig.getString("teams." + color + ".home.world");
                double x = teamsConfig.getDouble("teams." + color + ".home.x");
                double y = teamsConfig.getDouble("teams." + color + ".home.y");
                double z = teamsConfig.getDouble("teams." + color + ".home.z");
                float yaw = (float) teamsConfig.getDouble("teams." + color + ".home.yaw");
                float pitch = (float) teamsConfig.getDouble("teams." + color + ".home.pitch");
                
                org.bukkit.World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    teamHomes.put(color, new org.bukkit.Location(world, x, y, z, yaw, pitch));
                }
            }
        }
    }
    
    public void saveTeams() {
        for (String color : teams.keySet()) {
            // Save members
            List<String> members = new ArrayList<>();
            for (UUID uuid : teams.get(color)) {
                members.add(uuid.toString());
            }
            teamsConfig.set("teams." + color + ".members", members);
            
            // Save leader
            UUID leader = teamLeaders.get(color);
            if (leader != null) {
                teamsConfig.set("teams." + color + ".leader", leader.toString());
            } else {
                teamsConfig.set("teams." + color + ".leader", null);
            }
            
            // Save home
            org.bukkit.Location home = teamHomes.get(color);
            if (home != null) {
                teamsConfig.set("teams." + color + ".home.world", home.getWorld().getName());
                teamsConfig.set("teams." + color + ".home.x", home.getX());
                teamsConfig.set("teams." + color + ".home.y", home.getY());
                teamsConfig.set("teams." + color + ".home.z", home.getZ());
                teamsConfig.set("teams." + color + ".home.yaw", home.getYaw());
                teamsConfig.set("teams." + color + ".home.pitch", home.getPitch());
            } else {
                teamsConfig.set("teams." + color + ".home", null);
            }
        }
        
        try {
            teamsConfig.save(teamsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Impossible de sauvegarder teams.yml");
        }
    }
    
    public void setTeamHome(String color, org.bukkit.Location location) {
        teamHomes.put(color, location);
        saveTeams();
    }
    
    public void deleteTeamHome(String color) {
        teamHomes.remove(color);
        saveTeams();
    }
    
    public org.bukkit.Location getTeamHome(String color) {
        return teamHomes.get(color);
    }
    
    private void setupScoreboardTeams() {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        
        createScoreboardTeam(board, TEAM_GREEN, ChatColor.GREEN);
        createScoreboardTeam(board, TEAM_BLUE, ChatColor.BLUE);
        createScoreboardTeam(board, TEAM_RED, ChatColor.RED);
        
        // Setup health display below nametag
        setupHealthDisplay(board);
    }
    
    private void setupHealthDisplay(Scoreboard board) {
        org.bukkit.scoreboard.Objective healthObjective = board.getObjective("health");
        if (healthObjective != null) {
            healthObjective.unregister();
        }
        
        healthObjective = board.registerNewObjective("health", "health", ChatColor.RED + "❤");
        healthObjective.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.BELOW_NAME);
    }

    
    private void createScoreboardTeam(Scoreboard board, String name, ChatColor color) {
        Team team = board.getTeam(name);
        if (team == null) {
            team = board.registerNewTeam(name);
        }
        team.setColor(color);
        team.setPrefix(color.toString());
    }
    
    public void setLeader(String color, Player player) {
        // Remove from old team if exists
        removePlayer(player.getUniqueId());
        
        // Set as leader
        teamLeaders.put(color, player.getUniqueId());
        teams.get(color).add(player.getUniqueId());
        
        updatePlayerVisuals(player, color);
        saveTeams();
    }
    
    public void resignLeader(String color) {
        if (teamLeaders.containsKey(color)) {
            teamLeaders.remove(color);
            saveTeams();
        }
    }
    
    public void addMember(String color, Player player) {
        // Remove from old team
        removePlayer(player.getUniqueId());
        
        teams.get(color).add(player.getUniqueId());
        updatePlayerVisuals(player, color);
        saveTeams();
    }
    
    public void removePlayer(UUID playerId) {
        for (String color : teams.keySet()) {
            if (teams.get(color).remove(playerId)) {
                // If was leader, remove leadership
                if (teamLeaders.get(color) != null && teamLeaders.get(color).equals(playerId)) {
                    teamLeaders.remove(color);
                }
                
                Player player = Bukkit.getPlayer(playerId);
                if (player != null) {
                    Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
                    Team team = board.getTeam(color);
                    if (team != null) {
                        team.removeEntry(player.getName());
                    }
                    player.setPlayerListName(player.getName()); // Reset tab name
                }
            }
        }
        saveTeams();
    }
    
    private void updatePlayerVisuals(Player player, String colorName) {
        ChatColor color = getColor(colorName);
        
        // Update TabList
        player.setPlayerListName(color + player.getName());
        
        // Update Scoreboard Team (Name Tag)
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = board.getTeam(colorName);
        if (team != null) {
            team.addEntry(player.getName());
        }
    }
    
    public String getPlayerTeam(UUID playerId) {
        for (Map.Entry<String, Set<UUID>> entry : teams.entrySet()) {
            if (entry.getValue().contains(playerId)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    public boolean isLeader(UUID playerId) {
        return teamLeaders.containsValue(playerId);
    }
    
    public String getLeaderTeam(UUID leaderId) {
        for (Map.Entry<String, UUID> entry : teamLeaders.entrySet()) {
            if (entry.getValue().equals(leaderId)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    public ChatColor getColor(String teamName) {
        switch (teamName) {
            case TEAM_GREEN: return ChatColor.GREEN;
            case TEAM_BLUE: return ChatColor.BLUE;
            case TEAM_RED: return ChatColor.RED;
            default: return ChatColor.WHITE;
        }
    }
    
    /**
     * Get player's team color as string code for chat formatting
     */
    public String getPlayerTeamColor(UUID playerId) {
        String teamName = getPlayerTeam(playerId);
        if (teamName == null) return null;
        
        switch (teamName) {
            case TEAM_GREEN: return "&a"; // Vert
            case TEAM_BLUE: return "&9";  // Bleu
            case TEAM_RED: return "&c";   // Rouge
            default: return "&7";         // Gris
        }
    }
    
    public String getTeamDisplayName(String teamName) {
        switch (teamName) {
            case TEAM_GREEN: return ChatColor.GREEN + "VERT";
            case TEAM_BLUE: return ChatColor.BLUE + "BLEU";
            case TEAM_RED: return ChatColor.RED + "ROUGE";
            default: return teamName;
        }
    }
    
    // Team invitation system
    public void sendInvite(UUID inviter, UUID target) {
        pendingInvites.put(target, inviter);
    }
    
    public boolean hasPendingInvite(UUID playerId) {
        return pendingInvites.containsKey(playerId);
    }
    
    public UUID getInviter(UUID playerId) {
        return pendingInvites.get(playerId);
    }
    
    public void removeInvite(UUID playerId) {
        pendingInvites.remove(playerId);
    }
    
    // Transfer leadership
    public boolean transferLeadership(String teamColor, UUID newLeaderId) {
        // Check if new leader is in the team
        if (!teams.get(teamColor).contains(newLeaderId)) {
            return false;
        }
        
        teamLeaders.put(teamColor, newLeaderId);
        saveTeams();
        return true;
    }
    
    // Get all team members
    public Set<UUID> getTeamMembers(String teamColor) {
        return new HashSet<>(teams.get(teamColor));
    }
    
    // Get team leader
    public UUID getTeamLeader(String teamColor) {
        return teamLeaders.get(teamColor);
    }
    
    // Send message to all team members
    public void sendTeamMessage(String teamColor, String message) {
        ChatColor color = getColor(teamColor);
        String formattedMessage = color + "[Team] " + ChatColor.RESET + message;
        
        for (UUID memberId : teams.get(teamColor)) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage(formattedMessage);
            }
        }
    }
    
    // Get all teams with their data
    public Map<String, Set<UUID>> getAllTeams() {
        return new HashMap<>(teams);
    }
}
