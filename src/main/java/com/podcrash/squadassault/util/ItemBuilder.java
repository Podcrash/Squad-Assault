package com.podcrash.squadassault.util;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Arrays;

public final class ItemBuilder {

    public static ItemStack create(Material material, int n, String name, String lore) {
        ItemStack itemStack = new ItemStack(material, n);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        if (lore != null) {
            itemMeta.setLore(Arrays.asList(lore.split("#")));
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack create(Material material, int n, String name, boolean unbreakable) {
        ItemStack itemStack = new ItemStack(material, n);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (unbreakable) {
            itemMeta.spigot().setUnbreakable(true);
        }
        itemMeta.setDisplayName(name);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack create(Material material, int n, byte b, String name) {
        ItemStack itemStack = new ItemStack(material, n, b);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack create(Material material, int n, byte b, String name, String... lore) {
        ItemStack itemStack = new ItemStack(material, n, b);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        itemMeta.setLore(Arrays.asList(lore));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack createItem(Material material, Color color, String s) {
        ItemStack itemStack = new ItemStack(material);
        LeatherArmorMeta itemMeta = (LeatherArmorMeta)itemStack.getItemMeta();
        itemMeta.setColor(color);
        itemStack.setItemMeta(itemMeta);
        ItemMeta itemMeta2 = itemStack.getItemMeta();
        itemMeta2.setDisplayName(s);
        itemStack.setItemMeta(itemMeta2);
        return itemStack;
    }

    public static ItemStack create(Material material, int n, short n2, String name, String lore) {
        ItemStack itemStack = new ItemStack(material, n, n2);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        itemMeta.setLore(Arrays.asList(lore.split("#")));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
