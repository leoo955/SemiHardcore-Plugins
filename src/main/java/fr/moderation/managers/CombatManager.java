package fr.moderation.managers;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CombatManager {
    
    private final ModerationSMP plugin;
    private final Map<UUID, Long> combatPlayers; // UUID -> combat end timestamp
    private final int combatDuration; // in seconds
    
    public CombatManager(ModerationSMP plugin) {
        this.plugin = plugin;
        this.combatPlayers = new HashMap<>();
        this.combatDuration = plugin.getConfig().getInt("settings.combat_duration", 15);
        
        // Task to manage combat (Action Bar + Expiration)
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                Iterator<Map.Entry<UUID, Long>> iterator = combatPlayers.entrySet().iterator();
                
                while (iterator.hasNext()) {
                    Map.Entry<UUID, Long> entry = iterator.next();
                    UUID uuid = entry.getKey();
                    long endTime = entry.getValue();
                    Player player = Bukkit.getPlayer(uuid);
                    
                    if (player == null) {
                        iterator.remove();
                        continue;
                    }
                    
                    if (now > endTime) {
                        // Combat ended
                        player.sendMessage(plugin.getMessage("combat_expired"));
                        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText("§aCombat terminé"));
                        iterator.remove();
                    } else {
                        // Combat in progress
                        long remaining = (endTime - now) / 1000;
                        String message = "§cCombat : " + remaining + "s";
                        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(message));
                    }
                }
            }
        }.runTaskTimer(plugin, 5L, 5L); // Every 0.25 seconds for smoothness
    }
    
    public void tagPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        long endTime = System.currentTimeMillis() + (combatDuration * 1000L);
        
        boolean wasInCombat = isInCombat(player);
        combatPlayers.put(uuid, endTime);
        
        if (!wasInCombat) {
            player.sendMessage(plugin.getMessage("combat_tagged"));
        }
    }
    
    public void tagPlayers(Player player1, Player player2) {
        tagPlayer(player1);
        tagPlayer(player2);
    }
    
    public boolean isInCombat(Player player) {
        Long endTime = combatPlayers.get(player.getUniqueId());
        if (endTime == null) {
            return false;
        }
        return endTime > System.currentTimeMillis();
    }
    
    public int getRemainingTime(Player player) {
        Long endTime = combatPlayers.get(player.getUniqueId());
        if (endTime == null) {
            return 0;
        }
        long remaining = (endTime - System.currentTimeMillis()) / 1000;
        return (int) Math.max(0, remaining);
    }
    
    public void removeCombatTag(Player player) {
        combatPlayers.remove(player.getUniqueId());
    }
    
    public void handlePlayerQuit(Player player) {
        if (isInCombat(player)) {
            // Tuer le joueur pour éviter le combat log
            player.setHealth(0);
            
            // Broadcast message
            String message = plugin.getMessageRaw("combat_logout")
                .replace("{player}", player.getName());
            Bukkit.broadcastMessage(message);
            
            plugin.getLogger().info(player.getName() + " disconnected in combat and was killed.");
        }
        
        removeCombatTag(player);
    }
    
    public void handleServerShutdown() {
        // Don't kill players during server shutdown
        combatPlayers.clear();
    }
    
    public Set<UUID> getCombatPlayers() {
        return new HashSet<>(combatPlayers.keySet());
    }
}
