package com.podcrash.squadassault.game.events;

import com.podcrash.squadassault.game.SAGame;
import com.podcrash.squadassault.game.SAGameState;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameStateChangeEvent extends Event {

    private SAGame game;
    private SAGameState state;
    private static final HandlerList handlers = new HandlerList();

    public GameStateChangeEvent(SAGame game, SAGameState state) {
        this.game = game;
        this.state = state;
    }

    public SAGameState getState() {
        return state;
    }

    public SAGame getGame() {
        return game;
    }

    public HandlerList getHandlers() {
        return GameStateChangeEvent.handlers;
    }

    public static HandlerList getHandlerList() {
        return GameStateChangeEvent.handlers;
    }

}
