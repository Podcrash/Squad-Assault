package com.podcrash.squadassault.shop;

import com.podcrash.squadassault.Main;
import com.podcrash.squadassault.game.SAGame;
import com.podcrash.squadassault.game.SATeam;
import org.bukkit.entity.Player;

public class MoneyManager {

    private SATeam.Team lastWinner = SATeam.Team.ALPHA;
    private int streak;
    private final int[] streakMoney = {1400, 1900, 2400, 2900, 3400};

    public void addMoneyEndRound(SAGame game, SATeam.Team winner, boolean wasBombPlanted, RoundEndType type) {
        SATeam.Team loser = winner == SATeam.Team.ALPHA ? SATeam.Team.OMEGA : SATeam.Team.ALPHA;
        if(lastWinner == winner) {
            streak++;
            int trueStreak = Math.min(streak, 5);
            addMoneyTeam(game, loser, streakMoney[trueStreak-1]);
            addMoneyTeam(game, winner, type.getMoney());
            if(loser == SATeam.Team.OMEGA && wasBombPlanted) {
                addMoneyTeam(game, loser, 800);
            }
            return;
        }
        streak = 1;
        lastWinner = winner;
        addMoneyTeam(game, loser, streakMoney[streak-1]);
        addMoneyTeam(game, winner, type.getMoney());
        if(loser == SATeam.Team.OMEGA && wasBombPlanted) {
            addMoneyTeam(game, loser, 800);
        }
    }

    private void addMoneyTeam(SAGame game, SATeam.Team team, int money) {
        SATeam saTeam = Main.getGameManager().getTeam(game,team);
        saTeam.getPlayers().forEach(player -> game.setMoney(player, game.getMoney(player)+money));
    }

    private void addMoney(Player player, int money) {
        SAGame game = Main.getGameManager().getGame(player);
        game.setMoney(player, game.getMoney(player)+money);
    }

    public enum RoundEndType {
        KILLS(3250), TIME(3250), DEFUSED(3500), BOMB(3500);

        int money;

        RoundEndType(int money) {
            this.money = money;
        }

        public int getMoney() {
            return money;
        }
    }

}
