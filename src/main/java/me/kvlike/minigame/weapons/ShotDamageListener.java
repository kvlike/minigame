package me.kvlike.minigame.weapons;

import me.kvlike.minigame.Minigame;
import me.kvlike.minigame.database.MySQL;
import me.kvlike.minigame.events.ShotDamageEvent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ShotDamageListener implements Listener {

    @EventHandler
    public void onShotDamage(ShotDamageEvent e){

        String weaponName = WeaponsYaml.get().getString(e.getWeapon() + ".name");
        int weaponScore = WeaponsYaml.get().getInt(e.getWeapon() + ".scoreForKill");

        e.getAttacker().playSound(e.getAttacker().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        if(e.getVictim().getHealth() <= e.getDamage()){
            e.getVictim().getWorld().strikeLightningEffect(e.getVictim().getLocation());
            for(Player p : Minigame.arenaManager.getArena(Minigame.playerArenaMap.get(e.getVictim())).getPlayers()){
                p.sendMessage(ChatColor.RED + e.getVictim().getDisplayName() + ChatColor.YELLOW + " was shot by " + ChatColor.RED + e.getAttacker().getDisplayName() + ChatColor.YELLOW + " using " + weaponName);
            }
            Minigame.arenaManager.getArena(Minigame.playerArenaMap.get(e.getVictim())).setSpectator(e.getVictim(), true);
            if(e.getAttacker().getHealth() + WeaponsYaml.get().getInt("healthGainOnKill") < 20) e.getAttacker().setHealth(e.getAttacker().getHealth() + WeaponsYaml.get().getInt("healthGainOnKill"));
            else e.getAttacker().setHealth(20.0);
            e.getAttacker().setFoodLevel(20);
            PreparedStatement ps;
            try {
                ps = MySQL.getConnection().prepareStatement("UPDATE Players SET Total_score = Total_score + ? WHERE UUID = ?;");
                ps.setInt(1, weaponScore);
                ps.setString(2, e.getAttacker().getUniqueId().toString());
                ps.executeUpdate();

                ps = MySQL.getConnection().prepareStatement("UPDATE Players SET Last_game_score = Last_game_score + ? WHERE UUID = ?;");
                ps.setInt(1, weaponScore);
                ps.setString(2, e.getAttacker().getUniqueId().toString());
                ps.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        else{
            e.getVictim().damage(e.getDamage());
        }

    }

}
