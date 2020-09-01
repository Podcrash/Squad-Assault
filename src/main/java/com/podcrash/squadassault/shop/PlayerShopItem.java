package com.podcrash.squadassault.shop;

import com.podcrash.squadassault.game.SATeam;
import org.bukkit.Material;

public class PlayerShopItem {

    private int shopSlot;
    private int price;
    private SATeam.Team team;
    private String name;
    private String lore;
    private String weaponName;
    private int hotbarSlot;
    private Material material;
    private ItemType type;
    private String itemName;

    public PlayerShopItem(String weaponName, String name, int slot, int price, String lore, SATeam.Team team) {
        this.weaponName = weaponName;
        this.name = name;
        this.shopSlot = slot;
        this.price = price;
        this.lore = lore;
        this.team = team;
        type = ItemType.GUN;
    }

    public PlayerShopItem(String weaponName, String name, int slot, int price, String lore) {
        this.name = name;
        this.shopSlot = slot;
        this.price = price;
        this.lore = lore;
        this.weaponName = weaponName;
        type = ItemType.GRENADE;
    }

    public PlayerShopItem(int slot, int slotPlace, String name, Material material, int price, String lore, SATeam.Team team, String itemName) {
        type = ItemType.ITEM;
        this.shopSlot = slot;
        this.name = name;
        this.price = price;
        this.lore = lore;
        this.itemName = itemName;
        this.team = team;
        this.hotbarSlot = slotPlace;
        this.material = material;
    }

    public int getShopSlot() {
        return shopSlot;
    }

    public int getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }

    public int getHotbarSlot() {
        return hotbarSlot;
    }

    public String getWeaponName() {
        return weaponName;
    }

    public String getLore() {
        return lore;
    }

    public Material getMaterial() {
        return material;
    }

    public ItemType getType() {
        return type;
    }

    public String getItemName() {
        return itemName;
    }

    public SATeam.Team getTeam() {
        return team;
    }
}
