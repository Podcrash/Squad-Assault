package com.podcrash.squadassault.weapons;

import com.podcrash.squadassault.Main;
import com.podcrash.squadassault.game.SAGame;
import com.podcrash.squadassault.nms.NmsUtils;
import com.podcrash.squadassault.nms.PhysicsItem;
import com.podcrash.squadassault.util.Item;
import com.podcrash.squadassault.util.Utils;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Grenade {

    private Item item;
    private double effectPower;
    private String name;
    private int delay;
    private int duration;
    private GrenadeType type;
    private double throwSpeed;
    private List<GrenadeCache> played;

    public Grenade(String name, GrenadeType type, Item item, int delay, int duration, double throwSpeed, double effectPower) {
        this.name = name;
        this.type = type;
        this.item = item;
        this.delay = delay;
        this.duration = duration;
        this.throwSpeed = throwSpeed;
        this.effectPower = effectPower;
        played = new ArrayList<>();
    }

    public Item getItem() {
        return item;
    }

    public double getEffectPower() {
        return effectPower;
    }

    public String getName() {
        return name;
    }

    public int getDelay() {
        return delay;
    }

    public int getDuration() {
        return duration;
    }

    public GrenadeType getType() {
        return type;
    }

    public double getThrowSpeed() {
        return throwSpeed;
    }

    public void throwGrenade(SAGame game, Player player) {
        if (player.getInventory().getItemInHand().getType() != item.getType()) {
            return;
        } //might be a bug here
        played.add(new GrenadeCache(game, player, System.currentTimeMillis(), NmsUtils.spawnPhysicsItem(player,
                player.getItemInHand(), throwSpeed)));
        player.getInventory().setItem(player.getInventory().getHeldItemSlot(), null);
        //play sound
    }

    public void roll(SAGame game, Player player) {
        if (player.getInventory().getItemInHand().getType() != item.getType()) {
            return;
        } //might be a bug here
        played.add(new GrenadeCache(game, player, System.currentTimeMillis(), NmsUtils.spawnPhysicsItem(player,
                player.getItemInHand(), throwSpeed/20)));
        player.getInventory().setItem(player.getInventory().getHeldItemSlot(), null);
        //play sound
    }

    public void explode(PhysicsItem item) {
        for(GrenadeCache cache : played) {
            if(cache.getGrenade() == item) {
                spawnFire(cache);
                break;
            }
        }
    }


    private void spawnFire(GrenadeCache cache) {
        for(Block block : getBlocks(cache.getGrenade().getLocation().getBlock(), effectPower)) {
            if(block.getType() == Material.AIR) {
                if(!block.getRelative(BlockFace.DOWN).getType().isSolid() && (block.getRelative(BlockFace.DOWN).getType() != Material.STEP)) {
                    continue;
                }
                cache.getBlocks().add(block);
                block.setType(Material.FIRE);
            } else {
                if(!block.getType().isSolid() && block.getType() != Material.STEP) {
                    continue;
                }
                Block relative = block.getRelative(BlockFace.UP);
                if(relative.getType() != Material.AIR) {
                    continue;
                }
                cache.getBlocks().add(relative);
                relative.setType(Material.FIRE);
            }
        }
        cache.setDuration(System.currentTimeMillis());
        cache.getGrenade().remove();
    }

    @SuppressWarnings("deprecation")
    public void tick(long ticks) {
        Iterator<GrenadeCache> iterator = played.iterator();
        while(iterator.hasNext()) {
            GrenadeCache cache = iterator.next();
            if ((System.currentTimeMillis() - cache.getTime()) / 1000L < delay) {
                continue;
            }
            Location location = cache.getGrenade().getLocation();
            if(type == GrenadeType.FRAG) {
                //playsound
                location.getWorld().playEffect(location, Effect.EXPLOSION_LARGE, 15);
                for(Player player : cache.getNearbyPlayers(7.0)) {
                    if((cache.getPlayer() == player || Main.getGameManager().getTeam(cache.getGame(),
                            cache.getPlayer()) != Main.getGameManager().getTeam(cache.getGame(), player)) && !cache.getGame().isDead(player)) {
                        if (grenadeLos(location, player)) break;
                        double armorPen =
                                player.getInventory().getChestplate().getType() == Material.LEATHER_CHESTPLATE ? 1 :
                                        0.6;
                        Main.getGameManager().damage(cache.getGame(), cache.getPlayer(), player,
                                armorPen*(effectPower - cache.getGrenade().getLocation().distance(player.getLocation()) * 2),
                                "HE Grenade");
                    }
                }
            }
            if(type == GrenadeType.FLASH) {
                //sound
                for(Player player : cache.getNearbyPlayers(effectPower)) {
                    if(!cache.getGame().isDead(player)) {
                        if (flashbangLos(player, location)) continue;
                        double intensity =
                                2 - Utils.offset(player.getEyeLocation().clone().add(player.getLocation().getDirection()).toVector(), player.getEyeLocation().clone().add(player.getLocation().getDirection()).toVector());

                        double duration = ((2 + (3*location.distance(player.getLocation()))) * intensity) + 1;
                        //mineplex duration math
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) duration, 2), true);
                    }
                }
            }
            if(type == GrenadeType.DECOY) {
                if(cache.getDuration() == -1) {
                    cache.setDuration(System.currentTimeMillis());
                }
                if((System.currentTimeMillis() - cache.getDuration()) / 1000L >= duration) {
                    cache.getGrenade().remove();
                    iterator.remove();
                } else {
                    if(ticks % 2 == 0) {
                        location.getWorld().playSound(location, Sound.BURP, 1.0f, 1.0f);
                    }
                }
            } else if(type == GrenadeType.SMOKE) {
                if(cache.getGrenade().isRemoved()) {
                    if((System.currentTimeMillis() - cache.getDuration()) / 1000 < duration) {
                        continue;
                    }
                    for(Block block : cache.getBlocks()) {
                        block.setType(Material.AIR);
                    }
                    cache.getBlocks().clear();
                    iterator.remove();
                } else {
                    //play sound
                    for(Block block : getBlocks(cache.getGrenade().getLocation().getBlock(), effectPower)) {
                        if(block.getType() == Material.AIR || block.getType() == Material.FIRE || block.getType() == Material.CROPS) {
                            cache.getBlocks().add(block);
                            block.setType(Material.CROPS);
                            block.setData((byte) 7);
                            block.getDrops().clear();
                        }
                    }
                    cache.setDuration(System.currentTimeMillis());
                    cache.getGrenade().remove();
                }
            } else if (type == GrenadeType.FIRE) {
                if(cache.getGrenade().isRemoved()) {
                    if((System.currentTimeMillis() - cache.getDuration()) / 1000 >= duration) {
                        for(Block block : cache.getBlocks()) {
                            block.setType(Material.AIR);
                        }
                        cache.getBlocks().clear();
                        iterator.remove();
                    } else {
                        for(Player player : cache.getNearbyToBlockPlayers()) {
                            if(!cache.getGame().isDead(player)) {
                                player.setFireTicks(0);
                                Main.getGameManager().damage(cache.getGame(), cache.getPlayer(), player, 0.25, "Fire");
                            }
                        }
                    }
                } else {
                    //sound

                    spawnFire(cache);
                }
            } else {
                cache.getGrenade().remove();
                iterator.remove();
            }
        }
    }

    //the difference between these two methods is very small, but fixes a few directional errors we see

    private boolean flashbangLos(Player player, Location entityLoc) {
        Location location = player.getEyeLocation().clone();
        boolean breakLos = false;
        while(Utils.offset(location.toVector(), entityLoc.toVector()) > 0.5) {
            if(location.getBlock().getType().isSolid() && location.getBlock().getType() != Material.STEP) {
                breakLos = true;
                break;
            }
            location.add(Utils.getTrajectory(location.toVector(), entityLoc.toVector()).multiply(0.2));
        }
        return breakLos;
    }

    private boolean grenadeLos(Location location, Player player) {
        Location clone = player.getEyeLocation().clone();
        Vector trajectory = Utils.getTrajectory(location.toVector(), clone.toVector());
        boolean breakLos = false;
        for(int i = 0; i < Math.round(location.distance(clone)) + 1; i++) {
            clone.add(trajectory);
            if(clone.getBlock().getType() != Material.AIR || clone.getBlock().getType() != Material.CROPS) {
                breakLos = true;
                break;
            }
        }
        return breakLos;
    }

    public void remove() {
        Iterator<GrenadeCache> iterator = played.iterator();
        while(iterator.hasNext()) {
            GrenadeCache cache = iterator.next();
            for(Block block : cache.getBlocks()) {
                block.setType(Material.AIR);
            }
            cache.getGrenade().remove();
            iterator.remove();
        }
    }

    public void removePlayer(Player player) {
        Iterator<GrenadeCache> iterator = played.iterator();
        while(iterator.hasNext()) {
            GrenadeCache cache = iterator.next();
            if(cache.getPlayer() == player) {
                for(Block block : cache.getBlocks()) {
                    block.setType(Material.AIR);
                }
                cache.getGrenade().remove();
                iterator.remove();
            }
        }
    }

    private List<Block> getBlocks(Block block, double distance) {
        List<Block> list = new ArrayList<>();
        for (int n = (int)distance + 1, i = -n; i <= n; ++i) {
            for (int j = -n; j <= n; ++j) {
                for (int k = -n; k <= n; ++k) {
                    final Block relative = block.getRelative(i, k, j);
                    if (block.getLocation().toVector().subtract(relative.getLocation().toVector()).length() <= distance) {
                        list.add(relative);
                    }
                }
            }
        }
        return list;
    }
}
