package fr.moderation.managers;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportManager {
    
    private final ModerationSMP plugin;
    private final Map<UUID, UUID> tpaRequests;
    private final Map<UUID, Long> tpaTimestamps;
    private final Map<UUID, Long> lastTeleport;
    private Location spawnLocation;
    
    public TeleportManager(ModerationSMP plugin) {
        this.plugin = plugin;
        this.tpaRequests = new HashMap<>();
        this.tpaTimestamps = new HashMap<>();
        this.lastTeleport = new HashMap<>();
    }
    
    public void loadSpawn() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("spawn");
        if (section != null && section.getBoolean("set")) {
            World world = Bukkit.getWorld(section.getString("world"));
            if (world != null) {
                double x = section.getDouble("x");
                double y = section.getDouble("y");
                double z = section.getDouble("z");
                float yaw = (float) section.getDouble("yaw");
                float pitch = (float) section.getDouble("pitch");
                spawnLocation = new Location(world, x, y, z, yaw, pitch);
            }
        }
    }
    
    public void setSpawn(Location loc) {
        this.spawnLocation = loc;
        ConfigurationSection section = plugin.getConfig().createSection("spawn");
        section.set("world", loc.getWorld().getName());
        section.set("x", loc.getX());
        section.set("y", loc.getY());
        section.set("z", loc.getZ());
        section.set("yaw", loc.getYaw());
        section.set("pitch", loc.getPitch());
        section.set("set", true);
        plugin.saveConfig();
    }
    
    public Location getSpawn() {
        return spawnLocation;
    }
    
    public void sendTpaRequest(Player sender, Player target) {
        UUID senderId = sender.getUniqueId();
        UUID targetId = target.getUniqueId();
        
        tpaRequests.put(targetId, senderId);
        tpaTimestamps.put(targetId, System.currentTimeMillis());
        
        sender.sendMessage(plugin.getMessageRaw("tpa_sent").replace("{player}", target.getName()));
        target.sendMessage(plugin.getMessageRaw("tpa_received").replace("{player}", sender.getName()));
    }
    
    public void acceptTpa(Player target) {
        UUID targetId = target.getUniqueId();
        
        if (!hasRequest(targetId)) {
            target.sendMessage(plugin.getMessage("tpa_no_request"));
            return;
        }
        
        UUID senderId = tpaRequests.get(targetId);
        Player sender = Bukkit.getPlayer(senderId);
        
        if (sender != null && sender.isOnline()) {
            if (!sender.isOp() && !plugin.getDeathWorldManager().isGhost(senderId)) {
                long lastTp = lastTeleport.getOrDefault(senderId, 0L);
                long timeSince = (System.currentTimeMillis() - lastTp) / 1000;
                long cooldownSeconds = 20;
                
                if (timeSince < cooldownSeconds) {
                    long remaining = cooldownSeconds - timeSince;
                    sender.sendMessage(plugin.getMessage("prefix") + "§cVous devez attendre §e" + remaining + " secondes §cavant de vous téléporter !");
                    return;
                }
            }
            
            if (sender.isOp() || plugin.getDeathWorldManager().isGhost(senderId)) {
                sender.teleport(target);
                sender.sendMessage(plugin.getMessageRaw("tpa_accepted_sender").replace("{player}", target.getName()));
                target.sendMessage(plugin.getMessage("tpa_accepted"));
                lastTeleport.put(senderId, System.currentTimeMillis());
            } else {
                sender.sendMessage(plugin.getMessage("prefix") + "§aTéléportation vers §e" + target.getName() + " §adans §e3 secondes§a...");
                target.sendMessage(plugin.getMessage("tpa_accepted"));
                
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    sender.teleport(target);
                    sender.sendMessage(plugin.getMessageRaw("tpa_accepted_sender").replace("{player}", target.getName()));
                    lastTeleport.put(senderId, System.currentTimeMillis());
                }, 60L);
            }
        } else {
            target.sendMessage(plugin.getMessage("player_not_found"));
        }
        
        tpaRequests.remove(targetId);
        tpaTimestamps.remove(targetId);
    }
    
    public void denyTpa(Player target) {
        UUID targetId = target.getUniqueId();
        
        if (!hasRequest(targetId)) {
            target.sendMessage(plugin.getMessage("tpa_no_request"));
            return;
        }
        
        UUID senderId = tpaRequests.get(targetId);
        Player sender = Bukkit.getPlayer(senderId);
        
        if (sender != null && sender.isOnline()) {
            sender.sendMessage(plugin.getMessageRaw("tpa_denied_sender").replace("{player}", target.getName()));
        }
        
        target.sendMessage(plugin.getMessage("tpa_denied"));
        tpaRequests.remove(targetId);
        tpaTimestamps.remove(targetId);
    }
    
    private boolean hasRequest(UUID targetId) {
        if (!tpaRequests.containsKey(targetId)) return false;
        
        long timestamp = tpaTimestamps.get(targetId);
        int timeout = plugin.getConfig().getInt("settings.tpa_timeout", 30) * 1000;
        
        if (System.currentTimeMillis() - timestamp > timeout) {
            tpaRequests.remove(targetId);
            tpaTimestamps.remove(targetId);
            return false;
        }
        
        return true;
    }
}
