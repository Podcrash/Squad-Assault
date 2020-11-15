package com.podcrash.squadassault.game;

import com.podcrash.squadassault.Main;
import com.podcrash.squadassault.game.events.BombExplodeEvent;
import com.podcrash.squadassault.game.events.GameStateChangeEvent;
import com.podcrash.squadassault.nms.BossBar;
import com.podcrash.squadassault.nms.NmsUtils;
import com.podcrash.squadassault.scoreboard.SAScoreboard;
import com.podcrash.squadassault.shop.ItemType;
import com.podcrash.squadassault.shop.MoneyManager;
import com.podcrash.squadassault.shop.PlayerShopItem;
import com.podcrash.squadassault.util.ItemBuilder;
import com.podcrash.squadassault.util.Messages;
import com.podcrash.squadassault.util.Randomizer;
import com.podcrash.squadassault.util.Utils;
import com.podcrash.squadassault.weapons.Grenade;
import com.podcrash.squadassault.weapons.Gun;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
    private int scoreTeamA;
    private int scoreTeamB;
    private boolean gameStarted;
    private boolean gameEnding;
    private boolean roundEnding;
    private SABomb bomb;
    private SAGameState state;
    private MoneyManager moneyManager;
    private final Location lobby;
    private final Location bombA;
    private final Location bombB;
    private final List<Location> alphaSpawns;
    private final List<Location> omegaSpawns;
    private List<Location> openAlpha;
    private List<Location> openOmega;
    private List<Player> spectators;
    private SATeam teamA;
    private SATeam teamB;
    private Map<Item, Integer> drops;
    private Map<UUID, Integer> money;
    private Map<UUID, Inventory> shops;
    private Map<Player, Defuse> defusing;
    private Map<UUID, SAScoreboard> scoreboards;
    private Map<UUID, SATeam> queue;
    private Map<UUID, PlayerStats> stats;
    private static final Set<Material> transparentBlocks = new HashSet<>(Arrays.asList(Material.AIR, Material.CROPS));

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
        moneyManager = new MoneyManager();
        spectators = new ArrayList<>();
        openAlpha = new ArrayList<>(alphaSpawns);
        openOmega = new ArrayList<>(omegaSpawns);
        teamA = new SATeam(new ArrayList<>());
        teamB = new SATeam(new ArrayList<>());
        drops = new HashMap<>();
        money = new HashMap<>();
        shops = new HashMap<>();
        queue = new HashMap<>();
        defusing = new HashMap<>();
        scoreboards = new HashMap<>();
        stats = new HashMap<>();
        timer = 30;
        maxPlayers = alphaSpawns.size() + omegaSpawns.size();
        bar = NmsUtils.createBossbar(Messages.BOSSBAR_WAITING.replace("%name%", mapName));
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
        Main.getGameManager().updateTitle(this);
    }

    public void addRoundTeamB() {
        scoreTeamB++;
        Main.getGameManager().updateTitle(this);
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

    public Integer getMoney(Player player) {
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

    public int getPlayerCount() {
        return teamA.size() + teamB.size();
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

    public Map<UUID, SAScoreboard> getScoreboards() {
        return scoreboards;
    }

    public void endState() {
        this.state = SAGameState.END;
        for(Player player : Bukkit.getOnlinePlayers()) {
            if(Main.getGameManager().getGame(player) != null) {
                scoreboards.get(player.getUniqueId()).getStatus().reset();
            }
        }
        Bukkit.shutdown();
    }

    public void setState(SAGameState state) {
        this.state = state;
        Main.getInstance().getServer().getPluginManager().callEvent(new GameStateChangeEvent(this, state));
        Main.getGameManager().updateTitle(this);
        switch(state) {
            case ROUND_START:
                bar.setTitle(Messages.BOSSBAR_INGAME.toString().replace("%name%", mapName).replace("%timer%",
                        String.valueOf(timer))); //todo callouts
                bar.setProgress(((double) timer) / 15);
                break;
            case WAITING:
                bar.setTitle(Messages.BOSSBAR_WAITING.replace("%name%", mapName));
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
        teamA.removePlayer(player);
        teamB.removePlayer(player);
        teamA.addPlayer(player);
    }

    public void addTeamB(Player player) {
        teamB.removePlayer(player);
        teamA.removePlayer(player);
        teamB.addPlayer(player);
    }

    public void start() {
        gameStarted = true;
        bombA.getWorld().setStorm(false);
        if(Randomizer.randomBool()) {
            teamA.setTeam(ALPHA);
            teamB.setTeam(OMEGA);
        } else {
            teamA.setTeam(OMEGA);
            teamB.setTeam(ALPHA);
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
        money.clear();
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
            case ROUND_LIVE:
                runLiveRound();
                break;
            case ROUND_START:
                runRoundStart();
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
        Main.getGameManager().stopGame(this, false);
        Bukkit.shutdown();
        /*
          if(shutdown) {
            shutdown();
            return;
          }
         */
        //add bungee integration here todo
    }

    private void runLiveRound() {
        if(!roundEnding) {
            bar.setTitle(Messages.BOSSBAR_INGAME.toString().replace("%name%", mapName).replace("%timer%",
                    String.valueOf(timer))); //todo callouts
            bar.setProgress(bomb.isPlanted() ? (double) timer / 45 : (double) timer / 115);
        }
        //20s after round start close shop
        if(this.timer == 95) {
            for(Player player : teamA.getPlayers()) {
                if(player.getOpenInventory() != null && player.getOpenInventory().getTitle().equals("Shop")) {
                    player.closeInventory();
                    player.sendMessage(Messages.PLAYER_SHOP_DENIED_ELAPSED.toString());
                }
            }
            for(Player player : teamB.getPlayers()) {
                if(player.getOpenInventory() != null && player.getOpenInventory().getTitle().equals("Shop")) {
                    player.closeInventory();
                    player.sendMessage(Messages.PLAYER_SHOP_DENIED_ELAPSED.toString());
                }
            }
        }
        //bomb planted
        if(bomb.isPlanted()) {
            int timer = bomb.getTimer();
            bomb.setTimer(bomb.getTimer() - 1);
            //TODO tick sound/notifs
            //probably send a title with like 5s left or smth

            //defusing
            Iterator<Map.Entry<Player, Defuse>> iterator = defusing.entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry<Player, Defuse> entry = iterator.next();
                Player player = entry.getKey();
                if(isDead(player) || !gameStarted) {
                    iterator.remove();
                    break;
                }
                if(player.getTargetBlock(transparentBlocks, 3).getType() != Material.DAYLIGHT_DETECTOR) {
                    NmsUtils.sendTitle(player, 0, 40, 0, "", Messages.BOMB_DEF_CANCELLED.toString());
                    iterator.remove();
                    break;
                }
                NmsUtils.sendTitle(player, 0, 40, 0, Messages.BOMB_DEF_IN_PROGRESS.toString(),
                        String.valueOf(defusing.get(player).getTime()));
                //defused
                if(entry.getValue().getTime() == -1 && !roundEnding) {
                    round++;
                    stats.get(player.getUniqueId()).addBombDefuses(1);
                    moneyManager.addMoneyEndRound(this, ALPHA, true, MoneyManager.RoundEndType.DEFUSED);
                    for(Player p : teamA.getPlayers()) {
                        //play sound? todo
                        NmsUtils.sendTitle(p, 0, 60, 0, Messages.BOMB_DEF_COMPLETE.replace("%p", player.getName()),
                                "");
                        p.sendMessage(Messages.ROUND_OVER_DEF.replace("%a%",
                                String.valueOf(((teamA.getTeam() == ALPHA) ? scoreTeamA+1 : scoreTeamB+1))).replace(
                                        "%o%", String.valueOf((teamA.getTeam() == OMEGA) ? scoreTeamA : scoreTeamB)));
                        p.sendMessage(Messages.BOMB_DEF_START.replace("%p", player.getName()));
                    }
                    for(Player p : teamB.getPlayers()) {
                        //play sound? todo
                        NmsUtils.sendTitle(p, 0, 60, 0, Messages.BOMB_DEF_COMPLETE.replace("%p", player.getName()),
                                "");
                        p.sendMessage(Messages.ROUND_OVER_DEF.replace("%a%",
                                String.valueOf(((teamA.getTeam() == ALPHA) ? scoreTeamA+1 : scoreTeamB+1))).replace(
                                        "%o%", String.valueOf((teamA.getTeam() == OMEGA) ? scoreTeamA : scoreTeamB)));
                        p.sendMessage(Messages.BOMB_DEF_START.replace("%p", player.getName()));
                    }
                    this.timer = 7;
                    if(teamA.getTeam() == ALPHA) {
                        addRoundTeamA();
                    } else {
                        addRoundTeamB();
                    }
                    iterator.remove();
                    money.put(player.getUniqueId(), money.get(player.getUniqueId())+300);
                    bomb.reset();
                    for(Grenade grenade : Main.getWeaponManager().getGrenades()) {
                        grenade.remove();
                    }
                    roundEnding = true;
                    break;
                }
                entry.getValue().setTime(entry.getValue().getTime() - 1);
            }

            //bomb explode
            if(timer == 0 && bomb.isPlanted()) {
                round++;
                moneyManager.addMoneyEndRound(this, OMEGA, true, MoneyManager.RoundEndType.BOMB);
                this.timer = 7;
                if(teamA.getTeam() == OMEGA) {
                    addRoundTeamA();
                } else {
                    addRoundTeamB();
                }
                bomb.getLocation().getBlock().setType(Material.AIR);
                Main.getInstance().getServer().getPluginManager().callEvent(new BombExplodeEvent(bomb.getLocation()));
                bomb.getLocation().getWorld().playSound(bomb.getLocation(), Sound.EXPLODE, 5, 5);
                bomb.getLocation().getWorld().playEffect(bomb.getLocation(), Effect.EXPLOSION_HUGE, 15);
                bomb(teamA);
                bomb(teamB);
                if(!roundEnding) {
                    //todo sounds
                    for(Player p : teamA.getPlayers()) {
                        p.sendMessage(Messages.ROUND_OVER_DET.replace("%a%",
                                String.valueOf(((teamA.getTeam() == ALPHA) ? scoreTeamA : scoreTeamB))).replace("%o%"
                                , String.valueOf((teamA.getTeam() == OMEGA) ? scoreTeamA+1 : scoreTeamB+1)));
                    }
                    for(Player p : teamB.getPlayers()) {
                        p.sendMessage(Messages.ROUND_OVER_DET.replace("%a%",
                                String.valueOf(((teamA.getTeam() == ALPHA) ? scoreTeamA : scoreTeamB))).replace("%o%"
                                , String.valueOf((teamA.getTeam() == OMEGA) ? scoreTeamA+1 : scoreTeamB+1)));
                    }
                }
                bomb.reset();
                roundEnding = true;
            }
        }
        //round ending
        if(roundEnding) {
            //game is won
            if(scoreTeamA >= 16 || scoreTeamB >= 16) {
                timer = 10;
                setState(SAGameState.END);
                String winnerMsg = ChatColor.YELLOW + (scoreTeamA > scoreTeamB ?
                        "Team A" + ChatColor.AQUA + " wins" : "Team B" + ChatColor.AQUA +  " wins");
                for(Player player : teamA.getPlayers()) {
                    if(!isDead(player))
                        Main.getGameManager().clearPlayer(player);
                    player.sendMessage(winnerMsg);
                    NmsUtils.sendTitle(player, 0, 200, 0, Messages.GAME_OVER.toString(), winnerMsg);
                }
                for(Player player : teamB.getPlayers()) {
                    if(!isDead(player))
                        Main.getGameManager().clearPlayer(player);
                    player.sendMessage(winnerMsg);
                    NmsUtils.sendTitle(player, 0, 200, 0, Messages.GAME_OVER.toString(), winnerMsg);
                }
                Main.getGameManager().endRound(this);
                return;
            }
            if(timer == 0)  {
                timer = 11;
                if(round == 15) {
                    timer = 16;
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
                    initShop(OMEGA);
                    initShop(ALPHA);
                }
                if(queue.size() > 0) {
                    for (Player player : teamA.getPlayers()) {
                        player.sendMessage(ChatColor.AQUA + "Following players joined the game:");
                        for (UUID uuid : queue.keySet()) {
                            player.sendMessage(ChatColor.YELLOW + Bukkit.getPlayer(uuid).getName() + ChatColor.AQUA +
                                    " joined as " + ChatColor.YELLOW + queue.get(uuid).getTeam());
                        }
                    }
                    for (Player player : teamB.getPlayers()) {
                        player.sendMessage(ChatColor.AQUA + "Following players joined the game:");
                        for (UUID uuid : queue.keySet()) {
                            player.sendMessage(ChatColor.YELLOW + Bukkit.getPlayer(uuid).getName() + ChatColor.AQUA +
                                    " joined as " + ChatColor.YELLOW + queue.get(uuid).getTeam());
                        }
                    }
                }

                queue.clear();
                setState(SAGameState.ROUND_START);
                Main.getGameManager().endRound(this);
                Main.getGameManager().resetPlayers(this);
            }
        } else {
            if(!bomb.isPlanted()) {
                for (Player player : Main.getGameManager().getTeam(this, OMEGA).getPlayers()) {
                    player.setCompassTarget(bomb.getLocation());
                }
            }
            //rounds won on kills
            if(spectators.containsAll(Main.getGameManager().getTeam(this, ALPHA).getPlayers())) {
                for (Player player : Main.getGameManager().getTeam(this, ALPHA).getPlayers()) {
                    player.sendMessage(Messages.ROUND_OVER_KILLS.replace("%t%", "Omega").replace("%ot%", "Alpha").replace("%a%",
                            String.valueOf(((teamA.getTeam() == ALPHA) ? scoreTeamA : scoreTeamB))).replace("%o%",
                            String.valueOf((teamA.getTeam() == OMEGA) ? scoreTeamA+1 : scoreTeamB+1)));
                }
                for (Player player : Main.getGameManager().getTeam(this, OMEGA).getPlayers()) {
                    player.sendMessage(Messages.ROUND_OVER_KILLS.replace("%t%", "Omega").replace("%ot%", "Alpha").replace("%a%",
                            String.valueOf(((teamA.getTeam() == ALPHA) ? scoreTeamA : scoreTeamB))).replace("%o%",
                            String.valueOf((teamA.getTeam() == OMEGA) ? scoreTeamA+1 : scoreTeamB+1)));
                }
                finalizeRoundKills(OMEGA);
                return;
            }
            if(spectators.containsAll(Main.getGameManager().getTeam(this, OMEGA).getPlayers()) && !bomb.isPlanted()) {
                for (Player player : Main.getGameManager().getTeam(this, ALPHA).getPlayers()) {
                    player.sendMessage(Messages.ROUND_OVER_KILLS.replace("%t%", "Alpha").replace("%ot%", "Omega").replace("%a%",
                            String.valueOf(((teamA.getTeam() == ALPHA) ? scoreTeamA+1 : scoreTeamB+1))).replace("%o%",
                            String.valueOf((teamA.getTeam() == OMEGA) ? scoreTeamA : scoreTeamB)));
                }
                for (Player player : Main.getGameManager().getTeam(this, OMEGA).getPlayers()) {
                    player.sendMessage(Messages.ROUND_OVER_KILLS.replace("%t%", "Alpha").replace("%ot%", "Omega").replace("%a%",
                            String.valueOf(((teamA.getTeam() == ALPHA) ? scoreTeamA+1 : scoreTeamB+1))).replace("%o%",
                            String.valueOf((teamA.getTeam() == OMEGA) ? scoreTeamA : scoreTeamB)));

                }
                finalizeRoundKills(ALPHA);
                return;
            }

            //timer expires, CTs win
            if(timer == 0) {
                for (Player player : Main.getGameManager().getTeam(this, ALPHA).getPlayers()) {
                    player.sendMessage(Messages.ROUND_OVER_TIME.replace("%a%",
                            String.valueOf(((teamA.getTeam() == ALPHA) ? scoreTeamA+1 : scoreTeamB+1))).replace("%o%",
                            String.valueOf((teamA.getTeam() == OMEGA) ? scoreTeamA : scoreTeamB)));
                }
                for (Player player : Main.getGameManager().getTeam(this, OMEGA).getPlayers()) {
                    player.sendMessage(Messages.ROUND_OVER_TIME.replace("%a%",
                            String.valueOf(((teamA.getTeam() == ALPHA) ? scoreTeamA+1 : scoreTeamB+1))).replace("%o%",
                            String.valueOf((teamA.getTeam() == OMEGA) ? scoreTeamA : scoreTeamB)));
                }
                moneyManager.addMoneyEndRound(this, ALPHA, false, MoneyManager.RoundEndType.TIME);
                round++;
                finalizeRound(ALPHA);
            }
        }
    }

    private void bomb(SATeam team) {
        for (Player player : team.getPlayers()) {
            if (isDead(player)) {
                continue;
            }
            double distance = player.getLocation().distance(bomb.getLocation());
            if (distance >= 48) {
                continue;
            }
            if (distance <= 16) {
                Main.getGameManager().damage(this, null, player, 20.0,
                        "Bomb");
                continue;
            }
            double range = (distance - 16.0) / 32.0;
            double damage = Math.min(20.0, 0.0062 / range);

            Main.getGameManager().damage(this, null, player, damage,
                    "Bomb");
        }
    }

    private void finalizeRound(SATeam.Team alpha) {
        timer = 7;
        if(teamA.getTeam() == alpha) {
            addRoundTeamA();
        } else {
            addRoundTeamB();
        }
        for(Grenade grenade : Main.getWeaponManager().getGrenades()) {
            grenade.remove();
        }

        roundEnding = true;
    }

    private void resetReserveAmmo(Player player) {
        ItemStack stack = player.getInventory().getItem(0);
        Gun primary = Main.getWeaponManager().getGun(stack);
        if(primary != null) {
            stack = Utils.setReserveAmmo(stack, primary.getTotalAmmoSize());
            primary.resetReloading(player, 0);
            NmsUtils.sendActionBar(player,
                    stack.getAmount() + " / " + Utils.getReserveAmmo(stack));
            NmsUtils.addNBTInteger(stack, "outofammo", 0);
        }
        stack = player.getInventory().getItem(1);
        Gun secondary = Main.getWeaponManager().getGun(stack);
        if(secondary != null) {
            stack = Utils.setReserveAmmo(stack, secondary.getTotalAmmoSize());
            secondary.resetReloading(player, 1);
            NmsUtils.sendActionBar(player,
                    stack.getAmount() + " / " + Utils.getReserveAmmo(stack));
            NmsUtils.addNBTInteger(stack, "outofammo", 0);
        }
    }

    private void finalizeRoundKills(SATeam.Team winner) {
        round++;
        //bomb planted is irrelevant necessarily on a round won by kills
        moneyManager.addMoneyEndRound(this, winner, false, MoneyManager.RoundEndType.KILLS);
        finalizeRound(winner);
    }

    private void runRoundStart() {
        bombA.getWorld().setStorm(false);
        for(Player player : teamA.getPlayers()) {
            resetReserveAmmo(player);
        }
        for(Player player : teamB.getPlayers()) {
            resetReserveAmmo(player);
        }
        if(round == 15 && timer >= 11) {
            for (Player player : teamA.getPlayers()) {
                NmsUtils.sendTitle(player, 0, 40, 0, ChatColor.AQUA + "Teams Swapped!", "");
            }
            for (Player player : teamB.getPlayers()) {
                NmsUtils.sendTitle(player, 0, 40, 0, ChatColor.AQUA + "Teams Swapped!", "");
            }
            return;
        }
        if(timer == 0) {
            for (Player player : teamA.getPlayers()) {
                NmsUtils.sendTitle(player, 0, 30, 0, ChatColor.AQUA + "Go!", "");
            }
            for (Player player : teamB.getPlayers()) {
                NmsUtils.sendTitle(player, 0, 30, 0, ChatColor.AQUA + "Go!", "");
            }
            timer = 115;
            setState(SAGameState.ROUND_LIVE);
        }
    }

    private void runWaiting() {
        if(teamA.getPlayers().size() + teamB.getPlayers().size() < minPlayers) {
            stop();
            for (Player player : teamA.getPlayers()) {
                player.sendMessage(Messages.GAME_INSUFFICIENT_PLAYERS.toString());
            }
            for (Player player : teamB.getPlayers()) {
                player.sendMessage(Messages.GAME_INSUFFICIENT_PLAYERS.toString());
            }
            timer = 30;
            for(SAScoreboard scoreboard : scoreboards.values()) {
                Main.getGameManager().updateStatus(this, scoreboard.getStatus());
            }
            return;
        }
        //start the game
        if(timer == 0) {
            //team balancer
            while(true) {
                if(teamA.size() <= maxPlayers / 2 && teamB.size() <= maxPlayers / 2) {
                    if(Math.abs(teamA.size() - teamB.size()) <= 1) {
                        break;
                    }
                    if(teamA.size() < teamB.size()) {
                        Player player = teamB.getPlayers().get(Randomizer.randomInt(teamB.getPlayers().size()));
                        addTeamA(player);
                    } else {
                        if(teamB.size() >= teamA.size()) {
                            continue;
                        }
                        Player player = teamA.getPlayers().get(Randomizer.randomInt(teamA.getPlayers().size()));
                        addTeamB(player);
                    }
                } else if(teamA.size() > maxPlayers / 2) {
                    Player player = teamA.getPlayers().get(Randomizer.randomInt(teamA.getPlayers().size()));
                    addTeamB(player);
                } else {
                    if(teamB.size() <= maxPlayers / 2) {
                        continue;
                    }
                    Player player = teamB.getPlayers().get(Randomizer.randomInt(teamB.getPlayers().size()));
                    addTeamA(player);
                }
            }
            initShopWaiting(OMEGA);
            initShopWaiting(ALPHA);
            timer = 15;
            Main.getGameManager().resetPlayers(this);
            for(SAScoreboard scoreboard : scoreboards.values()) {
                scoreboard.getStatus().reset();
            }
            setState(SAGameState.ROUND_START);
            return;
        }
        for (Player player : teamA.getPlayers()) {
            if (timer <= 5 || timer % 10 == 0) {
                player.sendMessage(Messages.GAME_STARTING.replace("%v%", String.valueOf(timer)));
            }
        }
        for (Player player : teamB.getPlayers()) {
            if (timer <= 5 || timer % 10 == 0) {
                player.sendMessage(Messages.GAME_STARTING.replace("%v%", String.valueOf(timer)));
            }
        }
    }

    private void initShop(SATeam.Team team) {
        for(Player player : Main.getGameManager().getTeam(this, team).getPlayers()) {
            Inventory inventory = shops.get(player.getUniqueId());
            inventory.clear();
            initShopInventory(team, inventory);
        }
    }

    private void initShopWaiting(SATeam.Team team) {
        for(Player player : Main.getGameManager().getTeam(this, team).getPlayers()) {
            Inventory inventory = Bukkit.createInventory(null, 45, "Shop");
            initShopInventory(team, inventory);
            shops.put(player.getUniqueId(), inventory);
            scoreboards.get(player.getUniqueId()).showTeams(this);
            scoreboards.get(player.getUniqueId()).showHealth(this);
        }
    }

    private void initShopInventory(SATeam.Team team, Inventory inventory) {
        inventory.clear();
        for(PlayerShopItem shop : Main.getShopManager().getShops()) {
            if(shop.getType() == ItemType.GRENADE) {
                Grenade grenade = Main.getWeaponManager().getGrenade(shop.getWeaponName());
                inventory.setItem(shop.getShopSlot(), ItemBuilder.create(grenade.getItem().getType(),
                        1, grenade.getItem().getData(), shop.getName(),shop.getLore()));
            } else if(shop.getType() == ItemType.GUN && (shop.getTeam() == null || shop.getTeam() == team)) {
                Gun gun = Main.getWeaponManager().getGun(shop.getWeaponName());
                inventory.setItem(shop.getShopSlot(), ItemBuilder.create(gun.getItem().getType(), 1,
                        gun.getItem().getData(), shop.getName(), shop.getLore()));
            } else {
                if(shop.getTeam() != null && shop.getTeam() != team) {
                    continue;
                }
                inventory.setItem(shop.getShopSlot(), ItemBuilder.create(shop.getMaterial(), 1, shop.getName(), shop.getLore()));
            }
        }
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
        return Utils.offset(location.toVector(), bombA.toVector()) <= 4 || Utils.offset(location.toVector(), bombB.toVector()) <= 4;
    }

    public Map<UUID, PlayerStats> getStats() {
        return stats;
    }

    public boolean isDead(Player player) {
        return (teamA.getPlayers().contains(player) || teamB.getPlayers().contains(player)) && (!player.isOnline() || spectators.contains(player));
    }

    public List<Location> getOpenAlpha() {
        return openAlpha;
    }

    public List<Location> getOpenOmega() {
        return openOmega;
    }

    public void setOpenAlpha(List<Location> openAlpha) {
        this.openAlpha = new ArrayList<>(openAlpha);
    }

    public void setOpenOmega(List<Location> openOmega) {
        this.openOmega = new ArrayList<>(openOmega);
    }
}