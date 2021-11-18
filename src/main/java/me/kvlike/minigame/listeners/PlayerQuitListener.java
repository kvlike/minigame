package me.kvlike.minigame.listeners;

import me.kvlike.minigame.Minigame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (Minigame.playerArenaMap.containsKey(e.getPlayer())) {
            Minigame.arenaManager.getArena(Minigame.playerArenaMap.get(e.getPlayer())).leave(e.getPlayer(), true);
        }
    }

}
