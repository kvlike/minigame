package me.kvlike.minigame.weapons;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class WeaponsYaml {

    private static File file;
    private static FileConfiguration customFile;

    public static void setup(){
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("Minigame").getDataFolder(), "weapons.yml");

        if (!file.exists()){
            try{
                file.createNewFile();
            }catch (IOException ignore){

            }
        }
        customFile = YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration get(){
        return customFile;
    }

    public static void save(){
        try{
            customFile.save(file);
        }catch (IOException e){
            Bukkit.getConsoleSender().sendMessage("[Minigame] Couldn't save weapons.yml");
        }
    }

    public static void reload(){
        customFile = YamlConfiguration.loadConfiguration(file);
    }

}