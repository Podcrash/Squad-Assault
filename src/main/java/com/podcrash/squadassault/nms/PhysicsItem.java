package com.podcrash.squadassault.nms;

import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.ItemStack;

public class PhysicsItem extends EntityArmorStand {

    //todo

    public PhysicsItem(EntityPlayer player, ItemStack item, double n) {
        super(player.world, player.locX, player.locY + 0.3, player.locZ);
    }

    public void remove() {
        die();
    }

    public boolean isRemoved() {
        return !isAlive();
    }

}
