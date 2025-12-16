package fr.moderation.auction;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class AuctionListing {
    
    private final UUID listingId;
    private final UUID sellerId;
    private final String sellerName;
    private final ItemStack item;
    private final double price;
    private final long createdAt;
    private final long expiresAt;
    
    public AuctionListing(UUID sellerId, String sellerName, ItemStack item, double price, long duration) {
        this.listingId = UUID.randomUUID();
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.item = item.clone();
        this.price = price;
        this.createdAt = System.currentTimeMillis();
        this.expiresAt = createdAt + duration;
    }
    
    // Constructor pour charger depuis la config
    public AuctionListing(UUID listingId, UUID sellerId, String sellerName, ItemStack item, double price, long createdAt, long expiresAt) {
        this.listingId = listingId;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.item = item;
        this.price = price;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }
    
    public UUID getListingId() {
        return listingId;
    }
    
    public UUID getSellerId() {
        return sellerId;
    }
    
    public String getSellerName() {
        return sellerName;
    }
    
    public ItemStack getItem() {
        return item.clone();
    }
    
    public double getPrice() {
        return price;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public long getExpiresAt() {
        return expiresAt;
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }
    
    public long getTimeLeft() {
        return Math.max(0, expiresAt - System.currentTimeMillis());
    }
}
