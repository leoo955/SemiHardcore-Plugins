package fr.moderation.listeners;

import fr.moderation.ModerationSMP;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AuthListener implements Listener {
    
    private final ModerationSMP plugin;
    
    public AuthListener(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Envoyer le message de login/register
        if (plugin.getAuthManager().isRegistered(player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("auth_join_login"));
        } else {
            player.sendMessage(plugin.getMessage("auth_join_register"));
        }
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getAuthManager().logout(event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(PlayerMoveEvent event) {
        if (!plugin.getAuthManager().isLoggedIn(event.getPlayer())) {
            // Empêcher le mouvement mais permettre la rotation de la caméra
            if (event.getFrom().getX() != event.getTo().getX() || 
                event.getFrom().getZ() != event.getTo().getZ() || 
                event.getFrom().getY() != event.getTo().getY()) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!plugin.getAuthManager().isLoggedIn(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getMessage("auth_required"));
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getAuthManager().isLoggedIn(player)) {
            String msg = event.getMessage().toLowerCase();
            if (!msg.startsWith("/login") && !msg.startsWith("/register") && !msg.startsWith("/l") && !msg.startsWith("/reg")) {
                event.setCancelled(true);
                player.sendMessage(plugin.getMessage("auth_required"));
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (!plugin.getAuthManager().isLoggedIn(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getAuthManager().isLoggedIn(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.getAuthManager().isLoggedIn(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            if (!plugin.getAuthManager().isLoggedIn((Player) event.getWhoClicked())) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDrop(PlayerDropItemEvent event) {
        if (!plugin.getAuthManager().isLoggedIn(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            if (!plugin.getAuthManager().isLoggedIn((Player) event.getEntity())) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            if (!plugin.getAuthManager().isLoggedIn((Player) event.getEntity())) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            if (!plugin.getAuthManager().isLoggedIn((Player) event.getDamager())) {
                event.setCancelled(true);
            }
        }
    }
}
