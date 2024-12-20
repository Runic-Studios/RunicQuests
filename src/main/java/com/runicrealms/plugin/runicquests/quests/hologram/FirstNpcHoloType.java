package com.runicrealms.plugin.runicquests.quests.hologram;

import com.runicrealms.plugin.runicquests.util.StatusItemUtil;
import org.bukkit.inventory.ItemStack;

public enum FirstNpcHoloType {

    RED(StatusItemUtil.redStatusItem),
    YELLOW(StatusItemUtil.yellowStatusItem),
    GOLD(StatusItemUtil.goldStatusItem),
    BLUE(StatusItemUtil.blueStatusItem),
    GREEN(StatusItemUtil.greenStatusItem);

    private final ItemStack itemStack;

    FirstNpcHoloType(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

}
