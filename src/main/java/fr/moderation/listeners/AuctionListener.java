package fr.moderation.listeners;

import fr.moderation.ModerationSMP;
import fr.moderation.auction.AuctionGUI;
import fr.moderation.auction.AuctionHouse;
import fr.moderation.auction.AuctionListing;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class AuctionListener implements Listener {
    
    private final ModerationSMP plugin;
    
    public AuctionListener(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        // Vérifier si c'est un GUI d'AH
        if (!title.contains("Auction House") && !title.contains("Mes ventes") && !title.contains("Items expirés")) {
            return;
        }
        
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }
        
        AuctionHouse ah = plugin.getAuctionHouse();
        
        // Menu principal
        if (title.contains("Auction House")) {
            if (clicked.getType() == Material.CHEST) {
                // Mes ventes
                player.closeInventory();
                AuctionGUI.openPlayerListings(plugin, player);
                return;
            }
            
            if (clicked.getType() == Material.HOPPER) {
                // Items expirés
                player.closeInventory();
                AuctionGUI.openExpiredItems(plugin, player);
                return;
            }
            
            // Achat d'un item
            handlePurchase(player, clicked, event.getSlot());
        }
        
        // Mes ventes
        else if (title.contains("Mes ventes")) {
            if (clicked.getType() == Material.ARROW) {
                // Retour
                player.closeInventory();
                AuctionGUI.openMainMenu(plugin, player);
                return;
            }
            
            if (event.isRightClick()) {
                // Annuler une vente
                handleCancel(player, event.getSlot());
            }
        }
        
        // Items expirés
        else if (title.contains("Items expirés")) {
            if (clicked.getType() == Material.ARROW) {
                // Retour
                player.closeInventory();
                AuctionGUI.openMainMenu(plugin, player);
                return;
            }
            
            if (clicked.getType() != Material.BARRIER) {
                // Récupérer un item
                handleExpiredClaim(player, event.getSlot());
            }
        }
    }
    
    private void handlePurchase(Player player, ItemStack clicked, int slot) {
        List<AuctionListing> listings = plugin.getAuctionHouse().getAllListings();
        
        if (slot >= listings.size()) {
            return;
        }
        
        AuctionListing listing = listings.get(slot);
        
        // Vérifier que ce n'est pas le vendeur
        if (listing.getSellerId().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Vous ne pouvez pas acheter vos propres items !");
            return;
        }
        
        // Vérifier le solde
        if (!plugin.getEconomyManager().hasBalance(player.getUniqueId(), listing.getPrice())) {
            player.sendMessage(ChatColor.RED + "Vous n'avez pas assez d'argent !");
            player.sendMessage(ChatColor.GRAY + "Prix: " + ChatColor.YELLOW + 
                             plugin.getEconomyManager().format(listing.getPrice()));
            return;
        }
        
        // Vérifier l'espace dans l'inventaire
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ChatColor.RED + "Votre inventaire est plein !");
            return;
        }
        
        // Effectuer l'achat
        plugin.getEconomyManager().removeBalance(player.getUniqueId(), listing.getPrice());
        plugin.getEconomyManager().addBalance(listing.getSellerId(), listing.getPrice());
        
        if (plugin.getAuctionHouse().purchaseListing(listing.getListingId(), player.getUniqueId())) {
            player.getInventory().addItem(listing.getItem());
            player.sendMessage(ChatColor.GREEN + "Achat effectué pour " + ChatColor.GOLD + 
                             plugin.getEconomyManager().format(listing.getPrice()));
            
            // Notifier le vendeur s'il est en ligne
            Player seller = plugin.getServer().getPlayer(listing.getSellerId());
            if (seller != null) {
                seller.sendMessage(ChatColor.GREEN + "Votre item a été vendu pour " + ChatColor.GOLD + 
                                 plugin.getEconomyManager().format(listing.getPrice()));
            }
            
            player.closeInventory();
            AuctionGUI.openMainMenu(plugin, player);
        } else {
            // Rembourser
            plugin.getEconomyManager().addBalance(player.getUniqueId(), listing.getPrice());
            player.sendMessage(ChatColor.RED + "Erreur lors de l'achat !");
        }
    }
    
    private void handleCancel(Player player, int slot) {
        List<AuctionListing> listings = plugin.getAuctionHouse().getPlayerListings(player.getUniqueId());
        
        if (slot >= listings.size()) {
            return;
        }
        
        AuctionListing listing = listings.get(slot);
        
        ItemStack item = plugin.getAuctionHouse().cancelListing(listing.getListingId(), player.getUniqueId());
        
        if (item != null) {
            player.getInventory().addItem(item);
            player.sendMessage(ChatColor.YELLOW + "Vente annulée ! Item retourné.");
            player.closeInventory();
            AuctionGUI.openPlayerListings(plugin, player);
        } else {
            player.sendMessage(ChatColor.RED + "Impossible d'annuler cette vente !");
        }
    }
    
    private void handleExpiredClaim(Player player, int slot) {
        List<ItemStack> expired = plugin.getAuctionHouse().getExpiredItems(player.getUniqueId());
        
        if (slot >= expired.size()) {
            return;
        }
        
        ItemStack item = expired.get(slot);
        
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ChatColor.RED + "Votre inventaire est plein !");
            return;
        }
        
        player.getInventory().addItem(item);
        plugin.getAuctionHouse().removeExpiredItem(player.getUniqueId(), slot);
        player.sendMessage(ChatColor.GREEN + "Item récupéré !");
        
        player.closeInventory();
        AuctionGUI.openExpiredItems(plugin, player);
    }
}
