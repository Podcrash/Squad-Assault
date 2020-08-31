package com.podcrash.squadassault.nms;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class PhysicsItem extends EntityArmorStand {

    private final double airFriction = 0.99;
    private final double gravity = 0.05;
    private final double intersectionSlow = 0.4;
    private final double vertIntersectionSlow = 0.5;
    //todo maybe config?
    private Hitbox hitbox;
    private boolean intersects;
    private EntityPlayer player;

    public PhysicsItem(EntityPlayer player, ItemStack item, double n) {
        super(player.world, player.locX, player.locY + 0.3, player.locZ);
        this.player = player;
        intersects = false;
        this.yaw = player.yaw;
        Location location = player.getBukkitEntity().getLocation();
        location.setYaw(location.getYaw() - 90);
        location.setPitch(0);
        setArms(true);
        setEquipment(0, item);
        setRightArmPose(new Vector3f(player.pitch - 90, 0,0));
        setInvisible(true);
        setGravity(false);
        setBasePlate(false);
        Location location2 = new Location((World)world.getWorld(), locX,locY + getHeadHeight() - 0.4, locZ);
        location2.setYaw(yaw + 90);
        location2.add(location2.getDirection().multiply(0.45));
        location2.setYaw(yaw);
        location2.setPitch(rightArmPose.getX() + 90.0f);
        location2.add(location2.getDirection().multiply(0.7));
        location2.setPitch(location2.getPitch() - 90.0f);
        location2.add(location2.getDirection().multiply(0.2));
        hitbox = new Hitbox(location2.clone().add(-0.1, -0.15, -0.1).toVector(),
                location2.clone().add(0.1, 0.1, 0.1).toVector());
        Vector direction = player.getBukkitEntity().getEyeLocation().getDirection();
        direction.add(new Vector(player.motX, player.motY, player.motZ).multiply(0.3));
        direction.multiply(1.3);
        motX = direction.getX() * n;
        motY = direction.getY() * n;
        motZ = direction.getZ() * n;
        a(hitbox.toNmsHitbox());
        player.world.addEntity(this);
    }

    @Override
    public void t_() {
        if(motX == 0 && motY == 0 && motZ == 0) {
            if(!intersects) {
                intersects = true;
                PacketPlayOutEntityDestroy packetPlayOutEntityDestroy = new PacketPlayOutEntityDestroy(getId());
                PacketPlayOutSpawnEntity packetPlayOutSpawnEntity = new PacketPlayOutSpawnEntity(this, 78);
                PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(getId(), datawatcher, true);
                PacketPlayOutEntityEquipment packetPlayOutEntityEquipment = new PacketPlayOutEntityEquipment(getId(), 0, getEquipment(0));
                for(EntityHuman human : world.players) {
                    ((EntityPlayer)human).playerConnection.sendPacket(packetPlayOutEntityDestroy);
                    ((EntityPlayer)human).playerConnection.sendPacket(packetPlayOutSpawnEntity);
                    ((EntityPlayer)human).playerConnection.sendPacket(packetPlayOutEntityMetadata);
                    ((EntityPlayer)human).playerConnection.sendPacket(packetPlayOutEntityEquipment);
                }

            }
        } else {
            lastX = locX;
            lastY = locY;
            lastZ = locZ;
            double n = motX / 30.0;
            double n2 = motY / 30.0;
            double n3 = motZ / 30.0;
            int i = 0;
            while(i < 30) {
                this.locX += n;
                this.locY += n2;
                this.locZ += n3;

            }
        }
    }

    public void remove() {
        die();
    }

    public boolean isRemoved() {
        return !isAlive();
    }

}
