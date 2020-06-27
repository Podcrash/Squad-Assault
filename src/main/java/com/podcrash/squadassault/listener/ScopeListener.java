package com.podcrash.squadassault.listener;

import com.podcrash.api.game.GameManager;
import com.podcrash.api.game.GameState;
import com.podcrash.squadassault.InteractableManager;
import com.podcrash.squadassault.gun.Interactable;
import com.podcrash.squadassault.gun.Scope;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

public class ScopeListener implements Listener {

    private final InteractableManager interactableManager;

    public ScopeListener(InteractableManager interactableManager) {
        this.interactableManager = interactableManager;
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent playerToggleSneakEvent) {
        // Boilerplate
        if (GameManager.getGame().getGameState() != GameState.STARTED)
            return;
        Material material = playerToggleSneakEvent.getPlayer().getItemInHand().getType();
        Interactable interactable = interactableManager.getInteractable(material);
        if (interactable == null)
            return;
        // Scope logic
        if (!(interactable instanceof Scope))
            return;
        if (playerToggleSneakEvent.isSneaking()) {
            playerToggleSneakEvent.getPlayer().getInventory().setHelmet(new ItemStack(Material.PUMPKIN));
        } else {
            playerToggleSneakEvent.getPlayer().getInventory().setHelmet(new ItemStack(Material.LEATHER_HELMET));
        }
    }
}
