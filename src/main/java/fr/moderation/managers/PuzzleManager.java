package fr.moderation.managers;

import fr.moderation.ModerationSMP;
import fr.moderation.world.puzzles.StarSequencerPuzzle;
import fr.moderation.world.puzzles.ParkourPuzzle;
import fr.moderation.world.puzzles.CustomStructurePuzzle;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.*;
import java.util.Random;

/**
 * Gère les énigmes que les joueurs doivent résoudre pour sortir du monde de la mort
 */
public class PuzzleManager implements Listener {
    
    private final ModerationSMP plugin;
    private final DeathWorldManager deathWorldManager;
    
    // Active puzzles per player (can be StarSequencer or Parkour)
    private final Map<UUID, Object> activePuzzles; // StarSequencerPuzzle ou ParkourPuzzle
    private final Map<UUID, String> puzzleTypes; // Type de puzzle pour chaque joueur
    private final Map<UUID, Integer> puzzleStages; // Stage actuel du joueur (1 = Star, 2 = Parkour)
    private final Random random;
    
    // Grid system to space out rooms (100 blocks between each)
    private final int GRID_SPACING = 100;
    private int nextGridX = 0;
    private int nextGridZ = 0;
    
    public PuzzleManager(ModerationSMP plugin, DeathWorldManager deathWorldManager) {
        this.plugin = plugin;
        this.deathWorldManager = deathWorldManager;
        this.activePuzzles = new HashMap<>();
        this.puzzleTypes = new HashMap<>();
        this.puzzleStages = new HashMap<>();
        this.random = new Random();
    }
    
    /**
     * Génère une position unique pour un nouveau joueur (espacement de 100 blocs)
     */
    private Location getNextPuzzleLocation() {
        World deathWorld = deathWorldManager.getDeathWorld();
        
        // Centre de la grille
        int x = nextGridX * GRID_SPACING;
        int z = nextGridZ * GRID_SPACING;
        
        // Avancer dans la grille en spirale
        nextGridX++;
        if (nextGridX > 5) { // Limit to 5 positions per row
            nextGridX = 0;
            nextGridZ++;
        }
        
        return new Location(deathWorld, x, 60, z);
    }
    
