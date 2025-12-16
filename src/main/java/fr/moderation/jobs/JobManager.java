package fr.moderation.jobs;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class JobManager {
    
    private final ModerationSMP plugin;
    private final Map<UUID, Map<JobType, PlayerJob>> playerJobs;
    private final File jobsFile;
    private FileConfiguration jobsConfig;
    
    // XP Batching pour réduire le CPU
    private final Map<UUID, Map<JobType, Double>> pendingXP;
    
    // Configuration
    public static final int MAX_LEVEL = 100;
    public static final double REWARD_EVERY_N_LEVELS = 50;
    public static final double REWARD_AMOUNT = 100.0;
    
    public JobManager(ModerationSMP plugin) {
        this.plugin = plugin;
        this.playerJobs = new HashMap<>();
        this.pendingXP = new HashMap<>();
        
        // Créer le fichier jobs.yml
        this.jobsFile = new File(plugin.getDataFolder(), "jobs.yml");
        if (!jobsFile.exists()) {
            try {
                jobsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Impossible de créer jobs.yml");
            }
        }
        
        this.jobsConfig = YamlConfiguration.loadConfiguration(jobsFile);
        loadJobs();
        
        // Auto-save et XP processing
        startAutoSave();
        startXPProcessor();
    }
    
    /**
     * Charge les jobs depuis le fichier
     */
    public void loadJobs() {
        if (jobsConfig.contains("players")) {
            ConfigurationSection players = jobsConfig.getConfigurationSection("players");
            for (String uuidStr : players.getKeys(false)) {
                try {
                    UUID playerId = UUID.fromString(uuidStr);
                    Map<JobType, PlayerJob> jobs = new HashMap<>();
                    
                    ConfigurationSection playerSection = players.getConfigurationSection(uuidStr);
                    for (String jobName : playerSection.getKeys(false)) {
                        try {
                            JobType jobType = JobType.valueOf(jobName);
                            ConfigurationSection jobSection = playerSection.getConfigurationSection(jobName);
                            
                            int level = jobSection.getInt("level", 1);
                            double currentXP = jobSection.getDouble("xp", 0);
                            double totalXP = jobSection.getDouble("totalXP", 0);
                            long createdAt = jobSection.getLong("created", System.currentTimeMillis());
                            
                            PlayerJob job = new PlayerJob(playerId, jobType, level, currentXP, totalXP, createdAt);
                            jobs.put(jobType, job);
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Job invalide: " + jobName);
                        }
                    }
                    
                    if (!jobs.isEmpty()) {
                        playerJobs.put(playerId, jobs);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("UUID invalide dans jobs.yml: " + uuidStr);
                }
            }
        }
        
        plugin.getLogger().info("Chargé les jobs de " + playerJobs.size() + " joueurs");
    }
    
    /**
     * Sauvegarde les jobs dans le fichier
     */
    public void saveJobs() {
        jobsConfig.set("players", null);
        
        for (Map.Entry<UUID, Map<JobType, PlayerJob>> entry : playerJobs.entrySet()) {
            String uuid = entry.getKey().toString();
            
            for (Map.Entry<JobType, PlayerJob> jobEntry : entry.getValue().entrySet()) {
                PlayerJob job = jobEntry.getValue();
                String path = "players." + uuid + "." + job.getJobType().name();
                
                jobsConfig.set(path + ".level", job.getLevel());
                jobsConfig.set(path + ".xp", job.getCurrentXP());
                jobsConfig.set(path + ".totalXP", job.getTotalXP());
                jobsConfig.set(path + ".created", job.getCreatedAt());
            }
        }
        
        try {
            jobsConfig.save(jobsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Impossible de sauvegarder jobs.yml");
        }
    }
    
    /**
     * Obtient un job d'un joueur
     */
    public PlayerJob getJob(UUID playerId, JobType jobType) {
        return playerJobs.computeIfAbsent(playerId, k -> new HashMap<>())
                        .computeIfAbsent(jobType, k -> new PlayerJob(playerId, jobType));
    }
    
    /**
     * Obtient tous les jobs d'un joueur
     */
    public Map<JobType, PlayerJob> getAllJobs(UUID playerId) {
        if (!playerJobs.containsKey(playerId)) {
            Map<JobType, PlayerJob> jobs = new HashMap<>();
            for (JobType type : JobType.values()) {
                jobs.put(type, new PlayerJob(playerId, type));
            }
            playerJobs.put(playerId, jobs);
        }
        return playerJobs.get(playerId);
    }
    
    /**
     * Ajoute de l'XP à un job (batching pour performance)
     */
    public void addXP(Player player, JobType jobType, double amount) {
        UUID playerId = player.getUniqueId();
        
        // Accumuler l'XP au lieu de la traiter immédiatement
        pendingXP.computeIfAbsent(playerId, k -> new HashMap<>())
                 .merge(jobType, amount, Double::sum);
    }
    
    /**
     * Traite les XP en attente (appelé toutes les 3 secondes)
     */
    private void processPendingXP() {
        if (pendingXP.isEmpty()) return;
        
        Map<UUID, Map<JobType, Double>> xpToProcess = new HashMap<>(pendingXP);
        pendingXP.clear();
        
        for (Map.Entry<UUID, Map<JobType, Double>> playerEntry : xpToProcess.entrySet()) {
            UUID playerId = playerEntry.getKey();
            Player player = Bukkit.getPlayer(playerId);
            
            if (player == null || !player.isOnline()) continue;
            
            for (Map.Entry<JobType, Double> jobEntry : playerEntry.getValue().entrySet()) {
                JobType jobType = jobEntry.getKey();
                double amount = jobEntry.getValue();
                
                processXP(player, jobType, amount);
            }
        }
    }
    
    /**
     * Traite l'XP pour un job
     */
    private void processXP(Player player, JobType jobType, double amount) {
        PlayerJob job = getJob(player.getUniqueId(), jobType);
        
        int oldLevel = job.getLevel();
        job.addXP(amount);
        
        // Calculer les montées de niveau
        int levelsGained = 0;
        double currentXP = job.getCurrentXP();
        int currentLevel = job.getLevel();
        
        while (currentLevel < MAX_LEVEL) {
            double xpNeeded = 100.0 * currentLevel;
            
            if (currentXP >= xpNeeded) {
                currentXP -= xpNeeded;
                currentLevel++;
                levelsGained++;
            } else {
                break;
            }
        }
        
        if (levelsGained > 0) {
            job.setLevel(currentLevel);
            job.setCurrentXP(currentXP);
            handleLevelUp(player, job, oldLevel, currentLevel);
        }
    }
    
    /**
     * Gère la montée de niveau
     */
    private void handleLevelUp(Player player, PlayerJob job, int oldLevel, int newLevel) {
        // Message de félicitations
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════");
        player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "✦ NIVEAU SUPÉRIEUR ✦");
        player.sendMessage(ChatColor.YELLOW + job.getJobType().getEmoji() + " " + job.getJobType().getDisplayName() + 
                         ChatColor.GRAY + " : " + ChatColor.WHITE + oldLevel + " → " + ChatColor.GREEN + newLevel);
        
        // Calcul de la récompense progressive
        // Niveau 1 = 100$, Niveau 2 = 105$, Niveau 3 = 110$, etc. (+5$ par niveau)
        double baseReward = 100.0 + ((newLevel - 1) * 5.0);
        
        // Bonus tous les 10 niveaux (mais pas au niveau 10 car déjà dans la base)
        double bonusReward = 0.0;
        if (newLevel % 10 == 0 && newLevel > 10) {
            bonusReward = 50.0;
        }
        
        double totalReward = baseReward + bonusReward;
        
        plugin.getEconomyManager().addBalance(player.getUniqueId(), totalReward);
        
        if (bonusReward > 0) {
            player.sendMessage(ChatColor.GOLD + "Récompense: " + ChatColor.GREEN + "+" + 
                             plugin.getEconomyManager().format(baseReward) + 
                             ChatColor.YELLOW + " + Bonus: " + ChatColor.GREEN + "+" + 
                             plugin.getEconomyManager().format(bonusReward));
        } else {
            player.sendMessage(ChatColor.GOLD + "Récompense: " + ChatColor.GREEN + "+" + 
                             plugin.getEconomyManager().format(totalReward));
        }
        
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════");
        player.sendMessage("");
        
        // Son et effet
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }
    
    /**
     * Obtient le top des joueurs pour un job
     */
    public List<Map.Entry<UUID, PlayerJob>> getTopPlayers(JobType jobType, int limit) {
        List<Map.Entry<UUID, PlayerJob>> topList = new ArrayList<>();
        
        for (Map.Entry<UUID, Map<JobType, PlayerJob>> entry : playerJobs.entrySet()) {
            Map<JobType, PlayerJob> jobs = entry.getValue();
            if (jobs.containsKey(jobType)) {
                topList.add(new AbstractMap.SimpleEntry<>(entry.getKey(), jobs.get(jobType)));
            }
        }
        
        topList.sort((a, b) -> {
            int levelCompare = Integer.compare(b.getValue().getLevel(), a.getValue().getLevel());
            if (levelCompare != 0) return levelCompare;
            return Double.compare(b.getValue().getTotalXP(), a.getValue().getTotalXP());
        });
        
        return topList.subList(0, Math.min(limit, topList.size()));
    }
    
    /**
     * Démarre l'auto-save
     */
    private void startAutoSave() {
        // Auto-save toutes les 10 minutes au lieu de 5 (réduction CPU)
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::saveJobs, 20L * 60 * 10, 20L * 60 * 10);
    }
    
    /**
     * Démarre le processeur d'XP (toutes les 3 secondes)
     */
    private void startXPProcessor() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::processPendingXP, 60L, 60L); // 3 secondes
    }
}
