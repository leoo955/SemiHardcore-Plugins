package fr.moderation.managers;

import fr.moderation.ModerationSMP;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class AuthManager {
    
    private final ModerationSMP plugin;
    private final Set<UUID> loggedInPlayers;
    private File authFile;
    private FileConfiguration authConfig;
    
    public AuthManager(ModerationSMP plugin) {
        this.plugin = plugin;
        this.loggedInPlayers = new HashSet<>();
        loadAuthConfig();
    }
    
    private void loadAuthConfig() {
        authFile = new File(plugin.getDataFolder(), "auth.yml");
        if (!authFile.exists()) {
            try {
                authFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Unable to create auth.yml", e);
            }
        }
        authConfig = YamlConfiguration.loadConfiguration(authFile);
    }
    
    public void saveAuthConfig() {
        try {
            authConfig.save(authFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Unable to save auth.yml", e);
        }
    }
    
    public boolean isRegistered(UUID uuid) {
        return authConfig.contains("passwords." + uuid.toString());
    }
    
    public boolean isLoggedIn(Player player) {
        return loggedInPlayers.contains(player.getUniqueId());
    }
    
    public void register(Player player, String password) {
        String hash = hashPassword(password);
        authConfig.set("passwords." + player.getUniqueId().toString(), hash);
        saveAuthConfig();
        login(player);
    }
    
    public boolean login(Player player, String password) {
        String storedHash = authConfig.getString("passwords." + player.getUniqueId().toString());
        if (storedHash == null) return false;
        
        if (storedHash.equals(hashPassword(password))) {
            login(player);
            return true;
        }
        return false;
    }
    
    public void login(Player player) {
        loggedInPlayers.add(player.getUniqueId());
    }
    
    public void logout(Player player) {
        loggedInPlayers.remove(player.getUniqueId());
    }
    
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
