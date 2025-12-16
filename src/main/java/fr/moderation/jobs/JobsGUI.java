package fr.moderation.jobs;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JobsGUI {
    
    /**
     * Ouvre le menu principal des jobs
     */
    public static void openMainMenu(ModerationSMP plugin, Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "" + ChatColor.BOLD + "Jobs");
        
        Map<JobType, PlayerJob> jobs = plugin.getJobManager().getAllJobs(player.getUniqueId());
        
        int[] slots = {10, 11, 12, 14, 15, 16};
        int i = 0;
        
        for (JobType jobType : JobType.values()) {
            if (i >= slots.length) break;
            
            PlayerJob job = jobs.get(jobType);
            ItemStack item = createJobItem(plugin, job);
            inv.setItem(slots[i++], item);
        }
        
        // Bouton statistiques
        ItemStack stats = createButton(Material.BOOK, ChatColor.YELLOW + "Mes Statistiques", 
                                      ChatColor.GRAY + "Voir vos stats globales");
        inv.setItem(22, stats);
        
        player.openInventory(inv);
    }
    
    /**
     * Ouvre le menu d'un job spécifique
     */
    public static void openJobMenu(ModerationSMP plugin, Player player, JobType jobType) {
        PlayerJob job = plugin.getJobManager().getJob(player.getUniqueId(), jobType);
        
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.YELLOW + jobType.getDisplayName());
        
        // Item principal du job
        ItemStack mainItem = createDetailedJobItem(plugin, job);
        inv.setItem(13, mainItem);
        
        // Bouton retour
        ItemStack back = createButton(Material.ARROW, ChatColor.GRAY + "← Retour", 
                                     ChatColor.GRAY + "Retour au menu principal");
        inv.setItem(22, back);
        
        player.openInventory(inv);
    }
    
    /**
     * Crée un item représentant un job
     */
    private static ItemStack createJobItem(ModerationSMP plugin, PlayerJob job) {
        ItemStack item = new ItemStack(job.getJobType().getIcon());
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(ChatColor.YELLOW + job.getJobType().getEmoji() + " " + job.getJobType().getDisplayName());
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + job.getJobType().getDescription());
        lore.add("");
        lore.add(ChatColor.GOLD + "Niveau: " + ChatColor.WHITE + job.getLevel() + ChatColor.GRAY + "/" + JobManager.MAX_LEVEL);
        
        double xpNeeded = job.getXPForNextLevel();
        if (job.getLevel() < JobManager.MAX_LEVEL) {
            lore.add(ChatColor.GOLD + "XP: " + ChatColor.WHITE + (int)job.getCurrentXP() + ChatColor.GRAY + "/" + (int)xpNeeded);
            lore.add(ChatColor.GRAY + "Progrès: " + getProgressBar(job.getProgressPercent()));
        } else {
            lore.add(ChatColor.GREEN + "" + ChatColor.BOLD + "NIVEAU MAX");
        }
        
        lore.add("");
        lore.add(ChatColor.GREEN + "Cliquez pour plus de détails");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    /**
     * Crée un item détaillé d'un job
     */
    private static ItemStack createDetailedJobItem(ModerationSMP plugin, PlayerJob job) {
        ItemStack item = new ItemStack(job.getJobType().getIcon());
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + job.getJobType().getEmoji() + " " + job.getJobType().getDisplayName());
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + job.getJobType().getDescription());
        lore.add("");
        lore.add(ChatColor.GOLD + "═══ Statistiques ═══");
        lore.add(ChatColor.GOLD + "Niveau: " + ChatColor.WHITE + job.getLevel() + ChatColor.GRAY + "/" + JobManager.MAX_LEVEL);
        
        if (job.getLevel() < JobManager.MAX_LEVEL) {
            double xpNeeded = job.getXPForNextLevel();
            lore.add(ChatColor.GOLD + "XP: " + ChatColor.WHITE + (int)job.getCurrentXP() + ChatColor.GRAY + "/" + (int)xpNeeded);
            lore.add(ChatColor.GRAY + "Progrès: " + getProgressBar(job.getProgressPercent()));
        } else {
            lore.add(ChatColor.GREEN + "" + ChatColor.BOLD + "NIVEAU MAX ATTEINT !");
        }
        
        lore.add("");
        lore.add(ChatColor.GOLD + "XP Total: " + ChatColor.WHITE + (int)job.getTotalXP());
        lore.add("");
        
        // Prochaine récompense
        if (job.getLevel() < JobManager.MAX_LEVEL) {
            int nextReward = (int) (Math.ceil(job.getLevel() / JobManager.REWARD_EVERY_N_LEVELS) * JobManager.REWARD_EVERY_N_LEVELS);
            if (nextReward <= JobManager.MAX_LEVEL) {
                int levelsUntilReward = nextReward - job.getLevel();
                lore.add(ChatColor.GOLD + "Prochaine récompense:");
                lore.add(ChatColor.GRAY + "Niveau " + nextReward + " " + ChatColor.YELLOW + "(" + levelsUntilReward + " niveaux)");
                lore.add(ChatColor.GREEN + "+" + plugin.getEconomyManager().format(JobManager.REWARD_AMOUNT));
            }
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    /**
     * Crée un bouton
     */
    private static ItemStack createButton(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        
        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(line);
        }
        meta.setLore(loreList);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Génère une barre de progression
     */
    private static String getProgressBar(double percent) {
        int bars = (int) (percent / 10);
        StringBuilder bar = new StringBuilder(ChatColor.GREEN + "[");
        
        for (int i = 0; i < 10; i++) {
            if (i < bars) {
                bar.append("█");
            } else {
                bar.append(ChatColor.GRAY + "░");
            }
        }
        
        bar.append(ChatColor.GREEN + "] " + ChatColor.WHITE + (int)percent + "%");
        return bar.toString();
    }
}
