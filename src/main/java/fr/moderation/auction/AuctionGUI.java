package fr.moderation.auction;

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
import java.util.concurrent.TimeUnit;

public class AuctionGUI {
    
    /**
     * Ouvre le menu principal de l'AH
     */
    public static void openMainMenu(ModerationSMP plugin, Player player) {
        List<AuctionListing> listings = plugin.getAuctionHouse().getAllListings();
        
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "" + ChatColor.BOLD + "Auction House");
        
        // Afficher les items (slots 0-44)
        int slot = 0;
        for (AuctionListing listing : listings) {
            if (slot >= 45) break;
            
            ItemStack displayItem = listing.getItem().clone();
            ItemMeta meta = displayItem.getItemMeta();
            
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.YELLOW + "Vendeur: " + ChatColor.WHITE + listing.getSellerName());
            lore.add(ChatColor.YELLOW + "Prix: " + ChatColor.GOLD + plugin.getEconomyManager().format(listing.getPrice()));
            lore.add(ChatColor.GRAY + "Expire dans: " + ChatColor.WHITE + formatTime(listing.getTimeLeft()));
            lore.add("");
            lore.add(ChatColor.GREEN + "Clic gauche pour acheter");
            
            meta.setLore(lore);
            displayItem.setItemMeta(meta);
            
            inv.setItem(slot++, displayItem);
        }
        
        // Boutons de navigation
        ItemStack myListings = createButton(Material.CHEST, ChatColor.YELLOW + "Mes ventes", 
                                           ChatColor.GRAY + "Voir mes items en vente");
        inv.setItem(49, myListings);
        
        ItemStack expiredItems = createButton(Material.HOPPER, ChatColor.RED + "Items expirés", 
                                             ChatColor.GRAY + "Récupérer vos items expirés");
        inv.setItem(53, expiredItems);
        
        player.openInventory(inv);
    }
    
    /**
     * Ouvre les ventes du joueur
     */
    public static void openPlayerListings(ModerationSMP plugin, Player player) {
        List<AuctionListing> listings = plugin.getAuctionHouse().getPlayerListings(player.getUniqueId());
        
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.YELLOW + "Mes ventes");
        
        int slot = 0;
        for (AuctionListing listing : listings) {
            if (slot >= 45) break;
            
            ItemStack displayItem = listing.getItem().clone();
            ItemMeta meta = displayItem.getItemMeta();
            
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.YELLOW + "Prix: " + ChatColor.GOLD + plugin.getEconomyManager().format(listing.getPrice()));
            lore.add(ChatColor.GRAY + "Expire dans: " + ChatColor.WHITE + formatTime(listing.getTimeLeft()));
            lore.add("");
            lore.add(ChatColor.RED + "Clic droit pour annuler");
            
            meta.setLore(lore);
            displayItem.setItemMeta(meta);
            
            inv.setItem(slot++, displayItem);
        }
        
        // Bouton retour
        ItemStack back = createButton(Material.ARROW, ChatColor.GRAY + "← Retour", 
                                     ChatColor.GRAY + "Retour au menu principal");
        inv.setItem(49, back);
        
        player.openInventory(inv);
    }
    
    /**
     * Ouvre les items expirés
     */
    public static void openExpiredItems(ModerationSMP plugin, Player player) {
        List<ItemStack> expired = plugin.getAuctionHouse().getExpiredItems(player.getUniqueId());
        
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.RED + "Items expirés");
        
        int slot = 0;
        for (ItemStack item : expired) {
            if (slot >= 45) break;
            
            ItemStack displayItem = item.clone();
            ItemMeta meta = displayItem.getItemMeta();
            
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Cliquez pour récupérer");
            
            meta.setLore(lore);
            displayItem.setItemMeta(meta);
            
            inv.setItem(slot++, displayItem);
        }
        
        if (expired.isEmpty()) {
            ItemStack none = createButton(Material.BARRIER, ChatColor.RED + "Aucun item expiré", 
                                        ChatColor.GRAY + "Vous n'avez aucun item à récupérer");
            inv.setItem(22, none);
        }
        
        // Bouton retour
        ItemStack back = createButton(Material.ARROW, ChatColor.GRAY + "← Retour", 
                                     ChatColor.GRAY + "Retour au menu principal");
        inv.setItem(49, back);
        
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
    
    /**
     * Formate le temps restant
     */
    private static String formatTime(long millis) {
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        
        if (days > 0) {
            return days + "j " + hours + "h";
        } else if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            return minutes + "m";
        }
    }
}
