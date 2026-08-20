package com.ycorner.gamemode;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class YCornerGamemode extends JavaPlugin implements Listener {

    public static final String CREATIVE_PERM = "ycorner.gamemode.creative";

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        CommandBlockFixer commandBlockFixer = new CommandBlockFixer(this);
        getServer().getPluginManager().registerEvents(commandBlockFixer, this);
        getServer().getScheduler().runTaskLater(this, commandBlockFixer::scanLoadedWorlds, 40L);
        for (Player player : getServer().getOnlinePlayers()) {
            applyGamemode(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        getServer().getScheduler().runTask(this, () -> applyGamemode(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        getServer().getScheduler().runTask(this, () -> applyGamemode(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onGamemodeChange(PlayerGameModeChangeEvent event) {
        if (event.getPlayer().hasPermission(CREATIVE_PERM)) {
            return;
        }
        if (event.getNewGameMode() != GameMode.SURVIVAL) {
            event.setCancelled(true);
        }
    }

    private void applyGamemode(Player player) {
        GameMode target = player.hasPermission(CREATIVE_PERM) ? GameMode.CREATIVE : GameMode.SURVIVAL;
        if (player.getGameMode() != target) {
            player.setGameMode(target);
        }
    }
}
