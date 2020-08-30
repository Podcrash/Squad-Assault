package com.podcrash.squadassault.nms;

import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.ItemStack;

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
    }

    public void remove() {
        die();
    }

    public boolean isRemoved() {
        return !isAlive();
    }

}
