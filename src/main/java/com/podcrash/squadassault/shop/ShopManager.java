package com.podcrash.squadassault.shop;


import java.util.ArrayList;
import java.util.List;

public class ShopManager {

    private List<PlayerShop> shops;

    public ShopManager() {
        shops = new ArrayList<>();
    }

    public void addShop(PlayerShop shop) {
        shops.add(shop);
    }

}
