package com.podcrash.squadassault.scoreboard;

import com.packetwrapper.abstractpackets.WrapperPlayServerScoreboardTeam;
import com.podcrash.squadassault.Main;
import com.podcrash.squadassault.game.PlayerStats;
import com.podcrash.squadassault.game.SAGame;
import com.podcrash.squadassault.game.SATeam;
import com.podcrash.squadassault.nms.NmsUtils;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SAScoreboardTeam {

    private final Scoreboard scoreboard;
    private final List<Team> teams;

    public SAScoreboardTeam(SAGame game, Scoreboard scoreboard) {
        teams = new ArrayList<>();
        this.scoreboard = scoreboard;
        registerSides(game);
    }

    /*
    Create scoreboard team using packets
    Send packet to players of same team to have name visibility always
    Send packet to players of other team to have name visibility to never
    Send packet to spectators to have name visibility always
     */
    public void registerSides(SAGame game) {
        for(Player player : Main.getGameManager().getTeam(game, SATeam.Team.OMEGA).getPlayers()) {
            PlayerStats stats = game.getStats().get(player.getUniqueId());
//            Team registerNewTeam = scoreboard.registerNewTeam(player.getName());
//            registerNewTeam.setPrefix("O");
//            registerNewTeam.setSuffix(stats.getKills() + " - " + stats.getDeaths());
//            registerNewTeam.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);
////            NmsUtils.hideNametag(registerNewTeam);
//            registerNewTeam.addEntry(player.getName());
//            teams.add(registerNewTeam);
            WrapperPlayServerScoreboardTeam scoreboardTeam = new WrapperPlayServerScoreboardTeam();
            scoreboardTeam.setName(player.getName());
            scoreboardTeam.setPrefix("O");
            scoreboardTeam.setSuffix(stats.getKills() + " - " + stats.getDeaths());
            scoreboardTeam.setPlayers(Collections.singletonList(player.getName()));
            setTagVisibility(scoreboardTeam, Main.getGameManager().getTeam(game, SATeam.Team.OMEGA).getPlayers(), Main.getGameManager().getTeam(game, SATeam.Team.ALPHA).getPlayers());
        }
        for(Player player : Main.getGameManager().getTeam(game, SATeam.Team.ALPHA).getPlayers()) {
            PlayerStats stats = game.getStats().get(player.getUniqueId());
//            Team registerNewTeam = scoreboard.registerNewTeam(player.getName());
//            registerNewTeam.setPrefix("A");
//            registerNewTeam.setSuffix(stats.getKills() + " - " + stats.getDeaths());
//            registerNewTeam.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);
////            NmsUtils.hideNametag(registerNewTeam);
//            registerNewTeam.addEntry(player.getName());
//            teams.add(registerNewTeam);
            WrapperPlayServerScoreboardTeam scoreboardTeam = new WrapperPlayServerScoreboardTeam();
            scoreboardTeam.setName(player.getName());
            scoreboardTeam.setPrefix("O");
            scoreboardTeam.setSuffix(stats.getKills() + " - " + stats.getDeaths());
            scoreboardTeam.setPlayers(Collections.singletonList(player.getName()));
            setTagVisibility(scoreboardTeam, Main.getGameManager().getTeam(game, SATeam.Team.ALPHA).getPlayers(), Main.getGameManager().getTeam(game, SATeam.Team.OMEGA).getPlayers());
        }
    }

    public void add(SAGame game, Player player) {
        PlayerStats stats = game.getStats().get(player.getUniqueId());
        Team registerNewTeam = scoreboard.registerNewTeam(player.getName());
        registerNewTeam.setPrefix(Main.getGameManager().getTeam(game, player) == SATeam.Team.OMEGA ? "O" : "A");
        registerNewTeam.setSuffix(stats.getKills() + " - " + stats.getDeaths());
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
        PlayerStats stats = game.getStats().get(player.getUniqueId());
        Team team = scoreboard.getTeam(player.getName());
        team.setPrefix(Main.getGameManager().getTeam(game, player) == SATeam.Team.OMEGA ? "O" : "A");
        team.setSuffix(stats.getKills() + " - " + stats.getDeaths());
        NmsUtils.hideNametag(team);
    }

    private void setTagVisibility(WrapperPlayServerScoreboardTeam scoreboardTeam, List<Player> sameTeam, List<Player> otherTeam) {
        scoreboardTeam.setNameTagVisibility("always");
        for (Player player : sameTeam) {
            scoreboardTeam.sendPacket(player);
        }
        scoreboardTeam.setNameTagVisibility("never");
        for (Player player : otherTeam) {
            scoreboardTeam.sendPacket(player);
        }
    }
}