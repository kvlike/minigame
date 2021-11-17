package me.kvlike.minigame.weapons;

import me.kvlike.minigame.Minigame;
import me.kvlike.minigame.events.ShotDamageEvent;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;


public class ShootingListener implements Listener {

    private boolean getLookingAt(Player player, Player player1)
    {
        Location eye = player.getEyeLocation();
        Vector toEntity = player1.getEyeLocation().toVector().subtract(eye.toVector());
        double dot = toEntity.normalize().dot(eye.getDirection());

        if(player.hasLineOfSight(player1)) return dot > 0.99D;

        else return false;
    }


    @EventHandler
    public void onShoot(PlayerInteractEvent e){
        ItemStack item = e.getItem();
        if(item != null) {
            ItemMeta meta = item.getItemMeta();
            for (String weapon : WeaponsYaml.get().getConfigurationSection("weapons").getKeys(false)) {
                if (meta.getDisplayName().equalsIgnoreCase(WeaponsYaml.get().getString("weapons." + weapon + ".name"))
                        && item.getType() == Material.getMaterial(WeaponsYaml.get().getString("weapons." + weapon + ".material"))) {
                    if(Minigame.playerArenaMap.get(e.getPlayer()) != null){
                        if (!Cooldown.isInCooldown(e.getPlayer().getUniqueId(), weapon)) {
                            double damage = WeaponsYaml.get().getDouble("weapons." + weapon + ".damage");
                            int cooldown = WeaponsYaml.get().getInt("weapons." + weapon + ".cooldown");
                            int range = WeaponsYaml.get().getInt("weapons." + weapon + ".range");
                            int pitch = WeaponsYaml.get().getInt("weapons." + weapon + ".soundPitch");
                            e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, pitch);
                            new Cooldown(e.getPlayer().getUniqueId(), weapon, cooldown).start();
                            for (Entity entity : e.getPlayer().getNearbyEntities(range, range, range)) {
                                if (entity instanceof Player) {
                                    Player target = (Player) entity;
                                    if(Minigame.playerArenaMap.get(target) != null)
                                    if (Minigame.playerArenaMap.get(e.getPlayer()) == Minigame.playerArenaMap.get(target) && getLookingAt(e.getPlayer(), target) && target.getNoDamageTicks() == 0) {
                                        Bukkit.getPluginManager().callEvent(new ShotDamageEvent(e.getPlayer(), target, damage, "weapons." + weapon));
                                    }
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

}
