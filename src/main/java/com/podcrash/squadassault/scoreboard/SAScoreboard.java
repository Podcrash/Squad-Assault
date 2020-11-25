package com.podcrash.squadassault.scoreboard;

import com.podcrash.squadassault.game.SAGame;
import com.podcrash.squadassault.nms.NmsUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class SAScoreboard {

    private final Scoreboard scoreboard;
    private final ScoreboardStatus status;
    private SAScoreboardTeam team;
    private ScoreboardHealth health;

    public SAScoreboard(Player player) {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        status = new ScoreboardStatus(player, this);
        player.setScoreboard(scoreboard);
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public SAScoreboardTeam getTeams() {
        return team;
    }

    public ScoreboardStatus getStatus() {
        return status;
    }

    public ScoreboardHealth getHealth() {
        return health;
    }

    public void showTeams(SAGame game) {
        team = new SAScoreboardTeam(game, scoreboard);
        NmsUtils.sendInvisibility(this, game);
    }

    public void showHealth(SAGame game) {
        health = new ScoreboardHealth(game, this);
    }

    public void removeTeam() {
        if(team == null)
            return;

        for(Team team : team.getTeams()) {
            team.unregister();
        }
        team.getTeams().clear();
        team = null;
    }

    public void removeHealth() {
        if(health == null)
            return;
        for(Score score : health.getScores()) {
            scoreboard.resetScores(score.getEntry());
        }
        health.getObjective().unregister();
        health = null;
    }

    public void remove() {
        removeTeam();
        removeHealth();

        for(Objective objective : scoreboard.getObjectives()) {
            objective.unregister();
        }
    }
}
