package com.podcrash.squadassault.game;

import com.podcrash.api.game.Game;
import com.podcrash.squadassault.nms.NmsUtils;
import com.podcrash.squadassault.scoreboard.SAScoreboard;
import com.podcrash.squadassault.scoreboard.ScoreboardStatus;
import com.podcrash.squadassault.util.ItemBuilder;
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
        game.getScoreboards().put(player.getUniqueId(), new SAScoreboard(game, player));
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

    public void removePlayer(SAGame game, Player player) {
        game.removeFromQueue(player);
        if(game.getState() == SAGameState.INGAME && game.getBomb().getCarrier() == player) {
            Item dropItemNaturally = player.getWorld().dropItemNaturally(player.getLocation(),
                    ItemBuilder.create(Material.QUARTZ, 1, "Bomb", false));
            game.getBomb().setDrop(dropItemNaturally);
            game.getDrops().put(dropItemNaturally, 1);
        }

        if(game.getState() != SAGameState.WAITING && game.getState() != SAGameState.END && !game.isGameEnding() && (game.getTeamA().size() == 0 || game.getTeamB().size() == 0)) {
            stopGame(game);
            game.sendToAll("The game was stopped because a team had no players!");
        }
        game.getBar().removePlayer(player);

        for(Player p : Bukkit.getOnlinePlayers()) {
            player.showPlayer(p);
        }
        for(Player p : game.getTeamA().getPlayers()) {
            p.hidePlayer(player);
        }
        for(Player p : game.getTeamB().getPlayers()) {
            p.hidePlayer(player);
        }

        if(game.getState() == SAGameState.WAITING) {
            game.sendToAll(player.getDisplayName() + " left! " + game.getSize() + "/" + game.getMaxPlayers());
        }
        //todo bungee and actually correctly sending the player to where they should be
    }

    public void addGame(SAGame game) {
        games.add(game);
    }

    public void updateTitle(SAGame game) {

    }

    public void updateStatus(SAGame game, ScoreboardStatus status) {
        //todo scoreboard code
    }

    public void stopGame(SAGame game, boolean autoJoin) {
        game.stop();
        game.setGameEnding(true);
        endRound(game);
        for(SAScoreboard scoreboard : game.getScoreboards()) {
            scoreboard.getStatus().reset();
        }

        game.getTeamA().getPlayers().forEach(player -> removePlayer(game, player));
        game.getTeamB().getPlayers().forEach(player -> removePlayer(game, player));

        //todo NEW GAME CODE
        game.setState(SAGameState.WAITING);
        game.setGameEnding(false);
        game.setGameTimer(10); //todo get config lobby time
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
            player.getInventory().setItem(8, ItemBuilder.create(Material.GHAST_TEAR, 1, "Alpha Shop", false));
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
            player.getInventory().setItem(8, ItemBuilder.create(Material.GHAST_TEAR, 1, "Omega Shop", false));
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

        }
    }
}
