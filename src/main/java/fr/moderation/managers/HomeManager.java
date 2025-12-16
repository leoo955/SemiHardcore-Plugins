package fr.moderation.managers;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class HomeManager {
    
    private final ModerationSMP plugin;
    private final Map<UUID, Map<String, Location>> homes;
    
    public HomeManager(ModerationSMP plugin) {
        this.plugin = plugin;
        this.homes = new HashMap<>();
    }
    
    public void loadHomes() {
        homes.clear();
        ConfigurationSection section = plugin.getHomesConfig().getConfigurationSection("homes");
        if (section == null) return;
        
        for (String uuidStr : section.getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            ConfigurationSection playerSection = section.getConfigurationSection(uuidStr);
            Map<String, Location> playerHomes = new HashMap<>();
            
            for (String homeName : playerSection.getKeys(false)) {
                ConfigurationSection homeSection = playerSection.getConfigurationSection(homeName);
                World world = Bukkit.getWorld(homeSection.getString("world"));
                if (world != null) {
                    Location loc = new Location(
                        world,
                        homeSection.getDouble("x"),
                        homeSection.getDouble("y"),
                        homeSection.getDouble("z"),
                        (float) homeSection.getDouble("yaw"),
                        (float) homeSection.getDouble("pitch")
                    );
                    playerHomes.put(homeName, loc);
                }
            }
            homes.put(uuid, playerHomes);
        }
    }
    
    public void saveHomes() {
        plugin.getHomesConfig().set("homes", null); // Reset to avoid ghosts
        
        for (Map.Entry<UUID, Map<String, Location>> entry : homes.entrySet()) {
            String uuidStr = entry.getKey().toString();
            for (Map.Entry<String, Location> homeEntry : entry.getValue().entrySet()) {
                String path = "homes." + uuidStr + "." + homeEntry.getKey();
                Location loc = homeEntry.getValue();
                
                plugin.getHomesConfig().set(path + ".world", loc.getWorld().getName());
                plugin.getHomesConfig().set(path + ".x", loc.getX());
                plugin.getHomesConfig().set(path + ".y", loc.getY());
                plugin.getHomesConfig().set(path + ".z", loc.getZ());
                plugin.getHomesConfig().set(path + ".yaw", loc.getYaw());
                plugin.getHomesConfig().set(path + ".pitch", loc.getPitch());
            }
        }
        plugin.saveHomesConfig();
    }
    
    public void setHome(Player player, String name, Location loc) {
        UUID uuid = player.getUniqueId();
        Map<String, Location> playerHomes = homes.computeIfAbsent(uuid, k -> new HashMap<>());
        
        playerHomes.put(name, loc);
        saveHomes(); // Immediate save for safety
    }
    
    public void deleteHome(Player player, String name) {
        UUID uuid = player.getUniqueId();
        if (homes.containsKey(uuid)) {
            homes.get(uuid).remove(name);
            saveHomes();
        }
    }
    
    public Location getHome(Player player, String name) {
        UUID uuid = player.getUniqueId();
        if (!homes.containsKey(uuid)) return null;
        return homes.get(uuid).get(name);
    }
    
    public Map<String, Location> getHomes(Player player) {
        return homes.getOrDefault(player.getUniqueId(), new HashMap<>());
    }
    
    public int getHomeCount(Player player) {
        return getHomes(player).size();
    }
}
