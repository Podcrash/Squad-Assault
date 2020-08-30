package com.podcrash.squadassault.scoreboard;

import com.podcrash.squadassault.game.SAGame;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardHealth {

    private Objective objective;
    private List<Score> scores;

    public ScoreboardHealth(SAGame game, SAScoreboard scoreboard) {
        scores = new ArrayList<>();
        //dummy health todo
    }

}
