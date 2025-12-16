package fr.moderation.managers;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class XrayManager {
    
    private final ModerationSMP plugin;
    private final Map<Material, Integer> monitoredOres;
    private final Map<UUID, Map<Material, Integer>> miningStats; // Temporary statistics to detect suspicious patterns (reset every minute)
    private final Map<UUID, Map<Material, Integer>> globalStats; // Permanent statistics of all mined ores (for TopLuck leaderboard)
    private final Map<UUID, Integer> totalBlocksMined; // Global counter of broken blocks (stone, deepslate, netherrack) to calculate ratios
    private final Map<UUID, Long> lastReset;
    private final Set<UUID> alertsDisabled;
    
    // AUTOMATIC PUNISHMENT SYSTEM
    private final Map<UUID, Integer> xrayViolations; // Counts number of violations per player (the more there are, the harsher the punishment)
    private boolean autoBanEnabled = true; // If enabled, the system automatically punishes suspected cheaters
    private int violationsBeforeKick = 2; // Number of violations before being kicked from the server
    private int violationsBeforeBan = 3; // Number of violations before final kick (currently = kick, not permanent ban)
    
    public XrayManager(ModerationSMP plugin) {
        this.plugin = plugin;
        this.monitoredOres = new HashMap<>();
        this.miningStats = new HashMap<>();
        this.globalStats = new HashMap<>();
        this.totalBlocksMined = new HashMap<>();
        this.lastReset = new HashMap<>();
        this.alertsDisabled = new HashSet<>();
        this.xrayViolations = new HashMap<>();
        
        loadConfig();
    }
    
    public void loadConfig() {
        monitoredOres.clear();
        
        // Defines alert thresholds for each ore type
        // Format: Material -> Maximum number of blocks mined in 1 minute before trigger an alert
        monitoredOres.put(Material.DIAMOND_ORE, 5);
        monitoredOres.put(Material.DEEPSLATE_DIAMOND_ORE, 5);
        monitoredOres.put(Material.ANCIENT_DEBRIS, 3);
        monitoredOres.put(Material.GOLD_ORE, 8);
        monitoredOres.put(Material.DEEPSLATE_GOLD_ORE, 8);
        monitoredOres.put(Material.EMERALD_ORE, 3);
        monitoredOres.put(Material.DEEPSLATE_EMERALD_ORE, 3);
        monitoredOres.put(Material.IRON_ORE, 15);
        monitoredOres.put(Material.DEEPSLATE_IRON_ORE, 15);
        
        plugin.getLogger().info("XrayManager: " + monitoredOres.size() + " monitored ores");
    }

    public void handleBlockBreak(Player player, Material blockType) {
        UUID uuid = player.getUniqueId();
        
        // Counts common blocks (stone, deepslate) to calculate ore/total blocks ratio
        if (isCommonBlock(blockType)) {
            totalBlocksMined.put(uuid, totalBlocksMined.getOrDefault(uuid, 0) + 1);
            return;
        }
        
        if (!monitoredOres.containsKey(blockType)) return;
        
        // Records this ore in the player's global statistics (used for leaderboard)
        Map<Material, Integer> gStats = globalStats.computeIfAbsent(uuid, k -> new HashMap<>());
        gStats.put(blockType, gStats.getOrDefault(blockType, 0) + 1);
        
        if (player.hasPermission("moderation.xray.bypass")) return;
        
        checkReset(uuid);
        
        Map<Material, Integer> stats = miningStats.computeIfAbsent(uuid, k -> new HashMap<>());
        int count = stats.getOrDefault(blockType, 0) + 1;
        stats.put(blockType, count);
        
        int threshold = monitoredOres.get(blockType);
        
        // Checks if player has exceeded the suspicious threshold (5 diamonds in 1 minute = alert)
        if (count >= threshold && count % threshold == 0) {
            notifyAdmins(player, blockType, count);
        }
    }
    
    private boolean isCommonBlock(Material mat) {
        return mat == Material.STONE || mat == Material.DEEPSLATE || mat == Material.NETHERRACK || mat == Material.TUFF || mat == Material.GRANITE || mat == Material.DIORITE || mat == Material.ANDESITE;
    }
    
    public double getPercentage(UUID uuid, Material ore) {
        int total = totalBlocksMined.getOrDefault(uuid, 0);
        if (total == 0) return 0.0;
        
        Map<Material, Integer> stats = globalStats.get(uuid);
        if (stats == null) return 0.0;
        
        int oreCount = stats.getOrDefault(ore, 0);
        return (double) oreCount / total * 100.0;
    }
    
    public int getTotalMined(UUID uuid) {
        return totalBlocksMined.getOrDefault(uuid, 0);
    }
    
    public int getOreMined(UUID uuid, Material ore) {
        if (!globalStats.containsKey(uuid)) return 0;
        return globalStats.get(uuid).getOrDefault(ore, 0);
    }
    
    // ... checkReset, notifyAdmins, toggleAlerts ...
    
    private void checkReset(UUID uuid) {
        long now = System.currentTimeMillis();
        long last = lastReset.getOrDefault(uuid, 0L);
        
        // Mining statistics are automatically reset every 60 seconds
        // This prevents a legitimate miner from accumulating too many alerts over several hours
        if (now - last > 60000) {
            miningStats.remove(uuid);
            lastReset.put(uuid, now);
        }
    }
    
    private void notifyAdmins(Player miner, Material ore, int count) {
        UUID uuid = miner.getUniqueId();
        
        // Increment violations
        int violations = xrayViolations.getOrDefault(uuid, 0) + 1;
        xrayViolations.put(uuid, violations);
        
        String message = plugin.getMessageRaw("xray_alert")
            .replace("{player}", miner.getName())
            .replace("{count}", String.valueOf(count))
            .replace("{ore}", ore.name().toLowerCase().replace("_", " "));
            
        // Notify admins
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("moderation.xray.alerts") && !alertsDisabled.contains(player.getUniqueId())) {
                player.sendMessage(plugin.getMessage("prefix") + message);
                player.sendMessage(org.bukkit.ChatColor.YELLOW + "  ⚠ Violations: " + violations + "/" + violationsBeforeBan);
            }
        }
        
        // PROGRESSIVE PUNISHMENT SYSTEM (only if auto-punishment is enabled)
        // Players with bypass permission are never punished
        if (!autoBanEnabled || miner.hasPermission("moderation.xray.bypass")) {
            return;
        }
        
        // PUNISHMENT BASED ON VIOLATION LEVEL:
        // Level 3+ = Strong expulsion (kick with severe message)
        if (violations >= violationsBeforeBan) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                miner.kickPlayer(org.bukkit.ChatColor.RED + "" + org.bukkit.ChatColor.BOLD + "EXPULSÉ\n\n" +
                               org.bukkit.ChatColor.YELLOW + "Raison: Suspicion d'utilisation de Xray\n" +
                               org.bukkit.ChatColor.GRAY + "Violations: " + violations + "\n" +
                               org.bukkit.ChatColor.RED + "⚠ Arrêtez d'utiliser des hacks !");
                
                // Annonce publique pour les admins
                Bukkit.broadcast(org.bukkit.ChatColor.RED + "[AutoMod-Xray] " + org.bukkit.ChatColor.YELLOW + 
                               miner.getName() + " kick (Xray détecté - " + violations + " infractions)", "moderation.see");
            });
            
            xrayViolations.remove(uuid); // Resets the counter after kick
            
        } else if (violations >= violationsBeforeKick) {
            // KICK
            Bukkit.getScheduler().runTask(plugin, () -> {
                miner.kickPlayer(org.bukkit.ChatColor.RED + "" + org.bukkit.ChatColor.BOLD + "EXPULSÉ\n\n" +
                               org.bukkit.ChatColor.YELLOW + "Raison: Suspicion d'utilisation de Xray\n" +
                               org.bukkit.ChatColor.GRAY + "Violations: " + violations + "/" + violationsBeforeBan + "\n" +
                               org.bukkit.ChatColor.RED + "⚠ Continuez = Kick définitif !");
                
                Bukkit.broadcast(org.bukkit.ChatColor.RED + "[AutoMod-Xray] " + org.bukkit.ChatColor.YELLOW + 
                               miner.getName() + " kick (suspicion Xray)", "moderation.see");
            });
        } else {
            // LEVEL 1: Simple warning in player's chat
            miner.sendMessage(org.bukkit.ChatColor.RED + "" + org.bukkit.ChatColor.BOLD + "⚠ AVERTISSEMENT XRAY");
            miner.sendMessage(org.bukkit.ChatColor.YELLOW + "Vos activités minières sont suspectes.");
            miner.sendMessage(org.bukkit.ChatColor.GRAY + "Violations: " + violations + "/" + violationsBeforeBan);
            miner.sendMessage(org.bukkit.ChatColor.RED + "⚠ Arrêtez d'utiliser des hacks ou vous serez kick !");
        }
    }
    
    public void toggleAlerts(Player player) {
        UUID uuid = player.getUniqueId();
        if (alertsDisabled.contains(uuid)) {
            alertsDisabled.remove(uuid);
            player.sendMessage(plugin.getMessage("xray_alerts_enabled"));
        } else {
            alertsDisabled.add(uuid);
            player.sendMessage(plugin.getMessage("xray_alerts_disabled"));
        }
    }
}
