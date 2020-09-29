package com.podcrash.squadassault.game;

import com.podcrash.squadassault.Main;
import com.podcrash.squadassault.game.events.GunDamageEvent;
import com.podcrash.squadassault.nms.NmsUtils;
import com.podcrash.squadassault.shop.ItemType;
import com.podcrash.squadassault.shop.PlayerShopItem;
import com.podcrash.squadassault.util.ItemBuilder;
import com.podcrash.squadassault.util.Message;
import com.podcrash.squadassault.util.Utils;
import com.podcrash.squadassault.weapons.Grenade;
import com.podcrash.squadassault.weapons.GrenadeType;
import com.podcrash.squadassault.weapons.Gun;
import com.podcrash.squadassault.weapons.ProjectileStats;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings("unused")
public class GameListener implements Listener {

    private final Inventory selector;
    private final ConcurrentMap<SAGame, Boolean> bombPlants;

    public GameListener() {
        selector = Bukkit.createInventory(null, 27, "Team Selector");
        selector.setItem(11, ItemBuilder.create(Material.WOOL, 1, (short)14, "Team A", "Click to join Team A"));
        selector.setItem(13, ItemBuilder.create(Material.WOOL, 1, (short)8, "Random", "Click to join a random team"));
        selector.setItem(15, ItemBuilder.create(Material.WOOL, 1, (short)10, "Team B", "Click to join Team B"));
        bombPlants = new ConcurrentHashMap<>();
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
            } else if(game.getState() == SAGameState.ROUND_LIVE || game.getState() == SAGameState.ROUND_START) {
                ItemStack inHand = player.getItemInHand();
                if(inHand != null && inHand.getType() == Material.GHAST_TEAR) {
                    if(game.isAtSpawn(player)) {
                        if(game.getTimer() > 85 || game.getState() == SAGameState.ROUND_START) {
                            player.openInventory(game.getShops().get(player.getUniqueId()));
                        } else {
                            player.sendMessage(Message.SHOP_20_S.toString());
                        }
                    } else {
                        player.sendMessage("You can only open the shop at spawn");
                    }
                    return;
                }
                if(inHand != null && inHand.getType() != Material.AIR && game.getState() == SAGameState.ROUND_LIVE) {
                    if((inHand.getType() == Material.SHEARS || inHand.getType() == Material.GOLD_NUGGET) && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.DAYLIGHT_DETECTOR) {
                        addDefuse(event, player, game, inHand);
                    }
                    if((inHand.getType() == Material.SHEARS || inHand.getType() == Material.GOLD_NUGGET) && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.CROPS) {
                        addDefuse(event, player, game, inHand);
                    }
                    if(inHand.getType() == Material.GOLDEN_APPLE && game.getState() == SAGameState.ROUND_LIVE && !game.isRoundEnding() && player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isSolid() && bombPlants.get(game) == null) {
                        event.setCancelled(true);
                        bombPlants.put(game, true);
                        new BukkitRunnable() {
                            public void run() {
                                if(!game.isAtBombsite(player.getLocation())) {
                                    cancel();
                                    return;
                                }
                                Block block = player.getLocation().getBlock();
                                if(block.getType() == Material.AIR) {
                                    player.getInventory().setItem(7, ItemBuilder.create(Material.COMPASS, 1, "Bomb Locator", false));
                                    block.setType(Material.DAYLIGHT_DETECTOR);
                                    game.getBomb().setLocation(block.getLocation());
                                    game.getBomb().setTimer(40);
                                    game.getBomb().setPlanted(true);
                                    game.setMoney(player, game.getMoney(player)+300);
                                    game.getStats().get(player.getUniqueId()).addBombPlants(1);
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
                                bombPlants.remove(game);
                            }
                        }.runTaskLater(Main.getInstance(), 60);
                        return;
                    }
                }
                Gun gun = Main.getWeaponManager().getGun(inHand);
                if(gun != null && !game.isDefusing(player)) {
                    gun.shoot(game, player);
                }
                Grenade grenade = Main.getWeaponManager().getGrenade(inHand);
                if (grenade != null && game.getState() == SAGameState.ROUND_LIVE && !game.isRoundEnding() && !game.isDefusing(player)) {
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
        if(game == null || damaged == null || damager == null) {
            return;
        }
        event.setCancelled(true);
        if(game.getState() == SAGameState.ROUND_LIVE && !game.sameTeam(damaged, damager) && damager.getInventory().getHeldItemSlot() == 2 && damager.getInventory().getItemInHand() != null && Main.getUpdateTask().getDelay().get(damaged.getUniqueId()) == null && !game.getSpectators().contains(damaged)) {
            float angle =
                    damager.getEyeLocation().toVector().subtract(damaged.getEyeLocation().toVector()).angle(damaged.getEyeLocation().getDirection().normalize());
            //check if they are behind player or not
            if(damager.getLocation().distance(damaged.getLocation()) <= 1.7 || angle <= 1.5) {
                Main.getGameManager().damage(game, damager, damaged, 3, "Knife");
            } else {
                Main.getGameManager().damage(game, damager, damaged, 20, "Knife Backstab");
            }
            Main.getUpdateTask().getDelay().put(damaged.getUniqueId(), 35);
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
                player.setWalkSpeed(0.25f);
            } else {
                player.setWalkSpeed(0.2f);
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
                if(event.getSlot() == shop.getShopSlot() && (shop.getTeam() == null || Main.getGameManager().getTeam(game, player) == shop.getTeam())) {
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
                                        gun.getItem().getData(), gun.getItem().getName(), shop.getLore()));
                        break;
                    } else {
                        if(shop.getTeam() != null && Main.getGameManager().getTeam(game, player) != shop.getTeam()) {
                            break;
                        }
                        //buying armor
                        if(shop.getMaterial() != Material.SHEARS) {
                            ItemStack itemStack = player.getInventory().getItem(shop.getHotbarSlot());
                            if(shop.getHotbarSlot() == 2 || itemStack == null || itemStack.getType() == Material.LEATHER_HELMET || itemStack.getType() == Material.LEATHER_CHESTPLATE) {
                                game.setMoney(player, game.getMoney(player) - shop.getPrice());
                                player.getInventory().setItem(shop.getHotbarSlot(),
                                        ItemBuilder.create(shop.getMaterial(), 1, shop.getName(), false));
                                break;
                            }
                        } else { //buying defuse kit
                            if(player.getInventory().getItem(shop.getHotbarSlot()).getType() != Material.SHEARS) {
                                game.setMoney(player, game.getMoney(player) - shop.getPrice());
                                player.getInventory().setItem(shop.getHotbarSlot(),
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
            if(player.getInventory().getItem(i) == null) {
                return i;
            }
        }
        return -1;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if(Main.getGameManager().getGame(event.getPlayer()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(Main.getGameManager().getGame(event.getPlayer()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        //TODO: When we make this work properly with bungeecord, lots has to be changed probably
        for(SAGame game : Main.getGameManager().getGames()) {
            for (Player player : game.getTeamA().getPlayers()) {
                player.hidePlayer(event.getPlayer());
            }
            for (Player player : game.getTeamB().getPlayers()) {
                player.hidePlayer(event.getPlayer());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        SAGame game = Main.getGameManager().getGame(player);
        if(game != null) {
            Main.getGameManager().removePlayer(game, player, false, true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        event.setLeaveMessage(null);
        SAGame game = Main.getGameManager().getGame(player);
        if(game != null) {
            Main.getGameManager().removePlayer(game,player,false,true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        SAGame game = Main.getGameManager().getGame(player);
        if(game == null) {
            return;
        }
        if(game.getState() == SAGameState.ROUND_START && !game.getSpectators().contains(player) && (event.getTo().getX() != event.getFrom().getX() || event.getTo().getZ() != event.getFrom().getZ())) {
            event.setTo(event.getFrom());
            return;
        }
        if(game.getState() != SAGameState.ROUND_LIVE) {
            return;
        }
        if(player.getFallDistance() >= 6 && !game.getSpectators().contains(player) && player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) {
            Main.getGameManager().damage(game, null, player, player.getFallDistance(), "Fall");
        }
        if((event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) && game.getBomb().getCarrier() == player) {
            ItemStack itemStack = player.getInventory().getItem(7);
            if(itemStack != null) {
                if(game.isAtBombsite(event.getTo())) {
                    if(itemStack.getType() == Material.QUARTZ) {
                        ItemMeta meta = itemStack.getItemMeta();
                        meta.setDisplayName("Bomb - Right Click");
                        itemStack.setItemMeta(meta);
                        itemStack.setType(Material.GOLDEN_APPLE);
                    } else if(itemStack.getType() == Material.GOLDEN_APPLE) {
                        ItemMeta meta = itemStack.getItemMeta();
                        meta.setDisplayName("Bomb");
                        itemStack.setItemMeta(meta);
                        itemStack.setType(Material.QUARTZ);
                    }
                }
            }
        }
        //todo callouts
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && Main.getGameManager().getGame((Player)event.getEntity()) != null && event.getCause() != EntityDamageEvent.DamageCause.CUSTOM) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player && Main.getGameManager().getGame((Player)event.getEntity()) != null) {
            event.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onHealthRegain(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player && Main.getGameManager().getGame((Player)event.getEntity()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityBreak(HangingBreakByEntityEvent event) {
        if (event.getRemover().getType() == EntityType.PLAYER && Main.getGameManager().getGame((Player)event.getRemover()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityBreak(HangingBreakEvent event) {
        if (event.getCause() != HangingBreakEvent.RemoveCause.ENTITY) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        SAGame game = Main.getGameManager().getGame(player);
        if (game == null) {
            return;
        }
        event.setCancelled(true);
        if (game.getSpectators().contains(player) || (game.getState() != SAGameState.ROUND_LIVE && game.getState() != SAGameState.ROUND_START)) {
            return;
        }
        Item item = event.getItem();
        ItemStack itemStack = item.getItemStack();
        if((itemStack.getType() == Material.QUARTZ || itemStack.getType() == Material.GOLDEN_APPLE) && Main.getGameManager().getTeam(game, player) == SATeam.Team.OMEGA) {
            item.remove();
            game.getDrops().remove(item);
            if(itemStack.getType() == Material.QUARTZ && game.isAtBombsite(item.getLocation())) {
                ItemMeta meta = itemStack.getItemMeta();
                meta.setDisplayName("Bomb - Right Click");
                itemStack.setItemMeta(meta);
                itemStack.setType(Material.GOLDEN_APPLE);
            }
            if(itemStack.getType() == Material.GOLDEN_APPLE && !game.isAtBombsite(item.getLocation())) {
                ItemMeta meta = itemStack.getItemMeta();
                meta.setDisplayName("Bomb");
                itemStack.setItemMeta(meta);
                itemStack.setType(Material.QUARTZ);
            }
            game.getBomb().setCarrier(player);
            player.getInventory().setItem(7, itemStack);
            return;
        }
        Grenade grenade = Main.getWeaponManager().getGrenade(itemStack);
        if(grenade != null && game.getDrops().get(item) != null) {
            int slot = findNadeSlot(player);
            int current = 0;
            GrenadeType type = grenade.getType();
            int max = type.getMax();
            for(int i = 3; i < 8; i++) {
                if(Main.getWeaponManager().getGrenade(player.getInventory().getItem(i)) != null && Main.getWeaponManager().getGrenade(player.getInventory().getItem(i)).getType() ==type) {
                    current++;
                }
            }
            if (slot != -1 && current != max) {
                event.setCancelled(true);
                player.getInventory().setItem(slot, itemStack);
                game.getDrops().remove(item);
                item.remove();
            }
        }
        Gun gun = Main.getWeaponManager().getGun(itemStack);
        Integer n = game.getDrops().get(item);
        if (gun != null && n != null) {
            int gunSlot = gun.getType().ordinal();
            if (player.getInventory().getItem(gunSlot) == null) {
                event.setCancelled(true);
                itemStack.setAmount(n + 1);
                player.getInventory().setItem(gunSlot, itemStack);
                game.getDrops().remove(item);
                item.remove();
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        SAGame game = Main.getGameManager().getGame(player);
        if(game == null) {
            return;
        }
        int heldItemSlot = player.getInventory().getHeldItemSlot();
        ItemStack itemStack = event.getItemDrop().getItemStack();
        int amount = player.getItemInHand().getAmount();
        if(game.getState() == SAGameState.ROUND_LIVE || game.getState() == SAGameState.ROUND_START) {
            if(itemStack.getType() == Material.SHEARS) {
                event.setCancelled(true);
                return;
            }
            if(itemStack.getType() == Material.QUARTZ || itemStack.getType() == Material.GOLDEN_APPLE) {
                if(itemStack.getType() == Material.GOLDEN_APPLE) {
                    ItemMeta meta = itemStack.getItemMeta();
                    meta.setDisplayName("Bomb");
                    itemStack.setItemMeta(meta);
                    itemStack.setType(Material.QUARTZ);
                }
                game.getDrops().put(event.getItemDrop(), 1);
                game.getBomb().setDrop(event.getItemDrop());
                player.getInventory().setItem(7, ItemBuilder.create(Material.COMPASS, 1, "Bomb Locator", false));
                player.setCompassTarget(player.getLocation());
                return;
            }
            Gun gun = Main.getWeaponManager().getGun(itemStack);
            if(gun != null) {
                game.getDrops().put(event.getItemDrop(), amount);
                itemStack.setAmount(1);
                gun.resetDelay(player);
                event.getItemDrop().setItemStack(ItemBuilder.create(itemStack.getType(), 1, gun.getItem().getData(),
                        itemStack.getItemMeta().getDisplayName(),
                        itemStack.getItemMeta().getLore().toArray(new String[0])));
                player.getInventory().setItem(heldItemSlot, null);
                if(gun.hasScope()) {
                    NmsUtils.sendFakeItem(player, 5, player.getInventory().getHelmet());
                }
                return;
            }
            Grenade grenade = Main.getWeaponManager().getGrenade(itemStack);
            if(grenade != null) {
                game.getDrops().put(event.getItemDrop(), 1);
                itemStack.setAmount(1);
                event.getItemDrop().setItemStack(ItemBuilder.create(itemStack.getType(), 1,
                        grenade.getItem().getData(), itemStack.getItemMeta().getDisplayName()));
                player.getInventory().setItem(heldItemSlot, null);
                return;
            }
        }
        ItemStack clone = itemStack.clone();
        event.getItemDrop().remove();
        player.getInventory().setItem(player.getInventory().getHeldItemSlot(), clone);
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if(Main.getGameManager().getGame(player) == null) {
            return;
        }
        Gun gun = Main.getWeaponManager().getGun(player.getItemInHand());
        if(gun == null || !gun.hasScope()) {
            return;
        }
        if(event.isSneaking()) {
            NmsUtils.sendFakeItem(player, 5, new ItemStack(Material.PUMPKIN));
        } else {
            NmsUtils.sendFakeItem(player, 5, player.getInventory().getHelmet());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInHand();
        SAGame game = Main.getGameManager().getGame(player);
        if (game == null || itemInHand == null || itemInHand.getType() == Material.AIR) {
            return;
        }
        event.setCancelled(true);
        Gun gun = Main.getWeaponManager().getGun(itemInHand);
        if(gun != null && !game.isDefusing(player) && game.getState() == SAGameState.ROUND_LIVE) {
            gun.shoot(game, player);
        }
    }

    @EventHandler
    public void onPhysics(BlockPhysicsEvent event) {
        if(event.getBlock().getType() == Material.CROPS) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void projectileDamage(EntityDamageByEntityEvent event) {
        if(!(event.getDamager() instanceof Snowball) || !(event.getEntity() instanceof Player)) {
            return;
        }
        Snowball snowball = (Snowball) event.getDamager();

        ProjectileStats stats = Main.getWeaponManager().getProjectiles().get(snowball);
        if(stats == null) {
            event.setCancelled(true);
            return;
        }
        Player damaged = (Player) event.getEntity();
        if(Main.getGameManager().getGame(damaged).sameTeam(damaged,stats.getShooter()) || Main.getGameManager().getGame(damaged).getSpectators().contains(damaged)) {
            event.setCancelled(true);
            return;
        }
        boolean hs = snowballHeadshot(damaged, snowball);
        if(hs) {
            double armorPen = damaged.getInventory().getHelmet().getType() == Material.LEATHER_HELMET ? 1 :
                    stats.getArmorPen();
            double rangeFalloff = (stats.getDropoff() * damaged.getLocation().distance(stats.getLocation()));
            double damage = stats.getDamage()*2.5;
            double finalDamage = Math.max(0,armorPen*(damage - rangeFalloff));
            Main.getInstance().getServer().getPluginManager().callEvent(new GunDamageEvent(finalDamage, true, stats.getShooter(), damaged));
            Main.getGameManager().getGame(damaged).getStats().get(stats.getShooter().getUniqueId()).addHeadshots(1);
            Main.getGameManager().damage(Main.getGameManager().getGame(damaged), stats.getShooter(), damaged,
                    finalDamage, stats.getGunName() + " headshot");
        } else {
            double armorPen = damaged.getInventory().getChestplate().getType() == Material.LEATHER_CHESTPLATE ? 1 :
                    stats.getArmorPen();
            double rangeFalloff = (stats.getDropoff() * damaged.getLocation().distance(stats.getLocation()));
            double damage = stats.getDamage();
            double finalDamage = Math.max(0,armorPen*(damage - rangeFalloff));
            Main.getInstance().getServer().getPluginManager().callEvent(new GunDamageEvent(finalDamage, false, stats.getShooter(), damaged));
            Main.getGameManager().damage(Main.getGameManager().getGame(damaged), stats.getShooter(), damaged,
                    finalDamage, stats.getGunName());
        }
        Main.getWeaponManager().getProjectiles().remove(snowball);

    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        event.setCancelled(true);
    }

    private boolean snowballHeadshot(Player damaged, Snowball snowball) {
        Location start = snowball.getLocation();
        Location location = start.clone();

        while(!hitHead(damaged, location) && !hitBody(damaged, location) && Utils.offset(damaged.getLocation().toVector(), location.toVector()) < 6) {
            location.add(snowball.getVelocity().clone().multiply(0.1));
        }

        if(hitBody(damaged, location))
            return false;

        return hitHead(damaged, location);
    }

    private boolean hitBody(Player player, Location location) {
        return Utils.offset2d(location.toVector(), player.getLocation().toVector()) < 0.6 &&
                location.getY() > player.getLocation().getY() &&
                location.getY() < player.getEyeLocation().getY() - 0.1;
    }

    private boolean hitHead(Player player, Location location) {
        return Utils.offset2d(location.toVector(), player.getLocation().toVector()) < 0.4 &&
                location.getY() >= player.getEyeLocation().getY() - 0.0 &&
                location.getY() < player.getEyeLocation().getY() + 0.4;
    }


}
