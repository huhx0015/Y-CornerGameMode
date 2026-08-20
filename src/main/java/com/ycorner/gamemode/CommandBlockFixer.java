package com.ycorner.gamemode;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Repairs command-block teleporters after the Paper 26.2 upgrade:
 * force vanilla /minecraft:tp, widen dy so player hitboxes match,
 * and strip nausea/confusion effect commands.
 */
final class CommandBlockFixer implements Listener {

    private static final Pattern NAUSEA = Pattern.compile(
            "(?i)\\beffect\\b.*\\b(nausea|confusion|minecraft:nausea)\\b");
    private static final Pattern DY_ONE = Pattern.compile("(?i)([,\\[])dy=1([,\\]])");

    private final JavaPlugin plugin;
    private int updated;

    CommandBlockFixer(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    void scanLoadedWorlds() {
        for (World world : plugin.getServer().getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                scanChunk(chunk);
            }
        }
        plugin.getLogger().info("Updated " + updated + " command block(s) for teleporters.");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        scanChunk(event.getChunk());
    }

    private void scanChunk(Chunk chunk) {
        for (BlockState state : chunk.getTileEntities()) {
            if (!(state instanceof CommandBlock commandBlock)) {
                continue;
            }
            String original = commandBlock.getCommand();
            if (original == null || original.isBlank()) {
                continue;
            }
            String fixed = fixCommand(original);
            if (fixed.equals(original)) {
                continue;
            }
            commandBlock.setCommand(fixed);
            if (commandBlock.update(true, false)) {
                updated++;
                plugin.getLogger().info("Command block at "
                        + state.getX() + "," + state.getY() + "," + state.getZ()
                        + " => " + fixed);
            }
        }
    }

    static String fixCommand(String command) {
        String trimmed = command.trim();
        if (NAUSEA.matcher(trimmed).find()) {
            return "";
        }
        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (lower.startsWith("/tp ") || lower.startsWith("tp ")) {
            trimmed = trimmed.replaceFirst("(?i)^/?(tp)\\b", "minecraft:tp");
        } else if (lower.startsWith("/teleport ") || lower.startsWith("teleport ")) {
            trimmed = trimmed.replaceFirst("(?i)^/?(teleport)\\b", "minecraft:teleport");
        } else if (lower.startsWith("/effect ") || lower.startsWith("effect ")) {
            trimmed = trimmed.replaceFirst("(?i)^/?(effect)\\b", "minecraft:effect");
        }
        return DY_ONE.matcher(trimmed).replaceAll("$1dy=3$2");
    }
}
