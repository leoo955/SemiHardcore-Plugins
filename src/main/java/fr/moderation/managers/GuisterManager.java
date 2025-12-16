package fr.moderation.managers;

import fr.moderation.ModerationSMP;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GuisterManager {
    
    private final ModerationSMP plugin;
    private final File guisterFile;
    private final YamlConfiguration guisterConfig;
    private final Set<UUID> guisterPlayers;
    
    public GuisterManager(ModerationSMP plugin) {
        this.plugin = plugin;
        this.guisterPlayers = new HashSet<>();
        
        this.guisterFile = new File(plugin.getDataFolder(), "guister.yml");
        if (!guisterFile.exists()) {
            try {
                guisterFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Unable to create guister.yml");
            }
        }
        this.guisterConfig = YamlConfiguration.loadConfiguration(guisterFile);
        
        loadGuisters();
    }
    
    private void loadGuisters() {
        if (guisterConfig.contains("guisters")) {
            for (String uuidStr : guisterConfig.getStringList("guisters")) {
                try {
                    guisterPlayers.add(UUID.fromString(uuidStr));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in guister.yml: " + uuidStr);
                }
            }
        }
    }
    
    public void saveGuisters() {
        java.util.List<String> uuidList = new java.util.ArrayList<>();
        for (UUID uuid : guisterPlayers) {
            uuidList.add(uuid.toString());
        }
        guisterConfig.set("guisters", uuidList);
        
        try {
            guisterConfig.save(guisterFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Unable to save guister.yml");
        }
    }
    
    public void addGuister(UUID playerId) {
        guisterPlayers.add(playerId);
        saveGuisters();
    }
    
    public void removeGuister(UUID playerId) {
        guisterPlayers.remove(playerId);
        saveGuisters();
    }
    
    public boolean isGuister(UUID playerId) {
        return guisterPlayers.contains(playerId);
    }
    
    public Set<UUID> getAllGuisters() {
        return new HashSet<>(guisterPlayers);
    }
}
