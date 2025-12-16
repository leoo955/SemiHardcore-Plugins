package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class RtpCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    private final Map<UUID, Long> cooldowns;
    private final Random random;
    
    // Configuration
    private static final int MIN_DISTANCE = 500;    // Distance minimale du spawn
    private static final int MAX_DISTANCE = 5000;   // Distance maximale du spawn
    private static final int COOLDOWN_SECONDS = 60; // Cooldown entre chaque /rtp (1 minute)
    private static final int MAX_ATTEMPTS = 10;     // Nombre max de tentatives pour trouver un spawn safe
    
    public RtpCommand(ModerationSMP plugin) {
        this.plugin = plugin;
        this.cooldowns = new HashMap<>();
        this.random = new Random();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Seul un joueur peut utiliser cette commande.");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check if player is in death world
        if (plugin.getDeathWorldManager().isInDeathWorld(player)) {
            if (!player.hasPermission("moderation.deathdim.bypass")) {
                player.sendMessage(ChatColor.RED + "Vous ne pouvez pas vous téléporter depuis le monde de la mort !");
                return true;
            }
        }
        
        // Check cooldown (except for OPs)
        if (!player.hasPermission("moderation.rtp.nocooldown")) {
            UUID uuid = player.getUniqueId();
            long now = System.currentTimeMillis();
            
            if (cooldowns.containsKey(uuid)) {
                long lastUse = cooldowns.get(uuid);
                long timeLeft = (lastUse + (COOLDOWN_SECONDS * 1000)) - now;
                
                if (timeLeft > 0) {
                    int secondsLeft = (int) (timeLeft / 1000);
                    player.sendMessage(ChatColor.RED + "Vous devez attendre encore " + secondsLeft + " secondes avant de réutiliser /rtp !");
                    return true;
                }
            }
            
            cooldowns.put(uuid, now);
        }
        
        player.sendMessage(ChatColor.YELLOW + "Recherche d'un emplacement sûr...");
        
        // Recherche asynchrone pour ne pas lag le serveur
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Location safeLoc = findSafeLocation(player.getWorld());
            
            if (safeLoc != null) {
                // Teleportation on main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    // Sauvegarder la position actuelle pour /back
                    plugin.getLastLocationManager().setLastLocation(player, player.getLocation());
                    
                    player.teleport(safeLoc);
                    player.sendMessage(ChatColor.GREEN + "Téléportation aléatoire réussie !");
                    player.sendMessage(ChatColor.GRAY + "Position: " + 
                                     safeLoc.getBlockX() + ", " + 
                                     safeLoc.getBlockY() + ", " + 
                                     safeLoc.getBlockZ());
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                });
            } else {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage(ChatColor.RED + "Impossible de trouver un emplacement sûr. Réessayez !");
                    // Rembourser le cooldown
                    if (!player.hasPermission("moderation.rtp.nocooldown")) {
                        cooldowns.remove(player.getUniqueId());
                    }
                });
            }
        });
        
        return true;
    }
    
    private Location findSafeLocation(World world) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            // Generate random distance
            int distance = MIN_DISTANCE + random.nextInt(MAX_DISTANCE - MIN_DISTANCE);
            
            // Random angle
            double angle = random.nextDouble() * 2 * Math.PI;
            
            // Calculate X and Z coordinates
            int x = (int) (distance * Math.cos(angle));
            int z = (int) (distance * Math.sin(angle));
            
            // Find safe height
            Location testLoc = new Location(world, x, 100, z);
            
            // Chercher le bloc solide le plus haut
            Block highestBlock = world.getHighestBlockAt(testLoc);
            int y = highestBlock.getY();
            
            // Check if it's a safe location
            if (isSafeLocation(world, x, y, z)) {
                return new Location(world, x + 0.5, y + 1, z + 0.5);
            }
        }
        
        return null; // No safe location found after MAX_ATTEMPTS
    }
    
    private boolean isSafeLocation(World world, int x, int y, int z) {
        // Check that Y is within reasonable limits
        if (y < 0 || y > 250) {
            return false;
        }
        
        Block ground = world.getBlockAt(x, y, z);
        Block feet = world.getBlockAt(x, y + 1, z);
        Block head = world.getBlockAt(x, y + 2, z);
        
        // Ground must be solid
        if (!ground.getType().isSolid()) {
            return false;
        }
        
        // Ne pas spawn dans des blocs dangereux
        Material groundType = ground.getType();
        if (groundType == Material.LAVA || 
            groundType == Material.MAGMA_BLOCK ||
            groundType == Material.FIRE ||
            groundType == Material.CACTUS) {
            return false;
        }
        
        // Need air at feet and head level
        if (!feet.getType().isAir() || !head.getType().isAir()) {
            return false;
        }
        
        // Check there's no water/lava immediately below
        Block below = world.getBlockAt(x, y - 1, z);
        if (below.getType() == Material.WATER || below.getType() == Material.LAVA) {
            return false;
        }
        
        return true;
    }
}
