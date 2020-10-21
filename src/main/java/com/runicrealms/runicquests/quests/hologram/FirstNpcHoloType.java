package com.runicrealms.runicquests.quests.hologram;

import org.bukkit.Material;

public enum FirstNpcHoloType {

    RED(Material.BARRIER),
    YELLOW(Material.YELLOW_DYE),
    GOLD(Material.ORANGE_DYE),
    BLUE(Material.BLUE_DYE),
    GREEN(Material.GREEN_DYE);

    private final Material material;

    FirstNpcHoloType(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }

}
