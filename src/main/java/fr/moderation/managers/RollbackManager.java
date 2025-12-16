package fr.moderation.managers;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.*;

public class RollbackManager {
    
    private final ModerationSMP plugin;
    private final List<BlockChange> blockHistory;
    private final int maxHistory = 10000; // Keeps the last 10000 block modifications in memory
    
    public RollbackManager(ModerationSMP plugin) {
        this.plugin = plugin;
        this.blockHistory = new ArrayList<>();
    }
    
    public void logBlockBreak(Player player, Block block) {
        BlockChange change = new BlockChange(
            player.getUniqueId(),
            player.getName(),
            block.getLocation(),
            block.getType(),
            block.getBlockData().clone(),
            Material.AIR,
            null,
            System.currentTimeMillis(),
            ChangeType.BREAK
        );
        
        addToHistory(change);
    }
    
    public void logBlockPlace(Player player, Block block, Material previousMaterial) {
        BlockChange change = new BlockChange(
            player.getUniqueId(),
            player.getName(),
            block.getLocation(),
            previousMaterial,
            null,
            block.getType(),
            block.getBlockData().clone(),
            System.currentTimeMillis(),
            ChangeType.PLACE
        );
        
        addToHistory(change);
    }
    
    private void addToHistory(BlockChange change) {
        blockHistory.add(change);
        
        // If we exceed 10000 changes, remove the oldest ones to free memory
        if (blockHistory.size() > maxHistory) {
            blockHistory.remove(0);
        }
    }
    
    public int rollback(long timeSeconds, int radius, Location center, String playerFilter) {
        long cutoffTime = System.currentTimeMillis() - (timeSeconds * 1000);
        int count = 0;
        
        // Iterate backwards to undo the most recent actions first
        List<BlockChange> toRollback = new ArrayList<>();
        
        for (int i = blockHistory.size() - 1; i >= 0; i--) {
            BlockChange change = blockHistory.get(i);
            
            // Filter: only modifications made in the requested period
            if (change.getTimestamp() < cutoffTime) {
                break; // Older than requested time
            }
            
            // Filter: only blocks in the specified zone (if a radius is defined)
            if (center != null && radius > 0) {
                if (change.getLocation().distance(center) > radius) {
                    continue;
                }
            }
            
            // Filter: only actions from a specific player (if specified)
            if (playerFilter != null && !change.getPlayerName().equalsIgnoreCase(playerFilter)) {
                continue;
            }
            
            toRollback.add(change);
        }
        
        // Restores each block to its state BEFORE the modification
        for (BlockChange change : toRollback) {
            try {
                Block block = change.getLocation().getBlock();
                
                // Restores the block as it was before the player modified it
                block.setType(change.getOldMaterial());
                if (change.getOldBlockData() != null) {
                    block.setBlockData(change.getOldBlockData());
                }
                
                count++;
            } catch (Exception e) {
                plugin.getLogger().warning("Erreur lors du rollback d'un bloc: " + e.getMessage());
            }
        }
        
        return count;
    }
    
    public int restore(long timeSeconds, int radius, Location center, String playerFilter) {
        long cutoffTime = System.currentTimeMillis() - (timeSeconds * 1000);
        int count = 0;
        
        // Lists all changes that match the restore criteria
        List<BlockChange> toRestore = new ArrayList<>();
        
        for (BlockChange change : blockHistory) {
            // Check time
            if (change.getTimestamp() < cutoffTime) {
                continue;
            }
            
            // Check radius
            if (center != null && radius > 0) {
                if (change.getLocation().distance(center) > radius) {
                    continue;
                }
            }
            
            // Check player filter
            if (playerFilter != null && !change.getPlayerName().equalsIgnoreCase(playerFilter)) {
                continue;
            }
            
            toRestore.add(change);
        }
        
        // Reapplies the modifications (reverse of rollback)
        for (BlockChange change : toRestore) {
            try {
                Block block = change.getLocation().getBlock();
                
                // Restores the block to the state AFTER the player's modification
                block.setType(change.getNewMaterial());
                if (change.getNewBlockData() != null) {
                    block.setBlockData(change.getNewBlockData());
                }
                
                count++;
            } catch (Exception e) {
                plugin.getLogger().warning("Erreur lors du restore d'un bloc: " + e.getMessage());
            }
        }
        
        return count;
    }
    
    public int getHistorySize() {
        return blockHistory.size();
    }
    
    // Object that stores all info about a modification: who, what, where, when
    public static class BlockChange {
        private final UUID playerUUID;
        private final String playerName;
        private final Location location;
        private final Material oldMaterial;
        private final BlockData oldBlockData;
        private final Material newMaterial;
        private final BlockData newBlockData;
        private final long timestamp;
        private final ChangeType type;
        
        public BlockChange(UUID playerUUID, String playerName, Location location, 
                          Material oldMaterial, BlockData oldBlockData,
                          Material newMaterial, BlockData newBlockData,
                          long timestamp, ChangeType type) {
            this.playerUUID = playerUUID;
            this.playerName = playerName;
            this.location = location.clone();
            this.oldMaterial = oldMaterial;
            this.oldBlockData = oldBlockData;
            this.newMaterial = newMaterial;
            this.newBlockData = newBlockData;
            this.timestamp = timestamp;
            this.type = type;
        }
        
        public UUID getPlayerUUID() {
            return playerUUID;
        }
        
        public String getPlayerName() {
            return playerName;
        }
        
        public Location getLocation() {
            return location;
        }
        
        public Material getOldMaterial() {
            return oldMaterial;
        }
        
        public BlockData getOldBlockData() {
            return oldBlockData;
        }
        
        public Material getNewMaterial() {
            return newMaterial;
        }
        
        public BlockData getNewBlockData() {
            return newBlockData;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public ChangeType getType() {
            return type;
        }
    }
    
    public enum ChangeType {
        BREAK,
        PLACE
    }
}
