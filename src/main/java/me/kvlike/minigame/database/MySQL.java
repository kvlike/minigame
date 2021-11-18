package me.kvlike.minigame.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import me.kvlike.minigame.Minigame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;

public class MySQL {

    static Plugin plugin = Minigame.getPlugin(Minigame.class);

    public static String host = plugin.getConfig().getString("database.host");
    public static String port = plugin.getConfig().getString("database.port");
    public static String database = plugin.getConfig().getString("database.database");
    public static String username = plugin.getConfig().getString("database.user");
    public static String password = plugin.getConfig().getString("database.password");
    public static Connection con;

    static ConsoleCommandSender console = Bukkit.getConsoleSender();

    // connect
    public static void connect() {
        if (!isConnected()) {
            try {
                con = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
                console.sendMessage("[Minigame] Connected to MySQL database successfully!");
            } catch (SQLException e) {
                console.sendMessage("[Minigame] " + ChatColor.RED + "Can't connect to database properly! Please check your config.yml file!");
                Bukkit.getPluginManager().disablePlugin(plugin);
            }
        }
    }

    // disconnect
    public static void disconnect() {
        if (isConnected()) {
            try {
                con.close();
                console.sendMessage("[Minigame] Disconnected from MySQL database!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // isConnected
    public static boolean isConnected() {
        return (con != null);
    }

    // getConnection
    public static Connection getConnection() {
        return con;
    }
}