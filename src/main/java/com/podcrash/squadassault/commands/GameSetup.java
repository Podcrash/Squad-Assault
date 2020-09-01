package com.podcrash.squadassault.commands;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class GameSetup {

    private final String id;
    private final int minPlayers;
    private final String mapName;
    private Location lobby;
    private Location bombA;
    private Location bombB;
    private final List<Location> alphaSpawns;
    private final List<Location> omegaSpawns;

    public GameSetup(String id, String mapName, int min) {
        alphaSpawns = new ArrayList<>();
        omegaSpawns = new ArrayList<>();
        this.id = id;
        this.mapName = mapName;
        this.minPlayers = min;
    }

    public List<Location> getAlphaSpawns() {
        return alphaSpawns;
    }

    public List<Location> getOmegaSpawns() {
        return omegaSpawns;
    }

    public Location getLobby() {
        return lobby;
    }

    public void setLobby(Location lobby) {
        this.lobby = lobby;
    }

    public Location getBombA() {
        return bombA;
    }

    public void setBombA(Location bombA) {
        this.bombA = bombA;
    }

    public Location getBombB() {
        return bombB;
    }

    public void setBombB(Location bombB) {
        this.bombB = bombB;
    }

    public String getMapName() {
        return mapName;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public String getId() {
        return id;
    }
}
