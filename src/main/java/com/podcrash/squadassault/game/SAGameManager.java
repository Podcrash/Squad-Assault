package com.podcrash.squadassault.game;

import com.podcrash.squadassault.Main;
import com.podcrash.squadassault.commands.GameSetup;
import com.podcrash.squadassault.nms.NmsUtils;
import com.podcrash.squadassault.scoreboard.SAScoreboard;
import com.podcrash.squadassault.scoreboard.ScoreboardStatus;
import com.podcrash.squadassault.util.ItemBuilder;
import com.podcrash.squadassault.util.Messages;
import com.podcrash.squadassault.util.Randomizer;
import com.podcrash.squadassault.util.Utils;
import com.podcrash.squadassault.weapons.Grenade;
import com.podcrash.squadassault.weapons.Gun;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bukkit.ChatColor.translateAlternateColorCodes;

public class SAGameManager {

    private final List<SAGame> games;
    private final Map<Player, GameSetup> setup;
    private final Map<Player, List<Player>> assists;

    public SAGameManager() {
        games = new ArrayList<>();
        setup = new HashMap<>();
        assists = new HashMap<>();
    }

    public SAGame findQuickGame(Player player) {
        //todo, not high priority
        return null;
    }

    public void addPlayer(SAGame game, Player player) {
        if(game == null) {
            player.sendMessage(Messages.PLAYER_JOIN_FAIL.toString());
            return;
        }
        if(getGame(player) != null) {
            player.sendMessage(Messages.PLAYER_JOIN_FAIL_MULTIPLE.toString());
            return;
        }
        if(game.getState() == SAGameState.DISABLED) {
            player.sendMessage(Messages.PLAYER_JOIN_FAIL_DISABLED.toString());
            return;
        }
        if(game.getState() != SAGameState.WAITING) {
            player.sendMessage(Messages.PLAYER_JOIN_FAIL_STARTED.toString());
            return;
        }
        if(game.getTeamA().size() + game.getTeamB().size() >= game.getMaxPlayers()) {
            player.sendMessage(Messages.PLAYER_JOIN_FAIL_FULL.toString());
            return;
        }
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[]{null, null, null, null});
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setGameMode(GameMode.ADVENTURE);
        game.randomTeam(player);
        player.teleport(game.getLobby());
        game.getScoreboards().put(player.getUniqueId(), new SAScoreboard(player));
        player.getInventory().setItem(0, ItemBuilder.create(Material.DIAMOND, 1, "Team Selector", "Select a team"));
        game.getStats().put(player.getUniqueId(), new PlayerStats(player.getName()));
        //TODO leave game item
        player.updateInventory();
        game.sendToAll(Messages.PLAYER_JOIN.replace("%p%", player.getDisplayName()) + " " + ChatColor.YELLOW + game
                        .getSize() + ChatColor.AQUA +  " / " + ChatColor.YELLOW + game.getMaxPlayers());
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
        if(!game.isGameEnding()) {
            for(Grenade grenade : Main.getWeaponManager().getGrenades()) {
                grenade.removePlayer(player);
            }
        }

