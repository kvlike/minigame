package me.kvlike.minigame.listeners;

import me.kvlike.minigame.Minigame;
import me.kvlike.minigame.arenamanager.Arena;
import me.kvlike.minigame.arenamanager.GameState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player p = (Player) e.getDamager();
            if (Minigame.playerArenaMap.get(p) != null) {
                Arena arena = Minigame.arenaManager.getArena(Minigame.playerArenaMap.get(p));
                if (arena.checkSpectator(p)
                        || arena.getGameState() != GameState.PLAYING) {
                    e.setCancelled(true);
                }
            }
        }
    }

}
