package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;



public class JailCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public JailCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("moderation.jail")) {
            sender.sendMessage(plugin.getMessage("no_permission"));
            return true;
        }
        
        // /jail <joueur> <prison> <temps> [raison]
        if (args.length < 3) {
            sender.sendMessage(plugin.getMessage("prefix") + "§cUsage: /jail <joueur> <prison> <temps> [raison]");
            sender.sendMessage(plugin.getMessage("prefix") + "§7Temps en minutes (ex: 30, 60, 120)");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(plugin.getMessage("player_not_found"));
            return true;
        }
        
        String jailName = args[1];
        if (!plugin.getJailManager().jailExists(jailName)) {
            sender.sendMessage(plugin.getMessage("prefix") + "§cLa prison §e" + jailName + " §cn'existe pas.");
            sender.sendMessage(plugin.getMessage("prefix") + "§7Prisons disponibles: §e" + String.join(", ", plugin.getJailManager().getJailNames()));
            return true;
        }
        
        long durationMinutes;
        try {
            durationMinutes = Long.parseLong(args[2]);
            if (durationMinutes <= 0) {
                sender.sendMessage(plugin.getMessage("prefix") + "§cLa durée doit être positive !");
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getMessage("prefix") + "§cDurée invalide ! Utilisez un nombre en minutes.");
            return true;
        }
        
        // Raison optionnelle
        String reason = "";
        if (args.length > 3) {
            StringBuilder reasonBuilder = new StringBuilder();
            for (int i = 3; i < args.length; i++) {
                reasonBuilder.append(args[i]).append(" ");
            }
            reason = reasonBuilder.toString().trim();
        }
        
        // Emprisonner le joueur
        plugin.getJailManager().jailPlayer(target, jailName, durationMinutes, reason);
        
        // Message au staff
        sender.sendMessage(plugin.getMessage("prefix") + "§aVous avez emprisonné §e" + target.getName() + 
                          " §adans §e" + jailName + " §apour §e" + durationMinutes + " minutes§a.");
        
        // Broadcast aux admins
        String broadcastMessage = plugin.getMessage("prefix") + "§e" + sender.getName() + 
                                 " §aa emprisonné §e" + target.getName() + 
                                 " §adans §e" + jailName + " §apour §e" + durationMinutes + " minutes§a.";
        if (!reason.isEmpty()) {
            broadcastMessage += " §7(Raison: " + reason + ")";
        }
        
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("moderation.jail.notify") && !online.equals(sender)) {
                online.sendMessage(broadcastMessage);
            }
        }
        
        return true;
    }
}
