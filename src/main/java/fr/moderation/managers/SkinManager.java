package fr.moderation.managers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SkinManager {
    
    private final ModerationSMP plugin;
    private final Map<UUID, Object> skinCache; // Object = Property
    
    public SkinManager(ModerationSMP plugin) {
        this.plugin = plugin;
        this.skinCache = new HashMap<>();
    }
    
    public void setSkin(Player player, String skinName) {
        CompletableFuture.runAsync(() -> {
            try {
                
                // Step 1: Retrieves the target player's UUID from Mojang API
                // Example: "Notch" -> "069a79f4-44e9-4726-a5be-fca90e38aaf5"
                URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + skinName);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                
                if (conn.getResponseCode() != 200) {
                    player.sendMessage(plugin.getMessage("prefix") + "§cJoueur introuvable ou API Mojang indisponible.");
                    return;
                }
                
                JsonObject json = JsonParser.parseReader(new InputStreamReader(conn.getInputStream())).getAsJsonObject();
                String uuidStr = json.get("id").getAsString();
                String name = json.get("name").getAsString();
                
                
                // Step 2: Retrieves texture data (skin + cape) from Mojang servers
                // This data contains cryptographic signature to prevent falsification
                URL sessionUrl = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuidStr + "?unsigned=false");
                HttpURLConnection sessionConn = (HttpURLConnection) sessionUrl.openConnection();
                sessionConn.setRequestMethod("GET");
                
                if (sessionConn.getResponseCode() != 200) {
                    player.sendMessage(plugin.getMessage("prefix") + "§cImpossible de récupérer le skin.");
                    return;
                }
                
                JsonObject sessionJson = JsonParser.parseReader(new InputStreamReader(sessionConn.getInputStream())).getAsJsonObject();
                JsonObject propertyJson = sessionJson.getAsJsonArray("properties").get(0).getAsJsonObject();
                String value = propertyJson.get("value").getAsString();
                String signature = propertyJson.get("signature").getAsString();
                
                
                // Uses Java reflection to create a Property object (Mojang class)
                // Necessary because this class is not directly accessible
                Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
                Constructor<?> constructor = propertyClass.getConstructor(String.class, String.class, String.class);
                Object skinProperty = constructor.newInstance("textures", value, signature);
                
                skinCache.put(player.getUniqueId(), skinProperty);
                
                // Step 3: Applies the skin to the player (must be done on Minecraft's main thread)
                Bukkit.getScheduler().runTask(plugin, () -> {
                    applySkin(player, skinProperty);
                    player.sendMessage(plugin.getMessage("prefix") + "§aSkin changé pour celui de " + name + " !");
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(plugin.getMessage("prefix") + "§cErreur lors du changement de skin.");
            }
        });
    }
    
    private void applySkin(Player player, Object property) {
        try {
            // Retrieves the NMS (Net.Minecraft.Server) object of the player via reflection
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object profile = handle.getClass().getMethod("getProfile").invoke(handle);
            
            // profile.getProperties() returns a PropertyMap containing all player properties  
            Method getPropertiesMethod = profile.getClass().getMethod("getProperties");
            Object properties = getPropertiesMethod.invoke(profile);
            
            // Removes old texture then adds the new one
            // PropertyMap uses a Multimap (multiple values per key)
            Method removeAllMethod = properties.getClass().getMethod("removeAll", Object.class);
            removeAllMethod.invoke(properties, "textures");
            
            Method putMethod = properties.getClass().getMethod("put", Object.class, Object.class);
            putMethod.invoke(properties, "textures", property);
            
            // Refreshes the player so others can see the new skin
            hideAndShow(player);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void hideAndShow(Player player) {
        for (Player target : Bukkit.getOnlinePlayers()) {
            target.hidePlayer(plugin, player);
            target.showPlayer(plugin, player);
        }
    }
}
