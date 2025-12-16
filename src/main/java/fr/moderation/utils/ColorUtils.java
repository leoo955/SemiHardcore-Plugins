package fr.moderation.utils;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {
    
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    
    /**
     * Convertit les codes couleur (&) en ChatColor
     * Support des codes hexadécimaux &#RRGGBB pour Minecraft 1.16+
     */
    public static String colorize(String message) {
        if (message == null) {
            return "";
        }
        
        // Support for hexadecimal codes
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of("#" + hex).toString());
        }
        matcher.appendTail(buffer);
        
        // Convertir les codes & classiques
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
    
    /**
     * Retire tous les codes couleur d'un message
     */
    public static String stripColors(String message) {
        if (message == null) {
            return "";
        }
        return ChatColor.stripColor(colorize(message));
    }
}
