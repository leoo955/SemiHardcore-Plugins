package fr.moderation.jobs;

import org.bukkit.Material;

public enum JobType {
    
    MINER("Mineur", "⛏️", Material.DIAMOND_PICKAXE, "Gagnez de l'XP en minant des minerais"),
    LUMBERJACK("Bûcheron", "🪓", Material.DIAMOND_AXE, "Gagnez de l'XP en coupant des arbres"),
    FARMER("Fermier", "🌾", Material.WHEAT, "Gagnez de l'XP en récoltant des cultures"),
    HUNTER("Chasseur", "⚔️", Material.DIAMOND_SWORD, "Gagnez de l'XP en tuant des mobs"),
    FISHERMAN("Pêcheur", "🎣", Material.FISHING_ROD, "Gagnez de l'XP en pêchant"),
    BUILDER("Constructeur", "🏗️", Material.BRICKS, "Gagnez de l'XP en plaçant des blocs");
    
    private final String displayName;
    private final String emoji;
    private final Material icon;
    private final String description;
    
    JobType(String displayName, String emoji, Material icon, String description) {
        this.displayName = displayName;
        this.emoji = emoji;
        this.icon = icon;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getEmoji() {
        return emoji;
    }
    
    public Material getIcon() {
        return icon;
    }
    
    public String getDescription() {
        return description;
    }
}
