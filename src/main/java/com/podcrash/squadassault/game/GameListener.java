package com.podcrash.squadassault.game;

import com.podcrash.squadassault.Main;
import com.podcrash.squadassault.nms.NmsUtils;
import com.podcrash.squadassault.shop.ItemType;
import com.podcrash.squadassault.shop.PlayerShopItem;
import com.podcrash.squadassault.util.ItemBuilder;
import com.podcrash.squadassault.util.Message;
import com.podcrash.squadassault.weapons.Grenade;
import com.podcrash.squadassault.weapons.GrenadeType;
import com.podcrash.squadassault.weapons.Gun;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
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
        if(player.getItemInHand().getType() == Material.GOLDEN_APPLE && game.getState() == SAGameState.INGAME && !game.isRoundEnding() && player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) {
            Block block = player.getLocation().getBlock();
            if(block.getType() == Material.AIR) {
                player.getInventory().setItem(7, ItemBuilder.create(Material.COMPASS, 1, "Bomb Locator", false));
                block.setType(Material.DAYLIGHT_DETECTOR);
                game.getBomb().setLocation(block.getLocation());
                game.getBomb().setTimer(40);
                game.getBomb().setPlanted(true);
                game.setMoney(player, game.getMoney(player)+300);
                for(Player omega : Main.getGameManager().getTeam(game, SATeam.Team.OMEGA).getPlayers()) {
                    omega.setCompassTarget(game.getBomb().getLocation());
                    //todo play sound
                    NmsUtils.sendTitle(omega,0,23,0,"","BOMB PLANTED");
                }
                for(Player alpha : Main.getGameManager().getTeam(game, SATeam.Team.ALPHA).getPlayers()) {
                    //todo play sound
                    NmsUtils.sendTitle(alpha,0,23,0,"","BOMB PLANTED");
                }
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if(!event.getMessage().startsWith("#")) {
            return;
        }
        Player player = event.getPlayer();
        SAGame game = Main.getGameManager().getGame(player);
        if(game == null) {
            for(SAGame saGame : Main.getGameManager().getGames()) {
                event.getRecipients().removeAll(saGame.getTeamA().getPlayers());
                event.getRecipients().removeAll(saGame.getTeamB().getPlayers());
            }
            return;
        }
        event.getRecipients().clear();
        if(Main.getGameManager().getTeam(game, player) == SATeam.Team.ALPHA) {
            event.getRecipients().addAll(Main.getGameManager().getTeam(game, SATeam.Team.ALPHA).getPlayers());
            event.setFormat(Message.TEAM_CHAT_FORMAT.toString().replace("%player%",player.getDisplayName()).replace(
                    "%message%", "%2$s"));
        }
        if(Main.getGameManager().getTeam(game, player) == SATeam.Team.OMEGA) {
            event.getRecipients().addAll(Main.getGameManager().getTeam(game, SATeam.Team.OMEGA).getPlayers());
            event.setFormat(Message.TEAM_CHAT_FORMAT.toString().replace("%player%",player.getDisplayName()).replace(
                    "%message%", "%2$s"));
        }
    }

    @EventHandler
    public void onSlotChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        SAGame game = Main.getGameManager().getGame(player);
        if (game == null || game.getState() == SAGameState.WAITING) {
            return;
        }
        Gun gun = Main.getWeaponManager().getGun(player.getInventory().getItem(event.getPreviousSlot()));
        if(gun != null) {
            gun.resetPlayer(player);
            player.getInventory().getItem(event.getPreviousSlot()).setDurability((short) 0);
        }
        Gun gun2 = Main.getWeaponManager().getGun(player.getItemInHand());
        if(gun2 != null && gun2.hasScope() && player.isSneaking()) {
            event.setCancelled(true);
        }
        if(!event.isCancelled()) {
            if(event.getNewSlot() == 2) {
                player.setWalkSpeed(1.05f);
            } else {
                player.setWalkSpeed(1);
            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (Main.getGameManager().getGame(event.getPlayer()) != null && event.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if(Main.getGameManager().getGame(event.getPlayer()) == null) {
            return;
        }
        if(event.getRightClicked().getType() == EntityType.ARMOR_STAND) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        SAGame game = Main.getGameManager().getGame(player);
        if(game == null) {
            return;
        }
        event.setCancelled(true);
        if (event.getSlotType() == InventoryType.SlotType.OUTSIDE) {
            return;
        }
        //team selection
        if(event.getClickedInventory().equals(selector)) {
            if(event.getSlot() == 11) {
                game.addTeamA(player);
                player.sendMessage("Chose team A");
                player.closeInventory();
            } else if (event.getSlot() == 13) {
                game.randomTeam(player);
                player.sendMessage("Chose random team");
                player.closeInventory();
            } else if (event.getSlot() == 15) {
                game.addTeamB(player);
                player.sendMessage("Chose team B");
                player.closeInventory();
            }
        }
        //buying stuff
        if(event.getClickedInventory().getName().equals("Shop")) {
            for(PlayerShopItem shop : Main.getShopManager().getShops()) {
                if(event.getSlot() == shop.getSlot() && (shop.getTeam() == null || Main.getGameManager().getTeam(game, player) == shop.getTeam())) {
                    if(shop.getPrice() > game.getMoney(player)) {
                        player.closeInventory();
                        player.sendMessage("not enough money");
                        break;
                    }
                    if(shop.getType() == ItemType.GRENADE) {
                        Grenade grenade = Main.getWeaponManager().getGrenade(shop.getWeaponName());
                        GrenadeType type = grenade.getType();
                        int max = type.getMax();
                        int current = 0;
                        for(int i = 3; i < 8; i++) {
                            if(Main.getWeaponManager().getGrenade(player.getInventory().getItem(i)) != null && Main.getWeaponManager().getGrenade(player.getInventory().getItem(i)).getType() == type) {
                                current++;
                            }
                        }
                        if(current == max) {
                            player.closeInventory();
                            player.sendMessage("You already have the maximum amount of that grenade!");
                            break;
                        }
                        int desiredSlot = findNadeSlot(player);
                        if(desiredSlot != -1) {
                            game.setMoney(player, game.getMoney(player)-shop.getPrice());
                            player.getInventory().setItem(desiredSlot,
                                    ItemBuilder.create(grenade.getItem().getType(), 1,
                                            grenade.getItem().getData(), grenade.getItem().getName()));
                            break;
                        }
                        player.closeInventory();
                        player.sendMessage("Your slots are full!");
                        break;
                    } else if(shop.getType() == ItemType.GUN) {
                        if(shop.getTeam() != null && Main.getGameManager().getTeam(game, player) != shop.getTeam()) {
                            break;
                        }
                        Gun gun = Main.getWeaponManager().getGun(shop.getWeaponName());
                        game.setMoney(player, game.getMoney(player) - shop.getPrice());
                        player.getInventory().setItem(gun.getType().ordinal(),
                                ItemBuilder.create(gun.getItem().getType(), gun.getMagSize(),
                                        gun.getItem().getData(), gun.getItem().getName()));
                        break;
                    } else {
                        if(shop.getTeam() != null && Main.getGameManager().getTeam(game, player) != shop.getTeam()) {
                            break;
                        }
                        //buying armor
                        if(shop.getMaterial() != Material.SHEARS) {
                            ItemStack itemStack = player.getInventory().getItem(shop.getSlotPlace());
                            if(shop.getSlotPlace() == 2 || itemStack == null || itemStack.getType() == Material.LEATHER_HELMET || itemStack.getType() == Material.LEATHER_CHESTPLATE) {
                                game.setMoney(player, game.getMoney(player) - shop.getPrice());
                                player.getInventory().setItem(shop.getSlotPlace(),
                                        ItemBuilder.create(shop.getMaterial(), 1, shop.getName(), false));
                                break;
                            }
                        } else { //buying defuse kit
                            if(player.getInventory().getItem(shop.getSlotPlace()).getType() != Material.SHEARS) {
                                game.setMoney(player, game.getMoney(player) - shop.getPrice());
                                player.getInventory().setItem(shop.getSlotPlace(),
                                        ItemBuilder.create(shop.getMaterial(), 1, shop.getName(), false));
                                break;
                            }
                        }
                        player.closeInventory();
                        player.sendMessage("You already have that!");
                        break;
                    }
                }
            }
        }
    }

    private int findNadeSlot(Player player) {
        for(int i = 3; i < 8; i++) {
            if(player.getInventory().getItem(i).getType() == null) {
                return i;
            }
        }
        return -1;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {

    }
}
