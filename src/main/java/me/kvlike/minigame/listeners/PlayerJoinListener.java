package me.kvlike.minigame.listeners;

import me.kvlike.minigame.Minigame;
import me.kvlike.minigame.database.MySQL;
import me.kvlike.minigame.weapons.Cooldown;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e){

        PreparedStatement ps;
        try {
            ps = MySQL.getConnection().prepareStatement("INSERT INTO Players (UUID,Name) VALUES (?,?)");
            ps.setString(1, e.getPlayer().getUniqueId().toString());
            ps.setString(2, e.getPlayer().getName());
            ps.executeUpdate();
        } catch (SQLException ex) {
            try {
                ps = MySQL.getConnection().prepareStatement("UPDATE Players SET Name=? WHERE UUID=?");
                ps.setString(1, e.getPlayer().getName());
                ps.setString(2, e.getPlayer().getUniqueId().toString());
                ps.executeUpdate();
            } catch (SQLException ex1) {
                ex1.printStackTrace();
            }
        }

        try {
            ps = MySQL.getConnection().prepareStatement("SELECT Total_score FROM Players WHERE UUID = ?");
            ps.setString(1, e.getPlayer().getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            int score = 0;
            if (rs.next() == true) {
                score = rs.getInt("Total_score");
            }
            if(score == 0){
                ps = MySQL.getConnection().prepareStatement("UPDATE Players SET Total_score=0 WHERE UUID=?");
                ps.setString(1, e.getPlayer().getUniqueId().toString());
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

}
