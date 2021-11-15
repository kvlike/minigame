package me.kvlike.minigame.arenamanager;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArenaManager {

    public static Map<String, Arena> arenas = new HashMap<>();

    public void registerArena(String name, int maxPlayers) {
        Arena arena = new Arena(name, maxPlayers);
        arenas.put(name, arena);
        ArenasYaml.get().set("arenas." + name + ".name", name);
        ArenasYaml.get().set("arenas." + name + ".maxPlayers", maxPlayers);
        ArenasYaml.save();
    }

    public void loadArena(String path) {
        Arena arena = new Arena(ArenasYaml.get().getString(path + ".name"), ArenasYaml.get().getInt(path + ".maxPlayers"));
        arena.setLobbyLocation((Location) ArenasYaml.get().get(path + ".lobbyLocation"));
        arena.setSpawnLocations((List<Location>) ArenasYaml.get().get(path + ".spawnLocations"));
        arenas.put(ArenasYaml.get().getString(path + ".name"), arena);
    }

    public void remove(String name) {
        arenas.remove(name);
        ArenasYaml.get().set("arenas." + name, null);
        ArenasYaml.save();
    }

    public Arena getArena(String name) {
        return arenas.get(name);
    }

    public boolean exists(String name) {
        return arenas.get(name) != null;
    }

}
