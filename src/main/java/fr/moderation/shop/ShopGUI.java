package fr.moderation.shop;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ShopGUI {
    
    /**
     * Ouvre le menu des catégories
     */
    public static void openCategoriesMenu(ModerationSMP plugin, Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "" + ChatColor.BOLD + "Shop - Catégories");
        
        int slot = 10;
        for (ShopCategory category : ShopCategory.values()) {
            ItemStack icon = new ItemStack(category.getIcon());
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + category.getDisplayName());
            
            int itemCount = plugin.getShopManager().getItemsByCategory(category).size();
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Items: " + ChatColor.WHITE + itemCount);
            lore.add("");
            lore.add(ChatColor.GREEN + "Cliquez pour ouvrir");
            meta.setLore(lore);
            
            icon.setItemMeta(meta);
            inv.setItem(slot++, icon);
            
            if (slot == 17) slot = 19; // Sauter une ligne
        }
        
        player.openInventory(inv);
    }
    
    /**
     * Ouvre le menu d'une catégorie
     */
    public static void openCategoryMenu(ModerationSMP plugin, Player player, ShopCategory category) {
        List<ShopItem> items = plugin.getShopManager().getItemsByCategory(category);
        
        int pages = (int) Math.ceil(items.size() / 45.0);
        openCategoryPage(plugin, player, category, 0, pages);
    }
    
    /**
     * Ouvre une page spécifique d'une catégorie
     */
    public static void openCategoryPage(ModerationSMP plugin, Player player, ShopCategory category, int page, int totalPages) {
        List<ShopItem> items = plugin.getShopManager().getItemsByCategory(category);
        
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.YELLOW + category.getDisplayName() + " (Page " + (page + 1) + "/" + totalPages + ")");
        
        // Afficher les items (slots 0-44)
        int start = page * 45;
        int end = Math.min(start + 45, items.size());
        
        int slot = 0;
        for (int i = start; i < end; i++) {
            ShopItem shopItem = items.get(i);
            
            ItemStack displayItem = new ItemStack(shopItem.getMaterial());
            ItemMeta meta = displayItem.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + shopItem.getDisplayName());
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Prix: " + ChatColor.GOLD + plugin.getEconomyManager().format(shopItem.getPrice()));
            lore.add("");
            lore.add(ChatColor.GREEN + "Clic gauche: Acheter x1");
            lore.add(ChatColor.GREEN + "Clic droit: Acheter x64");
            lore.add(ChatColor.GREEN + "Shift + Clic: Acheter x16");
            
            meta.setLore(lore);
            displayItem.setItemMeta(meta);
            
            inv.setItem(slot++, displayItem);
        }
        
        // Boutons de navigation
        ItemStack back = createButton(Material.ARROW, ChatColor.GRAY + "← Retour", 
                                     ChatColor.GRAY + "Retour aux catégories");
        inv.setItem(49, back);
        
        if (page > 0) {
            ItemStack prevPage = createButton(Material.SPECTRAL_ARROW, ChatColor.YELLOW + "← Page précédente", 
                                            ChatColor.GRAY + "Page " + page);
            inv.setItem(48, prevPage);
        }
        
        if (page < totalPages - 1) {
            ItemStack nextPage = createButton(Material.SPECTRAL_ARROW, ChatColor.YELLOW + "Page suivante →", 
                                            ChatColor.GRAY + "Page " + (page + 2));
            inv.setItem(50, nextPage);
        }
        
        player.openInventory(inv);
    }
    
    /**
     * Crée un bouton
     */
    private static ItemStack createButton(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        
        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(line);
        }
        meta.setLore(loreList);
        
        item.setItemMeta(meta);
        return item;
    }
}
