package me.kvlike.minigame.commands;

import me.kvlike.minigame.Minigame;
import me.kvlike.minigame.database.MySQL;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerCommand implements CommandExecutor {

    private void sendHelpCommand(Player p){
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&1--------------- &eMinigame Help Menu &1---------------"));
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&1- &e/minigame join <arena name> &1- join arena by name"));
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&1- &e/minigame leave &1- leave the game"));
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&1- &e/minigame score &1- shows your statistics"));
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&1-----------------------------------------------"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 0) {
                sendHelpCommand(player);
            }
            else if(args[0].equalsIgnoreCase("score")){
                PreparedStatement ps;
                try {
                    ps = MySQL.getConnection().prepareStatement("SELECT Total_score FROM Players WHERE UUID = ?");
                    ps.setString(1, player.getUniqueId().toString());
                    ResultSet rs = ps.executeQuery();
                    int score = 0;
                    if (rs.next()) {
                        score = rs.getInt("Total_score");
                    }

                    ps = MySQL.getConnection().prepareStatement("SELECT Last_game_score FROM Players WHERE UUID = ?");
                    ps.setString(1, player.getUniqueId().toString());
                    rs = ps.executeQuery();
                    int last_score = 0;
                    if (rs.next()) {
                        last_score = rs.getInt("Last_game_score");
                    }

                    player.sendMessage(ChatColor.GREEN + "Your total score is " + ChatColor.AQUA + score);
                    player.sendMessage(ChatColor.GREEN + "In your last game you got a score of " + ChatColor.AQUA + last_score);
                } catch (SQLException e) {
                    player.sendMessage(ChatColor.RED + "Database error!");
                    e.printStackTrace();
                }
            }
            else if(args[0].equalsIgnoreCase("join")){
                if(args.length > 1){
                    if(Minigame.arenaManager.exists(args[1])) {
                        if(Minigame.playerArenaMap.get(player) == Minigame.arenaManager.getArena(args[1]).getName()){
                            player.sendMessage(ChatColor.RED + "You already are in this arena!");
                            return true;
                        }
                        if(Minigame.playerArenaMap.containsKey(player))
                            Minigame.arenaManager.getArena(Minigame.playerArenaMap.get(player)).leave(player, true);
                        Minigame.arenaManager.getArena(args[1]).join(player);
                    } else player.sendMessage(ChatColor.RED + "There is no such arena!");
                }
                else {
                    player.sendMessage(ChatColor.RED + "Usage: " + ChatColor.YELLOW + "/minigame join <arena name>");
                }
            }
            else if(args[0].equalsIgnoreCase("leave")){
                if(Minigame.playerArenaMap.containsKey(player)) {
                    Minigame.arenaManager.getArena(Minigame.playerArenaMap.get(player)).leave(player, true);
                }
                else{
                    player.sendMessage(ChatColor.RED + "You are not in game!");
                }
            }
            else{
                sendHelpCommand(player);
            }
        }
        else{
            sender.sendMessage(ChatColor.RED + "Only players can use that command!");
        }

        return true;
    }

}
