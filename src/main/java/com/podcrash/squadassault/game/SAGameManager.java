package com.podcrash.squadassault.game;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SAGameManager {

    private List<SAGame> games;

    public SAGameManager() {
        games = new ArrayList<>();
    }

    public SAGame findQuickGame(Player player) {
        //todo, not high priority
        return null;
    }

    public void addQuickJoinPlayer(SAGame game, Player player) {
        //todo not high priority
    }

    public void addPlayer(SAGame game, Player player) {
        if(game == null) {
            player.sendMessage("game doesn't exist");
        } else if(games)
    }

    public SAGame getGame(Player player) {
        for(SAGame game : games) {
            if(game.getTeamA().getPlayers().contains(player) || game.getTeamB().getPlayers().contains(player)) {
                return game;
            }
        }
        return null;
    }

}
