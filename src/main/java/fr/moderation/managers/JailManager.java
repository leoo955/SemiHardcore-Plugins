package fr.moderation.managers;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class JailManager {
    
    private final ModerationSMP plugin;
    private final Map<UUID, JailData> jailedPlayers;
    private final Map<String, Location> jailLocations;
    
    public JailManager(ModerationSMP plugin) {
        this.plugin = plugin;
        this.jailedPlayers = new HashMap<>();
        this.jailLocations = new HashMap<>();
        
        loadJails();
        startTimerTask();
    }
    
    public void loadJails() {
        jailLocations.clear();
        
        ConfigurationSection jailsSection = plugin.getConfig().getConfigurationSection("jails");
        if (jailsSection != null) {
            for (String jailName : jailsSection.getKeys(false)) {
                String path = "jails." + jailName + ".";
                
                String world = plugin.getConfig().getString(path + "world");
                double x = plugin.getConfig().getDouble(path + "x");
                double y = plugin.getConfig().getDouble(path + "y");
                double z = plugin.getConfig().getDouble(path + "z");
                float yaw = (float) plugin.getConfig().getDouble(path + "yaw");
                float pitch = (float) plugin.getConfig().getDouble(path + "pitch");
                
                Location loc = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
                jailLocations.put(jailName, loc);
            }
        }
    }
    
    public void saveJail(String name, Location location) {
        String path = "jails." + name + ".";
        plugin.getConfig().set(path + "world", location.getWorld().getName());
        plugin.getConfig().set(path + "x", location.getX());
        plugin.getConfig().set(path + "y", location.getY());
        plugin.getConfig().set(path + "z", location.getZ());
        plugin.getConfig().set(path + "yaw", location.getYaw());
        plugin.getConfig().set(path + "pitch", location.getPitch());
        plugin.saveConfig();
        
        jailLocations.put(name, location);
    }
    
    public boolean jailExists(String name) {
        return jailLocations.containsKey(name);
    }
    
    public Location getJailLocation(String name) {
        return jailLocations.get(name);
    }
    
    public Set<String> getJailNames() {
        return jailLocations.keySet();
    }
    
    public boolean isJailed(Player player) {
        return jailedPlayers.containsKey(player.getUniqueId());
    }
    
    public void jailPlayer(Player player, String jailName, long durationMinutes, String reason) {
        if (!jailExists(jailName)) {
            return;
        }
        
        Location jailLoc = getJailLocation(jailName);
        
        // Save current position
        Location previousLocation = player.getLocation();
        
        // Calculate end time
        long endTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000);
        
        // Create jail data
        JailData data = new JailData(player.getUniqueId(), jailName, previousLocation, endTime, reason);
        jailedPlayers.put(player.getUniqueId(), data);
        
        // Teleport the player
        player.teleport(jailLoc);
        
        // Message
        player.sendMessage(plugin.getMessage("prefix") + "§cVous avez été emprisonné dans §e" + jailName + " §cpour §e" + durationMinutes + " minutes§c.");
        if (reason != null && !reason.isEmpty()) {
            player.sendMessage(plugin.getMessage("prefix") + "§cRaison: §f" + reason);
        }
    }
    
    public void unjailPlayer(Player player) {
        JailData data = jailedPlayers.remove(player.getUniqueId());
        
        if (data != null && data.getPreviousLocation() != null) {
            player.teleport(data.getPreviousLocation());
            player.sendMessage(plugin.getMessage("prefix") + "§aVous avez été libéré de prison !");
        }
    }
    
    public JailData getJailData(Player player) {
        return jailedPlayers.get(player.getUniqueId());
    }
    
    public long getRemainingTime(Player player) {
        JailData data = jailedPlayers.get(player.getUniqueId());
        if (data == null) {
            return 0;
        }
        
        long remaining = data.getEndTime() - System.currentTimeMillis();
        return Math.max(0, remaining / 1000); // in seconds
    }
    
    private void startTimerTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                List<UUID> toRelease = new ArrayList<>();
                
                for (Map.Entry<UUID, JailData> entry : jailedPlayers.entrySet()) {
                    if (entry.getValue().getEndTime() <= currentTime) {
                        toRelease.add(entry.getKey());
                    }
                }
                
                for (UUID uuid : toRelease) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        unjailPlayer(player);
                    } else {
                        jailedPlayers.remove(uuid);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Check every second
    }
    
    // Inner class to store jail data
    public static class JailData {
        private final UUID playerUUID;
        private final String jailName;
        private final Location previousLocation;
        private final long endTime;
        private final String reason;
        
        public JailData(UUID playerUUID, String jailName, Location previousLocation, long endTime, String reason) {
            this.playerUUID = playerUUID;
            this.jailName = jailName;
            this.previousLocation = previousLocation;
            this.endTime = endTime;
            this.reason = reason;
        }
        
        public UUID getPlayerUUID() {
            return playerUUID;
        }
        
        public String getJailName() {
            return jailName;
        }
        
        public Location getPreviousLocation() {
            return previousLocation;
        }
        
        public long getEndTime() {
            return endTime;
        }
        
        public String getReason() {
            return reason;
        }
    }
}
