package fr.moderation.listeners;

import fr.moderation.ModerationSMP;
import fr.moderation.jobs.JobType;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;

public class JobsListener implements Listener {
    
    private final ModerationSMP plugin;
    
    public JobsListener(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material material = event.getBlock().getType();
        
        // MINEUR - Minerais
        double minerXP = getMinerXP(material);
        if (minerXP > 0) {
            plugin.getJobManager().addXP(player, JobType.MINER, minerXP);
        }
        
        // LUMBERJACK - Logs
        double lumberjackXP = getLumberjackXP(material);
        if (lumberjackXP > 0) {
            plugin.getJobManager().addXP(player, JobType.LUMBERJACK, lumberjackXP);
        }
        
        // FERMIER - Cultures
        double farmerXP = getFarmerXP(material);
        if (farmerXP > 0) {
            plugin.getJobManager().addXP(player, JobType.FARMER, farmerXP);
        }
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // CONSTRUCTEUR - Placer des blocs
        plugin.getJobManager().addXP(event.getPlayer(), JobType.BUILDER, 2.0);
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player) {
            Player player = event.getEntity().getKiller();
            EntityType type = event.getEntityType();
            
            // CHASSEUR - Tuer des mobs
            double hunterXP = getHunterXP(type);
            if (hunterXP > 0) {
                plugin.getJobManager().addXP(player, JobType.HUNTER, hunterXP);
            }
        }
    }
    
    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            // FISHERMAN
            plugin.getJobManager().addXP(event.getPlayer(), JobType.FISHERMAN, 10.0);
        }
    }
    
    /**
     * Calcule l'XP pour le job Mineur
     */
    private double getMinerXP(Material material) {
        switch (material) {
            case COAL_ORE:
            case DEEPSLATE_COAL_ORE:
                return 5.0;
            case COPPER_ORE:
            case DEEPSLATE_COPPER_ORE:
                return 8.0;
            case IRON_ORE:
            case DEEPSLATE_IRON_ORE:
                return 10.0;
            case GOLD_ORE:
            case DEEPSLATE_GOLD_ORE:
            case NETHER_GOLD_ORE:
                return 15.0;
            case LAPIS_ORE:
            case DEEPSLATE_LAPIS_ORE:
                return 12.0;
            case REDSTONE_ORE:
            case DEEPSLATE_REDSTONE_ORE:
                return 15.0;
            case EMERALD_ORE:
            case DEEPSLATE_EMERALD_ORE:
                return 40.0;
            case DIAMOND_ORE:
            case DEEPSLATE_DIAMOND_ORE:
                return 50.0;
            case NETHER_QUARTZ_ORE:
                return 10.0;
            case ANCIENT_DEBRIS:
                return 100.0;
            default:
                return 0.0;
        }
    }
    
    /**
     * Calcule l'XP pour le job Bûcheron
     */
    private double getLumberjackXP(Material material) {
        switch (material) {
            case OAK_LOG:
            case SPRUCE_LOG:
            case BIRCH_LOG:
            case JUNGLE_LOG:
            case ACACIA_LOG:
            case DARK_OAK_LOG:
            case MANGROVE_LOG:
            case CHERRY_LOG:
            case STRIPPED_OAK_LOG:
            case STRIPPED_SPRUCE_LOG:
            case STRIPPED_BIRCH_LOG:
            case STRIPPED_JUNGLE_LOG:
            case STRIPPED_ACACIA_LOG:
            case STRIPPED_DARK_OAK_LOG:
            case STRIPPED_MANGROVE_LOG:
            case STRIPPED_CHERRY_LOG:
                return 10.0;
            default:
                return 0.0;
        }
    }
    
    /**
     * Calcule l'XP pour le job Fermier
     */
    private double getFarmerXP(Material material) {
        switch (material) {
            case WHEAT:
                return 5.0;
            case CARROTS:
            case POTATOES:
                return 5.0;
            case BEETROOTS:
                return 6.0;
            case MELON:
                return 3.0;
            case PUMPKIN:
                return 8.0;
            case SWEET_BERRY_BUSH:
                return 4.0;
            case COCOA:
                return 7.0;
            case NETHER_WART:
                return 10.0;
            default:
                return 0.0;
        }
    }
    
    /**
     * Calcule l'XP pour le job Chasseur
     */
    private double getHunterXP(EntityType type) {
        switch (type) {
            // Hostiles communs
            case ZOMBIE:
            case SKELETON:
            case SPIDER:
            case CAVE_SPIDER:
                return 15.0;
            case CREEPER:
            case WITCH:
                return 20.0;
            case ENDERMAN:
            case BLAZE:
                return 30.0;
            case WITHER_SKELETON:
                return 40.0;
            // Boss
            case ENDER_DRAGON:
                return 1000.0;
            case WITHER:
                return 500.0;
            case ELDER_GUARDIAN:
                return 200.0;
            case WARDEN:
                return 300.0;
            // Passifs
            case COW:
            case PIG:
            case CHICKEN:
            case SHEEP:
                return 5.0;
            default:
                return 10.0;
        }
    }
}
