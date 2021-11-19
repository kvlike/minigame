package me.kvlike.minigame.arenamanager;

import me.kvlike.minigame.Minigame;
import me.kvlike.minigame.database.MySQL;
import me.kvlike.minigame.weapons.WeaponsYaml;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class Arena {

    private final String name;
    private Location lobbyLocation = null;
    private final int maxPlayers;
    private GameState gameState = GameState.WAITING;

    private final List<Player> players = new ArrayList<>();
    private final Map<Player, Boolean> isSpectator = new HashMap<>();
    private List<Location> spawnLocations = new ArrayList<>();

    private int time;
    private int taskID;
    private final Plugin plugin = Minigame.getPlugin(Minigame.class);

    private void countdown() {
        time = 10;
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        taskID = scheduler.scheduleSyncRepeatingTask(plugin, () -> {
            for (Player p : players) {
                p.sendTitle(ChatColor.RED + Integer.toString(time), "", 0, 20, 0);
            }
            if (time == 0) {
                Bukkit.getScheduler().cancelTask(taskID);
                this.setGameState(GameState.PLAYING);
            }
            time--;
        }, 0L, 20L);
    }

    public Arena(String name, int maxPlayers) {
        this.name = name;
        this.maxPlayers = maxPlayers;
    }

    public void join(Player p) {
        boolean ready = Minigame.hub != null;
        if (spawnLocations.size() < maxPlayers) ready = false;
        for (Location l : spawnLocations) {
            if (l == null) {
                ready = false;
                break;
            }
        }
        if (lobbyLocation == null) ready = false;
        if (ready) {
            if ((gameState == GameState.WAITING || gameState == GameState.STARTING) && players.size() < maxPlayers) {
                players.add(p);
                this.setSpectator(p, false);
                Minigame.playerArenaMap.put(p, this.getName());
                p.teleport(lobbyLocation);
                p.getInventory().clear();
                p.setHealth(20.0);
                p.setGameMode(GameMode.ADVENTURE);
                for (Player player : players) {
                    player.sendMessage(ChatColor.RED + p.getDisplayName() + ChatColor.YELLOW + " has joined the game " + ChatColor.GRAY + "(" + players.size() + "/" + maxPlayers + ")");
                }
                if (players.size() >= maxPlayers * ArenasYaml.get().getInt("playersPercentToStart") / 100)
                    setGameState(GameState.STARTING);
            } else {
                p.sendMessage(ChatColor.RED + "You can't join this arena now!");
            }
        } else {
            p.sendMessage(ChatColor.RED + "Can't join this arena! It is not fully set up yet!");
        }
    }

    public void leave(Player p, Boolean message) {
        p.setHealth(20.0);
        p.setGameMode(GameMode.SURVIVAL);
        if (message) for (Player player : players) {
            if (!this.checkSpectator(p))
                player.sendMessage(ChatColor.RED + p.getDisplayName() + ChatColor.YELLOW + " left the game");
        }
        players.remove(p);
        this.setSpectator(p, false);
        isSpectator.remove(p);
        Minigame.playerArenaMap.remove(p);
        p.getInventory().clear();
        p.teleport(Minigame.hub);
        if (gameState == GameState.PLAYING) {
            if (this.getAlivePlayers() == 1) {
                Player w = null;
                for (Player x : players) {
                    if (!this.checkSpectator(x) && x != p) {
                        w = x;
                        break;
                    }
                }
                win(w);
            }
        }
        if (players.size() < maxPlayers * ArenasYaml.get().getInt("playersPercentToStart") / 100 && gameState == GameState.STARTING) {
            gameState = GameState.WAITING;
            Bukkit.getScheduler().cancelTask(taskID);
        }
    }

    public void win(Player p) {
        p.sendTitle(ChatColor.GREEN + "You won!", "", 5, 40, 5);
        for (Player player : players) {
            player.sendMessage(ChatColor.RED + p.getDisplayName() + ChatColor.GREEN + " won the game!");
        }
        PreparedStatement ps;
        try {
            ps = MySQL.getConnection().prepareStatement("UPDATE Players SET Total_score = Total_score + ? WHERE UUID = ?;");
            ps.setInt(1, WeaponsYaml.get().getInt("scoreForWin"));
            ps.setString(2, p.getUniqueId().toString());
            ps.executeUpdate();

            ps = MySQL.getConnection().prepareStatement("UPDATE Players SET Last_game_score = Last_game_score + ? WHERE UUID = ?;");
            ps.setInt(1, WeaponsYaml.get().getInt("scoreForWin"));
            ps.setString(2, p.getUniqueId().toString());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        this.setGameState(GameState.WAITING);
    }

    public String getName() {
        return name;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Location> getSpawnLocations() {
        return spawnLocations;
    }

    public Location getLobbyLocation() {
        return lobbyLocation;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public GameState getGameState() {
        return gameState;
    }

    public boolean checkSpectator(Player player) {
        return isSpectator.get(player);
    }

    public int getAlivePlayers() {
        int spectators = 0;
        for (Player p : players) {
            if (this.checkSpectator(p)) spectators++;
        }
        return players.size() - spectators;
    }

    public void setSpectator(Player player, Boolean bool) {
        isSpectator.put(player, bool);
        if (bool) {
            for (Player p : players) p.hidePlayer(plugin, player);
            player.setHealth(20.0);
            player.setAllowFlight(true);
            player.setFlying(true);
            player.getInventory().clear();
            if (this.getAlivePlayers() == 1) {
                for (Player p : players) {
                    if (!this.checkSpectator(p)) {
                        this.win(p);
                        break;
                    }
                }
            }
        }
        if (!bool) {
            for (Player p : Bukkit.getOnlinePlayers()) p.showPlayer(plugin, player);
            player.setFlying(false);
            player.setAllowFlight(false);
        }
    }

    public void setSpawnLocation(Location spawnLocation, int index) {
        if (spawnLocations != null) {
            if (index >= spawnLocations.size()) {
                spawnLocations.add(index, spawnLocation);
            } else {
                spawnLocations.set(index, spawnLocation);
            }
        } else {
            spawnLocations = new ArrayList<>();
            spawnLocations.add(index, spawnLocation);
        }
        ArenasYaml.get().set("arenas." + name + ".spawnLocations", spawnLocations);
        ArenasYaml.save();
    }

    public void setSpawnLocations(List<Location> spawnLocations) {
        this.spawnLocations = spawnLocations;
        ArenasYaml.get().set("arenas." + name + ".spawnLocations", spawnLocations);
        ArenasYaml.save();
    }

    public void setLobbyLocation(Location lobbyLocation) {
        this.lobbyLocation = lobbyLocation;
        ArenasYaml.get().set("arenas." + name + ".lobbyLocation", lobbyLocation);
        ArenasYaml.save();
    }

    public void setGameState(GameState gameState) {
        if (this.gameState == gameState) return;
        if (this.gameState == GameState.PLAYING && gameState == GameState.STARTING) return;
        if (this.gameState == GameState.WAITING && gameState == GameState.PLAYING) return;
        if (this.gameState == GameState.STARTING && gameState == GameState.WAITING) return;
        if (gameState == GameState.STARTING && players.size() < 2) return;

        this.gameState = gameState;

        if (gameState == GameState.WAITING) {
            while (0 < players.size()) {
                this.leave(players.get(0), false);
            }
        }
        if (gameState == GameState.STARTING) {
            countdown();
        }
        if (gameState == GameState.PLAYING) {
            for (int i = 0; i < players.size(); i++) {
                players.get(i).teleport(spawnLocations.get(i));
                players.get(i).getInventory().clear();
                Set<String> weapons = WeaponsYaml.get().getConfigurationSection("weapons").getKeys(false);
                for (String weapon : weapons) {
                    ItemStack item = new ItemStack(Material.getMaterial(WeaponsYaml.get().getString("weapons." + weapon + ".material")));
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(WeaponsYaml.get().getString("weapons." + weapon + ".name"));
                    item.setItemMeta(meta);
                    players.get(i).getInventory().addItem(item);
                }
                PreparedStatement ps;
                try {
                    ps = MySQL.getConnection().prepareStatement("UPDATE Players SET Last_game_score = 0 WHERE UUID = ?;");
                    ps.setString(1, players.get(i).getUniqueId().toString());
                    ps.executeUpdate();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                players.get(i).sendTitle(ChatColor.GREEN + "Game started!", "", 0, 20, 5);
            }
        }
    }
}
