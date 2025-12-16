package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import fr.moderation.jobs.JobsGUI;
import fr.moderation.jobs.JobType;
import fr.moderation.jobs.PlayerJob;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JobsCommand implements CommandExecutor, TabCompleter {
    
    private final ModerationSMP plugin;
    
    public JobsCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Seul un joueur peut utiliser cette commande.");
            return true;
        }
        
        Player player = (Player) sender;
        
        // /jobs - Ouvre le GUI
        if (args.length == 0) {
            JobsGUI.openMainMenu(plugin, player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "stats":
                showStats(player);
                break;
            case "info":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /jobs info <job>");
                    return true;
                }
                showJobInfo(player, args[1]);
                break;
            default:
                JobsGUI.openMainMenu(plugin, player);
                break;
        }
        
        return true;
    }
    
    private void showStats(Player player) {
        Map<JobType, PlayerJob> jobs = plugin.getJobManager().getAllJobs(player.getUniqueId());
        
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "  VOS STATISTIQUES JOBS");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════");
        
        for (JobType type : JobType.values()) {
            PlayerJob job = jobs.get(type);
            String emoji = type.getEmoji();
            String name = type.getDisplayName();
            int level = job.getLevel();
            int totalXP = (int) job.getTotalXP();
            
            player.sendMessage(ChatColor.YELLOW + emoji + " " + name + " " + 
                             ChatColor.GRAY + "- Niveau " + ChatColor.WHITE + level + 
                             ChatColor.GRAY + " (" + totalXP + " XP total)");
        }
        
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════");
        player.sendMessage("");
    }
    
    private void showJobInfo(Player player, String jobName) {
        JobType jobType;
        try {
            jobType = JobType.valueOf(jobName.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Job invalide ! Jobs disponibles:");
            for (JobType type : JobType.values()) {
                player.sendMessage(ChatColor.YELLOW + "- " + type.name().toLowerCase());
            }
            return;
        }
        
        JobsGUI.openJobMenu(plugin, player, jobType);
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("stats", "info"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("info")) {
            for (JobType type : JobType.values()) {
                completions.add(type.name().toLowerCase());
            }
        }
        
        return completions;
    }
}