        if(!newGame && game.getState() == SAGameState.ROUND_LIVE && game.getBomb().getCarrier() == player) {
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
            game.getStats().remove(player.getUniqueId());
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
                game.sendToAll(Messages.PLAYER_LEAVE.replace("%p%", player.getName()));
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
                scoreboard.getStatus().setTitle(Messages.SCOREBOARD_TITLE.toString());
            } else if(game.getState() == SAGameState.ROUND_START || game.getState() == SAGameState.ROUND_LIVE) {
                int scoreTeamA = game.getScoreTeamA();
                int scoreTeamB = game.getScoreTeamB();
                SATeam.Team side = game.getTeamA().getTeam();
                scoreboard.getStatus().setTitle(ChatColor.translateAlternateColorCodes('&',
                        "&3" + ((side == SATeam.Team.ALPHA) ? scoreTeamA : scoreTeamB) + " Alpha" + " &4Omega " + ((side == SATeam.Team.OMEGA) ? scoreTeamA : scoreTeamB)));
            } else {
                scoreboard.getStatus().setTitle(Messages.SCOREBOARD_GAME_OVER.toString());
            }
        }
    }

    public void updateStatus(SAGame game, ScoreboardStatus status) {
        Player player = status.getPlayer();
        if(player == null) {
            return;
        }
        if(game.getState() == SAGameState.WAITING || game.isGameEnding()) {
            status.updateLine(7, "");
            status.updateLine(6, translateAlternateColorCodes('&', "&e" + game.getMapName()));
            status.updateLine(5, translateAlternateColorCodes('&',
                    "&e" + game.getSize() + "&b/&e" + game.getMaxPlayers()));
            status.updateLine(4, "");
            if(game.isGameStarted()) {
                status.updateLine(3, translateAlternateColorCodes('&',"&e" + game.getTimer()));
            } else {
                status.updateLine(3, translateAlternateColorCodes('&', "&bWaiting"));
            }
            status.updateLine(2, "");
            status.updateLine(1, translateAlternateColorCodes('&', "&bPodcrash Games"));
        } else if(game.getState() != SAGameState.END || game.getState() != SAGameState.DISABLED) {
            status.updateLine(15, "");
            if(getTeam(game, player) == SATeam.Team.ALPHA) {
                if(game.getBomb().isPlanted()) {
                    status.updateLine(14, Messages.SCOREBOARD_OBJECTIVE.replace("%t%", String.valueOf(game.getTimer())));
                    status.updateLine(13, translateAlternateColorCodes('&', "&eDefuse"));
                    status.updateLine(12, translateAlternateColorCodes('&', "&eBomb"));
                } else {
                    status.updateLine(14, Messages.SCOREBOARD_OBJECTIVE.replace("%t%", String.valueOf(game.getTimer())));
                    status.updateLine(13, translateAlternateColorCodes('&', "&eProtect"));
                    status.updateLine(12, translateAlternateColorCodes('&', "&eBombsites"));
                }
            } else if(game.getBomb().isPlanted()) {
                status.updateLine(14, Messages.SCOREBOARD_OBJECTIVE.replace("%t%", String.valueOf(game.getTimer())));
                status.updateLine(13, translateAlternateColorCodes('&', "&eDefend"));
                status.updateLine(12, translateAlternateColorCodes('&', "&eBomb"));
            } else if(game.getBomb().getCarrier() == player) {
                status.updateLine(14, Messages.SCOREBOARD_OBJECTIVE.replace("%t%", String.valueOf(game.getTimer())));
                status.updateLine(13, translateAlternateColorCodes('&', "&ePlant"));
                status.updateLine(12, translateAlternateColorCodes('&', "&eBomb"));
            } else {
                status.updateLine(14, Messages.SCOREBOARD_OBJECTIVE.replace("%t%", String.valueOf(game.getTimer())));
                status.updateLine(13, translateAlternateColorCodes('&', "&eProtect Bomb"));
                status.updateLine(12, Messages.SCOREBOARD_GOAL.replace("%t%", (game.getBomb().getCarrier() != null ?
                        game.getBomb().getCarrier().getDisplayName() : "")));
            }
            status.updateLine(11, "");
            if(game.getMoney(player) != null) {
                status.updateLine(10, Messages.SCOREBOARD_MONEY.replace("%t%", String.valueOf(game.getMoney(player))));
            } else {
                status.updateLine(9, "");
            }
            ItemStack helmet = player.getInventory().getHelmet();
            ItemStack chest = player.getInventory().getChestplate();
            status.updateLine(9,
                    ChatColor.YELLOW + ((helmet != null && helmet.getType() != Material.LEATHER_HELMET) ? "Helmet " :
                            " ") + ((chest != null && chest.getType() != Material.LEATHER_CHESTPLATE) ? "Kevlar" : ""));
            status.updateLine(8, "");
            status.updateLine(7, translateAlternateColorCodes('&', "&eK&b/&eD&b/&eA"));
            status.updateLine(6, Messages.SCOREBOARD_STATS.replace("%k%",
                    String.valueOf(game.getStats().get(player.getUniqueId()).getKills())).replace("%d%",
                    String.valueOf(game.getStats().get(player.getUniqueId()).getDeaths())).replace("%a%",
                    String.valueOf(game.getStats().get(player.getUniqueId()).getAssists())));
            status.updateLine(5, "");
            status.updateLine(4, "Alpha Alive: " + getAlivePlayers(game, getTeam(game, SATeam.Team.ALPHA)));
            if(getTeam(game, player) == SATeam.Team.ALPHA) {
                status.updateLine(4, translateAlternateColorCodes('&', "&3" + getAlivePlayers(game, getTeam(game, SATeam.Team.ALPHA)) + "&b/&4" + getAlivePlayers(game, getTeam(game, SATeam.Team.OMEGA))));
            } else {
                status.updateLine(4, translateAlternateColorCodes('&', "&4" + getAlivePlayers(game,
                        getTeam(game, SATeam.Team.OMEGA)) + "&b/&3" + getAlivePlayers(game, getTeam(game,
                        SATeam.Team.ALPHA))));
            }
            status.updateLine(3, "");
            status.updateLine(2, "");
            status.updateLine(1,
                    translateAlternateColorCodes('&', (getTeam(game, SATeam.Team.ALPHA).getPlayers().contains(player) ?
                    "&3Alpha" : "&4Omega")));
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
        if (game.getTeamA().size() > 0) {
            for (int i = 0; i < game.getTeamA().size(); --i, ++i) {
                removePlayer(game, game.getTeamA().getPlayers().get(i), autoJoin, false);
            }
        }
        if (game.getTeamB().size() > 0) {
            for (int i = 0; i < game.getTeamB().size(); --i, ++i) {
                removePlayer(game, game.getTeamB().getPlayers().get(i), autoJoin, false);
            }
        }


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
        for(Grenade grenade : Main.getWeaponManager().getGrenades()) {
            grenade.remove();
        }
        for(Item drop : game.getDrops().keySet()) {
            drop.remove();
        }
        game.getDrops().clear();
        if(game.getState() == SAGameState.ROUND_START) {
            for (Player player : game.getSpectators()) {
                if(player.isOnline()) {
                    player.setGameMode(GameMode.ADVENTURE);
                }
            }
        }
        for(Player player : game.getTeamA().getPlayers()) {
            assists.remove(player);
        }
        for(Player player : game.getTeamB().getPlayers()) {
            assists.remove(player);
        }
        for(PlayerStats stats : game.getStats().values()) {
            stats.addRoundsPlayed(1);
            stats.getDamagedPlayers().clear();
        }
        game.resetDefusers();
        game.getBomb().reset();
        game.getSpectators().removeIf(OfflinePlayer::isOnline);
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
        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().setArmorContents(new ItemStack[]{null, null, null, null});
        if (player.isInsideVehicle()) {
            player.leaveVehicle();
        }
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            player.removePotionEffect(potionEffect.getType());
        }
        player.closeInventory();
    }

    public void resetPlayers(SAGame game) {
        game.setOpenAlpha(game.getAlphaSpawns());
        game.setOpenOmega(game.getOmegaSpawns());
        for(Player player : getTeam(game, SATeam.Team.ALPHA).getPlayers()) {
            player.setHealth(20);
            player.closeInventory();

            for(Player p : game.getTeamA().getPlayers()) {
                game.getScoreboards().get(p.getUniqueId()).getHealth().update(player);
            }
            for(Player p : game.getTeamB().getPlayers()) {
                game.getScoreboards().get(p.getUniqueId()).getHealth().update(player);
            }

            player.sendMessage(Messages.PLAYER_JOIN_TEAM_DEFENCE.toString());
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
                Gun gun = Main.getWeaponManager().getGun("P2000");
                ItemStack stack =  ItemBuilder.create(gun.getItem().getType(), gun.getMagSize(),
                        gun.getItem().getData(), gun.getItem().getName());
                stack = Utils.setReserveAmmo(stack, gun.getTotalAmmoSize());
                NmsUtils.addNBTInteger(stack, "outofammo", 0);
                player.getInventory().setItem(1, stack);
                player.getInventory().setItem(2, ItemBuilder.create(Material.WOOD_SWORD, 1, "Knife", true));
            }
            if(player.getInventory().getItem(1) == null) {
                Gun gun = Main.getWeaponManager().getGun("P2000");
                ItemStack stack =  ItemBuilder.create(gun.getItem().getType(), gun.getMagSize(),
                        gun.getItem().getData(), gun.getItem().getName());
                stack = Utils.setReserveAmmo(stack, gun.getTotalAmmoSize());
                NmsUtils.addNBTInteger(stack, "outofammo", 0);
                player.getInventory().setItem(1, stack);
            }

            if(player.getInventory().getItem(2) == null) {
                player.getInventory().setItem(2, ItemBuilder.create(Material.WOOD_SWORD, 1, "Knife", true));
            }

            if(player.getInventory().getItem(7) == null) {
                player.getInventory().setItem(7, ItemBuilder.create(Material.GOLD_NUGGET, 1, "Wire Cutters", false));
            }

            for(int i = 0; i < 2; i++) {
                Gun gun = Main.getWeaponManager().getGun(player.getInventory().getItem(i));
                if(gun != null) {
                    gun.resetPlayer(player);
                    player.getInventory().setItem(i, ItemBuilder.create(gun.getItem().getType(), gun.getMagSize(),
                            gun.getItem().getData(), gun.getName()));
                    NmsUtils.addNBTInteger(player.getInventory().getItem(i), "outofammo", 0);
                }
            }

            SAScoreboard scoreboard = game.getScoreboards().get(player.getUniqueId());
            for(Player p : game.getTeamA().getPlayers()) {
                scoreboard.getTeams().update(game,p);
            }
            for(Player p : game.getTeamB().getPlayers()) {
                scoreboard.getTeams().update(game,p);
            }
            NmsUtils.sendInvisibility(scoreboard, game);
            player.getInventory().setItem(8, ItemBuilder.create(Material.GHAST_TEAR, 1, "Shop", false));
            Location tp = game.getOpenAlpha().get(Randomizer.randomInt(game.getOpenAlpha().size()));
            player.teleport(tp);
            game.getOpenAlpha().remove(tp);
            player.setWalkSpeed(0.2f);

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

            for(Player p : game.getTeamA().getPlayers()) {
                game.getScoreboards().get(p.getUniqueId()).getHealth().update(player);
            }
            for(Player p : game.getTeamB().getPlayers()) {
                game.getScoreboards().get(p.getUniqueId()).getHealth().update(player);
            }

            player.sendMessage(Messages.PLAYER_JOIN_TEAM_OFFENCE.toString());
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
                Gun gun = Main.getWeaponManager().getGun("Glock-18");
                ItemStack stack = ItemBuilder.create(gun.getItem().getType(), gun.getMagSize(),
                        gun.getItem().getData(), gun.getItem().getName());
                stack = Utils.setReserveAmmo(stack, gun.getTotalAmmoSize());
                NmsUtils.addNBTInteger(stack, "outofammo", 0);
                player.getInventory().setItem(1, stack);
                player.getInventory().setItem(2, ItemBuilder.create(Material.WOOD_SWORD, 1, "Knife", true));
            }
            if(player.getInventory().getItem(1) == null) {
                Gun gun = Main.getWeaponManager().getGun("Glock-18");
                ItemStack stack = ItemBuilder.create(gun.getItem().getType(), gun.getMagSize(),
                        gun.getItem().getData(), gun.getItem().getName());
                stack = Utils.setReserveAmmo(stack, gun.getTotalAmmoSize());
                NmsUtils.addNBTInteger(stack, "outofammo", 0);
                player.getInventory().setItem(1, stack);
            }
            if(player.getInventory().getItem(2) == null) {
                player.getInventory().setItem(2, ItemBuilder.create(Material.WOOD_SWORD, 1, "Knife", true));
            }
            if(player.getInventory().getItem(7) == null) {
                player.getInventory().setItem(7, ItemBuilder.create(Material.GOLD_NUGGET, 1, "Wire Cutters", false));
            }

            for(int i = 0; i < 2; i++) {
                Gun gun = Main.getWeaponManager().getGun(player.getInventory().getItem(i));
                if(gun != null) {
                    gun.resetPlayer(player);
                    player.getInventory().setItem(i, ItemBuilder.create(gun.getItem().getType(), gun.getMagSize(),
                            gun.getItem().getData(), gun.getName()));
                    NmsUtils.addNBTInteger(player.getInventory().getItem(i), "outofammo", 0);
                }
            }

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
            Location tp = game.getOpenOmega().get(Randomizer.randomInt(game.getOpenOmega().size()));
            player.teleport(tp);
            game.getOpenOmega().remove(tp);
            player.setWalkSpeed(0.2f);

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
                player.sendMessage(Messages.PLAYER_BOMB_SELF.toString());
            } else {
                player.sendMessage(Messages.PLAYER_BOMB_OTHER.replace("%p%", bombCarrier.getName()));
            }
        }
    }

    public boolean damage(SAGame game, Player damager, Player damaged, double damage, String cause) {
        if(damager != null && !assists.containsKey(damaged)) {
            assists.put(damaged, new ArrayList<>());
        }
        if(damager != null && !assists.get(damaged).contains(damager)) {
            assists.get(damaged).add(damager);
        }
        if(damage >= damaged.getHealth()) { //they die
            if(damager != null) {
                if(game.getStats().get(damager.getUniqueId()) == null) {
                    game.getStats().put(damager.getUniqueId(), new PlayerStats(damager.getName()));
                }
                PlayerStats stats = game.getStats().get(damager.getUniqueId());
                stats.addDamage(damaged.getHealth());
                stats.getDamagedPlayers().put(damaged, stats.getDamagedPlayers().getOrDefault(damaged, 0.0)+damaged.getHealth());
            }
            damaged.setHealth(5); //do this so they experience the hit effect
            damaged.damage(4);
            damaged.setHealth(20);
            damaged.closeInventory();
            game.getSpectators().add(damaged);
            ItemStack[] contents = damaged.getInventory().getContents();
            for(int length = contents.length, i = 0; i < length; i++) {
                ItemStack itemStack = contents[i];
                if (itemStack == null) {
                    continue;
                }
                if(Main.getWeaponManager().getGun(itemStack) != null) {
                    int amount = itemStack.getAmount()-1;
                    itemStack.setAmount(1);
                    game.getDrops().put(damaged.getWorld().dropItemNaturally(damaged.getLocation(), itemStack), amount);
                    damaged.getInventory().remove(itemStack);
                }
                if(Main.getWeaponManager().getGrenade(itemStack) != null) {
                    itemStack.setAmount(1);
                    game.getDrops().put(damaged.getWorld().dropItemNaturally(damaged.getLocation(), itemStack), 1);
                    damaged.getInventory().remove(itemStack);
                }
                if (itemStack.getType() == Material.SHEARS) {
                    Item dropItemNaturally = damaged.getWorld().dropItemNaturally(damaged.getLocation(), itemStack);
                    game.getDrops().put(dropItemNaturally, 1);
                    dropItemNaturally.setItemStack(itemStack);
                }
                if(itemStack.getType() == Material.QUARTZ) {
                    Item dropItemNaturally = damaged.getWorld().dropItemNaturally(damaged.getLocation(), itemStack);
                    game.getDrops().put(dropItemNaturally, 1);
                    dropItemNaturally.setItemStack(itemStack);
                    game.getBomb().setDrop(dropItemNaturally);
                }
                if(itemStack.getType() == Material.GOLDEN_APPLE) {
                    Item dropItemNaturally = damaged.getWorld().dropItemNaturally(damaged.getLocation(), itemStack);
                    game.getDrops().put(dropItemNaturally, 1);
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName("Bomb");
                    itemStack.setItemMeta(itemMeta);
                    itemStack.setType(Material.QUARTZ);
                    dropItemNaturally.setItemStack(itemStack);
                    game.getBomb().setDrop(dropItemNaturally);
                }
            }
            for (Player player : game.getTeamA().getPlayers()) {
                SAScoreboard scoreboard = game.getScoreboards().get(player.getUniqueId());
                if (damager != null) {
                    scoreboard.getTeams().update(game, damager);
                }
                scoreboard.getTeams().update(game, damaged);
                NmsUtils.sendInvisibility(scoreboard, game);
            }
            for (Player player : game.getTeamB().getPlayers()) {
                SAScoreboard scoreboard = game.getScoreboards().get(player.getUniqueId());
                if (damager != null) {
                    scoreboard.getTeams().update(game, damager);
                }
                scoreboard.getTeams().update(game, damaged);
                NmsUtils.sendInvisibility(scoreboard, game);
            }
            clearPlayer(damaged);
            damaged.updateInventory();
            damaged.setGameMode(GameMode.SPECTATOR);
            if(damager != null) {
                game.setMoney(damager, game.getMoney(damager) +
                        (damager.getInventory().getHeldItemSlot() == 2 ? 1500 :
                                Main.getWeaponManager().getGun(damager.getItemInHand()) != null ?
                                        Main.getWeaponManager().getGun(damager.getItemInHand()).getKillReward() : 300));
                game.sendToAll(Messages.KILL_BASE.replace("%p%", damaged.getDisplayName()).replace("%op%",
                        damager.getDisplayName()).replace("%i%", cause) + listStringAssists(assists.get(damaged), damager));
                game.getStats().get(damager.getUniqueId()).addKills(1);

            } else {
                game.sendToAll(Messages.KILL_BASE_NO_KILLER.replace("%p%", damaged.getDisplayName()).replace("%op%",
                        cause) + listStringAssists(assists.getOrDefault(damaged, new ArrayList<>()), null));
            }
            for(Player assisted : assists.getOrDefault(damaged, new ArrayList<>())) {
                if (assisted != damager) {
                    game.getStats().get(assisted.getUniqueId()).addAssists(1);
                }
            }
            assists.remove(damaged);
            game.getStats().get(damaged.getUniqueId()).addDeaths(1);
            DecimalFormat format = new DecimalFormat("##.#");
            if(damager != null) {
                String killerText = "Killer: " + damager.getDisplayName();
                NmsUtils.sendTitle(damaged, 0, 100, 0, killerText,
                        "Damage: " + format.format(game.getStats().get(damager.getUniqueId()).getDamagedPlayers().get(damaged)));
            } else {
                NmsUtils.sendTitle(damaged, 0, 100, 0, "You died", "");
            }
            return true;
        }
        if(damaged.getNoDamageTicks() < 1) {
            double health = damaged.getHealth();
            damaged.setHealth(5);
            damaged.damage(4);
            damaged.setHealth(health);
            damaged.setNoDamageTicks(1);
        }
        if(damager != null) {
            if(game.getStats().get(damager.getUniqueId()) == null) {
                game.getStats().put(damager.getUniqueId(), new PlayerStats(damager.getName()));
            }
            PlayerStats stats = game.getStats().get(damager.getUniqueId());
            stats.addDamage(damage);
            stats.getDamagedPlayers().put(damaged, stats.getDamagedPlayers().getOrDefault(damaged, 0.0)+damage);
        }
        damaged.setHealth(damaged.getHealth() - damage);
        for (Player player : game.getTeamA().getPlayers()) {
            game.getScoreboards().get(player.getUniqueId()).getHealth().update(damaged);
        }
        for (Player player : game.getTeamB().getPlayers()) {
            game.getScoreboards().get(player.getUniqueId()).getHealth().update(damaged);
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
            if(player.isOnline() && !game.getSpectators().contains(player)) {
                n++;
            }
        }
        return n;
    }

    public SAGame getGame(String id) {
        for (SAGame game : games) {
            if (game.getId().equalsIgnoreCase(id)) {
                return game;
            }
        }
        return null;
    }

    public Map<Player, GameSetup> getSetup() {
        return setup;
    }

    private String listStringAssists(List<Player> list, Player damager) {
        StringBuilder builder = new StringBuilder();
        if(list.size() > 0 && !list.get(0).equals(damager)) {
            builder.append("&b, assisted by &e");
        }
        for(Player player : list) {
            if(!player.equals(damager)) {
                builder.append(player.getDisplayName()).append(" ");
            }
        }
        return translateAlternateColorCodes('&', builder.toString());
    }
}
