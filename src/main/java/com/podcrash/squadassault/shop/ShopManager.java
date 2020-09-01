package com.podcrash.squadassault.shop;


import java.util.ArrayList;
import java.util.List;

public class ShopManager {

    private List<PlayerShopItem> shops;

    public ShopManager() {
        shops = new ArrayList<>();
    }

    public void addShop(PlayerShopItem shop) {
        shops.add(shop);
    }

    public List<PlayerShopItem> getShops() {
        return shops;
    }
}
