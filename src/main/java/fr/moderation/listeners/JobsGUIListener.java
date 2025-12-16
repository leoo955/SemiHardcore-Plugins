package fr.moderation.listeners;

import fr.moderation.ModerationSMP;
import fr.moderation.jobs.JobType;
import fr.moderation.jobs.JobsGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class JobsGUIListener implements Listener {
    
    private final ModerationSMP plugin;
    
    public JobsGUIListener(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        // Check if it's a jobs GUI
        if (!title.contains("Jobs") && !isJobTitle(title)) {
            return;
        }
        
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }
        
        // Menu principal
        if (title.contains(ChatColor.GOLD + "" + ChatColor.BOLD + "Jobs")) {
            if (clicked.getType() == Material.BOOK) {
                // Bouton stats - afficher en chat
                player.closeInventory();
                player.performCommand("jobs stats");
                return;
            }
            
            // Clic sur un job
            JobType jobType = getJobTypeFromItem(clicked);
            if (jobType != null) {
                player.closeInventory();
                JobsGUI.openJobMenu(plugin, player, jobType);
            }
        }
        // Menu d'un job
        else if (isJobTitle(title)) {
            if (clicked.getType() == Material.ARROW) {
                // Bouton retour
                player.closeInventory();
                JobsGUI.openMainMenu(plugin, player);
            }
        }
    }
    
    private boolean isJobTitle(String title) {
        for (JobType type : JobType.values()) {
            if (title.contains(type.getDisplayName())) {
                return true;
            }
        }
        return false;
    }
    
    private JobType getJobTypeFromItem(ItemStack item) {
        for (JobType type : JobType.values()) {
            if (item.getType() == type.getIcon()) {
                return type;
            }
        }
        return null;
    }
}
