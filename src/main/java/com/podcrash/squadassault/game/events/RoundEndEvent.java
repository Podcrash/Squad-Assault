package com.podcrash.squadassault.game.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RoundEndEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public RoundEndEvent() {

    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
