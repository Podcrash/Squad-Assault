package com.podcrash.squadassault.nms;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class PhysicsItem extends EntityArmorStand {

    private Hitbox hitbox;
    private boolean intersects;

    public PhysicsItem(EntityPlayer player, ItemStack item, double n) {
        super(player.world, player.locX, player.locY + 0.3, player.locZ);
        intersects = false;
        yaw = player.yaw;
        Location location = player.getBukkitEntity().getLocation();
        location.setYaw(location.getYaw() - 90);
        location.setPitch(0);
        setArms(true);
        setEquipment(0, item);
        setRightArmPose(new Vector3f(player.pitch - 90, 0,0));
        setInvisible(true);
        setGravity(false);
        setBasePlate(false);
        Location location2 = new Location(world.getWorld(), locX,locY + getHeadHeight() - 0.4, locZ);
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
                locX += n;
                locY += n2;
                locZ += n3;
                updateHitbox();
                List<BlockFace> intersects = intersects(hitbox);
                if(intersects.size() > 0) {
                    if (intersects.contains(BlockFace.UP) || intersects.contains(BlockFace.DOWN)) {
                        motY = -(motY * 0.5);
                        motX *= 0.85;
                        motZ *= 0.85;
                        if (Math.abs(motY) <= 0.15) {
                            motY = 0.0;
                        }
                    }
                    if (intersects.contains(BlockFace.WEST) || intersects.contains(BlockFace.EAST)) {
                        motX = -(motX * 0.4);
                        motZ *= 0.4;
                    }
                    if (intersects.contains(BlockFace.SOUTH) || intersects.contains(BlockFace.NORTH)) {
                        motZ = -(motZ * 0.4);
                        motX *= 0.4;
                    }
                    if (Math.abs(motX) <= 0.07) {
                        motX = 0.0;
                    }
                    if (Math.abs(motZ) <= 0.07) {
                        motZ = 0.0;
                    }
                    locX += motX / 5.0;
                    locY += motY / 5.0;
                    locZ += motZ / 5.0;
                    break;
                } else {
                    i++;
                }
            }
            motX *= 0.99;
            if (intersects(hitbox, getLocation().add(0.0, -0.2, 0.0).getBlock()).size() == 0) {
                motY -= 0.05;
            }
            motZ *= 0.99;
            updateHitbox();
            final PacketPlayOutEntityTeleport packetPlayOutEntityTeleport = new PacketPlayOutEntityTeleport(this);
            for (EntityHuman entityHuman : world.players) {
                ((EntityPlayer) entityHuman).playerConnection.sendPacket(packetPlayOutEntityTeleport);
            }
        }
    }

    private void updateHitbox() {
        Location location = new Location(world.getWorld(), locX, locY + getHeadHeight() - 0.4, locZ);
        location.setYaw(yaw + 90.0f);
        location.add(location.getDirection().multiply(0.45));
        location.setYaw(yaw);
        location.setPitch(rightArmPose.getX() + 90.0f);
        location.add(location.getDirection().multiply(0.7));
        location.setPitch(location.getPitch() - 90.0f);
        location.add(location.getDirection().multiply(0.2));
        hitbox = new Hitbox(location.clone().add(-0.1, -0.15, -0.1).toVector(), location.clone().add(0.1, 0.1,
                0.1).toVector());
    }

    private List<BlockFace> intersects(Hitbox hitbox) {
        List<BlockFace> list = new ArrayList<>();
        for(int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; ++j) {
                for (int k = -1; k <= 1; ++k) {
                    list.addAll(intersects(hitbox,
                            getLocation().add(i, k, j).getBlock()));
                }
            }
        }
        return list;
    }

    private List<BlockFace> intersects(Hitbox hitbox, Block block) {
        final ArrayList<BlockFace> list = new ArrayList<>();
        final Hitbox grow = new Hitbox(block).grow(0.05, 0.05, 0.05);
        if (block.getType().isSolid() && hitbox.intersects(grow)) {
            BlockFace[] array;
            for (int length = (array = new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN }).length, i = 0; i < length; ++i) {
                final BlockFace blockFace = array[i];
                if (block.getType() == Material.STEP && blockFace == BlockFace.UP) {
                    if (new Hitbox(block).intersects(hitbox)) {
                        list.add(BlockFace.UP);
                    }
                }
                else if (new Hitbox(block.getRelative(blockFace)).intersects(hitbox)) {
                    list.add(blockFace);
                }
            }
        }
        return list;
    }


    public void remove() {
        die();
    }

    public Location getLocation() {
        return new Location(world.getWorld(), (hitbox.getMin().getX() + hitbox.getMax().getX()) / 2.0, (hitbox.getMin().getY() + hitbox.getMax().getY()) / 2.0, (hitbox.getMin().getZ() + hitbox.getMax().getZ()) / 2.0);
    }

    public boolean isRemoved() {
        return !isAlive();
    }

}
