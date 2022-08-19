package com.runicrealms.runicquests.util;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.model.CharacterField;
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
        int level = player.getLevel();
        return level >= reqLevel;
    }

    /**
     * Checks to see if the player has completed the required quests to accept a new quest
     *
     * @param player to check
     * @param quests a list of quest ids required
     * @return true if the player can start the quest
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

    /**
     * Check whether the player has the required crafting level for any given quest requirement
     *
     * @param player     to check
     * @param profession the crafting profession of the player
     * @param level      the required level for crafting
     * @return true if the player has met the requirement
     */
    public static boolean isRequiredCraftingLevel(Player player, CraftingProfessionType profession, int level) {
        String playerProf = RunicCoreAPI.getRedisCharacterValue(player.getUniqueId(), CharacterField.PROF_NAME.getField(), RunicCoreAPI.getCharacterSlot(player.getUniqueId()));
        if (playerProf.equalsIgnoreCase("none") && profession == CraftingProfessionType.ANY) return false;
        int profLevel = Integer.parseInt(RunicCoreAPI.getRedisCharacterValue
                (
                        player.getUniqueId(),
                        CharacterField.PROF_LEVEL.getField(),
                        RunicCoreAPI.getCharacterSlot(player.getUniqueId())
                ));
        if (profession == CraftingProfessionType.ANY && profLevel >= level) return true;
        return profession != CraftingProfessionType.ANY && playerProf.equals(profession.getName()) && profLevel >= level;
    }

    /**
     * Check whether the player has met the profession requirement for the given quest
     *
     * @param player      to check
     * @param professions a list of professions that the player must be. can be multiple
     * @return true if the requirement is met
     */
    public static boolean hasProfession(Player player, List<CraftingProfessionType> professions) {
        String playerProf = RunicCoreAPI.getRedisCharacterValue
                (
                        player.getUniqueId(),
                        CharacterField.PROF_NAME.getField(),
                        RunicCoreAPI.getCharacterSlot(player.getUniqueId())
                );
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

    /**
     * Check whether the player has met the class requirement for the given class
     *
     * @param classType the class required
     * @param player    the player to check
     * @return true if the player is the required class
     */
    public static boolean isRequiredClass(PlayerClassType classType, Player player) {
        String className = RunicCoreAPI.getPlayerClass(player);
        return classType.getName().equalsIgnoreCase(className);
    }
}