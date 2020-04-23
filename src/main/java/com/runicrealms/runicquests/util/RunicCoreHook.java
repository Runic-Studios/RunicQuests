package com.runicrealms.runicquests.util;

import java.util.HashMap;
import java.util.List;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.utilities.CurrencyUtil;
import com.runicrealms.runicquests.data.PlayerDataLoader;
import com.runicrealms.runicquests.data.QuestProfile;
import com.runicrealms.runicquests.quests.Quest;
import org.bukkit.entity.Player;

import com.runicrealms.runicquests.quests.CraftingProfessionType;
import com.runicrealms.runicquests.quests.PlayerClassType;
import com.runicrealms.runicquests.quests.QuestRewards;
import org.bukkit.inventory.ItemStack;

public class RunicCoreHook {

	/*
	 * This is meant to contain static methods that check with other RR plugins for player requirements
	 */

	public static boolean isReqClassLv(Player player, int reqLevel) {
		int level = RunicCore.getCacheManager().getPlayerCache(player.getUniqueId()).getClassLevel();
		return level >= reqLevel;
	}

	public static boolean hasCompletedRequiredQuests(Player player, List<Integer> quests) {
		QuestProfile profile = PlayerDataLoader.getPlayerQuestData(player.getUniqueId());
		Integer completed = 0;
		for (Quest quest : profile.getQuests()) {
			if (quest.getQuestState().isCompleted()){
				if (quests.contains(quest.getQuestID())) {
					completed++;
					if (completed == quests.size()) {
						return true;
					}
				}
			}
		}
		if (completed == quests.size()) {
			return true;
		}
		return false;
	}

	public static boolean isRequiredCraftingLevel(Player player, CraftingProfessionType profession, int level) {
		String playerProf = RunicCore.getCacheManager().getPlayerCache(player.getUniqueId()).getProfName().toLowerCase();
		if (playerProf.equalsIgnoreCase("none") && profession == CraftingProfessionType.ANY) return false;
		int profLevel = RunicCore.getCacheManager().getPlayerCache(player.getUniqueId()).getProfLevel();
		if (profession == CraftingProfessionType.ANY && profLevel >= level) return true;
		return profession != CraftingProfessionType.ANY && playerProf.equals(profession.getName()) && profLevel >= level;
	}

	public static boolean hasProfession(Player player, List<CraftingProfessionType> professions) {
		String playerProf = RunicCore.getCacheManager().getPlayerCache(player.getUniqueId()).getProfName();
		if (playerProf.equalsIgnoreCase("none") && professions.contains(CraftingProfessionType.ANY)) return false;
		if (professions.contains(CraftingProfessionType.ANY)) return true;
		for (CraftingProfessionType profession : professions) {
			if (playerProf.equalsIgnoreCase(profession.getName())) return true;
		}
		return false;
	}

	public static void giveRewards(Player player, QuestRewards rewards) {
		// todo: quest points?
		if (rewards.getExperienceReward() > 0) {
			PlayerLevelUtil.giveExperience(player, rewards.getExperienceReward());
		}
		if (rewards.getMoneyReward() > 0) {
			HashMap<Integer, ItemStack> leftOvers = player.getInventory().addItem(CurrencyUtil.goldCoin(rewards.getMoneyReward()));
			for (ItemStack is : leftOvers.values()) {
				player.getWorld().dropItem(player.getLocation(), is); // inventory too full
			}
		}
	}

	public static boolean isRequiredClass(PlayerClassType classType, Player player) {
		String className = RunicCore.getCacheManager().getPlayerCache(player.getUniqueId()).getClassName().toLowerCase();
		return classType.getName().equals(className);
	}
}