package com.podcrash.squadassault.game;

import com.podcrash.squadassault.util.Randomizer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class SATeam {

    private Team team;
    private final List<Player> players;

    public SATeam(List<Player> players) {
        this.players = players;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }

    public Player randomPlayer() {
        return players.get(Randomizer.randomInt(players.size()));
    }

    public List<Player> getPlayers() {
        return players;
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
        ALPHA(ChatColor.AQUA), OMEGA(ChatColor.RED);

        private ChatColor color;

        Team(ChatColor color) {
            this.color = color;
        }

        public ChatColor getColor() {
            return color;
        }
    }
}
