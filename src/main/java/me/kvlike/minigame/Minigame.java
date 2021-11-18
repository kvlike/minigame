package me.kvlike.minigame;

import me.kvlike.minigame.arenamanager.ArenaManager;
import me.kvlike.minigame.arenamanager.ArenasYaml;
import me.kvlike.minigame.commands.AdminCommand;
import me.kvlike.minigame.commands.PlayerCommand;
import me.kvlike.minigame.database.MySQL;
import me.kvlike.minigame.listeners.DamageListener;
import me.kvlike.minigame.listeners.FoodLevelChangeListener;
import me.kvlike.minigame.listeners.PlayerJoinListener;
import me.kvlike.minigame.listeners.PlayerQuitListener;
import me.kvlike.minigame.weapons.ShootingListener;
import me.kvlike.minigame.weapons.ShotDamageListener;
import me.kvlike.minigame.weapons.WeaponsYaml;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class Minigame extends JavaPlugin {

    public static Location hub = null;
    public static ArenaManager arenaManager = new ArenaManager();
    public static Map<Player, String> playerArenaMap = new HashMap<>();

    static ConsoleCommandSender console = Bukkit.getConsoleSender();

    @Override
    public void onEnable() {

        // config file setup
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        // arenas yaml
        ArenasYaml.setup();
        ArenasYaml.get().addDefault("hub", null);
        ArenasYaml.get().addDefault("playersPercentToStart", 0.75);
        ArenasYaml.get().addDefault("arenas", null);
        ArenasYaml.get().options().copyDefaults(true);
        ArenasYaml.save();

        // weapons yaml
        WeaponsYaml.setup();

        WeaponsYaml.get().addDefault("weapons.pistol.name", "§c§lPistol");
        WeaponsYaml.get().addDefault("weapons.pistol.material", "STICK");
        WeaponsYaml.get().addDefault("weapons.pistol.damage", 2);
        WeaponsYaml.get().addDefault("weapons.pistol.cooldown", 1);
        WeaponsYaml.get().addDefault("weapons.pistol.range", 20);
        WeaponsYaml.get().addDefault("weapons.pistol.scoreForKill", 10);
        WeaponsYaml.get().addDefault("weapons.pistol.soundPitch", 6);

        WeaponsYaml.get().addDefault("weapons.rifle.name", "§1§lRifle");
        WeaponsYaml.get().addDefault("weapons.rifle.material", "BONE");
        WeaponsYaml.get().addDefault("weapons.rifle.damage", 4);
        WeaponsYaml.get().addDefault("weapons.rifle.cooldown", 0);
        WeaponsYaml.get().addDefault("weapons.rifle.range", 30);
        WeaponsYaml.get().addDefault("weapons.rifle.scoreForKill", 10);
        WeaponsYaml.get().addDefault("weapons.rifle.soundPitch", 3);

        WeaponsYaml.get().addDefault("weapons.sniper.name", "§6§lSniper");
        WeaponsYaml.get().addDefault("weapons.sniper.material", "BLAZE_ROD");
        WeaponsYaml.get().addDefault("weapons.sniper.damage", 10);
        WeaponsYaml.get().addDefault("weapons.sniper.cooldown", 10);
        WeaponsYaml.get().addDefault("weapons.sniper.range", 50);
        WeaponsYaml.get().addDefault("weapons.sniper.scoreForKill", 20);
        WeaponsYaml.get().addDefault("weapons.sniper.soundPitch", -1);

        WeaponsYaml.get().addDefault("scoreForWin", 10);
        WeaponsYaml.get().addDefault("healthGainOnKill", 4);

        WeaponsYaml.get().options().copyDefaults(true);
        WeaponsYaml.save();

        MySQL.connect(); // connect to MySQL

        PreparedStatement ps;

        // create table if not exist
        try {
            ps = MySQL.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS Players (UUID VARCHAR(100),Name VARCHAR(100),Total_score INT(100),Last_game_score INT(100),PRIMARY KEY (UUID))");
            ps.executeUpdate();
            console.sendMessage("[Minigame] Table created successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        hub = (Location) ArenasYaml.get().get("hub");

        if (ArenasYaml.get().getConfigurationSection("arenas") != null) {

            Set<String> keys = ArenasYaml.get().getConfigurationSection("arenas").getKeys(false);

            for (String key : keys) {
                arenaManager.loadArena("arenas." + key);
                console.sendMessage("[Minigame] Loaded arena " + key);
            }

        }

        getCommand("minigame").setExecutor(new PlayerCommand());
        getCommand("mga").setExecutor(new AdminCommand());

        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(), this);
        Bukkit.getPluginManager().registerEvents(new FoodLevelChangeListener(), this);
        Bukkit.getPluginManager().registerEvents(new DamageListener(), this);
        Bukkit.getPluginManager().registerEvents(new ShootingListener(), this);
        Bukkit.getPluginManager().registerEvents(new ShotDamageListener(), this);

    }

    @Override
    public void onDisable() {

        if (MySQL.isConnected())
            MySQL.disconnect(); // disconnect from MySQL database

    }
}
