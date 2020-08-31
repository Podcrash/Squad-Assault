package com.podcrash.squadassault.game;

import com.podcrash.squadassault.Main;
import com.podcrash.squadassault.game.events.BombExplodeEvent;
import com.podcrash.squadassault.game.events.GameStateChangeEvent;
import com.podcrash.squadassault.nms.BossBar;
import com.podcrash.squadassault.nms.NmsUtils;
import com.podcrash.squadassault.scoreboard.SAScoreboard;
import com.podcrash.squadassault.util.Message;
import com.podcrash.squadassault.util.Randomizer;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.*;

import static com.podcrash.squadassault.game.SATeam.Team.ALPHA;
import static com.podcrash.squadassault.game.SATeam.Team.OMEGA;

/**
 * This classs represents a single ongoing lobby of the game. probably needs bungee integration todo
 */
public class SAGame {

    private final String id;
    private final String mapName;
    private int round;
    private int minPlayers;
    private int maxPlayers;
    private int timer;
    private BossBar bar;
    private SATeam.Team roundWinner;
    private int scoreTeamA;
    private int scoreTeamB;
    private boolean gameStarting;
    private boolean gameStarted;
    private boolean gameEnding;
    private boolean roundEnding;
    private SABomb bomb;
    private SAGameState state;
    private final Location lobby;
    private final Location bombA;
    private final Location bombB;
    private final List<Location> alphaSpawns;
    private final List<Location> omegaSpawns;
    private List<Player> spectators;
    private SATeam teamA;
    private SATeam teamB;
    private Map<Item, Integer> drops;
    private Map<UUID, Integer> money;
    private Map<UUID, Inventory> shops;
    private Map<Player, Defuse> defusing;
    private Map<UUID, SAScoreboard> scoreboards;
    private Map<UUID, SATeam> queue;

    public SAGame(String id, String mapName, Location lobby, int minPlayers, List<Location> alphaSpawns,
                  List<Location> omegaSpawns, Location bombA, Location bombB) {
        this.id = id;
        this.mapName = mapName;
        this.bombA = bombA;
        this.bombB = bombB;
        this.alphaSpawns = alphaSpawns;
        this.omegaSpawns = omegaSpawns;
        this.lobby = lobby;
        this.minPlayers = minPlayers;
        round = 0;
        scoreTeamA = 0;
        scoreTeamB = 0;
        roundEnding = false;
        bomb = new SABomb();
        state = SAGameState.WAITING;
        spectators = new ArrayList<>();
        teamA = new SATeam(new ArrayList<>());
        teamB = new SATeam(new ArrayList<>());
        drops = new HashMap<>();
        money = new HashMap<>();
        shops = new HashMap<>();
        queue = new HashMap<>();
        defusing = new HashMap<>();
        scoreboards = new HashMap<>();
        this.timer = Main.getSAConfig().getLobbyTime();
        this.maxPlayers = alphaSpawns.size() + omegaSpawns.size();
        this.bar = NmsUtils.createBossbar(Message.BOSSBAR_WAITING.getMsg().replace("%name%", mapName));
    }

    public String getId() {
        return id;
    }

    public BossBar getBar() {
        return bar;
    }

    public Location getLobby() {
        return lobby;
    }

