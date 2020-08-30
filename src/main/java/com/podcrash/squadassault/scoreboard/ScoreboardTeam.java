package com.podcrash.squadassault.scoreboard;

import com.podcrash.squadassault.game.SAGame;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardTeam {

    private Scoreboard scoreboard;
    private List<Team> teams;

    public ScoreboardTeam(SAGame game, Scoreboard scoreboard) {
        teams = new ArrayList<>();
        this.scoreboard = scoreboard;
        registerSides(game);
    }

    public void registerSides(SAGame game) {

    }

    public void remove(SAGame game, Player player) {
        Team team = scoreboard.getTeam(player.getName());
        if(team == null)
            return;
        teams.remove(team);
        team.unregister();
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void update(SAGame game, Player player) {
        Team team = scoreboard.getTeam(player.getName());
    }
}