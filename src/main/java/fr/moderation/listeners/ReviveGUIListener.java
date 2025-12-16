package fr.moderation.listeners;

import fr.moderation.ModerationSMP;
import fr.moderation.managers.DeathWorldManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.UUID;

public class ReviveGUIListener implements Listener {
    
    private final ModerationSMP plugin;
    private final DeathWorldManager deathWorldManager;
    
    public ReviveGUIListener(ModerationSMP plugin, DeathWorldManager deathWorldManager) {
        this.plugin = plugin;
        this.deathWorldManager = deathWorldManager;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(ChatColor.DARK_PURPLE + "Autel de Résurrection")) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();
        ItemStack clickedItem = event.getCurrentItem();
        
        // Prevent taking decoration items (glass panes, indicators)
        if (clickedItem != null && (clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE || 
            clickedItem.getType() == Material.EMERALD_BLOCK ||
            (clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName() && clickedItem.getItemMeta().getDisplayName().contains("Lingots")))) { // Check plus large
            
            if (event.getSlot() != 49 && !isInputSlot(event.getSlot())) {
                event.setCancelled(true);
            }
        }
        
        // Clic sur le bouton confirmer
        if (event.getSlot() == 49 && clickedItem != null && clickedItem.getType() == Material.EMERALD_BLOCK) {
            event.setCancelled(true);
            checkAndRevive(player, inv);
        }
    }
    
    private boolean isInputSlot(int slot) {
        return slot == 13 || slot == 20 || slot == 22 || slot == 24 || slot == 31;
    }
    
    private void checkAndRevive(Player player, Inventory inv) {
        // Check items
        ItemStack head = inv.getItem(13);
        ItemStack gold = inv.getItem(20);
        ItemStack carrots = inv.getItem(22);
        ItemStack eyes = inv.getItem(24);
        ItemStack helmet = inv.getItem(31);
        
        // 1. Check head
        if (head == null || head.getType() != Material.PLAYER_HEAD) {
            player.sendMessage(ChatColor.RED + "Il manque la tête du joueur !");
            return;
        }
        
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            player.sendMessage(ChatColor.RED + "Ce n'est pas une tête de résurrection valide.");
            return;
        }
        
        List<String> lore = meta.getLore();
        if (lore.size() < 3 || !lore.get(1).contains("ressusciter")) {
            player.sendMessage(ChatColor.RED + "Ce n'est pas une tête de résurrection valide.");
            return;
        }
        
        String uuidStr = ChatColor.stripColor(lore.get(2));
        UUID targetId;
        try {
            targetId = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Tête invalide (UUID corrompu).");
            return;
        }
        
        // 2. Check other items
        if (!checkItem(gold, Material.NETHERITE_INGOT, 5)) {
            player.sendMessage(ChatColor.RED + "Il manque les 5 lingots de netherite !");
            return;
        }
        
        if (!checkItem(carrots, Material.GOLDEN_CARROT, 124)) {
            player.sendMessage(ChatColor.RED + "Il manque les 124 carottes dorées !");
            return;
        }
        
        if (!checkItem(eyes, Material.ENDER_EYE, 4)) {
            player.sendMessage(ChatColor.RED + "Il manque les 4 yeux de l'End !");
            return;
        }
        
        if (!checkItem(helmet, Material.NETHERITE_HELMET, 1)) {
            player.sendMessage(ChatColor.RED + "Il manque le casque en Netherite !");
            return;
        }
        
        // 3. Attempt resurrection
        if (deathWorldManager.fullyRevivePlayer(targetId, player.getLocation())) {
            // Success! Consume items
            inv.setItem(13, null);
            inv.setItem(20, null);
            inv.setItem(22, null);
            inv.setItem(24, null);
            inv.setItem(31, null);
            
            player.sendMessage(ChatColor.GREEN + "Le rituel est un succès !");
            player.closeInventory();
        } else {
            player.sendMessage(ChatColor.RED + "Ce joueur n'est pas prêt à être ressuscité (il doit finir son épreuve) ou n'est pas mort.");
        }
    }
    
    private boolean checkItem(ItemStack item, Material material, int amount) {
        return item != null && item.getType() == material && item.getAmount() >= amount;
    }
}
