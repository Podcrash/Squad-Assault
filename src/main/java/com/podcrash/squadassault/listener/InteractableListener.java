package com.podcrash.squadassault.listener;

import com.podcrash.api.game.GameManager;
import com.podcrash.api.game.GameState;
import com.podcrash.squadassault.InteractableManager;
import com.podcrash.squadassault.gun.Interactable;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class InteractableListener implements Listener {

    private final InteractableManager interactableManager;

    public InteractableListener(InteractableManager interactableManager) {
        this.interactableManager = interactableManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent playerInteractEvent) {
        if (GameManager.getGame().getGameState() != GameState.STARTED)
            return;
        if (playerInteractEvent.getMaterial() == Material.AIR)
            return;
        Material material = playerInteractEvent.getMaterial();
        Interactable interactable = this.interactableManager.getInteractable(material);
        if (interactable == null)
            return;
        interactable.onInteract(playerInteractEvent);
    }
}
