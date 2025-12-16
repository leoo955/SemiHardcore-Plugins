package fr.moderation.auction;

import fr.moderation.ModerationSMP;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AuctionHouse {
    
    private final ModerationSMP plugin;
    private final Map<UUID, AuctionListing> activeListings;
    private final Map<UUID, List<ItemStack>> expiredItems;
    private final File auctionFile;
    private FileConfiguration auctionConfig;
    
    // Configuration
    public static final int MAX_LISTINGS_PER_PLAYER = 10;
    public static final long LISTING_DURATION = TimeUnit.DAYS.toMillis(7); // 7 jours
    public static final double COMMISSION_PERCENT = 5.0;
    public static final double MIN_PRICE = 1.0;
    public static final double MAX_PRICE = 1000000.0;
    
    public AuctionHouse(ModerationSMP plugin) {
        this.plugin = plugin;
        this.activeListings = new HashMap<>();
        this.expiredItems = new HashMap<>();
        
        // Créer le fichier auctions.yml
        this.auctionFile = new File(plugin.getDataFolder(), "auctions.yml");
        if (!auctionFile.exists()) {
            try {
                auctionFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Impossible de créer auctions.yml");
            }
        }
        
        this.auctionConfig = YamlConfiguration.loadConfiguration(auctionFile);
        loadAuctions();
        
        // Démarrer le nettoyage automatique des items expirés
        startExpirationTask();
    }
    
    /**
     * Charge les ventes depuis le fichier
     */
    public void loadAuctions() {
        if (auctionConfig.contains("listings")) {
            ConfigurationSection listings = auctionConfig.getConfigurationSection("listings");
            for (String key : listings.getKeys(false)) {
                try {
                    UUID listingId = UUID.fromString(key);
                    UUID sellerId = UUID.fromString(listings.getString(key + ".seller"));
                    String sellerName = listings.getString(key + ".sellerName");
                    ItemStack item = listings.getItemStack(key + ".item");
                    double price = listings.getDouble(key + ".price");
                    long createdAt = listings.getLong(key + ".createdAt");
                    long expiresAt = listings.getLong(key + ".expiresAt");
                    
                    AuctionListing listing = new AuctionListing(listingId, sellerId, sellerName, item, price, createdAt, expiresAt);
                    
                    if (!listing.isExpired()) {
                        activeListings.put(listingId, listing);
                    } else {
                        // Déplacer vers expired items
                        expiredItems.computeIfAbsent(sellerId, k -> new ArrayList<>()).add(item);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Erreur lors du chargement de listing: " + key);
                }
            }
        }
        
        plugin.getLogger().info("Chargé " + activeListings.size() + " ventes actives");
    }
    
    /**
     * Sauvegarde les ventes dans le fichier
     */
    public void saveAuctions() {
        // Clear existing
        auctionConfig.set("listings", null);
        
        for (Map.Entry<UUID, AuctionListing> entry : activeListings.entrySet()) {
            AuctionListing listing = entry.getValue();
            String key = "listings." + listing.getListingId().toString();
            
            auctionConfig.set(key + ".seller", listing.getSellerId().toString());
            auctionConfig.set(key + ".sellerName", listing.getSellerName());
            auctionConfig.set(key + ".item", listing.getItem());
            auctionConfig.set(key + ".price", listing.getPrice());
            auctionConfig.set(key + ".createdAt", listing.getCreatedAt());
            auctionConfig.set(key + ".expiresAt", listing.getExpiresAt());
        }
        
        try {
            auctionConfig.save(auctionFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Impossible de sauvegarder auctions.yml");
        }
    }
    
    /**
     * Ajoute une vente
     */
    public boolean addListing(UUID sellerId, String sellerName, ItemStack item, double price) {
        // Vérifier le nombre de ventes du joueur
        if (getPlayerListingCount(sellerId) >= MAX_LISTINGS_PER_PLAYER) {
            return false;
        }
        
        // Vérifier le prix
        if (price < MIN_PRICE || price > MAX_PRICE) {
            return false;
        }
        
        AuctionListing listing = new AuctionListing(sellerId, sellerName, item, price, LISTING_DURATION);
        activeListings.put(listing.getListingId(), listing);
        saveAuctions();
        
        return true;
    }
    
    /**
     * Achète un item
     */
    public boolean purchaseListing(UUID listingId, UUID buyerId) {
        AuctionListing listing = activeListings.get(listingId);
        if (listing == null || listing.isExpired()) {
            return false;
        }
        
        // Ne peut pas acheter ses propres items
        if (listing.getSellerId().equals(buyerId)) {
            return false;
        }
        
        // Retirer de la liste active
        activeListings.remove(listingId);
        saveAuctions();
        
        return true;
    }
    
    /**
     * Annule une vente
     */
    public ItemStack cancelListing(UUID listingId, UUID playerId) {
        AuctionListing listing = activeListings.get(listingId);
        if (listing == null) {
            return null;
        }
        
        // Vérifier que c'est le vendeur
        if (!listing.getSellerId().equals(playerId)) {
            return null;
        }
        
        activeListings.remove(listingId);
        saveAuctions();
        
        return listing.getItem();
    }
    
    /**
     * Obtient toutes les ventes actives
     */
    public List<AuctionListing> getAllListings() {
        return new ArrayList<>(activeListings.values());
    }
    
    /**
     * Obtient les ventes d'un joueur
     */
    public List<AuctionListing> getPlayerListings(UUID playerId) {
        return activeListings.values().stream()
                .filter(listing -> listing.getSellerId().equals(playerId))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtient le nombre de ventes d'un joueur
     */
    public int getPlayerListingCount(UUID playerId) {
        return (int) activeListings.values().stream()
                .filter(listing -> listing.getSellerId().equals(playerId))
                .count();
    }
    
    /**
     * Obtient les items expirés d'un joueur
     */
    public List<ItemStack> getExpiredItems(UUID playerId) {
        return expiredItems.getOrDefault(playerId, new ArrayList<>());
    }
    
    /**
     * Retire un item expiré
     */
    public void removeExpiredItem(UUID playerId, int index) {
        List<ItemStack> items = expiredItems.get(playerId);
        if (items != null && index >= 0 && index < items.size()) {
            items.remove(index);
            if (items.isEmpty()) {
                expiredItems.remove(playerId);
            }
        }
    }
    
    /**
     * Calcule la commission
     */
    public double calculateCommission(double price) {
        return price * (COMMISSION_PERCENT / 100.0);
    }
    
    /**
     * Démarre la tâche de nettoyage des items expirés
     */
    private void startExpirationTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            List<UUID> toRemove = new ArrayList<>();
            
            for (Map.Entry<UUID, AuctionListing> entry : activeListings.entrySet()) {
                AuctionListing listing = entry.getValue();
                if (listing.isExpired()) {
                    // Déplacer vers expired items
                    expiredItems.computeIfAbsent(listing.getSellerId(), k -> new ArrayList<>()).add(listing.getItem());
                    toRemove.add(entry.getKey());
                }
            }
            
            if (!toRemove.isEmpty()) {
                toRemove.forEach(activeListings::remove);
                saveAuctions();
                plugin.getLogger().info("Nettoyé " + toRemove.size() + " ventes expirées");
            }
        }, 20L * 60L, 20L * 60L); // Toutes les minutes
    }
    
    /**
     * Obtient un listing par ID
     */
    public AuctionListing getListing(UUID listingId) {
        return activeListings.get(listingId);
    }
}
