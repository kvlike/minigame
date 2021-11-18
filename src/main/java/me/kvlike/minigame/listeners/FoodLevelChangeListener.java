package me.kvlike.minigame.listeners;

import me.kvlike.minigame.Minigame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class FoodLevelChangeListener implements Listener {

    @EventHandler
    public void onHungerLoss(FoodLevelChangeEvent e) {

        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            if (Minigame.playerArenaMap.get(player) != null) {
                e.setCancelled(true);
            }
        }

    }

}
