package fr.moderation.listeners;

import fr.moderation.ModerationSMP;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CombatListener implements Listener {
    
    private final ModerationSMP plugin;
    
    public CombatListener(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player victim = (Player) event.getEntity();
        Player attacker = null;
        
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile proj = (Projectile) event.getDamager();
            if (proj.getShooter() instanceof Player) {
                attacker = (Player) proj.getShooter();
            }
        }
        
        if (attacker != null && !attacker.equals(victim)) {
            plugin.getCombatManager().tagPlayers(attacker, victim);
        }
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getCombatManager().handlePlayerQuit(event.getPlayer());
    }
}
