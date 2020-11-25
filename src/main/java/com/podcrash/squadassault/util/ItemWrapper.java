package com.podcrash.squadassault.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * this is a basic wrapper class for ItemStacks without having to worry about the amount of the item. the reason this
 * is helpful is because the stack count is used for ammo, so this avoids that
 */
public class ItemWrapper {

    private String name;
    private byte data;
    private Material material;

    public ItemWrapper(ItemStack itemStack) {
        this.material = itemStack.getType();
        this.data = itemStack.getData().getData();
        this.name = itemStack.getItemMeta().getDisplayName();
    }

    public ItemWrapper(Material material, byte data, String name) {
        this.material = material;
        this.data = data;
        this.name = name;
    }

    public boolean equals(ItemStack itemStack) {
        return itemStack != null && itemStack.getItemMeta() != null && itemStack.getItemMeta().getDisplayName() != null && name != null && material != null &&
                itemStack.getType() == material && itemStack.getItemMeta().getDisplayName().contains(name);
    }

    public Material getType() {
        return this.material;
    }

    public String getName() {
        return this.name;
    }

    public byte getData() {
        return this.data;
    }


}
