package com.podcrash.squadassault.nms;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class BossBar {

    private String title;
    private Map<Player, EntityWither> withers;

    public BossBar(String title) {
        this.title = title;
        withers = new HashMap<>();
    }

    public void addPlayer(Player player) {
        EntityWither entityWither = new EntityWither(((CraftWorld)player.getWorld()).getHandle());
        Location witherLocation = getWitherLocation(player.getLocation());
        entityWither.setCustomName(title);
        entityWither.setInvisible(true);
        entityWither.setLocation(witherLocation.getX(), witherLocation.getY(),  witherLocation.getZ(), 0,0);
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(new PacketPlayOutSpawnEntityLiving(entityWither));
        this.withers.put(player, entityWither);
    }

    public void removePlayer(Player player) {
        EntityWither entityWither = this.withers.remove(player);
        if(entityWither == null)
            return;
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy((entityWither.getId())));
    }

    public void setTitle(String title) {
        for(Map.Entry<Player, EntityWither> entry : withers.entrySet()) {
            //technically faster than keySet() with lots of get() calls because of HashMap impl
            EntityWither entityWither = entry.getValue();
            entityWither.setCustomName(title);
            ((CraftPlayer)entry.getKey()).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityMetadata(entityWither.getId(), entityWither.getDataWatcher(), true));
        }
    }

    public void setProgress(final double n) {
        for (final Map.Entry<Player, EntityWither> entry : this.withers.entrySet()) {
            final EntityWither entityWither = entry.getValue();
            entityWither.setHealth((float)(n * entityWither.getMaxHealth()));
            ((CraftPlayer)entry.getKey()).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityMetadata(entityWither.getId(), entityWither.getDataWatcher(), true));
        }
    }

    public void update() {
        for (final Map.Entry<Player, EntityWither> entry : this.withers.entrySet()) {
            final EntityWither entityWither = entry.getValue();
            final Location witherLocation = getWitherLocation(entry.getKey().getLocation());
            entityWither.setLocation(witherLocation.getX(), witherLocation.getY(), witherLocation.getZ(), 0.0f, 0.0f);
            ((CraftPlayer)entry.getKey()).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityTeleport(entityWither));
        }
    }

    public Location getWitherLocation(Location location) {
        return location.add(location.getDirection().multiply(60));
    }

}
