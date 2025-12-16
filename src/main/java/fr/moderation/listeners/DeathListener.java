package fr.moderation.listeners;

import fr.moderation.ModerationSMP;
import fr.moderation.managers.DeathWorldManager;
import fr.moderation.managers.PuzzleManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import java.util.ArrayList;
import java.util.List;

public class DeathListener implements Listener {
    
    private final ModerationSMP plugin;
    private final DeathWorldManager deathWorldManager;
    private final PuzzleManager puzzleManager;
    
    public DeathListener(ModerationSMP plugin, DeathWorldManager deathWorldManager, PuzzleManager puzzleManager) {
        this.plugin = plugin;
        this.deathWorldManager = deathWorldManager;
        this.puzzleManager = puzzleManager;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        plugin.getLogger().info("DeathListener: Mort de " + player.getName());
        
        // Check if system is enabled
        if (!plugin.getConfig().getBoolean("death-dimension.enabled", true)) {
            plugin.getLogger().info("DeathListener: Système désactivé");
            return;
        }
        
        plugin.getLogger().info("DeathListener: Système activé, vérification bypass");
        
        // Check if player has bypass permission
        if (player.hasPermission("moderation.deathdim.bypass")) {
            plugin.getLogger().info("DeathListener: Joueur a bypass permission");
            return;
        }
        
        // Check if player is already in death world (avoid loop)
        if (deathWorldManager.isDeathWorld(player.getWorld())) {
            plugin.getLogger().info("DeathListener: Joueur déjà dans death world");
            return;
        }
        
        plugin.getLogger().info("DeathListener: Tous les checks passés!");
        
        // Message de mort custom
        String deathMessage = plugin.getConfig().getString("messages.death_dimension_death",
            "&c{player} est mort et a été envoyé dans le monde de la mort...");
        deathMessage = deathMessage.replace("{player}", player.getName());
        event.setDeathMessage(ChatColor.translateAlternateColorCodes('&', deathMessage));
        
        // Drop player head
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        org.bukkit.inventory.meta.SkullMeta meta = (org.bukkit.inventory.meta.SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName(ChatColor.RED + "Tête de " + player.getName());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Utilisez cette tête pour");
        lore.add(ChatColor.GRAY + "ressusciter ce joueur !");
        lore.add(ChatColor.DARK_GRAY + player.getUniqueId().toString()); // UUID hidden in lore for identification
        meta.setLore(lore);
        head.setItemMeta(meta);
        
        player.getWorld().dropItemNaturally(player.getLocation(), head);
        
        // Handle inventory according to config
        boolean keepInventory = plugin.getConfig().getBoolean("death-dimension.keep-inventory", true);
        event.setKeepInventory(keepInventory);
        event.setKeepLevel(keepInventory);
        
        if (!keepInventory) {
            event.getDrops().clear();
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        // Check if system is enabled
        if (!plugin.getConfig().getBoolean("death-dimension.enabled", true)) {
            return;
        }
        
        // Check if player has bypass permission
        if (player.hasPermission("moderation.deathdim.bypass")) {
            return;
        }
        
        // Teleport to death world after short delay
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            deathWorldManager.teleportToDeathWorld(player);
            
            plugin.getLogger().info("Joueur " + player.getName() + " téléporté au monde de la mort");
            
            // Create puzzle after longer delay (so teleportation is complete)
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                plugin.getLogger().info("Création du puzzle pour " + player.getName());
                puzzleManager.createPuzzleForPlayer(player);
                plugin.getLogger().info("Puzzle créé pour " + player.getName());
            }, 40L); // 2 seconds after teleportation
        }, 1L); // 1 tick delay to avoid conflicts
    }
}

