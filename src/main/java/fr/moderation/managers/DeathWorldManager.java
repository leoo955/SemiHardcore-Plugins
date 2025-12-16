package fr.moderation.managers;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import fr.moderation.ModerationSMP;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class DeathWorldManager {
    
    private final ModerationSMP plugin;
    private World deathWorld;
    private Location deathSpawn;
    private final Set<UUID> playersInDeathWorld;
    private final Set<UUID> ghostPlayers; // Joueurs qui ont fini le puzzle mais attendent le rituel
    
    private final File playersFile;
    private final YamlConfiguration playersConfig;
    
    public DeathWorldManager(ModerationSMP plugin) {
        this.plugin = plugin;
        this.playersInDeathWorld = new HashSet<>();
        this.ghostPlayers = new HashSet<>();
        
        // Initialiser le fichier de sauvegarde
        this.playersFile = new File(plugin.getDataFolder(), "death_players.yml");
        if (!playersFile.exists()) {
            try {
                playersFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Impossible de créer death_players.yml");
            }
        }
        this.playersConfig = YamlConfiguration.loadConfiguration(playersFile);
        
        // Charger les joueurs morts et fantômes
        loadDeadPlayers();
        
        createDeathWorld();
    }
    
    private void loadDeadPlayers() {
        List<String> uuidStrings = playersConfig.getStringList("dead-players");
        for (String uuidStr : uuidStrings) {
            try {
                playersInDeathWorld.add(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException e) {
                // Ignorer UUID invalide
            }
        }
        
        List<String> ghostStrings = playersConfig.getStringList("ghost-players");
        for (String uuidStr : ghostStrings) {
            try {
                ghostPlayers.add(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException e) {
                // Ignorer UUID invalide
            }
        }
    }
    
    private void saveDeadPlayers() {
        List<String> uuidStrings = new ArrayList<>();
        for (UUID uuid : playersInDeathWorld) {
            uuidStrings.add(uuid.toString());
        }
        playersConfig.set("dead-players", uuidStrings);
        
        List<String> ghostStrings = new ArrayList<>();
        for (UUID uuid : ghostPlayers) {
            ghostStrings.add(uuid.toString());
        }
        playersConfig.set("ghost-players", ghostStrings);
        
        try {
            playersConfig.save(playersFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Impossible de sauvegarder death_players.yml");
        }
    }
    
    /**
     * Crée ou charge le monde de la mort
     */
    private void createDeathWorld() {
        String worldName = plugin.getConfig().getString("death-dimension.world-name", "death_world");
        
        WorldCreator creator = new WorldCreator(worldName);
        creator.generator(new DeathWorldGenerator());
        creator.environment(World.Environment.NORMAL); // Ciel bleu comme le monde normal
        creator.generateStructures(false);
        
        deathWorld = creator.createWorld();
        
        if (deathWorld == null) {
            plugin.getLogger().severe("Impossible de créer le monde de la mort !");
            return;
        }
        
        // Configuration du monde - Paradis paisible
        deathWorld.setDifficulty(Difficulty.PEACEFUL);
        deathWorld.setPVP(false);
        deathWorld.setSpawnFlags(false, false);
        
        // Temps fixe - Jour permanent pour un paradis
        deathWorld.setTime(6000); // Midi
        deathWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        deathWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        deathWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        deathWorld.setGameRule(GameRule.KEEP_INVENTORY, true);
        
        // Spawn au centre
        deathWorld.setSpawnLocation(0, 65, 0);
        
        plugin.getLogger().info("Monde 'Paradis' créé avec succès !");
        loadDeadPlayers();
        
        // Définir le spawn par défaut
        if (deathWorld != null) {
            double x = plugin.getConfig().getDouble("death-dimension.spawn.x", 0.0);
            double y = plugin.getConfig().getDouble("death-dimension.spawn.y", 100.0);
            double z = plugin.getConfig().getDouble("death-dimension.spawn.z", 0.0);
            
            deathSpawn = new Location(deathWorld, x, y, z);
            deathWorld.setSpawnLocation(deathSpawn);
        }
    }
    
    /**
     * Générateur de monde personnalisé pour le Paradis - FLAT avec herbe
     */
    private class DeathWorldGenerator extends ChunkGenerator {
        
        @Override
        public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
            ChunkData chunk = createChunkData(world);
            
            // Monde COMPLÈTEMENT PLAT style Paradis
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    // Bedrock au fond
                    chunk.setBlock(x, 0, z, Material.BEDROCK);
                    
                    // Remplissage en pierre jusqu'à Y=58
                    for (int y = 1; y < 59; y++) {
                        chunk.setBlock(x, y, z, Material.STONE);
                    }
                    
                    // Couches de terre (59-63)
                    for (int y = 59; y < 64; y++) {
                        chunk.setBlock(x, y, z, Material.DIRT);
                    }
                    
                    // Surface en HERBE à Y=64
                    chunk.setBlock(x, 64, z, Material.GRASS_BLOCK);
                    
                    // Quelques fleurs aléatoires
                    if (random.nextInt(15) == 0) {
                        Material flower = random.nextBoolean() ? Material.POPPY : Material.DANDELION;
                        chunk.setBlock(x, 65, z, flower);
                    }
                }
            }
            
            return chunk;
        }
        
        @Override
        public boolean shouldGenerateStructures() {
            return false;
        }
        
        @Override
        public boolean shouldGenerateMobs() {
            return false;
        }
        
        @Override
        public boolean shouldGenerateDecorations() {
            return false;
        }
    }
    
    /**
     * Téléporte un joueur dans le monde de la mort
     */
    public void teleportToDeathWorld(Player player) {
        if (deathWorld == null) {
            plugin.getLogger().warning("Le monde de la mort n'existe pas!");
            return;
        }
        
        // Ajouter le joueur à la liste et sauvegarder
        playersInDeathWorld.add(player.getUniqueId());
        saveDeadPlayers();
        
        // Générer une position aléatoire TRÈS éloignée pour éviter que les joueurs se croisent
        // Chaque joueur aura son propre spawn isolé
        Random random = new Random();
        
        // Distance aléatoire entre 1000 et 5000 blocs du spawn
        int distance = 1000 + random.nextInt(4000);
        
        // Angle aléatoire (0-360 degrés)
        double angle = random.nextDouble() * 2 * Math.PI;
        
        // Calculer les coordonnées X et Z
        int x = (int) (deathSpawn.getX() + distance * Math.cos(angle));
        int z = (int) (deathSpawn.getZ() + distance * Math.sin(angle));
        
        // Utiliser la hauteur du spawn par défaut
        int y = deathSpawn.getBlockY();
        
        // Créer la location de spawn personnel
        Location personalSpawn = new Location(deathWorld, x + 0.5, y, z + 0.5);
        
        // Trouver un spawn sûr (sur un bloc solide avec de l'air au-dessus)
        Location spawnLoc = findSafeSpawn(personalSpawn);
        spawnLoc.setYaw(0);
        spawnLoc.setPitch(0);
        
        plugin.getLogger().info("Téléportation de " + player.getName() + " au death world à: " + 
                               spawnLoc.getBlockX() + ", " + spawnLoc.getBlockY() + ", " + spawnLoc.getBlockZ() +
                               " (distance: " + distance + " blocs)");
        
        player.teleport(spawnLoc);
        
        // IMPORTANT: Vider l'inventaire pour éviter la duplication
        // (car KEEP_INVENTORY = true dans le death world)
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setExtraContents(null);
        
        // Mettre en mode CRÉATIF
        player.setGameMode(GameMode.CREATIVE);
        
        player.sendMessage("");
        player.sendMessage(ChatColor.AQUA + "═══════════════════════════");
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "  ✦ PARADIS ✦");
        player.sendMessage(ChatColor.AQUA + "═══════════════════════════");
        player.sendMessage(ChatColor.GRAY + "Vous êtes en mode créatif dans");
        player.sendMessage(ChatColor.GRAY + "le paradis. Attendez qu'un joueur");
        player.sendMessage(ChatColor.GRAY + "vous ressuscite avec votre tête.");
        player.sendMessage(ChatColor.AQUA + "═══════════════════════════");
        player.sendMessage("");
    }
    
    /**
     * Trouve un spawn sûr (bloc solide avec de l'air au-dessus)
     */
    private Location findSafeSpawn(Location location) {
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        // Chercher un bloc solide en dessous
        for (int checkY = y; checkY >= 0; checkY--) {
            Block blockBelow = world.getBlockAt(x, checkY, z);
            Block blockAt = world.getBlockAt(x, checkY + 1, z);
            Block blockAbove = world.getBlockAt(x, checkY + 2, z);
            
            // Vérifier si c'est safe (bloc solide en dessous, air au-dessus)
            if (blockBelow.getType().isSolid() && 
                blockAt.getType().isAir() && 
                blockAbove.getType().isAir()) {
                return new Location(world, x + 0.5, checkY + 1, z + 0.5, 0, 0);
            }
        }
        
        // Si aucun spawn safe trouvé, retourner la hauteur par défaut (60)
        return new Location(world, x + 0.5, 60, z + 0.5, 0, 0);
    }

    /**
     * Met le joueur en mode fantôme (Spectateur dans l'Overworld) après le puzzle
     */
    public void setPlayerAsGhost(Player player) {
        playersInDeathWorld.remove(player.getUniqueId());
        ghostPlayers.add(player.getUniqueId());
        saveDeadPlayers();
        
        // Téléporter au spawn du monde normal
        World overworld = Bukkit.getWorlds().get(0);
        Location spawn = overworld.getSpawnLocation();
        
        // Vérifier si le plugin a un spawn custom
        if (plugin.getConfig().getBoolean("spawn.set", false)) {
            String world = plugin.getConfig().getString("spawn.world");
            double x = plugin.getConfig().getDouble("spawn.x");
            double y = plugin.getConfig().getDouble("spawn.y");
            double z = plugin.getConfig().getDouble("spawn.z");
            float yaw = (float) plugin.getConfig().getDouble("spawn.yaw");
            float pitch = (float) plugin.getConfig().getDouble("spawn.pitch");
            
            World spawnWorld = Bukkit.getWorld(world);
            if (spawnWorld != null) {
                spawn = new Location(spawnWorld, x, y, z, yaw, pitch);
            }
        }
        
        player.teleport(spawn);
        player.setGameMode(GameMode.SPECTATOR);
        
        // Message
        player.sendMessage(ChatColor.GOLD + "Vous avez réussi l'épreuve !");
        player.sendMessage(ChatColor.GRAY + "Vous êtes maintenant un " + ChatColor.WHITE + "FANTÔME" + ChatColor.GRAY + ".");
        player.sendMessage(ChatColor.GRAY + "Un joueur vivant doit effectuer le rituel de résurrection");
        player.sendMessage(ChatColor.GRAY + "avec votre tête et des offrandes pour vous ramener à la vie.");
        
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 0.5f);
    }
    
    /**
     * Ressuscite complètement un joueur (appelé par le rituel)
     */
    public boolean fullyRevivePlayer(UUID playerId, Location reviveLocation) {
        // Vérifier si le joueur est fantôme OU dans le death world
        boolean wasGhost = ghostPlayers.contains(playerId);
        boolean wasInDD = playersInDeathWorld.contains(playerId);
        
        if (wasGhost || wasInDD) {
            // Retirer de toutes les listes
            ghostPlayers.remove(playerId);
            playersInDeathWorld.remove(playerId);
            saveDeadPlayers();
            
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.teleport(reviveLocation);
                
                // CLEAR l'inventaire (éviter de garder items du créatif)
                player.getInventory().clear();
                player.getInventory().setArmorContents(null);
                player.getInventory().setExtraContents(null);
                
                // FORCER le mode SURVIVAL
                player.setGameMode(GameMode.SURVIVAL);
                
                player.sendMessage("");
                player.sendMessage(ChatColor.GREEN + "═══════════════════════════");
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "  VOUS ÊTES RESSUSCITÉ !");
                player.sendMessage(ChatColor.GREEN + "═══════════════════════════");
                player.sendMessage(ChatColor.GRAY + "Vous recommencez de zéro.");
                player.sendMessage(ChatColor.GREEN + "═══════════════════════════");
                player.sendMessage("");
                
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                
                // Effets
                player.getWorld().strikeLightningEffect(player.getLocation());
            }
            return true;
        }
        return false;
    }

    /**
     * Vérifie si un joueur est un fantôme (a fini le puzzle mais attend le rituel)
     */
    public boolean isGhost(UUID playerId) {
        return ghostPlayers.contains(playerId);
    }

    /**
     * Vérifie si un monde est le monde de la mort
     */
    public boolean isDeathWorld(World world) {
        return deathWorld != null && world.equals(deathWorld);
    }
    
    /**
     * Vérifie si un joueur est dans le monde de la mort
     */
    public boolean isInDeathWorld(Player player) {
        return isDeathWorld(player.getWorld());
    }
    
    /**
     * Obtient le monde de la mort
     */
    public World getDeathWorld() {
        return deathWorld;
    }
    
    /**
     * Définit le spawn du monde de la mort
     */
    public void setDeathSpawn(Location location) {
        if (deathWorld != null && location.getWorld().equals(deathWorld)) {
            deathSpawn = location.clone();
            deathWorld.setSpawnLocation(location);
            
            // Sauvegarder dans la config
            plugin.getConfig().set("death-dimension.spawn.x", location.getX());
            plugin.getConfig().set("death-dimension.spawn.y", location.getY());
            plugin.getConfig().set("death-dimension.spawn.z", location.getZ());
            plugin.saveConfig();
        }
    }
    
    /**
     * Obtient tous les joueurs dans le monde de la mort
     */
    public Set<UUID> getPlayersInDeathWorld() {
        return new HashSet<>(playersInDeathWorld);
    }
    
    /**
     * Obtient tous les joueurs fantômes
     */
    public Set<UUID> getGhostPlayers() {
        return new HashSet<>(ghostPlayers);
    }
}
