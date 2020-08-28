package com.podcrash.squadassault.game;

import org.bukkit.entity.Player;

import java.util.List;

public class SATeam {

    private Team team;
    private List<Player> players;

    public SATeam(List<Player> players) {
        this.players = players;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public int size() {
        return players.size();
    }


    public enum Team {
        ALPHA, OMEGA
    }
}
