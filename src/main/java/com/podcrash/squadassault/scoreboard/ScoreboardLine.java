package com.podcrash.squadassault.scoreboard;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;

public class ScoreboardLine {

    private Team team;
    private Score score;
    private String name;
    private SAScoreboard scoreboard;

    public ScoreboardLine(SAScoreboard scoreboard, String line, int score) {
        String string = ChatColor.values()[score-1].toString();
        team = scoreboard.getScoreboard().registerNewTeam(string);
        (this.score = scoreboard.getStatus().getObjective().getScore(string)).setScore(score);
        team.addEntry(string);
        this.scoreboard = scoreboard;
        update(line);
    }

    public void unregister() {
        if (team == null) {
            return;
        }
        team.unregister();
        team = null;
    }

    public Score getScore() {
        return score;
    }

    public void update(String line) {
        if(!line.equals(name)) {
            name = line;
            String substring = (line.length() >= 16) ? line.substring(0, 16) : line;
            boolean b = false;
            if (substring.length() > 0 && substring.charAt(substring.length() - 1) == '\u00a7') {
                substring = substring.substring(0, substring.length() - 1);
                b = true;
            }
            team.setPrefix(substring);
            if(line.length() > 16) {
                String string = b ? "" : ChatColor.getLastColors(substring) + line.substring(substring.length());
                if(string.length() <= 16) {
                    team.setSuffix(string);
                } else {
                    team.setSuffix(string.substring(0,16));
                }
            }
        } else {
            team.setSuffix("");
        }
    }

}
