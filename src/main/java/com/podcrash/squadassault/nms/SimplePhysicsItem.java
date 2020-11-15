package com.podcrash.squadassault.nms;

import com.podcrash.squadassault.util.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SimplePhysicsItem {

    private Item item;
    private boolean removed;

    public SimplePhysicsItem(final Player player, final ItemStack itemStack, final double n) {
        this.removed = false;
        final ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(Utils.randomString(15));
        itemStack.setItemMeta(itemMeta);
        (this.item = player.getWorld().dropItem(player.getEyeLocation(), itemStack)).setPickupDelay(Integer.MAX_VALUE);
        this.item.setVelocity(player.getEyeLocation().getDirection().multiply(n));
    }

    public void remove() {
        this.item.remove();
        this.removed = true;
    }

    public boolean isRemoved() {
        return this.removed;
    }

    public Location getLocation() {
        return this.item.getLocation();
    }
}
