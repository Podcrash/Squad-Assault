package com.podcrash.squadassault.game;

import com.podcrash.squadassault.nms.NmsUtils;
import com.podcrash.squadassault.scoreboard.SAScoreboard;
import com.podcrash.squadassault.scoreboard.ScoreboardStatus;
import com.podcrash.squadassault.util.ItemBuilder;
import com.podcrash.squadassault.util.Message;
import com.podcrash.squadassault.util.Randomizer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

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
            return;
        }
        if(getGame(player) != null) {
            player.sendMessage("already in a game");
            return;
        }
        if(game.getState() == SAGameState.DISABLED) {
            player.sendMessage("disabled");
            return;
        }
        if(game.getState() != SAGameState.WAITING) {
            player.sendMessage("already started");
            return;
        }
        if(game.getTeamA().size() + game.getTeamB().size() == game.getMaxPlayers()) {
            player.sendMessage("full");
            return;
        }
        game.randomTeam(player);
        player.teleport(game.getLobby());
        game.getScoreboards().put(player.getUniqueId(), new SAScoreboard(player));
        player.getInventory().setItem(0, ItemBuilder.create(Material.DIAMOND, 1, "Team Selector", "Select a team"));
        //TODO leave game item
        player.updateInventory();
        game.sendToAll(player.getDisplayName() + " joined the game. " + game.getSize() + "/" + game.getMaxPlayers());
        if(game.getSize() >= game.getMinPlayers()) {
            game.start();
        }
        updateTitle(game);
        game.getBar().addPlayer(player);
        for(SAScoreboard scoreboard : game.getScoreboards().values()) {
            updateStatus(game, scoreboard.getStatus());
        }

        for(Player p : Bukkit.getOnlinePlayers()) {
            if(game.getTeamA().getPlayers().contains(p) || game.getTeamB().getPlayers().contains(p)) {
                p.showPlayer(player);
            } else {
                player.hidePlayer(p);
            }
        }
    }

    public void removePlayer(SAGame game, Player player, boolean newGame, boolean leftServer) {
        game.removeFromQueue(player);
        //todo remove grenades

        if(!newGame && game.getState() == SAGameState.INGAME && game.getBomb().getCarrier() == player) {
            Item dropItemNaturally = player.getWorld().dropItemNaturally(player.getLocation(),
                    ItemBuilder.create(Material.QUARTZ, 1, "Bomb", false));
            game.getBomb().setDrop(dropItemNaturally);
            game.getDrops().put(dropItemNaturally, 1);
        }

        if(!newGame && game.getState() != SAGameState.WAITING && game.getState() != SAGameState.END && !game.isGameEnding() && (game.getTeamA().size() == 0 || game.getTeamB().size() == 0)) {
            stopGame(game, true);
            game.sendToAll("The game was stopped because a team had no players!");
        }
        game.getBar().removePlayer(player);

        if(newGame) {
            clearPlayer(player);
            SAScoreboard scoreboard = game.getScoreboards().get(player.getUniqueId());
            scoreboard.getStatus().reset();
            updateStatus(game, scoreboard.getStatus());
            player.getInventory().setItem(0, ItemBuilder.create(Material.DIAMOND, 1, "Team Selector", "Select a team"));
        } else {
            SAScoreboard scoreboard = game.getScoreboards().remove(player.getUniqueId());
            if(game.getState() != SAGameState.WAITING) {
                for(SAScoreboard saScoreboard : game.getScoreboards().values()) {
                    if(scoreboard != saScoreboard) {
                        saScoreboard.getTeams().remove(player);
                    }
                }
            }
            scoreboard.remove();
            if(!leftServer) {
                for(Player p : Bukkit.getOnlinePlayers()) {
                    player.showPlayer(p);
                }
                for(Player p : game.getTeamA().getPlayers()) {
                    p.hidePlayer(player);
                }
                for(Player p : game.getTeamB().getPlayers()) {
                    p.hidePlayer(player);
                }
            }
            if(leftServer && game.getState() == SAGameState.WAITING) {
                game.sendToAll(player.getDisplayName() + " left! " + game.getSize() + "/" + game.getMaxPlayers());
            }
            //call an event?
        }
        player.updateInventory();
    }

    public void addGame(SAGame game) {
        games.add(game);
    }

    public void updateTitle(SAGame game) {
        for(SAScoreboard scoreboard : game.getScoreboards().values()) {
            if(game.getState() == SAGameState.WAITING) {
                scoreboard.getStatus().setTitle(Message.SCOREBOARD_TITLE.toString());
            } else if(game.getState() == SAGameState.ROUND || game.getState() == SAGameState.INGAME) {
                int scoreTeamA = game.getScoreTeamA();
                int scoreTeamB = game.getScoreTeamB();
                SATeam.Team side = game.getTeamA().getTeam();
                scoreboard.getStatus().setTitle(((side == SATeam.Team.ALPHA) ? scoreTeamA : scoreTeamB) + " α" + " Ω " + ((side == SATeam.Team.OMEGA) ? scoreTeamA : scoreTeamB));
            } else {
                scoreboard.getStatus().setTitle("GAME OVER");
            }
        }
    }

    public void updateStatus(SAGame game, ScoreboardStatus status) {
        Player player = status.getPlayer();
        if(game.getState() == SAGameState.WAITING || game.isGameEnding()) {
            status.updateLine(7, "");
            status.updateLine(6, game.getMapName());
            status.updateLine(5, game.getSize() + "/" + game.getMaxPlayers());
            status.updateLine(4, "");
            if(game.isGameStarted()) {
                status.updateLine(3, String.valueOf(game.getTimer()));
            } else {
                status.updateLine(3, "Waiting");
            }
            status.updateLine(2, "");
            status.updateLine(1, "Podcrash Games");
        } else if(game.getState() != SAGameState.END || game.getState() != SAGameState.DISABLED) {
            status.updateLine(15, "");
            if(getTeam(game, player) == SATeam.Team.ALPHA) {
                if(game.getBomb().isPlanted()) {
                    status.updateLine(14, "OBJECTIVE:");
                    status.updateLine(13, "Defuse the bomb");
                } else {
                    status.updateLine(14, "OBJECTIVE:");
                    status.updateLine(13, "Protect bombsites");
                }
            } else if(game.getBomb().isPlanted()) {
                status.updateLine(14, "OBJECTIVE:");
                status.updateLine(13, "Protect the bomb");
            } else if(game.getBomb().getCarrier().equals(player)) {
                status.updateLine(14, "OBJECTIVE:");
                status.updateLine(13, "Plant the bomb");
            } else {
                status.updateLine(14, "OBJECTIVE:");
                status.updateLine(13, "Protect the bomb carrier " + (game.getBomb().getCarrier() != null ?
                        game.getBomb().getCarrier().getDisplayName() : ""));
            }
            if(game.getState() == SAGameState.INGAME && !game.isRoundEnding()) {
                int timer = game.getTimer();
                status.updateLine(12,
                        ((timer % 3600 / 60 < 10) ? "0" : "") + timer % 3600 / 60 + ":" + ((timer % 3600 % 60 < 10) ? "0" : "") + timer % 3600 % 60);
            } else if(game.isRoundEnding() && !game.getBomb().isPlanted()) {
                status.updateLine(12, "00:00");
            } else {
                int time = game.getBomb().isPlanted() ? game.getBomb().getTimer() : 115;
                status.updateLine(12,
                        ((time % 3600 / 60 < 10) ? "0" : "") + time % 3600 / 60 + ":" + ((time % 3600 % 60 < 10) ? "0" : "") + time % 3600 % 60);
            }
            status.updateLine(11,"");
            status.updateLine(10, "");
            status.updateLine(9, "Money: $" + game.getMoney(player));
            ItemStack helmet = player.getInventory().getHelmet();
            ItemStack chest = player.getInventory().getHelmet();
            status.updateLine(8,
                    ((helmet != null && helmet.getType() != Material.LEATHER_HELMET) ? "helmet " : " ") + ((chest != null && chest.getType() != Material.LEATHER_CHESTPLATE) ? "kevlar" : ""));
            status.updateLine(7, "kills - deaths");
            //todo maybe cooler stats like adr or rating
            status.updateLine(6, "");
            status.updateLine(5, "");
            status.updateLine(4, "Alpha Alive: " + getAlivePlayers(game, getTeam(game, SATeam.Team.ALPHA)));
            status.updateLine(3, "Omega Alive: " + getAlivePlayers(game, getTeam(game, SATeam.Team.OMEGA)));
            status.updateLine(2, "");
            status.updateLine(1, (getTeam(game, SATeam.Team.ALPHA).getPlayers().contains(player) ? "Alpha" : "Omega"));
        }
    }

    public void stopGame(SAGame game, boolean autoJoin) {
        game.stop();
        game.setGameEnding(true);
        endRound(game);
        for(SAScoreboard scoreboard : game.getScoreboards().values()) {
            scoreboard.getStatus().reset();
        }
        List<Player> list = new ArrayList<>();
        if(autoJoin) {
            list.addAll(game.getTeamA().getPlayers());
            list.addAll(game.getTeamB().getPlayers());
        }
        game.getTeamA().getPlayers().forEach(player -> removePlayer(game, player, autoJoin, false));
        game.getTeamB().getPlayers().forEach(player -> removePlayer(game, player, autoJoin, false));

        //todo bungee code here

        if(autoJoin) {
            for(Player player : list) {
                game.randomTeam(player);
            }
            for(SAScoreboard scoreboard : game.getScoreboards().values()) {
                scoreboard.removeHealth();
                scoreboard.removeTeam();
                updateStatus(game, scoreboard.getStatus());
            }
            list.clear();
        }
        if(autoJoin && game.getTeamA().size() + game.getTeamB().size() >= game.getMinPlayers()) {
            game.start();
        }
        game.setGameTimer(30); //todo get config lobby time
        game.setState(SAGameState.WAITING);
        game.setGameEnding(false);
    }

    public void endRound(SAGame game) {
        game.getDrops().clear();
        if(game.getState() == SAGameState.ROUND) {
            game.getSpectators().forEach(player -> player.setGameMode(GameMode.ADVENTURE));
        }
        game.setRoundWinner(null);
        game.resetDefusers();
        game.getBomb().reset();
        game.getSpectators().clear();
        game.setRoundEnding(false);
    }

    public SAGame getGame(Player player) {
        for(SAGame game : games) {
            if(game.getTeamA().getPlayers().contains(player) || game.getTeamB().getPlayers().contains(player)) {
                return game;
            }
        }
        return null;
    }

    public List<SAGame> getGames() {
        return games;
    }

    public SATeam getTeam(SAGame game, SATeam.Team side) {
        if(game.getTeamA().getTeam() == side) {
            return game.getTeamA();
        }
        return game.getTeamB();
    }

    public SATeam.Team getTeam(SAGame game, Player player) {
        if (game.getTeamA().getPlayers().contains(player)) {
            return game.getTeamA().getTeam();
        }
        return game.getTeamB().getTeam();
    }

    public void clearPlayer(Player player) {
        player.setExp(0);
        player.setLevel(0);
        player.setFireTicks(0);
        player.setFoodLevel(20);
        player.setFlying(false);
        player.setFallDistance(0.0f);
        player.setAllowFlight(false);
        player.getInventory().clear();
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().setArmorContents(null);
        if (player.isInsideVehicle()) {
            player.leaveVehicle();
        }
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            player.removePotionEffect(potionEffect.getType());
        }
        player.closeInventory();
    }

    public void resetPlayers(SAGame game) {
        for(Player player : getTeam(game, SATeam.Team.ALPHA).getPlayers()) {
            player.setHealth(20);
            player.closeInventory();

            //todo update scoreboard health

            player.sendMessage("You are alpha yay go");
            if(game.getRound() == 0) {
                game.setMoney(player, 800);
                player.getInventory().setItem(0, null); //clear team selector
                player.getInventory().setItem(2, ItemBuilder.create(Material.WOOD_SWORD, 1, "Knife", true));
            } else if(game.getRound() == 15) { //switch half
                game.setMoney(player, 800);
                player.getInventory().setItem(0, null);
                player.getInventory().setItem(3,null);
                player.getInventory().setItem(4, null);
                player.getInventory().setItem(5, null);
                player.getInventory().setItem(6, null);
                //give player default pistol todo
                player.getInventory().setItem(2, ItemBuilder.create(Material.WOOD_SWORD, 1, "Knife", true));
            }
            if(player.getInventory().getItem(1) == null) {
                //give player default pistol todo
            }
            if(player.getInventory().getItem(2) == null) {
                player.getInventory().setItem(2, ItemBuilder.create(Material.WOOD_SWORD, 1, "Knife", true));
            }
            if(player.getInventory().getItem(7) == null) {
                player.getInventory().setItem(7, ItemBuilder.create(Material.GOLD_NUGGET, 1, "Wire Cutters", false));
            }

            //reset guns todo
            SAScoreboard scoreboard = game.getScoreboards().get(player.getUniqueId());
            for(Player p : game.getTeamA().getPlayers()) {
                scoreboard.getTeams().update(game,p);
            }
            for(Player p : game.getTeamB().getPlayers()) {
                scoreboard.getTeams().update(game,p);
            }
            NmsUtils.sendInvisibility(scoreboard, game);
            player.getInventory().setItem(8, ItemBuilder.create(Material.GHAST_TEAR, 1, "Shop", false));
            player.teleport(game.getAlphaSpawns().get(Randomizer.randomInt(game.getAlphaSpawns().size())));
            if(player.getInventory().getHeldItemSlot() == 2) {
                player.setWalkSpeed(1.05f);
            } else {
                player.setWalkSpeed(1);
            }

            if(player.getInventory().getHelmet() == null || game.getRound() == 15) {
                player.getInventory().setHelmet(ItemBuilder.createItem(Material.LEATHER_HELMET, Color.BLUE, "Alpha Helmet"));
            }
            if(player.getInventory().getChestplate() == null || game.getRound() == 15) {
                player.getInventory().setChestplate(ItemBuilder.createItem(Material.LEATHER_CHESTPLATE, Color.BLUE,
                        "Alpha Chestplate"));
            }
        }
        for(Player player : getTeam(game, SATeam.Team.OMEGA).getPlayers()) {
            player.setHealth(20);
            player.closeInventory();

            //todo update scoreboard health

            player.sendMessage("You are omega yay go");
            if(game.getRound() == 0) {
                game.setMoney(player, 800);
                player.getInventory().setItem(0, null); //clear team selector
                player.getInventory().setItem(2, ItemBuilder.create(Material.WOOD_SWORD, 1, "Knife", true));
            } else if(game.getRound() == 15) { //switch half
                game.setMoney(player, 800);
                player.getInventory().setItem(0, null);
                player.getInventory().setItem(3,null);
                player.getInventory().setItem(4, null);
                player.getInventory().setItem(5, null);
                player.getInventory().setItem(6, null);
                player.getInventory().setItem(7, ItemBuilder.create(Material.GOLD_NUGGET, 1, "Wire Cutters", true));
                //give player default pistol todo
                player.getInventory().setItem(2, ItemBuilder.create(Material.WOOD_SWORD, 1, "Knife", true));
            }
            if(player.getInventory().getItem(1) == null) {
                //give player default pistol todo
            }
            if(player.getInventory().getItem(2) == null) {
                player.getInventory().setItem(2, ItemBuilder.create(Material.WOOD_SWORD, 1, "Knife", true));
            }
            if(player.getInventory().getItem(7) == null) {
                player.getInventory().setItem(7, ItemBuilder.create(Material.GOLD_NUGGET, 1, "Wire Cutters", false));
            }

            //reset guns todo
            SAScoreboard scoreboard = game.getScoreboards().get(player.getUniqueId());
            for(Player p : game.getTeamA().getPlayers()) {
                scoreboard.getTeams().update(game,p);
            }
            for(Player p : game.getTeamB().getPlayers()) {
                scoreboard.getTeams().update(game,p);
            }
            NmsUtils.sendInvisibility(scoreboard, game);
            player.getInventory().setItem(7, ItemBuilder.create(Material.COMPASS, 1, "Bomb Locator", false));
            player.getInventory().setItem(8, ItemBuilder.create(Material.GHAST_TEAR, 1, "Shop", false));
            player.teleport(game.getAlphaSpawns().get(Randomizer.randomInt(game.getOmegaSpawns().size())));
            if(player.getInventory().getHeldItemSlot() == 2) {
                player.setWalkSpeed(1.05f);
            } else {
                player.setWalkSpeed(1);
            }

            if(player.getInventory().getHelmet() == null || game.getRound() == 15) {
                player.getInventory().setHelmet(ItemBuilder.createItem(Material.LEATHER_HELMET, Color.RED, "Omega Helmet"));
            }
            if(player.getInventory().getChestplate() == null || game.getRound() == 15) {
                player.getInventory().setChestplate(ItemBuilder.createItem(Material.LEATHER_CHESTPLATE, Color.RED,
                        "Omega Chestplate"));
            }
        }

        Player bombCarrier = getTeam(game, SATeam.Team.OMEGA).getPlayers().get(Randomizer.randomInt(getTeam(game,
                SATeam.Team.OMEGA).size()));
        game.getBomb().setCarrier(bombCarrier);
        bombCarrier.getInventory().setItem(7, ItemBuilder.create(Material.QUARTZ, 1, "Bomb", false));

        for(Player player : getTeam(game, SATeam.Team.OMEGA).getPlayers()) {
            player.setCompassTarget(bombCarrier.getLocation());
            if(player.equals(bombCarrier)) {
                player.sendMessage("You have the bomb");
            } else {
                player.sendMessage(bombCarrier.getDisplayName() + " has the bomb!");
            }
        }
    }

    public boolean damage(SAGame game, Player damager, Player damaged, double damage, String cause) {
        if(damaged.getHealth() <= damage) {
            //todo
        }
        return false;
    }

    public void removeGame(SAGame game) {
        game.getShops().clear();
        game.getAlphaSpawns().clear();
        game.getOmegaSpawns().clear();
        games.remove(game);
    }

    public int getAlivePlayers(SAGame game, SATeam side) {
        int n = 0;
        for(Player player : side.getPlayers()) {
            if(!game.getSpectators().contains(player)) {
                n++;
            }
        }
        return n;
    }
}
