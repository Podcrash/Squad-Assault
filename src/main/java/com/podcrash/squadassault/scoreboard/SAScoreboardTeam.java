package com.podcrash.squadassault.scoreboard;

import com.podcrash.squadassault.Main;
import com.podcrash.squadassault.game.SAGame;
import com.podcrash.squadassault.game.SATeam;
import com.podcrash.squadassault.nms.NmsUtils;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

public class SAScoreboardTeam {

    private Scoreboard scoreboard;
    private List<Team> teams;

    public SAScoreboardTeam(SAGame game, Scoreboard scoreboard) {
        teams = new ArrayList<>();
        this.scoreboard = scoreboard;
        registerSides(game);
    }

    public void registerSides(SAGame game) {
        for(Player player : Main.getManager().getTeam(game, SATeam.Team.OMEGA).getPlayers()) {
            Team registerNewTeam = scoreboard.registerNewTeam(player.getName());
            registerNewTeam.setPrefix("Ω");
            registerNewTeam.setSuffix("kills - deaths"); //kills - deaths ? todo
            NmsUtils.hideNametag(registerNewTeam);
            registerNewTeam.addEntry(player.getName());
            teams.add(registerNewTeam);
        }
        for(Player player : Main.getManager().getTeam(game, SATeam.Team.OMEGA).getPlayers()) {
            Team registerNewTeam = scoreboard.registerNewTeam(player.getName());
            registerNewTeam.setPrefix("α");
            registerNewTeam.setSuffix("kills - deaths"); //kills - deaths ? todo
            NmsUtils.hideNametag(registerNewTeam);
            registerNewTeam.addEntry(player.getName());
            teams.add(registerNewTeam);
        }
    }

    public void add(SAGame game, Player player) {
        Team registerNewTeam = scoreboard.registerNewTeam(player.getName());
        registerNewTeam.setPrefix(Main.getManager().getTeam(game, player) == SATeam.Team.OMEGA ? "Ω" : "α");
        registerNewTeam.setSuffix("kills - deaths"); //kills - deaths ? todo
        NmsUtils.hideNametag(registerNewTeam);
        registerNewTeam.addEntry(player.getName());
        teams.add(registerNewTeam);
    }

    public void remove(Player player) {
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
        team.setPrefix(Main.getManager().getTeam(game, player) == SATeam.Team.OMEGA ? "Ω" : "α");
        team.setSuffix("kills - deaths"); //kills - deaths ? todo
        NmsUtils.hideNametag(team);
    }
}