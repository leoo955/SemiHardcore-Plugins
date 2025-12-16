package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CoreProtectCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public CoreProtectCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("moderation.coreprotect")) {
            player.sendMessage(plugin.getMessage("no_permission"));
            return true;
        }
        
        // /co rollback t:<temps> r:<rayon> [u:<utilisateur>]
        // /co restore t:<temps> r:<rayon> [u:<utilisateur>]
        
        if (args.length < 1) {
            sendUsage(player);
            return true;
        }
        
        String action = args[0].toLowerCase();
        
        if (!action.equals("rollback") && !action.equals("restore") && !action.equals("rb") && !action.equals("rs")) {
            sendUsage(player);
            return true;
        }
        
        // Parse parameters
        long timeSeconds = 3600; // 1 hour default
        int radius = 10; // 10 blocks default
        String userFilter = null;
        
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            
            if (arg.toLowerCase().startsWith("t:")) {
                timeSeconds = parseTime(arg.substring(2));
                if (timeSeconds <= 0) {
                    player.sendMessage(plugin.getMessage("prefix") + "§cTemps invalide !");
                    return true;
                }
            } else if (arg.toLowerCase().startsWith("r:")) {
                try {
                    radius = Integer.parseInt(arg.substring(2));
                    if (radius <= 0 || radius > 100) {
                        player.sendMessage(plugin.getMessage("prefix") + "§cRayon invalide ! (Max: 100)");
                        return true;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(plugin.getMessage("prefix") + "§cRayon invalide !");
                    return true;
                }
            } else if (arg.toLowerCase().startsWith("u:")) {
                userFilter = arg.substring(2);
            }
        }
        
        // Execute action
        player.sendMessage(plugin.getMessage("prefix") + "§7Traitement en cours...");
        
        int count;
        if (action.equals("rollback") || action.equals("rb")) {
            count = plugin.getRollbackManager().rollback(timeSeconds, radius, player.getLocation(), userFilter);
            player.sendMessage(plugin.getMessage("prefix") + "§aRollback terminé : §e" + count + " §ablocs restaurés.");
        } else {
            count = plugin.getRollbackManager().restore(timeSeconds, radius, player.getLocation(), userFilter);
            player.sendMessage(plugin.getMessage("prefix") + "§aRestore terminé : §e" + count + " §ablocs restaurés.");
        }
        
        return true;
    }
    
    private void sendUsage(Player player) {
        player.sendMessage(plugin.getMessage("prefix") + "§7========== CoreProtect ==========");
        player.sendMessage(plugin.getMessage("prefix") + "§e/co rollback t:<temps> r:<rayon> [u:<utilisateur>]");
        player.sendMessage(plugin.getMessage("prefix") + "§e/co restore t:<temps> r:<rayon> [u:<utilisateur>]");
        player.sendMessage(plugin.getMessage("prefix") + "");
        player.sendMessage(plugin.getMessage("prefix") + "§7Exemples:");
        player.sendMessage(plugin.getMessage("prefix") + "§e/co rollback t:30m r:20");
        player.sendMessage(plugin.getMessage("prefix") + "§e/co restore t:1h r:10 u:Notch");
        player.sendMessage(plugin.getMessage("prefix") + "");
        player.sendMessage(plugin.getMessage("prefix") + "§7Temps: §fs = secondes, §fm = minutes, §fh = heures, §fd = jours");
    }
    
    private long parseTime(String timeStr) {
        try {
            timeStr = timeStr.toLowerCase();
            
            if (timeStr.endsWith("s")) {
                return Long.parseLong(timeStr.substring(0, timeStr.length() - 1));
            } else if (timeStr.endsWith("m")) {
                return Long.parseLong(timeStr.substring(0, timeStr.length() - 1)) * 60;
            } else if (timeStr.endsWith("h")) {
                return Long.parseLong(timeStr.substring(0, timeStr.length() - 1)) * 3600;
            } else if (timeStr.endsWith("d")) {
                return Long.parseLong(timeStr.substring(0, timeStr.length() - 1)) * 86400;
            } else {
                // By default, consider as seconds
                return Long.parseLong(timeStr);
            }
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
