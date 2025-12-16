package fr.moderation.listeners;

import fr.moderation.ModerationSMP;
import fr.moderation.managers.DeathWorldManager;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    
    private final ModerationSMP plugin;
    private final DeathWorldManager deathWorldManager;
    
    public PlayerJoinListener(ModerationSMP plugin) {
        this.plugin = plugin;
        this.deathWorldManager = plugin.getDeathWorldManager();
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (deathWorldManager != null && deathWorldManager.isGhost(player.getUniqueId())) {
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage(ChatColor.GRAY + "Vous êtes un " + ChatColor.WHITE + "FANTÔME" + ChatColor.GRAY + ".");
            player.sendMessage(ChatColor.GRAY + "Attendez qu'un joueur effectue le rituel pour vous ressusciter.");
        }
    }
}
