package fr.moderation.shop;

import org.bukkit.Material;

import java.util.*;
import java.util.stream.Collectors;

public class ShopManager {
    
    private final Map<Material, ShopItem> shopItems;
    
    public ShopManager() {
        this.shopItems = new HashMap<>();
        loadShopItems();
    }
    
    private void loadShopItems() {
        // BLOCS
        addItem(Material.STONE, "Pierre", 1, ShopCategory.BLOCKS);
        addItem(Material.GRANITE, "Granit", 1, ShopCategory.BLOCKS);
        addItem(Material.DIORITE, "Diorite", 1, ShopCategory.BLOCKS);
        addItem(Material.ANDESITE, "Andésite", 1, ShopCategory.BLOCKS);
        addItem(Material.COBBLESTONE, "Cobblestone", 1, ShopCategory.BLOCKS);
        addItem(Material.OAK_PLANKS, "Planches de chêne", 2, ShopCategory.BLOCKS);
        addItem(Material.SPRUCE_PLANKS, "Planches de sapin", 2, ShopCategory.BLOCKS);
        addItem(Material.BIRCH_PLANKS, "Planches de bouleau", 2, ShopCategory.BLOCKS);
        addItem(Material.JUNGLE_PLANKS, "Planches de jungle", 2, ShopCategory.BLOCKS);
        addItem(Material.ACACIA_PLANKS, "Planches d'acacia", 2, ShopCategory.BLOCKS);
        addItem(Material.DARK_OAK_PLANKS, "Planches de chêne noir", 2, ShopCategory.BLOCKS);
        addItem(Material.GLASS, "Verre", 3, ShopCategory.BLOCKS);
        addItem(Material.SANDSTONE, "Grès", 2, ShopCategory.BLOCKS);
        addItem(Material.BRICK, "Briques", 5, ShopCategory.BLOCKS);
        addItem(Material.OBSIDIAN, "Obsidienne", 100, ShopCategory.BLOCKS);
        addItem(Material.DIRT, "Terre", 1, ShopCategory.BLOCKS);
        addItem(Material.GRASS_BLOCK, "Bloc d'herbe", 2, ShopCategory.BLOCKS);
        addItem(Material.SAND, "Sable", 2, ShopCategory.BLOCKS);
        addItem(Material.GRAVEL, "Gravier", 2, ShopCategory.BLOCKS);
        addItem(Material.OAK_LOG, "Bûche de chêne", 4, ShopCategory.BLOCKS);
        addItem(Material.SPRUCE_LOG, "Bûche de sapin", 4, ShopCategory.BLOCKS);
        addItem(Material.BIRCH_LOG, "Bûche de bouleau", 4, ShopCategory.BLOCKS);
        addItem(Material.JUNGLE_LOG, "Bûche de jungle", 4, ShopCategory.BLOCKS);
        addItem(Material.ACACIA_LOG, "Bûche d'acacia", 4, ShopCategory.BLOCKS);
        addItem(Material.DARK_OAK_LOG, "Bûche de chêne noir", 4, ShopCategory.BLOCKS);
        
        // MINERAIS
        addItem(Material.COAL, "Charbon", 5, ShopCategory.ORES);
        addItem(Material.IRON_INGOT, "Lingot de fer", 20, ShopCategory.ORES);
        addItem(Material.GOLD_INGOT, "Lingot d'or", 50, ShopCategory.ORES);
        addItem(Material.DIAMOND, "Diamant", 500, ShopCategory.ORES);
        addItem(Material.EMERALD, "Émeraude", 300, ShopCategory.ORES);
        addItem(Material.LAPIS_LAZULI, "Lapis-lazuli", 10, ShopCategory.ORES);
        addItem(Material.REDSTONE, "Redstone", 15, ShopCategory.ORES);
        addItem(Material.QUARTZ, "Quartz", 20, ShopCategory.ORES);
        
        // OUTILS (DIAMANT)
        addItem(Material.DIAMOND_PICKAXE, "Pioche en diamant", 3000, ShopCategory.TOOLS);
        addItem(Material.DIAMOND_AXE, "Hache en diamant", 3000, ShopCategory.TOOLS);
        addItem(Material.DIAMOND_SHOVEL, "Pelle en diamant", 1500, ShopCategory.TOOLS);
        addItem(Material.DIAMOND_HOE, "Houe en diamant", 2000, ShopCategory.TOOLS);
        
        // OUTILS (FER)
        addItem(Material.IRON_PICKAXE, "Pioche en fer", 300, ShopCategory.TOOLS);
        addItem(Material.IRON_AXE, "Hache en fer", 300, ShopCategory.TOOLS);
        addItem(Material.IRON_SHOVEL, "Pelle en fer", 150, ShopCategory.TOOLS);
        addItem(Material.IRON_HOE, "Houe en fer", 200, ShopCategory.TOOLS);
        
        // OUTILS (PIERRE)
        addItem(Material.STONE_PICKAXE, "Pioche en pierre", 50, ShopCategory.TOOLS);
        addItem(Material.STONE_AXE, "Hache en pierre", 50, ShopCategory.TOOLS);
        addItem(Material.STONE_SHOVEL, "Pelle en pierre", 25, ShopCategory.TOOLS);
        addItem(Material.STONE_HOE, "Houe en pierre", 30, ShopCategory.TOOLS);
        
        // ARMES (DIAMANT)
        addItem(Material.DIAMOND_SWORD, "Épée en diamant", 3000, ShopCategory.WEAPONS);
        
        // ARMES (FER)
        addItem(Material.IRON_SWORD, "Épée en fer", 300, ShopCategory.WEAPONS);
        
        // ARMES (PIERRE)
        addItem(Material.STONE_SWORD, "Épée en pierre", 50, ShopCategory.WEAPONS);
        
        // ARMES (AUTRES)
        addItem(Material.BOW, "Arc", 200, ShopCategory.WEAPONS);
        addItem(Material.ARROW, "Flèche", 2, ShopCategory.WEAPONS);
        addItem(Material.SHIELD, "Bouclier", 150, ShopCategory.WEAPONS);
        
        // ARMURES (DIAMANT)
        addItem(Material.DIAMOND_HELMET, "Casque en diamant", 2500, ShopCategory.ARMOR);
        addItem(Material.DIAMOND_CHESTPLATE, "Plastron en diamant", 4000, ShopCategory.ARMOR);
        addItem(Material.DIAMOND_LEGGINGS, "Jambières en diamant", 3500, ShopCategory.ARMOR);
        addItem(Material.DIAMOND_BOOTS, "Bottes en diamant", 2000, ShopCategory.ARMOR);
        
        // ARMURES (FER)
        addItem(Material.IRON_HELMET, "Casque en fer", 250, ShopCategory.ARMOR);
        addItem(Material.IRON_CHESTPLATE, "Plastron en fer", 400, ShopCategory.ARMOR);
        addItem(Material.IRON_LEGGINGS, "Jambières en fer", 350, ShopCategory.ARMOR);
        addItem(Material.IRON_BOOTS, "Bottes en fer", 200, ShopCategory.ARMOR);
        
        // ARMURES (OR)
        addItem(Material.GOLDEN_HELMET, "Casque en or", 500, ShopCategory.ARMOR);
        addItem(Material.GOLDEN_CHESTPLATE, "Plastron en or", 800, ShopCategory.ARMOR);
        addItem(Material.GOLDEN_LEGGINGS, "Jambières en or", 700, ShopCategory.ARMOR);
        addItem(Material.GOLDEN_BOOTS, "Bottes en or", 400, ShopCategory.ARMOR);
        
        // NOURRITURE
        addItem(Material.APPLE, "Pomme", 10, ShopCategory.FOOD);
        addItem(Material.GOLDEN_APPLE, "Pomme dorée", 500, ShopCategory.FOOD);
        addItem(Material.BREAD, "Pain", 15, ShopCategory.FOOD);
        addItem(Material.COOKED_BEEF, "Steak", 20, ShopCategory.FOOD);
        addItem(Material.COOKED_PORKCHOP, "Côtelette de porc", 20, ShopCategory.FOOD);
        addItem(Material.COOKED_CHICKEN, "Poulet cuit", 15, ShopCategory.FOOD);
        addItem(Material.COOKED_MUTTON, "Mouton cuit", 15, ShopCategory.FOOD);
        addItem(Material.BAKED_POTATO, "Pomme de terre cuite", 10, ShopCategory.FOOD);
        addItem(Material.COOKIE, "Cookie", 5, ShopCategory.FOOD);
        addItem(Material.CAKE, "Gâteau", 50, ShopCategory.FOOD);
        addItem(Material.CARROT, "Carotte", 5, ShopCategory.FOOD);
        addItem(Material.POTATO, "Pomme de terre", 5, ShopCategory.FOOD);
        
        // AGRICULTURE
        addItem(Material.WHEAT_SEEDS, "Graines de blé", 3, ShopCategory.FARMING);
        addItem(Material.CARROT, "Carotte", 5, ShopCategory.FARMING);
        addItem(Material.POTATO, "Pomme de terre", 5, ShopCategory.FARMING);
        addItem(Material.BEETROOT_SEEDS, "Graines de betterave", 3, ShopCategory.FARMING);
        addItem(Material.MELON_SEEDS, "Graines de pastèque", 10, ShopCategory.FARMING);
        addItem(Material.PUMPKIN_SEEDS, "Graines de citrouille", 10, ShopCategory.FARMING);
        addItem(Material.BONE_MEAL, "Poudre d'os", 5, ShopCategory.FARMING);
        addItem(Material.OAK_SAPLING, "Pousse de chêne", 10, ShopCategory.FARMING);
        addItem(Material.SPRUCE_SAPLING, "Pousse de sapin", 10, ShopCategory.FARMING);
        addItem(Material.BIRCH_SAPLING, "Pousse de bouleau", 10, ShopCategory.FARMING);
        addItem(Material.JUNGLE_SAPLING, "Pousse de jungle", 10, ShopCategory.FARMING);
        
        // REDSTONE
        addItem(Material.REDSTONE, "Redstone", 15, ShopCategory.REDSTONE);
        addItem(Material.REPEATER, "Répéteur", 30, ShopCategory.REDSTONE);
        addItem(Material.COMPARATOR, "Comparateur", 40, ShopCategory.REDSTONE);
        addItem(Material.PISTON, "Piston", 25, ShopCategory.REDSTONE);
        addItem(Material.STICKY_PISTON, "Piston collant", 30, ShopCategory.REDSTONE);
        addItem(Material.LEVER, "Levier", 10, ShopCategory.REDSTONE);
        addItem(Material.STONE_BUTTON, "Bouton", 5, ShopCategory.REDSTONE);
        addItem(Material.REDSTONE_TORCH, "Torche de redstone", 10, ShopCategory.REDSTONE);
        addItem(Material.REDSTONE_LAMP, "Lampe de redstone", 40, ShopCategory.REDSTONE);
        addItem(Material.HOPPER, "Entonnoir", 100, ShopCategory.REDSTONE);
        addItem(Material.DROPPER, "Dropper", 50, ShopCategory.REDSTONE);
        addItem(Material.DISPENSER, "Distributeur", 50, ShopCategory.REDSTONE);
        
        // DECORATIONS
        addItem(Material.TORCH, "Torche", 2, ShopCategory.DECORATIONS);
        addItem(Material.GLOWSTONE, "Glowstone", 30, ShopCategory.DECORATIONS);
        addItem(Material.SEA_LANTERN, "Lanterne marine", 50, ShopCategory.DECORATIONS);
        addItem(Material.PAINTING, "Tableau", 20, ShopCategory.DECORATIONS);
        addItem(Material.ITEM_FRAME, "Cadre", 15, ShopCategory.DECORATIONS);
        addItem(Material.FLOWER_POT, "Pot de fleur", 10, ShopCategory.DECORATIONS);
        addItem(Material.LADDER, "Échelle", 5, ShopCategory.DECORATIONS);
        addItem(Material.RAIL, "Rail", 10, ShopCategory.DECORATIONS);
        addItem(Material.POWERED_RAIL, "Rail motorisé", 30, ShopCategory.DECORATIONS);
        addItem(Material.MINECART, "Wagonnet", 50, ShopCategory.DECORATIONS);
        
        // DIVERS
        addItem(Material.BUCKET, "Seau", 50, ShopCategory.MISCELLANEOUS);
        addItem(Material.WATER_BUCKET, "Seau d'eau", 75, ShopCategory.MISCELLANEOUS);
        addItem(Material.LAVA_BUCKET, "Seau de lave", 150, ShopCategory.MISCELLANEOUS);
        addItem(Material.ENDER_PEARL, "Perle de l'End", 100, ShopCategory.MISCELLANEOUS);
        addItem(Material.ENDER_CHEST, "Coffre de l'End", 500, ShopCategory.MISCELLANEOUS);
        addItem(Material.CHEST, "Coffre", 25, ShopCategory.MISCELLANEOUS);
        addItem(Material.CRAFTING_TABLE, "Table de craft", 20, ShopCategory.MISCELLANEOUS);
        addItem(Material.FURNACE, "Fourneau", 30, ShopCategory.MISCELLANEOUS);
        addItem(Material.ANVIL, "Enclume", 300, ShopCategory.MISCELLANEOUS);
        addItem(Material.ENCHANTING_TABLE, "Table d'enchantement", 2000, ShopCategory.MISCELLANEOUS);
        addItem(Material.BOOKSHELF, "Bibliothèque", 100, ShopCategory.MISCELLANEOUS);
        addItem(Material.TNT, "TNT", 200, ShopCategory.MISCELLANEOUS);
        addItem(Material.STRING, "Fil", 5, ShopCategory.MISCELLANEOUS);
        addItem(Material.COBWEB, "Toile d'araignée", 50, ShopCategory.MISCELLANEOUS);
        addItem(Material.EXPERIENCE_BOTTLE, "Fiole d'XP", 100, ShopCategory.MISCELLANEOUS);
    }
    
    private void addItem(Material material, String displayName, double price, ShopCategory category) {
        shopItems.put(material, new ShopItem(material, displayName, price, category));
    }
    
    public ShopItem getShopItem(Material material) {
        return shopItems.get(material);
    }
    
    public List<ShopItem> getItemsByCategory(ShopCategory category) {
        return shopItems.values().stream()
                .filter(item -> item.getCategory() == category)
                .sorted(Comparator.comparingDouble(ShopItem::getPrice))
                .collect(Collectors.toList());
    }
    
    public List<ShopItem> getAllItems() {
        return new ArrayList<>(shopItems.values());
    }
    
    public boolean hasItem(Material material) {
        return shopItems.containsKey(material);
    }
}