    public String getMapName() {
        return mapName;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public SATeam getTeamA() {
        return teamA;
    }

    public SATeam getTeamB() {
        return teamB;
    }

    public int getTimer() {
        return timer;
    }

    public int getRound() {
        return round;
    }

    public int getScoreTeamA() {
        return scoreTeamA;
    }

    public int getScoreTeamB() {
        return scoreTeamB;
    }

    public void addRoundTeamA() {
        scoreTeamA++;
        //todo update scoreboard
    }

    public void addRoundTeamB() {
        scoreTeamB++;
    }

   public void removeFromQueue(Player player) {
        queue.remove(player.getUniqueId());
        teamA.removePlayer(player);
        teamB.removePlayer(player);
        spectators.remove(player);
   }

    public Location getBombA() {
        return bombA;
    }

    public Location getBombB() {
        return bombB;
    }

    public List<Location> getAlphaSpawns() {
        return alphaSpawns;
    }

    public List<Location> getOmegaSpawns() {
        return omegaSpawns;
    }

    public List<Player> getSpectators() {
        return spectators;
    }

    public SABomb getBomb() {
        return bomb;
    }

    public void setMoney(Player player, int money) {
        this.money.put(player.getUniqueId(), money);
    }

    public int getMoney(Player player) {
        return money.get(player.getUniqueId());
    }

    public void addDefuser(Player player, int time) {
        defusing.put(player, new Defuse(time));
    }

    public boolean isDefusing(Player player) {
        return defusing.get(player) != null;
    }

    public void resetDefusers() {
        defusing.clear();
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public boolean isGameStarting() {
        return gameStarting;
    }

    public boolean isGameEnding() {
        return gameEnding;
    }

    public void setGameEnding(boolean gameEnding) {
        this.gameEnding = gameEnding;
    }

    public boolean isRoundEnding() {
        return roundEnding;
    }

    public SAGameState getState() {
        return state;
    }

    public void setRoundEnding(boolean roundEnding) {
        this.roundEnding = roundEnding;
    }

    public void setGameTimer(int timer) {
        this.timer = timer;
    }

    public Map<UUID, Inventory> getShops() {
        return shops;
    }

    public void setRoundWinner(SATeam.Team roundWinner) {
        this.roundWinner = roundWinner;
    }

    public Map<UUID, SAScoreboard> getScoreboards() {
        return scoreboards;
    }

    public void setState(SAGameState state) {
        this.state = state;
        Main.getInstance().getServer().getPluginManager().callEvent(new GameStateChangeEvent(this, state));
        //todo update scoreboard
        switch(state) {
            case END:
                //todo
                break;
            case ROUND:
                bar.setTitle(Message.BOSSBAR_INGAME.toString().replace("%name%", mapName).replace("%timer%",
                        String.valueOf(timer))); //todo callouts
                bar.setProgress(1);
                break;
            case WAITING:
                bar.setTitle(Message.BOSSBAR_WAITING.getMsg().replace("%name%", mapName));
                bar.setProgress(1);
                break;
        }
    }

    public void randomTeam(Player player) {
        teamA.removePlayer(player);
        teamB.removePlayer(player);
        if(Randomizer.randomBool()) {
            teamA.addPlayer(player);
        } else {
            teamB.addPlayer(player);
        }
    }

    public void addTeamA(Player player) {
        teamB.removePlayer(player);
        teamA.addPlayer(player);
    }

    public void addTeamB(Player player) {
        teamA.removePlayer(player);
        teamB.addPlayer(player);
    }

    public void start() {
        gameStarted = true;
        if(Randomizer.randomBool()) {
            teamA.setTeam(ALPHA);
            teamB.setTeam(OMEGA);
        } else {
            teamA.setTeam(OMEGA);
            teamA.setTeam(ALPHA);
        }
    }

    public void quickJoin(Player player) {
        if(teamA.size() <= teamB.size()) {
            queue.put(player.getUniqueId(), teamA);
            player.teleport(teamA.randomPlayer());
            teamA.addPlayer(player);
        } else {
            queue.put(player.getUniqueId(), teamB);
            player.teleport(teamB.randomPlayer());
            teamB.addPlayer(player);
        }
        spectators.add(player);
    }

    public void sendToAll(String msg) {
        for(Player player : teamA.getPlayers()) {
            player.sendMessage(msg);
        }
        for(Player player : teamB.getPlayers()) {
            player.sendMessage(msg);
        }
        //todo make this work properly with spectators
    }

    public void stop() {
        scoreTeamA = 0;
        scoreTeamB = 0;
        teamA.setTeam(null);
        teamB.setTeam(null);
        gameStarted = false;
    }

    public void run() {
        if(!gameStarted) {
            return;
        }
        switch(state) {
            case END:
                runEnd();
                break;
            case INGAME:
                runIngame();
                break;
            case ROUND:
                runRound();
                break;
            case WAITING:
                runWaiting();
                break;
        }
        timer--;
    }

    private void runEnd() {
        if(timer != 0) {
            return;
        }
        //getManager, stop game todo, add bungee integration here too
    }

    private void runIngame() {
        if(!roundEnding) {
            bar.setTitle(Message.BOSSBAR_INGAME.toString().replace("%name%", mapName).replace("%timer%",
                    String.valueOf(timer))); //todo callouts
            bar.setProgress(bomb.isPlanted() ? (double) timer / 45 : (double) timer / 115);
        }
        //30s after round start close shop
        if(this.timer == 95) {
            for(Player player : teamA.getPlayers()) {
                if(player.getOpenInventory() != null && player.getOpenInventory().getTitle().equals("Shop")) {
                    player.closeInventory();
                    player.sendMessage(Message.SHOP_30_S.getMsg());
                }
            }
            for(Player player : teamB.getPlayers()) {
                if(player.getOpenInventory() != null && player.getOpenInventory().getTitle().equals("Shop")) {
                    player.closeInventory();
                    player.sendMessage(Message.SHOP_30_S.getMsg());
                }
            }
        }
        //bomb planted
        if(bomb.isPlanted()) {
            bomb.setTimer(bomb.getTimer() - 1);
            //TODO tick sound/notifs
            //probably send a title with like 5s left or smth

            //defusing
            for(Player player : defusing.keySet()) {
                if(spectators.contains(player) || !gameStarted) {
                    defusing.remove(player);
                    break;
                }
                if(player.getLocation().distance(bomb.getLocation()) > 3) {
                    NmsUtils.sendTitle(player, 0, 40, 0, "", Message.CANCEL_DEFUSE.toString());
                    defusing.remove(player);
                    break;
                }
                NmsUtils.sendTitle(player, 0, 40, 0, Message.BOMB_DEFUSE.toString(), String.valueOf(defusing.get(player).getTime()));
                //defused
                if(defusing.get(player).getTime() == -1 && !roundEnding) {
                    round++;
                    for(Player p : teamA.getPlayers()) {
                        //play sound? todo
                        NmsUtils.sendTitle(p, 0, 60, 0, Message.BOMB_DEFUSED.toString(), player.getName());
                        p.sendMessage(Message.ROUND_WINNER_ALPHA.toString());
                    }
                    for(Player p : teamB.getPlayers()) {
                        //play sound? todo
                        NmsUtils.sendTitle(p, 0, 60, 0, Message.BOMB_DEFUSED.toString(), player.getName());
                        p.sendMessage(Message.ROUND_WINNER_ALPHA.toString());
                    }
                    timer = 7;
                    roundWinner = ALPHA;
                    if(teamA.getTeam() == ALPHA) {
                        addRoundTeamA();
                    } else {
                        addRoundTeamB();
                    }
                    defusing.remove(player);
                    bomb.reset();
                    //todo reset grenades, maybe extract method
                    roundEnding = true;
                    break;
                }
                defusing.get(player).setTime(defusing.get(player).getTime() - 1);
            }

            //bomb explode
            if(timer == 0 && bomb.isPlanted()) {
                round++;
                timer = 7;
                roundWinner = OMEGA;
                if(teamA.getTeam() == OMEGA) {
                    addRoundTeamA();
                } else {
                    addRoundTeamB();
                }
                bomb.getLocation().getBlock().setType(Material.AIR);
                Main.getInstance().getServer().getPluginManager().callEvent(new BombExplodeEvent(bomb.getLocation()));
                bomb.getLocation().getWorld().playSound(bomb.getLocation(), Sound.EXPLODE, 5, 5);
                bomb.getLocation().getWorld().playEffect(bomb.getLocation(), Effect.EXPLOSION_HUGE, 15);
                bomb.getLocation().getWorld().createExplosion(bomb.getLocation(), 48,false);
                if(!roundEnding) {
                    //todo sounds
                    for(Player p : teamA.getPlayers()) {
                        p.sendMessage(Message.ROUND_WINNER_OMEGA.toString());
                    }
                    for(Player p : teamB.getPlayers()) {
                        p.sendMessage(Message.ROUND_WINNER_OMEGA.toString());
                    }
                }
                bomb.reset();
                roundEnding = true;
            }
        }
        //round ending
        if(roundEnding) {
            //game is won
            if(scoreTeamA == 16 || scoreTeamB == 16) {
                timer = 10;
                setState(SAGameState.END);
                String winnerMsg = scoreTeamA > scoreTeamB ? "Team A wins" : "Team B wins";
                for(Player player : teamA.getPlayers()) {
                    if(!spectators.contains(player))
                        Main.getManager().clearPlayer(player);
                    player.sendMessage(winnerMsg);
                    NmsUtils.sendTitle(player, 0, 200, 0, "Game Over", winnerMsg);
                }
                for(Player player : teamB.getPlayers()) {
                    if(!spectators.contains(player))
                        Main.getManager().clearPlayer(player);
                    player.sendMessage(winnerMsg);
                    NmsUtils.sendTitle(player, 0, 200, 0, "Game Over", winnerMsg);
                }
                Main.getManager().endRound(this);
                return;
            }
            if(timer == 0)  {
                timer = 11;
                if(teamA.getTeam() == ALPHA) {
                    teamA.setTeam(OMEGA);
                    teamB.setTeam(ALPHA);
                } else {
                    teamA.setTeam(ALPHA);
                    teamB.setTeam(OMEGA);
                }
                for(SAScoreboard scoreboard : scoreboards.values()) {
                    scoreboard.removeTeam();
                    scoreboard.showTeams(this);
                }

            }
        }
    }

    private void runRound() {

    }

    private void runWaiting() {

    }

    public int getSize() {
        return teamA.size() + teamB.size();
    }

    public Map<Item, Integer> getDrops() {
        return drops;
    }

    public boolean sameTeam(Player player, Player player2) {
        return (teamA.getPlayers().contains(player) && teamA.getPlayers().contains(player2)) || (teamB.getPlayers().contains(player) && teamB.getPlayers().contains(player2));
    }

    public boolean isAtSpawn(Player player) {
        SATeam team = teamA.getPlayers().contains(player) ? teamA : teamB;
        if(team.getTeam() == ALPHA) {
            for(Location location : alphaSpawns) {
                if(player.getLocation().distance(location) <= 7) {
                    return true;
                }
            }
        } else {
            for(Location location : omegaSpawns) {
                if(player.getLocation().distance(location) <= 7) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isAtBombsite(Location location) {
        return location.distance(bombA) <= 4 || location.distance(bombB) <= 4;
    }

    public int alivePlayers(SATeam team) {
        int n = 0;
        for(Player player : team.getPlayers()) {
            if(!spectators.contains(player)) {
                n++;
            }
        }
        return n;
    }

}