package com.podcrash.squadassault.game;

import com.podcrash.squadassault.Main;
import com.podcrash.squadassault.util.ItemBuilder;
import com.podcrash.squadassault.util.Message;
import com.podcrash.squadassault.weapons.Grenade;
import com.podcrash.squadassault.weapons.Gun;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("unused")
public class GameListener implements Listener {

    private final Inventory selector;

    public GameListener() {
        selector = Bukkit.createInventory(null, 27, "Team Selector");
        selector.setItem(11, ItemBuilder.create(Material.WOOL, 1, (short)14, "Team A", "Click to join Team A"));
        selector.setItem(13, ItemBuilder.create(Material.WOOL, 1, (short)8, "Random", "Click to join a random team"));
        selector.setItem(15, ItemBuilder.create(Material.WOOL, 1, (short)10, "Team B", "Click to join Team B"));
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onIgnite(BlockIgniteEvent blockIgniteEvent) {
        if (blockIgniteEvent.getCause() == BlockIgniteEvent.IgniteCause.SPREAD) {
            blockIgniteEvent.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        SAGame game = Main.getGameManager().getGame(player);
        if(game == null) {
            return;
        }
        if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if(game.getState() == SAGameState.WAITING) {
                if(player.getInventory().getItemInHand() != null) {
                    if(player.getInventory().getItemInHand().getType() == Material.DIAMOND) {
                        player.openInventory(selector);
                    } else if(player.getInventory().getItemInHand().getType() == Material.LEATHER) {
                        event.setCancelled(true);
                        Main.getGameManager().removePlayer(game, player, false, false);
                        player.sendMessage("You left the game");
                    }
                }
            } else if(game.getState() == SAGameState.INGAME || game.getState() == SAGameState.ROUND) {
                ItemStack inHand = player.getItemInHand();
                if(inHand != null && inHand.getType() == Material.GHAST_TEAR) {
                    if(game.isAtSpawn(player)) {
                        if(game.getTimer() > 85 || game.getState() == SAGameState.ROUND) {
                            player.openInventory(game.getShops().get(player.getUniqueId()));
                        } else {
                            player.sendMessage(Message.SHOP_20_S.toString());
                        }
                    } else {
                        player.sendMessage("You can only open the shop at spawn");
                    }
                    return;
                }
                if(inHand != null && inHand.getType() != Material.AIR && game.getState() == SAGameState.INGAME) {
                    if((inHand.getType() == Material.SHEARS || inHand.getType() == Material.GOLD_NUGGET) && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.DAYLIGHT_DETECTOR) {
                        addDefuse(event, player, game, inHand);
                    }
                    if((inHand.getType() == Material.SHEARS || inHand.getType() == Material.GOLD_NUGGET) && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.CROPS) {
                        addDefuse(event, player, game, inHand);
                    }
                }
                Gun gun = Main.getWeaponManager().getGun(inHand);
                if(gun != null && !game.isDefusing(player)) {
                    gun.shoot(game, player);
                }
                Grenade grenade = Main.getWeaponManager().getGrenade(inHand);
                if (grenade != null && game.getState() == SAGameState.INGAME && !game.isRoundEnding() && !game.isDefusing(player)) {
                    event.setCancelled(true);
                    grenade.throwGrenade(game, player);
                }
            }
        } else if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            ItemStack inHand = player.getItemInHand();
            if(inHand != null && inHand.getType() != Material.AIR) {
                Gun gun = Main.getWeaponManager().getGun(inHand);
                if(gun != null) {
                    gun.reload(player, player.getInventory().getHeldItemSlot());
                    return;
                }
                Grenade grenade = Main.getWeaponManager().getGrenade(inHand);
                if(grenade != null) {
                    grenade.roll(game, player);
                }
            }
        }
    }

    private void addDefuse(PlayerInteractEvent event, Player player, SAGame game, ItemStack inHand) {
        event.setCancelled(true);
        if(Main.getGameManager().getTeam(game, SATeam.Team.ALPHA).getPlayers().contains(player) && !game.isDefusing(player) && player.getLocation().distance(game.getBomb().getLocation()) <= 3) {
            game.addDefuser(player, (inHand.getType() == Material.SHEARS ? 5 : 10));
            //play sound? todo
        }
    }

    @EventHandler
    public void onKnifeDamage(EntityDamageByEntityEvent event) {
        if(event.getEntity().getType() == EntityType.ITEM_FRAME && event.getDamager() instanceof Player && Main.getGameManager().getGame((Player) event.getDamager()) != null) {
            event.setCancelled(true);
            return;
        }
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }
        Player damaged = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();
        SAGame game = Main.getGameManager().getGame(damaged);
        if(game == null) {
            return;
        }
        event.setCancelled(true);
        if(game.getState() == SAGameState.INGAME && !game.sameTeam(damaged, damager) && damager.getInventory().getHeldItemSlot() == 2 && damager.getInventory().getItemInHand() != null && Main.getUpdateTask().getDelay().get(damaged.getUniqueId()) == null && !game.getSpectators().contains(damaged)) {
            float angle =
                    damager.getEyeLocation().toVector().subtract(damaged.getEyeLocation().toVector()).angle(damaged.getEyeLocation().getDirection().normalize());
            //check if they are behind player or not
            if(damager.getLocation().distance(damaged.getLocation()) <= 1.7 || angle <= 1.5) {
                Main.getGameManager().damage(game, damager, damaged, 3, "Knife");
            } else {
                Main.getGameManager().damage(game, damager, damaged, 20, "Knife");
            }
            Main.getUpdateTask().getDelay().put(damaged.getUniqueId(), 35);
        }
    }

    @EventHandler
    public void onBombPlant(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        SAGame game = Main.getGameManager().getGame(player);
        if(game == null) {
            return;
        }
        event.setCancelled(true);

    }

}
