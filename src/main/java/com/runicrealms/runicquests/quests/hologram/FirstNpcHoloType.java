package com.runicrealms.runicquests.quests.hologram;

import com.runicrealms.runicquests.util.QuestionMarkUtil;
import org.bukkit.inventory.ItemStack;

public enum FirstNpcHoloType {

    RED(QuestionMarkUtil.redQuestionMark()),
    YELLOW(QuestionMarkUtil.yellowQuestionMark()),
    GOLD(QuestionMarkUtil.goldQuestionMark()),
    BLUE(QuestionMarkUtil.blueQuestionMark()),
    GREEN(QuestionMarkUtil.greenQuestionMark());

    private final ItemStack itemStack;

    FirstNpcHoloType(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

}
