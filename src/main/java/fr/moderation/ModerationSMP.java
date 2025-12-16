package fr.moderation;

import fr.moderation.commands.*;
import fr.moderation.listeners.*;
import fr.moderation.managers.*;
import fr.moderation.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class ModerationSMP extends JavaPlugin {
    
    private static ModerationSMP instance;
    private VanishManager vanishManager;
    private TeleportManager teleportManager;
    private HomeManager homeManager;
    private CombatManager combatManager;
    private XrayManager xrayManager;
    private AuthManager authManager;
    private SkinManager skinManager;
    private JailManager jailManager;
    private RollbackManager rollbackManager;
    private DeathWorldManager deathWorldManager;
    private PuzzleManager puzzleManager;
    private TeamManager teamManager;
    private StructureManager structureManager;
    private GuisterManager guisterManager;
    private NoClipManager noClipManager;
    private GodModeManager godModeManager;
    private LastLocationManager lastLocationManager;
    private FreezeManager freezeManager;
    private ScoreboardManager scoreboardManager;
    private EconomyManager economyManager;
    private fr.moderation.auction.AuctionHouse auctionHouse;
    private fr.moderation.shop.ShopManager shopManager;
    private fr.moderation.jobs.JobManager jobManager;
    
    private File homesFile;
    private FileConfiguration homesConfig;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config
        saveDefaultConfig();
        
        // Initialiser les managers
        vanishManager = new VanishManager(this);
        teleportManager = new TeleportManager(this);
        homeManager = new HomeManager(this);
        combatManager = new CombatManager(this);
        xrayManager = new XrayManager(this);
        authManager = new AuthManager(this);
        skinManager = new SkinManager(this);
        jailManager = new JailManager(this);
        rollbackManager = new RollbackManager(this);
        
        // Death World & Puzzles
        deathWorldManager = new DeathWorldManager(this);
        puzzleManager = new PuzzleManager(this, deathWorldManager);
        
        // Teams
        teamManager = new TeamManager(this);
        
        // Guister
        guisterManager = new GuisterManager(this);
        
        // NoClip
        noClipManager = new NoClipManager();
        
        // God Mode
        godModeManager = new GodModeManager();
        
        // Last Location (for /back)
        lastLocationManager = new LastLocationManager();
        
        // Freeze Manager
        freezeManager = new FreezeManager();
        
        // Scoreboard Manager
        scoreboardManager = new ScoreboardManager(this);
        
        // Economy Manager
        economyManager = new EconomyManager(this);
        
        // Auction House
        auctionHouse = new fr.moderation.auction.AuctionHouse(this);
        
        // Shop Manager
        shopManager = new fr.moderation.shop.ShopManager();
        
        // Job Manager
        jobManager = new fr.moderation.jobs.JobManager(this);
        
        // Structures (WorldEdit) - Optional
        if (getServer().getPluginManager().getPlugin("WorldEdit") != null) {
            structureManager = new StructureManager(this);
            getLogger().info("WorldEdit détecté - Système de structures activé !");
        } else {
            getLogger().warning("WorldEdit non détecté - Système de structures désactivé.");
        }
        
       // Charger les homes
        loadHomesConfig();
        homeManager.loadHomes();
        
        // Enregistrer les commandes
        registerCommands();
        
        // Enregistrer les listeners
        registerListeners();
        
        // Enregistrer les recettes
        registerRecipes();
        
        // Charger le spawn
        teleportManager.loadSpawn();
        
        // Start scoreboard
        scoreboardManager.start();
        
        getLogger().info("ModerationSMP activé avec succès !");
        getLogger().info("Limite de homes: " + getConfig().getInt("settings.max_homes", 3));
        getLogger().info("Durée de combat: " + getConfig().getInt("settings.combat_duration", 15) + " secondes");
    }
    
    @Override
    public void onDisable() {
        // Sauvegarder les homes
        if (homeManager != null) {
            homeManager.saveHomes();
        }
        
        // Disable vanish
        if (vanishManager != null) {
            vanishManager.disableAllVanish();
        }
        
        // Handle combat players
        if (combatManager != null) {
            combatManager.handleServerShutdown();
        }
        
        // Disable all noclips
        if (noClipManager != null) {
            noClipManager.disableAllNoClip();
        }
        
        // Disable all god modes
        if (godModeManager != null) {
            godModeManager.disableAllGodMode();
        }
        
        // Unfreeze all players
        if (freezeManager != null) {
            freezeManager.unfreezeAll();
        }
        
        // Stop scoreboard
        if (scoreboardManager != null) {
            scoreboardManager.stop();
        }
        
        // Save economy
        if (economyManager != null) {
            economyManager.saveBalances();
        }
        
        // Save auctions
        if (auctionHouse != null) {
            auctionHouse.saveAuctions();
        }
        

        
        getLogger().info("ModerationSMP désactivé.");
    }
    
    private void registerCommands() {
        // Utils
        fr.moderation.utils.PlayerNameTabCompleter playerCompleter = new fr.moderation.utils.PlayerNameTabCompleter();
        fr.moderation.utils.HomeTabCompleter homeCompleter = new fr.moderation.utils.HomeTabCompleter(homeManager);
        
        // Vanish
        getCommand("vanish").setExecutor(new VanishCommand(this));
        getCommand("supervanish").setExecutor(new SuperVanishCommand(this));
        
        // Spawn
        getCommand("spawn").setExecutor(new SpawnCommand(this));
        getCommand("setspawn").setExecutor(new SetSpawnCommand(this));
        
        // TPA
        getCommand("tpa").setExecutor(new TpaCommand(this));
        getCommand("tpa").setTabCompleter(playerCompleter);
        getCommand("tpaccept").setExecutor(new TpAcceptCommand(this));
        getCommand("tpdeny").setExecutor(new TpDenyCommand(this));
        getCommand("tphere").setExecutor(new TpHereCommand(this));
        getCommand("tphere").setTabCompleter(playerCompleter);
        
        // Homes
        getCommand("sethome").setExecutor(new SetHomeCommand(this));
        getCommand("sethome").setTabCompleter(homeCompleter);
        getCommand("home").setExecutor(new HomeCommand(this));
        getCommand("home").setTabCompleter(homeCompleter);
        getCommand("delhome").setExecutor(new DelHomeCommand(this));
        getCommand("delhome").setTabCompleter(homeCompleter);
        getCommand("homes").setExecutor(new HomesCommand(this));
        
        // Admin
        getCommand("invsee").setExecutor(new InvSeeCommand(this));
        getCommand("invsee").setTabCompleter(playerCompleter);
        getCommand("broadcast").setExecutor(new BroadcastCommand(this));
        getCommand("xray").setExecutor(new XrayCommand(this));
        getCommand("topluck").setExecutor(new TopLuckCommand(this));
        getCommand("spec").setExecutor(new SpecCommand(this));
        getCommand("spec").setTabCompleter(playerCompleter);
        
        // Auth
        getCommand("register").setExecutor(new RegisterCommand(this));
        getCommand("login").setExecutor(new LoginCommand(this));
        
        // Skin
        getCommand("skin").setExecutor(new SkinCommand(this));
        getCommand("skin").setTabCompleter(playerCompleter);
        
        // Jail
        getCommand("jail").setExecutor(new JailCommand(this));
        getCommand("jail").setTabCompleter(playerCompleter);
        getCommand("unjail").setExecutor(new UnjailCommand(this));
        getCommand("unjail").setTabCompleter(playerCompleter);
        getCommand("setjail").setExecutor(new SetJailCommand(this));
        
        // Death Dimension
        DeathDimCommand deathDimCmd = new DeathDimCommand(this, deathWorldManager, puzzleManager);
        getCommand("deathdim").setExecutor(deathDimCmd);
        getCommand("deathdim").setTabCompleter(deathDimCmd);
        
        // CoreProtect
        getCommand("co").setExecutor(new CoreProtectCommand(this));
        
        // Get Head (for resurrection ritual)
        getCommand("gethead").setExecutor(new GetHeadCommand());
        getCommand("gethead").setTabCompleter(playerCompleter);
        
        // Teams
        TeamCommand teamCmd = new TeamCommand(this, teamManager);
        getCommand("team").setExecutor(teamCmd);
        getCommand("team").setTabCompleter(teamCmd);
        
        // Guister
        GuisterCommand guisterCmd = new GuisterCommand(this, guisterManager);
        getCommand("guister").setExecutor(guisterCmd);
        getCommand("guister").setTabCompleter(guisterCmd);
        
        // Structures (only if WorldEdit is present)
        if (structureManager != null) {
            StructureCommand structureCmd = new StructureCommand(this, structureManager);
            getCommand("structure").setExecutor(structureCmd);
            getCommand("structure").setTabCompleter(structureCmd);
        }
        
        // PVP
        getCommand("pvp").setExecutor(new PvpCommand(this));
        
        // NoClip
        getCommand("noclip").setExecutor(new NoClipCommand(this));
        
        // Utility Commands
        getCommand("heal").setExecutor(new HealCommand(this));
        getCommand("heal").setTabCompleter(playerCompleter);
        
        getCommand("feed").setExecutor(new FeedCommand(this));
        getCommand("feed").setTabCompleter(playerCompleter);
        
        getCommand("fly").setExecutor(new FlyCommand(this));
        getCommand("fly").setTabCompleter(playerCompleter);
        
        getCommand("speed").setExecutor(new SpeedCommand(this));
        
        getCommand("god").setExecutor(new GodCommand(this));
        getCommand("god").setTabCompleter(playerCompleter);
        
        getCommand("clear").setExecutor(new ClearInventoryCommand(this));
        getCommand("clear").setTabCompleter(playerCompleter);
        
        getCommand("gamemode").setExecutor(new GamemodeCommand(this));
        getCommand("gamemode").setTabCompleter(playerCompleter);
        
        // Teleportation Commands
        getCommand("back").setExecutor(new BackCommand(this));
        getCommand("tpall").setExecutor(new TpAllCommand(this));
        getCommand("tppos").setExecutor(new TpPosCommand(this));
        
        // Freeze
        getCommand("freeze").setExecutor(new FreezeCommand(this));
        getCommand("freeze").setTabCompleter(playerCompleter);
        
        // Random Teleport
        getCommand("rtp").setExecutor(new RtpCommand(this));
        
        // Scoreboard
        getCommand("scoreboard").setExecutor(new ScoreboardCommand(this));
        
        // Economy
        getCommand("balance").setExecutor(new BalanceCommand(this));
        getCommand("pay").setExecutor(new PayCommand(this));
        getCommand("pay").setTabCompleter(playerCompleter);
        EcoCommand ecoCmd = new EcoCommand(this);
        getCommand("eco").setExecutor(ecoCmd);
        getCommand("eco").setTabCompleter(ecoCmd);
        
        // Auction House
        AhCommand ahCmd = new AhCommand(this);
        getCommand("ah").setExecutor(ahCmd);
        getCommand("ah").setTabCompleter(ahCmd);
        
        // Shop
        getCommand("shop").setExecutor(new ShopCommand(this));
        
        // Jobs
        JobsCommand jobsCmd = new JobsCommand(this);
        getCommand("jobs").setExecutor(jobsCmd);
        getCommand("jobs").setTabCompleter(jobsCmd);
        
        // Fantome
        getCommand("fantome").setExecutor(new FantomeCommand(this, deathWorldManager));
    }
    
    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new VanishListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CombatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new XrayListener(this), this);
        Bukkit.getPluginManager().registerEvents(new AuthListener(this), this);
        Bukkit.getPluginManager().registerEvents(new HardcoreListener(this), this);
        Bukkit.getPluginManager().registerEvents(new JailListener(this), this);
        Bukkit.getPluginManager().registerEvents(new RollbackListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DeathListener(this, deathWorldManager, puzzleManager), this);
        Bukkit.getPluginManager().registerEvents(puzzleManager, this);
        Bukkit.getPluginManager().registerEvents(new ReviveGUIListener(this, deathWorldManager), this);
        Bukkit.getPluginManager().registerEvents(new ReviveTotemListener(this, deathWorldManager), this);
        Bukkit.getPluginManager().registerEvents(new TeamListener(this, teamManager), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PvpListener(this), this);
        Bukkit.getPluginManager().registerEvents(new NoClipListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GodModeListener(this), this);
        Bukkit.getPluginManager().registerEvents(new TeleportTrackingListener(this), this);
        Bukkit.getPluginManager().registerEvents(new FreezeListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ScoreboardListener(this), this);
        Bukkit.getPluginManager().registerEvents(new EconomyListener(this), this);
        Bukkit.getPluginManager().registerEvents(new fr.moderation.listeners.AuctionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new fr.moderation.listeners.ShopListener(this), this);
        Bukkit.getPluginManager().registerEvents(new fr.moderation.listeners.JobsListener(this), this);
        Bukkit.getPluginManager().registerEvents(new fr.moderation.listeners.JobsGUIListener(this), this);
        Bukkit.getPluginManager().registerEvents(new fr.moderation.listeners.DeathDimensionListener(this), this);
    }
    
    private void registerRecipes() {
        // Resurrection Totem Craft
        // Recipe: Beacon in center, 4 Nether Stars in corners, 4 Netherite Blocks on sides
        org.bukkit.inventory.ShapedRecipe totemRecipe = new org.bukkit.inventory.ShapedRecipe(
            new org.bukkit.NamespacedKey(this, "resurrection_totem"),
            createResurrectionTotem()
        );
        
        totemRecipe.shape("NSN", "SBS", "NSN");
        totemRecipe.setIngredient('N', org.bukkit.Material.NETHER_STAR);
        totemRecipe.setIngredient('S', org.bukkit.Material.NETHERITE_BLOCK);
        totemRecipe.setIngredient('B', org.bukkit.Material.BEACON);
        
        Bukkit.addRecipe(totemRecipe);
    }
    
    private org.bukkit.inventory.ItemStack createResurrectionTotem() {
        org.bukkit.inventory.ItemStack totem = new org.bukkit.inventory.ItemStack(org.bukkit.Material.TOTEM_OF_UNDYING);
        org.bukkit.inventory.meta.ItemMeta meta = totem.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(org.bukkit.ChatColor.GOLD + "" + org.bukkit.ChatColor.BOLD + "Totem de Résurrection");
            
            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add(org.bukkit.ChatColor.GRAY + "Clic droit pour ressusciter");
            lore.add(org.bukkit.ChatColor.GRAY + "un joueur mort");
            lore.add("");
            lore.add(org.bukkit.ChatColor.DARK_PURPLE + "" + org.bukkit.ChatColor.ITALIC + "Consommé lors de l'utilisation");
            meta.setLore(lore);
            
            totem.setItemMeta(meta);
        }
        
        return totem;
    }
    
    private void loadHomesConfig() {
        homesFile = new File(getDataFolder(), "homes.yml");
        if (!homesFile.exists()) {
            try {
                homesFile.createNewFile();
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Impossible de créer homes.yml", e);
            }
        }
        homesConfig = YamlConfiguration.loadConfiguration(homesFile);
    }
    
    public void saveHomesConfig() {
        try {
            homesConfig.save(homesFile);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Impossible de sauvegarder homes.yml", e);
        }
    }
    
    public String getMessage(String path) {
        String message = getConfig().getString("messages." + path, path);
        return ColorUtils.colorize(getConfig().getString("messages.prefix", "") + message);
    }
    
    public String getMessageRaw(String path) {
        return ColorUtils.colorize(getConfig().getString("messages." + path, path));
    }
    
    // Getters
    public static ModerationSMP getInstance() {
        return instance;
    }
    
    public VanishManager getVanishManager() {
        return vanishManager;
    }
    
    public TeleportManager getTeleportManager() {
        return teleportManager;
    }
    
    public HomeManager getHomeManager() {
        return homeManager;
    }
    
    public CombatManager getCombatManager() {
        return combatManager;
    }
    
    public XrayManager getXrayManager() {
        return xrayManager;
    }
    
    public AuthManager getAuthManager() {
        return authManager;
    }
    
    public SkinManager getSkinManager() {
        return skinManager;
    }
    
    public FileConfiguration getHomesConfig() {
        return homesConfig;
    }
    
    public JailManager getJailManager() {
        return jailManager;
    }
    
    public RollbackManager getRollbackManager() {
        return rollbackManager;
    }
    
    public DeathWorldManager getDeathWorldManager() {
        return deathWorldManager;
    }
    
    public PuzzleManager getPuzzleManager() {
        return puzzleManager;
    }
    
    public TeamManager getTeamManager() {
        return teamManager;
    }
    
    public StructureManager getStructureManager() {
        return structureManager;
    }
    
    public GuisterManager getGuisterManager() {
        return guisterManager;
    }
    
    public NoClipManager getNoClipManager() {
        return noClipManager;
    }
    
    public GodModeManager getGodModeManager() {
        return godModeManager;
    }
    
    public LastLocationManager getLastLocationManager() {
        return lastLocationManager;
    }
    
    public FreezeManager getFreezeManager() {
        return freezeManager;
    }
    
    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
    
    public EconomyManager getEconomyManager() {
        return economyManager;
    }
    
    public fr.moderation.auction.AuctionHouse getAuctionHouse() {
        return auctionHouse;
    }
    
    public fr.moderation.shop.ShopManager getShopManager() {
        return shopManager;
    }
    
    public fr.moderation.jobs.JobManager getJobManager() {
        return jobManager;
    }
}
