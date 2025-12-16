package fr.moderation.shop;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ShopItem {
    
    private final Material material;
    private final String displayName;
    private final double price;
    private final ShopCategory category;
    
    public ShopItem(Material material, String displayName, double price, ShopCategory category) {
        this.material = material;
        this.displayName = displayName;
        this.price = price;
        this.category = category;
    }
    
    public Material getMaterial() {
        return material;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public double getPrice() {
        return price;
    }
    
    public ShopCategory getCategory() {
        return category;
    }
    
    public ItemStack createItemStack(int amount) {
        return new ItemStack(material, amount);
    }
}
