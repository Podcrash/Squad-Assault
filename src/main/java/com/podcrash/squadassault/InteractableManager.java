package com.podcrash.squadassault;

import com.podcrash.squadassault.gun.Interactable;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class InteractableManager {

    private final Map<Material, Interactable> interactables;

    public InteractableManager() {
        this.interactables = new HashMap<>();
    }

    public void addInteractable(Material material, Interactable interactable) {
        this.interactables.put(material, interactable);
    }

    public Interactable getInteractable(Material material) {
        return this.interactables.get(material);
    }
}
