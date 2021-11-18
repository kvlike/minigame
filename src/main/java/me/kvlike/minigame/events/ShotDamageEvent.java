package me.kvlike.minigame.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ShotDamageEvent extends Event implements Cancellable {

    private Player attacker;
    private Player victim;
    private double damage;
    private String weapon;

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private boolean isCancelled;

    public ShotDamageEvent(Player attacker, Player victim, double damage, String weapon) {
        this.attacker = attacker;
        this.victim = victim;
        this.damage = damage;
        this.weapon = weapon;
        this.isCancelled = false;
    }

    public Player getAttacker() {
        return attacker;
    }

    public void setAttacker(Player attacker) {
        this.attacker = attacker;
    }

    public Player getVictim() {
        return victim;
    }

    public void setVictim(Player victim) {
        this.victim = victim;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public String getWeapon() {
        return weapon;
    }

    public void setWeapon(String weapon) {
        this.weapon = weapon;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

}
