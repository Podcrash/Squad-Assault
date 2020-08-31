package com.podcrash.squadassault.scoreboard;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.HashMap;
import java.util.Map;

public class ScoreboardStatus {

    private Player player;
    private Objective objective;
    private SAScoreboard scoreboard;
    private Map<Integer, ScoreboardLine> entries;

    public ScoreboardStatus(Player player, SAScoreboard scoreboard) {
        this.entries = new HashMap<>();
        this.player = player;
        this.scoreboard = scoreboard;
        (this.objective = scoreboard.getScoreboard().registerNewObjective("status","dummy")).setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public Player getPlayer() {
        return player;
    }

    public Objective getObjective() {
        return objective;
    }

    public void setTitle(String displayName) {
        objective.setDisplayName(displayName);
    }

    public void reset() {
        for(ScoreboardLine line : entries.values()) {
            line.unregister();
            scoreboard.getScoreboard().resetScores(line.getScore().getEntry());
        }
        entries.clear();
    }

    public void updateLine(int n, String line) {
        if(entries.get(n) != null) {
            entries.get(n).update(line);
        } else {
            entries.put(n, new ScoreboardLine(scoreboard, line, n));
        }
    }

}
