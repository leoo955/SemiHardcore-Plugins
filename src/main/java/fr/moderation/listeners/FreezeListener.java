package fr.moderation.listeners;

import fr.moderation.ModerationSMP;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.entity.Player;

public class FreezeListener implements Listener {
    
    private final ModerationSMP plugin;
    
    public FreezeListener(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getFreezeManager().isFrozen(player.getUniqueId())) {
            // Allow looking around but not moving
            if (event.getFrom().getX() != event.getTo().getX() ||
                event.getFrom().getY() != event.getTo().getY() ||
                event.getFrom().getZ() != event.getTo().getZ()) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Vous êtes gelé ! Ne bougez pas !");
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getFreezeManager().isFrozen(player.getUniqueId())) {
            // Block all commands except /msg and /r for communication
            String command = event.getMessage().toLowerCase().split(" ")[0];
            if (!command.equals("/msg") && !command.equals("/r") && !command.equals("/tell") && 
                !command.equals("/w") && !command.equals("/reply")) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Vous ne pouvez pas utiliser de commandes pendant que vous êtes gelé !");
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getFreezeManager().isFrozen(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Vous ne pouvez pas jeter d'objets pendant que vous êtes gelé !");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        
        if (plugin.getFreezeManager().isFrozen(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Vous ne pouvez pas utiliser votre inventaire pendant que vous êtes gelé !");
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getFreezeManager().isFrozen(player.getUniqueId())) {
            // Notify admins that a frozen player disconnected
            String message = ChatColor.RED + "[FREEZE] " + ChatColor.YELLOW + player.getName() + 
                           ChatColor.RED + " s'est déconnecté alors qu'il était gelé !";
            
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                if (online.hasPermission("moderation.freeze")) {
                    online.sendMessage(message);
                }
            }
            
            plugin.getLogger().warning("Joueur gelé déconnecté: " + player.getName());
        }
    }
}
