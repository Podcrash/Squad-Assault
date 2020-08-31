package com.podcrash.squadassault.game.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GunDamageEvent extends Event {
    private double damage;
    private boolean isHeadshot;
    private Player damager;
    private Player victim;
    private static final HandlerList handlers = new HandlerList();

    public GunDamageEvent(double damage, boolean isHeadshot, Player damager, Player victim) {
        this.damage = damage;
        this.isHeadshot = isHeadshot;
        this.damager = damager;
        this.victim = victim;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public double getDamage() {
        return damage;
    }

    public boolean isHeadshot() {
        return isHeadshot;
    }

    public Player getDamager() {
        return damager;
    }

    public Player getVictim() {
        return victim;
    }
}
