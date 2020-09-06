package com.podcrash.squadassault.scoreboard;

import com.podcrash.squadassault.game.SAGame;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardHealth {

    private Objective objective;
    private List<Score> scores;

    public ScoreboardHealth(SAGame game, SAScoreboard scoreboard) {
        scores = new ArrayList<>();
        (this.objective = scoreboard.getScoreboard().registerNewObjective("health", "dummy")).setDisplaySlot(DisplaySlot.BELOW_NAME);
        this.objective.setDisplayName("health");
        for(Player player : game.getTeamA().getPlayers()) {
            Score score = objective.getScore(player.getName());
            score.setScore((int)(player.getHealth() / player.getMaxHealth() * 100));
            scores.add(score);
        }
        for(Player player : game.getTeamB().getPlayers()) {
            Score score = objective.getScore(player.getName());
            score.setScore((int)(player.getHealth() / player.getMaxHealth() * 100));
            scores.add(score);
        }
    }

    public Objective getObjective() {
        return objective;
    }

    public List<Score> getScores() {
        return scores;
    }

    public void update(Player player) {
        objective.getScore(player.getName()).setScore((int)(player.getHealth() / player.getMaxHealth() * 100));
    }
}
