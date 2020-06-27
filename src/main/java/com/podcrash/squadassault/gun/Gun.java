package com.podcrash.squadassault.gun;

import com.podcrash.api.game.GameManager;
import com.podcrash.api.game.GameState;
import com.podcrash.squadassault.InteractableManager;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;

public abstract class Gun implements Interactable {

    // Might just set up dependency injection for this, I use it everywhere
    private InteractableManager interactableManager;

    private int magazineSize;
    private int reserveMagazines;
    private double damage;
    private double recoil;
    private double damageDropoff;

    protected Gun(int magazineSize, int reserveMagazines, double damage, double recoil, double damageDropoff) {
        this.magazineSize = magazineSize;
        this.reserveMagazines = reserveMagazines;
        this.damage = damage;
        this.recoil = recoil;
        this.damageDropoff = damageDropoff;
    }

    @Override
    public void onInteract(PlayerInteractEvent playerInteractEvent) {
        // Boilerplate
        if (GameManager.getGame().getGameState() != GameState.STARTED)
            return;
        if (playerInteractEvent.getMaterial() == Material.AIR)
            return;
        // Gun logic
    }
}
