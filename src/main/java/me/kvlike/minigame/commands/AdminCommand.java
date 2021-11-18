package me.kvlike.minigame.commands;

import me.kvlike.minigame.Minigame;
import me.kvlike.minigame.arenamanager.ArenasYaml;
import me.kvlike.minigame.database.MySQL;
import me.kvlike.minigame.weapons.WeaponsYaml;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AdminCommand implements CommandExecutor {

    private void sendHelpCommand(Player p) {
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&1--------------- &eMinigame Admin Help Menu &1---------------"));
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&1- &e/mga create <arena name> <max players> &1- creates a new arena named <arena name>"));
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&1- &e/mga setlobbyspawn <arena name> &1- sets a new lobby spawn for an arena"));
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&1- &e/mga setspawn <arena name> <index> &1- sets a new spawn of index <index> for an arena"));
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&1- &e/mga remove <arena name> &1- removes an arena"));
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&1- &e/mga sethub &1- sets hub's location"));
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&1- &e/mga setscore <player> <amount> &1- sets player's total score"));
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&1- &e/mga addscore <player> <amount> &1- adds score to player's total score"));
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&1- &e/mga reload &1- reloads plugin's configs (this does not reconnect you to mysql database!)"));
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&1----------------------------------------------------"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;
            Plugin plugin = Minigame.getPlugin(Minigame.class);
            if (args.length == 0) {
                sendHelpCommand(player);
            } else if (args[0].equalsIgnoreCase("setscore")) {
                if (args.length > 2) {
                    PreparedStatement ps;
                    try {
                        ps = MySQL.getConnection().prepareStatement("UPDATE Players SET Total_score = ? WHERE Name = ?;");
                        ps.setInt(1, Integer.parseInt(args[2]));
                        ps.setString(2, args[1]);
                        ps.executeUpdate();
                        player.sendMessage(ChatColor.GREEN + "Set " + ChatColor.RED + args[1] + ChatColor.GREEN + "'s score to " + ChatColor.AQUA + args[2]);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        player.sendMessage(ChatColor.RED + "Database error!");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Usage: " + ChatColor.YELLOW + "/mga setscore <player> <amount>");
                }
            } else if (args[0].equalsIgnoreCase("addscore")) {
                if (args.length > 2) {
                    PreparedStatement ps;
                    try {
                        ps = MySQL.getConnection().prepareStatement("UPDATE Players SET Total_score = Total_score + ? WHERE Name = ?;");
                        ps.setInt(1, Integer.parseInt(args[2]));
                        ps.setString(2, args[1]);
                        ps.executeUpdate();
                        player.sendMessage(ChatColor.GREEN + "Added " + ChatColor.AQUA + args[2] + ChatColor.GREEN + " to " + ChatColor.RED + args[1] + ChatColor.GREEN + "'s score");
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        player.sendMessage(ChatColor.RED + "Database error!");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Usage: " + ChatColor.YELLOW + "/mga addscore <player> <amount>");
                }
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (args.length > 1) {
                    if (Minigame.arenaManager.exists(args[1])) {
                        Minigame.arenaManager.remove(args[1]);
                        player.sendMessage(ChatColor.GREEN + "Arena " + ChatColor.RED + args[1] + ChatColor.GREEN + " removed successfully!");
                    } else {
                        player.sendMessage(ChatColor.RED + "There is no such arena!");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Usage: " + ChatColor.YELLOW + "/mga remove <arena name>");
                }
            } else if (args[0].equalsIgnoreCase("sethub")) {
                Minigame.hub = player.getLocation();
                ArenasYaml.get().set("hub", player.getLocation());
                ArenasYaml.save();
                player.sendMessage(ChatColor.GREEN + "New hub location set successfully!");
            } else if (args[0].equalsIgnoreCase("setlobbyspawn")) {
                if (args.length > 1) {
                    if (Minigame.arenaManager.exists(args[1])) {
                        Minigame.arenaManager.getArena(args[1]).setLobbyLocation(player.getLocation());

                        player.sendMessage(ChatColor.GREEN + "Lobby spawn for arena " + ChatColor.RED + args[1] + ChatColor.GREEN + " set successfully!");
                    } else {
                        player.sendMessage(ChatColor.RED + "There is no such arena!");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Usage: " + ChatColor.YELLOW + "/mga setlobbyspawn <arena name>");
                }
            } else if (args[0].equalsIgnoreCase("setspawn")) {
                if (args.length > 2) {
                    if (Minigame.arenaManager.exists(args[1])) {
                        if (Minigame.arenaManager.getArena(args[1]).getMaxPlayers() >= Integer.parseInt(args[2]) && Integer.parseInt(args[2]) > 0) {
                            Minigame.arenaManager.getArena(args[1]).setSpawnLocation(player.getLocation(), Integer.parseInt(args[2]) - 1);
                            player.sendMessage(ChatColor.GREEN + "Spawn " + args[2] + " for arena " + ChatColor.RED + args[1] + ChatColor.GREEN + " set successfully!");
                        } else {
                            player.sendMessage(ChatColor.RED + "This arena can only fit " + Minigame.arenaManager.getArena(args[1]).getMaxPlayers() + " players!");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "There is no such arena!");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Usage: " + ChatColor.YELLOW + "/mga setspawn <arena name> <index>");
                }
            } else if (args[0].equalsIgnoreCase("create")) {
                if (args.length > 2) {
                    if (!Minigame.arenaManager.exists(args[1])) {
                        if (Integer.parseInt(args[2]) >= 2) {
                            Minigame.arenaManager.registerArena(args[1], Integer.parseInt(args[2]));
                            player.sendMessage(ChatColor.GREEN + "Successfully created a new arena named " + ChatColor.RED + args[1]);
                        } else {
                            player.sendMessage(ChatColor.RED + "A minimum value of argument <max players> is 2!");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Arena of this name already exists!");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Usage: " + ChatColor.YELLOW + "/mga create <arena name> <max players>");
                }
            } else if (args[0].equalsIgnoreCase("reload")) {
                ArenasYaml.reload();
                WeaponsYaml.reload();
                plugin.saveConfig();
                player.sendMessage(ChatColor.GREEN + "Configs reloaded successfully!");
            } else {
                sendHelpCommand(player);
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Only players can use that command!");
        }

        return true;
    }

}
