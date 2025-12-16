package fr.moderation.shop;

import org.bukkit.Material;

public enum ShopCategory {
    
    BLOCKS("Blocs", Material.STONE),
    ORES("Minerais", Material.DIAMOND_ORE),
    TOOLS("Outils", Material.DIAMOND_PICKAXE),
    WEAPONS("Armes", Material.DIAMOND_SWORD),
    ARMOR("Armures", Material.DIAMOND_CHESTPLATE),
    FOOD("Nourriture", Material.COOKED_BEEF),
    FARMING("Agriculture", Material.WHEAT_SEEDS),
    REDSTONE("Redstone", Material.REDSTONE),
    DECORATIONS("Décorations", Material.PAINTING),
    MISCELLANEOUS("Divers", Material.ENDER_PEARL);
    
    private final String displayName;
    private final Material icon;
    
    ShopCategory(String displayName, Material icon) {
        this.displayName = displayName;
        this.icon = icon;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public Material getIcon() {
        return icon;
    }
}
