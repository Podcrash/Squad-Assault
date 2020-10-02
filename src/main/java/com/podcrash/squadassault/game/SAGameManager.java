package com.podcrash.squadassault.game;

import com.podcrash.squadassault.Main;
import com.podcrash.squadassault.commands.GameSetup;
import com.podcrash.squadassault.nms.NmsUtils;
import com.podcrash.squadassault.scoreboard.SAScoreboard;
import com.podcrash.squadassault.scoreboard.ScoreboardStatus;
import com.podcrash.squadassault.util.ItemBuilder;
import com.podcrash.squadassault.util.Message;
import com.podcrash.squadassault.util.Randomizer;
import com.podcrash.squadassault.util.Utils;
import com.podcrash.squadassault.weapons.Grenade;
import com.podcrash.squadassault.weapons.Gun;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
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
        player.getInventory().clear();
        player.setGameMode(GameMode.ADVENTURE);
        game.randomTeam(player);
        player.teleport(game.getLobby());
        game.getScoreboards().put(player.getUniqueId(), new SAScoreboard(player));
        player.getInventory().setItem(0, ItemBuilder.create(Material.DIAMOND, 1, "Team Selector", "Select a team"));
        game.getStats().put(player.getUniqueId(), new PlayerStats(player.getName()));
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
            } else if(game.getState() == SAGameState.ROUND_START || game.getState() == SAGameState.ROUND_LIVE) {
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
        if(player == null) {
            return;
        }
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
            } else if(game.getBomb().getCarrier() == player) {
                status.updateLine(14, "OBJECTIVE:");
                status.updateLine(13, "Plant the bomb");
            } else {
                status.updateLine(14, "OBJECTIVE:");
                status.updateLine(13, "Protect the bomb carrier " + (game.getBomb().getCarrier() != null ?
                        game.getBomb().getCarrier().getDisplayName() : ""));
            }
            if(game.getState() == SAGameState.ROUND_LIVE && !game.isRoundEnding()) {
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
            if(game.getMoney(player) != null) {
                status.updateLine(9, "Money: $" + game.getMoney(player));
            }
            ItemStack helmet = player.getInventory().getHelmet();
            ItemStack chest = player.getInventory().getChestplate();
            status.updateLine(8,
                    ((helmet != null && helmet.getType() != Material.LEATHER_HELMET) ? "helmet " : " ") + ((chest != null && chest.getType() != Material.LEATHER_CHESTPLATE) ? "kevlar" : ""));
            status.updateLine(7,
                    "K/D/A/ADR:");
            String adr = new DecimalFormat("##.#").format(game.getStats().get(player.getUniqueId()).getADR());
            status.updateLine(6,
                    game.getStats().get(player.getUniqueId()).getKills() + "/" + game.getStats().get(player.getUniqueId()).getDeaths() + "/" + game.getStats().get(player.getUniqueId()).getAssists()+ "/" + adr);
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
            grenade.remove(game);
        }
        for(Item drop : game.getDrops().keySet()) {
            drop.remove();
        }
        game.getDrops().clear();
        if(game.getState() == SAGameState.ROUND_START) {
            for (Player player : game.getSpectators()) {
                player.setGameMode(GameMode.ADVENTURE);
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
        }
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
        player.setGameMode(GameMode.ADVENTURE);
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

            for(Player p : game.getTeamA().getPlayers()) {
                game.getScoreboards().get(p.getUniqueId()).getHealth().update(player);
            }
            for(Player p : game.getTeamB().getPlayers()) {
                game.getScoreboards().get(p.getUniqueId()).getHealth().update(player);
            }

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
            player.teleport(game.getAlphaSpawns().get(Randomizer.randomInt(game.getAlphaSpawns().size())));
            if(player.getInventory().getHeldItemSlot() == 2) {
                player.setWalkSpeed(0.25f);
            } else {
                player.setWalkSpeed(0.2f);
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

            for(Player p : game.getTeamA().getPlayers()) {
                game.getScoreboards().get(p.getUniqueId()).getHealth().update(player);
            }
            for(Player p : game.getTeamB().getPlayers()) {
                game.getScoreboards().get(p.getUniqueId()).getHealth().update(player);
            }

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
            player.teleport(game.getOmegaSpawns().get(Randomizer.randomInt(game.getOmegaSpawns().size())));
            if(player.getInventory().getHeldItemSlot() == 2) {
                player.setWalkSpeed(0.25f);
            } else {
                player.setWalkSpeed(0.2f);
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
                game.getStats().get(damager.getUniqueId()).addDamage(damaged.getHealth());
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
                game.sendToAll(damager.getDisplayName() + " killed " + damaged.getDisplayName() + " via " + cause + listStringAssists(assists.get(damaged), damager));
                game.getStats().get(damager.getUniqueId()).addKills(1);

            } else {
                game.sendToAll(damaged.getDisplayName() + " died to " + cause + listStringAssists(assists.getOrDefault(damaged, new ArrayList<>()), null));
            }
            for(Player assisted : assists.getOrDefault(damaged, new ArrayList<>())) {
                if (assisted != damager) {
                    game.getStats().get(assisted.getUniqueId()).addAssists(1);
                }
            }
            assists.remove(damaged);
            game.getStats().get(damaged.getUniqueId()).addDeaths(1);
            NmsUtils.sendTitle(damaged, 0, 100, 0, "You died", damager != null ? damager.getDisplayName() : "");
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
            game.getStats().get(damager.getUniqueId()).addDamage(damage);
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
            if(!game.getSpectators().contains(player)) {
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
        if(list.size() > 1) {
            builder.append(", assisted by ");
        }
        for(Player player : list) {
            if(player != damager) {
                builder.append(player.getDisplayName()).append(" ");
            }
        }
        return builder.toString();
    }
}
