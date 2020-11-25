package com.podcrash.squadassault.nms;

import com.podcrash.squadassault.util.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SimplePhysicsItem implements PhysicsItem {

    private final Item item;
    private boolean removed;

    public SimplePhysicsItem(final Player player, final ItemStack itemStack, final double n) {
        this.removed = false;
        final ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(Utils.randomString(15));
        itemStack.setItemMeta(itemMeta);
        (this.item = player.getWorld().dropItem(player.getEyeLocation(), itemStack)).setPickupDelay(Integer.MAX_VALUE);
        this.item.setVelocity(player.getEyeLocation().getDirection().multiply(n));
    }

    @Override
    public void remove() {
        this.item.remove();
        this.removed = true;
    }

    @Override
    public boolean isRemoved() {
        return this.removed;
    }

    @Override
    public Location getLocation() {
        return this.item.getLocation();
    }

    @Override
    public Entity getEntity() {
        return item;
    }
}
