package fr.moderation.listeners;

import fr.moderation.ModerationSMP;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    
    private final ModerationSMP plugin;
    
    public ChatListener(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        // Obtenir la couleur de team du joueur (code couleur, pas le nom)
        String teamColor = plugin.getTeamManager().getPlayerTeamColor(player.getUniqueId());
        
        // Préfixe STAFF pour les OPs
        String staffPrefix = "";
        if (player.isOp()) {
            staffPrefix = ChatColor.RED + "" + ChatColor.BOLD + "[STAFF] " + ChatColor.RESET;
        }
        
        // Format du nom avec couleur de team (JUSTE LA COULEUR, PAS LE NOM)
        String coloredName;
        if (teamColor != null) {
            // Joueur dans une team - nom en couleur de la team
            coloredName = staffPrefix + ChatColor.translateAlternateColorCodes('&', teamColor) + player.getName();
        } else {
            // Joueur sans team - nom en gris
            coloredName = staffPrefix + ChatColor.GRAY + player.getName();
        }
        
        // Shout - tout le monde entend (bold)
        if (message.startsWith("!")) {
            String shoutMessage = message.substring(1).trim();
            if (shoutMessage.isEmpty()) {
                return;
            }
            
            event.setFormat(coloredName + ChatColor.RESET + ChatColor.WHITE + ": " + ChatColor.BOLD + "%2$s");
            event.setMessage(shoutMessage);
            return;
        }
        
        // Chat global - tout le monde entend
        event.setFormat(coloredName + ChatColor.RESET + ChatColor.WHITE + ": %2$s");
    }
}
