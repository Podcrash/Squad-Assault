package com.podcrash.squadassault.game.scoreboard;

import com.podcrash.api.game.GameType;
import com.podcrash.api.game.scoreboard.GameScoreboard;

public class SAScoreboard extends GameScoreboard {

    public SAScoreboard(int gameId) {
        super(17, gameId, GameType.DOM); //todo fix engine game type
    }

    public void setup() {
        //todo
    }
    @Override
    public void update() {

    }

    @Override
    public void startScoreboardTimer() {

    }
}
