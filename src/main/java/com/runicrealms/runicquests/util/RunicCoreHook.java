package com.runicrealms.runicquests.util;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.utilities.CurrencyUtil;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicquests.data.PlayerDataLoader;
import com.runicrealms.runicquests.data.QuestProfile;
import com.runicrealms.runicquests.quests.CraftingProfessionType;
import com.runicrealms.runicquests.quests.PlayerClassType;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.QuestRewards;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * This is meant to contain static methods that check with other RR plugins for player requirements
 */
public class RunicCoreHook {

    public static boolean isReqClassLv(Player player, int reqLevel) {
        int level = RunicCore.getCacheManager().getPlayerCaches().get(player).getClassLevel();
        return level >= reqLevel;
    }

    /**
     * @param player
     * @param quests
     * @return
     */
    public static boolean hasCompletedRequiredQuests(Player player, List<Integer> quests) {
        QuestProfile profile = PlayerDataLoader.getPlayerQuestData(player.getUniqueId());
        int completed = 0;
        for (Quest quest : profile.getQuests()) {
            if (quest.getQuestState().isCompleted()) {
                if (quests.contains(quest.getQuestID())) {
                    completed++;
                    if (completed == quests.size()) {
                        return true;
                    }
                }
            }
        }
        return completed == quests.size();
    }

    public static boolean isRequiredCraftingLevel(Player player, CraftingProfessionType profession, int level) {
        String playerProf = RunicCore.getCacheManager().getPlayerCaches().get(player).getProfName().toLowerCase();
        if (playerProf.equalsIgnoreCase("none") && profession == CraftingProfessionType.ANY) return false;
        int profLevel = RunicCore.getCacheManager().getPlayerCaches().get(player).getProfLevel();
        if (profession == CraftingProfessionType.ANY && profLevel >= level) return true;
        return profession != CraftingProfessionType.ANY && playerProf.equals(profession.getName()) && profLevel >= level;
    }

    public static boolean hasProfession(Player player, List<CraftingProfessionType> professions) {
        String playerProf = RunicCore.getCacheManager().getPlayerCaches().get(player).getProfName();
        if (playerProf.equalsIgnoreCase("none") && professions.contains(CraftingProfessionType.ANY)) return false;
        if (professions.contains(CraftingProfessionType.ANY)) return true;
        for (CraftingProfessionType profession : professions) {
            if (playerProf.equalsIgnoreCase(profession.getName())) return true;
        }
        return false;
    }

    /**
     * Gives player quest rewards. Uses RunicItemsAPI to properly stack items
     *
     * @param player  to receive rewards
     * @param rewards reward types
     */
    public static void giveRewards(Player player, QuestRewards rewards) {
        if (rewards.getExperienceReward() > 0) {
            PlayerLevelUtil.giveExperience(player, rewards.getExperienceReward());
        }
        if (rewards.getMoneyReward() > 0) {
            RunicItemsAPI.addItem(player.getInventory(), CurrencyUtil.goldCoin(rewards.getMoneyReward()), player.getLocation());
        }
    }

    public static boolean isRequiredClass(PlayerClassType classType, Player player) {
        String className = RunicCore.getCacheManager().getPlayerCaches().get(player).getClassName();
        return classType.getName().equalsIgnoreCase(className);
    }
}