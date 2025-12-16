package fr.moderation.managers;

import fr.moderation.ModerationSMP;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager {
    
    private final ModerationSMP plugin;
    private final Map<UUID, Double> balances;
    private final File economyFile;
    private FileConfiguration economyConfig;
    
    private final double STARTING_BALANCE = 1000.0;
    private final String CURRENCY_SYMBOL = "$";
    
    public EconomyManager(ModerationSMP plugin) {
        this.plugin = plugin;
        this.balances = new HashMap<>();
        
        this.economyFile = new File(plugin.getDataFolder(), "economy.yml");
        if (!economyFile.exists()) {
            try {
                economyFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Unable to create economy.yml");
            }
        }
        
        this.economyConfig = YamlConfiguration.loadConfiguration(economyFile);
        loadBalances();
    }
    
    public void loadBalances() {
        if (economyConfig.contains("balances")) {
            for (String uuidStr : economyConfig.getConfigurationSection("balances").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    double balance = economyConfig.getDouble("balances." + uuidStr);
                    balances.put(uuid, balance);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in economy.yml: " + uuidStr);
                }
            }
        }
        plugin.getLogger().info("Loaded " + balances.size() + " bank accounts");
    }
    
    public void saveBalances() {
        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            economyConfig.set("balances." + entry.getKey().toString(), entry.getValue());
        }
        
        try {
            economyConfig.save(economyFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Unable to save economy.yml");
        }
    }
    
    public double getBalance(UUID playerId) {
        return balances.getOrDefault(playerId, STARTING_BALANCE);
    }
    
    public void setBalance(UUID playerId, double amount) {
        if (amount < 0) {
            amount = 0;
        }
        balances.put(playerId, amount);
        saveBalances();
    }
    
    public void addBalance(UUID playerId, double amount) {
        double current = getBalance(playerId);
        setBalance(playerId, current + amount);
    }
    
    public boolean removeBalance(UUID playerId, double amount) {
        double current = getBalance(playerId);
        if (current < amount) {
            return false;
        }
        setBalance(playerId, current - amount);
        return true;
    }
    
    public boolean hasBalance(UUID playerId, double amount) {
        return getBalance(playerId) >= amount;
    }
    
    public boolean transfer(UUID from, UUID to, double amount) {
        if (!hasBalance(from, amount)) {
            return false;
        }
        
        removeBalance(from, amount);
        addBalance(to, amount);
        return true;
    }
    
    public String format(double amount) {
        return String.format("%,.2f%s", amount, CURRENCY_SYMBOL);
    }
    
    public String getCurrencySymbol() {
        return CURRENCY_SYMBOL;
    }
    
    public double getStartingBalance() {
        return STARTING_BALANCE;
    }
    
    public void createAccount(UUID playerId) {
        if (!balances.containsKey(playerId)) {
            setBalance(playerId, STARTING_BALANCE);
            plugin.getLogger().info("Account created for " + playerId + " with " + format(STARTING_BALANCE));
        }
    }
}
