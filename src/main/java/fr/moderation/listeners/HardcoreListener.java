package fr.moderation.listeners;

import fr.moderation.ModerationSMP;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class HardcoreListener implements Listener {
    
    private final ModerationSMP plugin;
    
    public HardcoreListener(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        // Do NOT activate spectator mode if death world system is active
        if (plugin.getConfig().getBoolean("death-dimension.enabled", true)) {
            plugin.getLogger().info("HardcoreListener: Death dimension activé, pas de spectateur pour " + player.getName());
            return;
        }
        
        // Mettre le joueur en mode spectateur
        player.setGameMode(GameMode.SPECTATOR);
        
        // Optionnel: Ajouter Night Vision comme pour /spec
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
            org.bukkit.potion.PotionEffectType.NIGHT_VISION, 
            Integer.MAX_VALUE, 
            0, 
            false, 
            false
        ));
        
        // Message
        player.sendMessage(plugin.getMessage("prefix") + "§cVous êtes mort ! Mode spectateur activé.");
    }
}
