package com.podcrash.squadassault.game;

import com.podcrash.api.db.pojos.map.GameMap;
import com.podcrash.api.game.Game;
import com.podcrash.api.game.GameType;
import com.podcrash.api.game.TeamEnum;
import com.podcrash.api.game.TeamSettings;
import com.podcrash.api.game.objects.ItemObjective;
import com.podcrash.api.game.objects.WinObjective;

import java.util.List;

public class SAGame extends Game {

    public SAGame(int id, String name) {
        super(id, name, GameType.DOM); //TODO fix engine api to not be shitty
    }

    @Override
    public int getAbsoluteMinPlayers() {
        return 2;
    }

    @Override
    public void leaveCheck() {

    }

    @Override
    public Class<? extends GameMap> getMapClass() {
        return null;
    }

    @Override
    public TeamSettings getTeamSettings() {
        return new TeamSettings.Builder().setTeamColors(TeamEnum.BLUE, TeamEnum.RED).setMin(2).setMax(10).build();
    }

    @Override
    public String getMode() {
        return "Squad Assault";
    }

    @Override
    public String getPresentableResult() {
        return null;
    }

    @Override
    public List<WinObjective> getWinObjectives() {
        return null;
    }

    @Override
    public List<ItemObjective> getItemObjectives() {
        return null;
    }
}
