package com.podcrash.squadassault.shop;

import com.podcrash.squadassault.game.SATeam;
import org.bukkit.Material;

public class PlayerShopItem {

    private int slot;
    private int price;
    private SATeam.Team team;
    private String name;
    private String lore;
    private String weaponName;
    private int slotPlace;
    private Material material;
    private ItemType type;
    private String itemName;

    public PlayerShopItem(String weaponName, String name, int slot, int price, String lore, SATeam.Team team) {
        this.weaponName = weaponName;
        this.name = name;
        this.slot = slot;
        this.price = price;
        this.lore = lore;
        this.team = team;
        type = ItemType.GUN;
    }

    public PlayerShopItem(String weaponName, String name, int slot, int price, String lore) {
        this.name = name;
        this.slot = slot;
        this.price = price;
        this.lore = lore;
        this.weaponName = weaponName;
        type = ItemType.GRENADE;
    }

    public PlayerShopItem(int slot, int slotPlace, String name, Material material, int price, String lore, SATeam.Team team, String itemName) {
        type = ItemType.ITEM;
        this.slot = slot;
        this.name = name;
        this.price = price;
        this.lore = lore;
        this.itemName = itemName;
        this.team = team;
        this.slotPlace = slotPlace;
        this.material = material;
    }

    public int getSlot() {
        return slot;
    }

    public int getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }

    public int getSlotPlace() {
        return slotPlace;
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
