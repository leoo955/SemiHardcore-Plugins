package fr.moderation.listeners;

import fr.moderation.ModerationSMP;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class EconomyListener implements Listener {
    
    private final ModerationSMP plugin;
    
    public EconomyListener(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Create account for player if they don't have one
        plugin.getEconomyManager().createAccount(event.getPlayer().getUniqueId());
    }
}
