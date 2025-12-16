package fr.moderation.listeners;

import fr.moderation.ModerationSMP;
import fr.moderation.shop.ShopCategory;
import fr.moderation.shop.ShopGUI;
import fr.moderation.shop.ShopItem;
import fr.moderation.shop.ShopManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShopListener implements Listener {
    
    private final ModerationSMP plugin;
    
    public ShopListener(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        // Remove color codes for verification
        String cleanTitle = ChatColor.stripColor(title);
        
        // Debug log
        plugin.getLogger().info("Shop GUI Check - Title: '" + cleanTitle + "'");
        
        // Check if it's a shop GUI (multiple patterns)
        boolean isShopGUI = cleanTitle.contains("Shop") || 
                           cleanTitle.contains("Catégories") ||
                           cleanTitle.contains("category") ||
                           (cleanTitle.contains("Page") && cleanTitle.contains("/"));
        
        if (!isShopGUI) {
            return;
        }
        
        // BLOCK ALL ITEM MOVEMENT
        event.setCancelled(true);
        
        plugin.getLogger().info("Shop GUI - Event cancelled for: " + player.getName());
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }
        
        // Categories menu
        if (cleanTitle.contains("Catégories")) {
            ShopCategory category = getCategoryFromIcon(clicked.getType());
            if (category != null) {
                player.closeInventory();
                ShopGUI.openCategoryMenu(plugin, player, category);
            }
        }
        // Category menu
        else {
            // Back button
            if (clicked.getType() == Material.ARROW) {
                player.closeInventory();
                ShopGUI.openCategoriesMenu(plugin, player);
                return;
            }
            
            // Pagination buttons
            if (clicked.getType() == Material.SPECTRAL_ARROW) {
                handlePagination(player, cleanTitle, clicked);
                return;
            }
            
            // Item purchase
            handlePurchase(player, clicked, event.getClick());
        }
    }
    
    private ShopCategory getCategoryFromIcon(Material icon) {
        for (ShopCategory category : ShopCategory.values()) {
            if (category.getIcon() == icon) {
                return category;
            }
        }
        return null;
    }
    
    private void handlePagination(Player player, String cleanTitle, ItemStack clicked) {
        // Extract category name and page from title (already clean, no color codes)
        String[] parts = cleanTitle.split(" \\(Page ");
        if (parts.length < 2) return;
        
        String categoryName = parts[0].trim();
        ShopCategory category = null;
        
        for (ShopCategory cat : ShopCategory.values()) {
            if (cat.getDisplayName().equals(categoryName)) {
                category = cat;
                break;
            }
        }
        
        if (category == null) return;
        
        // Extract page number
        Pattern pattern = Pattern.compile("Page (\\d+)/(\\d+)\\)");
        Matcher matcher = pattern.matcher(cleanTitle);
        if (!matcher.find()) return;
        
        int currentPage = Integer.parseInt(matcher.group(1)) - 1;
        int totalPages = Integer.parseInt(matcher.group(2));
        
        String lore = clicked.getItemMeta().getLore().get(0);
        String cleanLore = ChatColor.stripColor(lore);
        if (cleanLore.contains("précédente")) {
            player.closeInventory();
            ShopGUI.openCategoryPage(plugin, player, category, currentPage - 1, totalPages);
        } else if (cleanLore.contains("suivante")) {
            player.closeInventory();
            ShopGUI.openCategoryPage(plugin, player, category, currentPage + 1, totalPages);
        }
    }
    
    private void handlePurchase(Player player, ItemStack clicked, ClickType clickType) {
        ShopManager shopManager = plugin.getShopManager();
        ShopItem shopItem = shopManager.getShopItem(clicked.getType());
        
        if (shopItem == null) {
            return;
        }
        
        // Determine quantity
        int amount;
        if (clickType == ClickType.RIGHT) {
            amount = 64;
        } else if (clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT) {
            amount = 16;
        } else {
            amount = 1;
        }
        
        double totalPrice = shopItem.getPrice() * amount;
        
        // Check balance
        if (!plugin.getEconomyManager().hasBalance(player.getUniqueId(), totalPrice)) {
            player.sendMessage(ChatColor.RED + "Vous n'avez pas assez d'argent !");
            player.sendMessage(ChatColor.GRAY + "Prix total: " + ChatColor.YELLOW + 
                             plugin.getEconomyManager().format(totalPrice));
            player.sendMessage(ChatColor.GRAY + "Votre solde: " + ChatColor.YELLOW + 
                             plugin.getEconomyManager().format(plugin.getEconomyManager().getBalance(player.getUniqueId())));
            return;
        }
        
        // Check inventory space
        ItemStack itemToGive = shopItem.createItemStack(amount);
        if (!canFitInInventory(player, itemToGive)) {
            player.sendMessage(ChatColor.RED + "Vous n'avez pas assez d'espace dans votre inventaire !");
            return;
        }
        
        // Make purchase
        plugin.getEconomyManager().removeBalance(player.getUniqueId(), totalPrice);
        player.getInventory().addItem(itemToGive);
        
        player.sendMessage(ChatColor.GREEN + "Achat effectué !");
        player.sendMessage(ChatColor.GRAY + "Item: " + ChatColor.YELLOW + shopItem.getDisplayName() + 
                         ChatColor.GRAY + " x" + amount);
        player.sendMessage(ChatColor.GRAY + "Prix: " + ChatColor.GOLD + plugin.getEconomyManager().format(totalPrice));
    }
    
    private boolean canFitInInventory(Player player, ItemStack item) {
        int needed = item.getAmount();
        
        for (ItemStack invItem : player.getInventory().getContents()) {
            if (invItem == null || invItem.getType() == Material.AIR) {
                needed -= item.getMaxStackSize();
            } else if (invItem.isSimilar(item)) {
                int space = item.getMaxStackSize() - invItem.getAmount();
                needed -= space;
            }
            
            if (needed <= 0) {
                return true;
            }
        }
        
        return needed <= 0;
    }
}
