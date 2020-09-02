package com.podcrash.squadassault.weapons;

import com.podcrash.squadassault.Main;
import com.podcrash.squadassault.game.SAGame;
import com.podcrash.squadassault.nms.NmsUtils;
import com.podcrash.squadassault.util.Item;
import com.podcrash.squadassault.util.Randomizer;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
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
                player.getItemInHand(), throwSpeed/100)));
        player.getInventory().setItem(player.getInventory().getHeldItemSlot(), null);
        //play sound
    }

    @SuppressWarnings("deprecation")
    public void tick(long ticks) {
        for(GrenadeCache cache : played) {
            if ((System.currentTimeMillis() - cache.getTime()) / 1000L < delay) {
                continue;
            }
            Location location = cache.getGrenade().getLocation();
            if(type == GrenadeType.FRAG) {
                //playsound
                location.getWorld().playEffect(location, Effect.EXPLOSION_LARGE, 15);
                for(Player player : cache.getNearbyPlayers(7.0)) {
                    if((cache.getPlayer() == player || Main.getGameManager().getTeam(cache.getGame(),
                            cache.getPlayer()) != Main.getGameManager().getTeam(cache.getGame(), player)) && !cache.getGame().getSpectators().contains(player)) {
                        if (los(location, player)) break;
                        Main.getGameManager().damage(cache.getGame(), cache.getPlayer(), player,
                                effectPower - cache.getGrenade().getLocation().distance(player.getLocation()) * 2,
                                "HE Grenade");
                    }
                }
            }
            if(type == GrenadeType.FLASH) {
                //sound
                for(Player player : cache.getNearbyPlayers(effectPower)) {
                    if(!cache.getGame().getSpectators().contains(player) || cache.getPlayer() == player || Main.getGameManager().getTeam(cache.getGame(),
                            cache.getPlayer()) != Main.getGameManager().getTeam(cache.getGame(), player)) {
                        float angle =
                                location.toVector().subtract(player.getEyeLocation().toVector()).angle(player.getEyeLocation().getDirection().normalize());
                        if(Float.isNaN(angle) || angle > 1) {
                            continue;
                        }
                        if (los(location, player)) continue;
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, duration * 20, 2), true);
                    }
                }
            }
            if(type == GrenadeType.DECOY) {
                if(cache.getDuration() == -1) {
                    cache.setDuration(System.currentTimeMillis());
                }
                if((System.currentTimeMillis() - cache.getDuration()) / 1000L >= duration) {
                    cache.getGrenade().remove();
                    played.remove(cache);
                } else {
                    //play sound
                }
            } else if(type == GrenadeType.SMOKE) {
                if(cache.getGrenade().isRemoved()) {
                    if((System.currentTimeMillis() - cache.getDuration()) / 1000 < duration) {
                        continue;
                    }
                    for(Block block : cache.getBlocks()) {
                        block.setType(Material.AIR);
                    }
                } else {
                    //play sound
                    for(Block block : getBlocks(cache.getGrenade().getLocation().getBlock(), effectPower)) {
                        if(block.getType() == Material.AIR) {
                            cache.getBlocks().add(block);
                            block.setType(Material.CROPS);
                            block.setData((byte) Randomizer.randomRange(1,3));
                            block.getDrops().clear();
                        }
                    }
                    cache.setDuration(System.currentTimeMillis());
                    cache.getGrenade().remove();
                }
            } else if (type == GrenadeType.FIRE) {
                if(cache.getGrenade().isRemoved()) {
                    if((System.currentTimeMillis() - cache.getDuration()) / 1000 > duration) {
                        for(Block block : cache.getBlocks()) {
                            block.setType(Material.AIR);
                        }
                        cache.getBlocks().clear();
                        played.remove(cache);
                    } else {
                        if(ticks % 15 != 0) {
                            continue;
                        }
                        for(Player player : cache.getNearbyToBlockPlayers()) {
                            if((cache.getPlayer() == player || Main.getGameManager().getTeam(cache.getGame(),
                                    cache.getPlayer()) != Main.getGameManager().getTeam(cache.getGame(), player)) && !cache.getGame().getSpectators().contains(player)) {
                                Main.getGameManager().damage(cache.getGame(), cache.getPlayer(), player, 1.5, "Fire");
                                player.setFireTicks(0);
                            }
                        }
                    }
                } else {
                    //sound

                    for(Block block : getBlocks(cache.getGrenade().getLocation().getBlock(), effectPower)) {
                        if(block.getType() == Material.AIR) {
                            if(!block.getRelative(BlockFace.DOWN).getType().isSolid()) {
                                continue;
                            }
                            cache.getBlocks().add(block);
                            block.setType(Material.FIRE);
                        } else {
                            if(!block.getType().isSolid()) {
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
            } else {
                cache.getGrenade().remove();
                played.remove(cache);
            }

        }
    }

    private boolean los(Location location, Player player) {
        Location clone = player.getEyeLocation().clone();
        Vector subtract = location.toVector().subtract(clone.toVector());
        boolean breakLos = false;
        for(int i = 0; i < Math.round(location.distance(clone)) + 1; i++) {
            clone.add(subtract.normalize());
            if(clone.getBlock().getType() != Material.AIR) {
                breakLos = true;
                break;
            }
        }
        return breakLos;
    }

    public void remove(SAGame game) {
        for(GrenadeCache cache : played) {
            if(cache.getGame() == game) {
                for(Block block : cache.getBlocks()) {
                    block.setType(Material.AIR);
                }
                cache.getGrenade().remove();
                played.remove(cache);
            }
        }
    }

    public void removePlayer(Player player) {
        for(GrenadeCache cache : played) {
            if(cache.getPlayer() == player) {
                for(Block block : cache.getBlocks()) {
                    block.setType(Material.AIR);
                }
                cache.getGrenade().remove();
                played.remove(cache);
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
