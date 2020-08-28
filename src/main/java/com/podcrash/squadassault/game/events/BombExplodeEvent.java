package com.podcrash.squadassault.game.events;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BombExplodeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private Location location;

    public BombExplodeEvent(Location location) {
        this.location = location;
    }

    public Location getBombLocation() {
        return location;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
