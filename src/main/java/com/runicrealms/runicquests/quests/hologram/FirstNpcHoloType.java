package com.runicrealms.runicquests.quests.hologram;

import com.runicrealms.runicquests.util.StatusItemUtil;
import org.bukkit.inventory.ItemStack;

public enum FirstNpcHoloType {

    RED(StatusItemUtil.redStatusItem()),
    YELLOW(StatusItemUtil.yellowStatusItem()),
    GOLD(StatusItemUtil.orangeStatusItem()),
    BLUE(StatusItemUtil.blueStatusItem()),
    GREEN(StatusItemUtil.greenStatusItem());

    private final ItemStack itemStack;

    FirstNpcHoloType(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

}
