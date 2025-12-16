package fr.moderation.managers;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import fr.moderation.ModerationSMP;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StructureManager {
    
    private final ModerationSMP plugin;
    private final File structuresDir;
    
    public StructureManager(ModerationSMP plugin) {
        this.plugin = plugin;
        this.structuresDir = new File(plugin.getDataFolder(), "structures");
        
        if (!structuresDir.exists()) {
            structuresDir.mkdirs();
        }
    }
    
    public boolean saveStructure(Player player, String name) {
        try {
            com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(player);
            com.sk89q.worldedit.LocalSession session = WorldEdit.getInstance().getSessionManager().get(wePlayer);
            
            Region region = session.getSelection(wePlayer.getWorld());
            if (region == null) {
                player.sendMessage("§cVous devez faire une sélection WorldEdit d'abord !");
                return false;
            }
            
            // Copy the region into a clipboard
            com.sk89q.worldedit.world.World weWorld = wePlayer.getWorld();
            Clipboard clipboard = new com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard(region);
            
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)) {
                com.sk89q.worldedit.function.operation.ForwardExtentCopy copy = 
                    new com.sk89q.worldedit.function.operation.ForwardExtentCopy(
                        editSession, region, clipboard, region.getMinimumPoint()
                    );
                copy.setCopyingEntities(true);
                Operations.complete(copy);
            }
            
            // Save to file
            File file = new File(structuresDir, name + ".schem");
            
            try (ClipboardWriter writer = com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(file))) {
                writer.write(clipboard);
            }
            
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error saving structure " + name + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public Clipboard loadStructure(String name) {
        File file = new File(structuresDir, name + ".schem");
        if (!file.exists()) {
            return null;
        }
        
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) {
            return null;
        }
        
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            return reader.read();
        } catch (IOException e) {
            plugin.getLogger().warning("Error loading structure " + name + ": " + e.getMessage());
            return null;
        }
    }
    
    public boolean pasteStructure(String name, Location location) {
        Clipboard clipboard = loadStructure(name);
        if (clipboard == null) {
            return false;
        }
        
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(location.getWorld()))) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
                    .ignoreAirBlocks(false)
                    .build();
            
            Operations.complete(operation);
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error pasting structure " + name + ": " + e.getMessage());
            return false;
        }
    }
    
    public List<String> listStructures() {
        List<String> structures = new ArrayList<>();
        File[] files = structuresDir.listFiles((dir, name) -> name.endsWith(".schem"));
        
        if (files != null) {
            for (File file : files) {
                String name = file.getName().replace(".schem", "");
                structures.add(name);
            }
        }
        
        return structures;
    }
    
    public boolean structureExists(String name) {
        File file = new File(structuresDir, name + ".schem");
        return file.exists();
    }
    
    public void setPuzzleStructure(String puzzleType, String structureName) {
        plugin.getConfig().set("puzzles." + puzzleType + ".custom-structure", structureName);
        plugin.saveConfig();
    }
    
    public String getPuzzleStructure(String puzzleType) {
        return plugin.getConfig().getString("puzzles." + puzzleType + ".custom-structure", null);
    }
}
