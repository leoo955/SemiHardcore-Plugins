package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import fr.moderation.managers.DeathWorldManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReviveCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    private final DeathWorldManager deathWorldManager;
    
    public ReviveCommand(ModerationSMP plugin, DeathWorldManager deathWorldManager) {
        this.plugin = plugin;
        this.deathWorldManager = deathWorldManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Seul un joueur peut utiliser cette commande.");
            return true;
        }
        
        Player player = (Player) sender;
        openReviveGUI(player);
        return true;
    }
    
    private void openReviveGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "Autel de Résurrection");
        
        // Remplir avec des vitres noires
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        
        for (int i = 0; i < 54; i++) {
            gui.setItem(i, glass);
        }
        
        // Slots for items (air to let player deposit)
        // Slot 13: Head
        gui.setItem(13, null);
        // Slot 20: Or
        gui.setItem(20, null);
        // Slot 22: Carottes
        gui.setItem(22, null);
        // Slot 24: Yeux de l'End
        gui.setItem(24, null);
        // Slot 31: Casque Netherite
        gui.setItem(31, null);
        
        // Indicateurs
        gui.setItem(4, createGuiItem(Material.PLAYER_HEAD, ChatColor.YELLOW + "Déposez la tête du joueur ici", ""));
        gui.setItem(11, createGuiItem(Material.NETHERITE_INGOT, ChatColor.DARK_PURPLE + "5 Lingots de Netherite", ""));
        gui.setItem(13, null); // Head (empty)
        gui.setItem(15, createGuiItem(Material.ENDER_EYE, ChatColor.DARK_AQUA + "4 Yeux de l'End", ""));
        
        gui.setItem(29, createGuiItem(Material.GOLDEN_CARROT, ChatColor.GOLD + "124 Carottes Dorées", ""));
        gui.setItem(33, createGuiItem(Material.NETHERITE_HELMET, ChatColor.DARK_PURPLE + "1 Casque en Netherite", ""));
        
        // Bouton de confirmation
        gui.setItem(49, createGuiItem(Material.EMERALD_BLOCK, ChatColor.GREEN + "CONFIRMER LE RITUEL", ChatColor.GRAY + "Cliquez pour tenter la résurrection"));
        
        // Free input slots (set to null to make it clear)
        List<Integer> inputSlots = Arrays.asList(13, 20, 22, 24, 31);
        for (int slot : inputSlots) {
            gui.setItem(slot, null);
        }
        
        player.openInventory(gui);
    }
    
    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
}