    /**
     * Crée une nouvelle énigme pour un joueur
     */
    public void createPuzzleForPlayer(Player player) {
        UUID playerId = player.getUniqueId();
        
        plugin.getLogger().info("Tentative de création de puzzle pour " + player.getName());
        
        // Check if player already has a puzzle in progress
        if (activePuzzles.containsKey(playerId)) {
            plugin.getLogger().warning("Le joueur a déjà un puzzle actif!");
            return;
        }
        
        // Generate unique spaced position for this player
        Location center = getNextPuzzleLocation();
        
        plugin.getLogger().info("Position de la salle: " + center);
        
        // STAGE 1: Always start with Star Sequencer
        plugin.getLogger().info("Type de puzzle: Séquenceur d'Étoiles (Stage 1/2)");
        
        // Check if custom structure exists for this puzzle
        String customStructure = plugin.getConfig().getString("puzzles.star.custom-structure", null);
        boolean useCustomStructure = false;
        
        if (customStructure != null && !customStructure.isEmpty()) {
            // Essayer de charger la structure custom
            StructureManager structureManager = plugin.getStructureManager();
            if (structureManager != null && structureManager.structureExists(customStructure)) {
                plugin.getLogger().info("Utilisation de la structure custom: " + customStructure);
                if (structureManager.pasteStructure(customStructure, center)) {
                    useCustomStructure = true;
                    plugin.getLogger().info("Structure custom chargée avec succès!");
                } else {
                    plugin.getLogger().warning("Erreur lors du chargement de la structure custom, utilisation du puzzle par défaut.");
                }
            }
        }
        
        // Always create StarSequencerPuzzle
        StarSequencerPuzzle puzzle = new StarSequencerPuzzle(player, center);
        activePuzzles.put(playerId, puzzle);
        puzzleTypes.put(playerId, "STAR_SEQUENCER");
        puzzleStages.put(playerId, 1);
        
        if (useCustomStructure) {
            // Custom structure: only add puzzle elements
            plugin.getLogger().info("Ajout des éléments du puzzle dans la structure custom...");
            puzzle.buildPuzzleElements();
            plugin.getLogger().info("Éléments du puzzle ajoutés!");
        } else {
            // No custom structure: build complete room
            plugin.getLogger().info("Construction de la salle du puzzle...");
            puzzle.buildPuzzleRoom();
            plugin.getLogger().info("Salle construite!");
        }
        
        // Teleport player to room center
        Location playerSpawn = center.clone();
        playerSpawn.setY(61); // Sur le sol
        playerSpawn.add(0.5, 0, 0.5); // Centrer
        player.teleport(playerSpawn);
        
        plugin.getLogger().info("Joueur téléporté à " + playerSpawn);
        
        // Envoyer les instructions
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_PURPLE + "╔═══════════════════════════════════════════════╗");
        player.sendMessage(ChatColor.DARK_PURPLE + "║  " + ChatColor.BOLD + "LE SÉQUENCEUR D'ÉTOILES" + ChatColor.DARK_PURPLE + "                ║");
        player.sendMessage(ChatColor.DARK_PURPLE + "╚═══════════════════════════════════════════════╝");
        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "Pour revenir à la vie, vous devez aligner");
        player.sendMessage(ChatColor.GRAY + "la constellation céleste...");
        player.sendMessage("");
        player.sendMessage(ChatColor.AQUA + "➤ Observez la " + ChatColor.YELLOW + "carte céleste au plafond");
        player.sendMessage(ChatColor.AQUA + "➤ Lisez les " + ChatColor.YELLOW + "pancartes" + ChatColor.AQUA + " pour connaître les piliers");
        player.sendMessage(ChatColor.AQUA + "➤ Activez les " + ChatColor.YELLOW + "5 leviers" + ChatColor.AQUA + " dans l'ordre des étoiles");
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_GRAY + "(Astuce: Suivez le chemin lumineux entre les étoiles)");
        player.sendMessage("");
        
        // Debug: show sequence (for tests)
        if (player.hasPermission("moderation.deathdim.debug")) {
            Object activePuzzle = activePuzzles.get(playerId);
            if (activePuzzle instanceof StarSequencerPuzzle) {
                player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Séquence: " + ((StarSequencerPuzzle) activePuzzle).getLeverSequence());
            }
        }
    }
    
    /**
     * Gère l'interaction avec les leviers (Séquenceur d'Étoiles)
     */
    @EventHandler
    public void onLeverPull(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        
        if (block == null || block.getType() != Material.LEVER) {
            return;
        }
        
        if (!deathWorldManager.isInDeathWorld(player)) {
            return;
        }
        
        // Check if it's a Star Sequencer puzzle
        String puzzleType = puzzleTypes.get(player.getUniqueId());
        if (puzzleType == null || !puzzleType.equals("STAR_SEQUENCER")) {
            return;
        }
        
        StarSequencerPuzzle puzzle = (StarSequencerPuzzle) activePuzzles.get(player.getUniqueId());
        if (puzzle == null) {
            return;
        }
        
        boolean puzzleComplete = puzzle.checkLever(block.getLocation());
        
        if (puzzleComplete) {
            UUID playerId = player.getUniqueId();
            activePuzzles.remove(playerId);
            puzzleTypes.remove(playerId);
            
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                // Stage 1 complete! Create Stage 2 (Parkour)
                player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "✓ Énigme 1/2 résolue !");
                player.sendMessage(ChatColor.YELLOW + "Préparez-vous pour le parcours final...");
                player.sendMessage("");
                
                // Create parkour at new position
                Location parkourCenter = getNextPuzzleLocation();
                
                plugin.getLogger().info("Création du Parkour (Stage 2/2) pour " + player.getName());
                ParkourPuzzle parkourPuzzle = new ParkourPuzzle(player, parkourCenter);
                activePuzzles.put(playerId, parkourPuzzle);
                puzzleTypes.put(playerId, "PARKOUR");
                puzzleStages.put(playerId, 2); // Stage 2
                
                parkourPuzzle.buildPuzzleRoom();
                
                // Teleport player to parkour
                Location parkourSpawn = parkourCenter.clone();
                parkourSpawn.setY(61);
                parkourSpawn.add(0.5, 0, 0.5);
                player.teleport(parkourSpawn);
                
                player.sendMessage(ChatColor.AQUA + "Atteignez la plateforme finale pour revenir parmi les vivants !");
            }, 80L);
        }
    }
    
    /**
     * Gère le mouvement du joueur (Parkour)
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        if (!deathWorldManager.isInDeathWorld(player)) {
            return;
        }
        
        // Check if it's a Parkour puzzle
        String puzzleType = puzzleTypes.get(player.getUniqueId());
        if (puzzleType == null || !puzzleType.equals("PARKOUR")) {
            return;
        }
        
        ParkourPuzzle puzzle = (ParkourPuzzle) activePuzzles.get(player.getUniqueId());
        if (puzzle == null) {
            return;
        }
        
        // Check completion
        if (puzzle.checkCompletion(player.getLocation())) {
            UUID playerId = player.getUniqueId();
            puzzle.onComplete();
            
            activePuzzles.remove(playerId);
            puzzleTypes.remove(playerId);
            puzzleStages.remove(playerId);
            
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                deathWorldManager.setPlayerAsGhost(player);
            }, 80L);
            return;
        }
        
        // Check if player fell (Y < 55)
        if (player.getLocation().getY() < 55) {
            puzzle.respawnAtCheckpoint();
        }
        
        // Check checkpoints (pressure plates)
        Block blockBelow = player.getLocation().subtract(0, 1, 0).getBlock();
        if (blockBelow.getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
            // Determine which checkpoint based on position
            Location loc = blockBelow.getLocation();
            Block platformBlock = loc.subtract(0, 1, 0).getBlock();
            
            if (platformBlock.getType() == Material.GREEN_CONCRETE) {
                puzzle.activateCheckpoint(1, player.getLocation());
            } else if (platformBlock.getType() == Material.YELLOW_CONCRETE) {
                puzzle.activateCheckpoint(2, player.getLocation());
            }
        }
    }
    
    /**
     * Réinitialise l'énigme d'un joueur (commande admin)
     */
    public void resetPuzzle(Player player) {
        Object puzzle = activePuzzles.get(player.getUniqueId());
        if (puzzle != null) {
            activePuzzles.remove(player.getUniqueId());
            puzzleTypes.remove(player.getUniqueId());
            player.sendMessage(ChatColor.YELLOW + "Votre énigme a été supprimée. Rechargez pour en créer une nouvelle.");
        } else {
            player.sendMessage(ChatColor.RED + "Vous n'avez pas d'énigme active.");
        }
    }
    
    /**
     * Nettoie le puzzle d'un joueur qui quitte
     */
    public void removePuzzle(UUID playerId) {
        activePuzzles.remove(playerId);
        puzzleTypes.remove(playerId);
        puzzleStages.remove(playerId);
    }
}
