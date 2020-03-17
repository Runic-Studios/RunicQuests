package com.runicrealms.runicquests.util;

import java.util.HashMap;
import java.util.List;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.utilities.CurrencyUtil;
import org.bukkit.entity.Player;

import com.runicrealms.runiccharacters.api.RunicCharactersApi;
import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.player.QuestProfile;
import com.runicrealms.runicquests.quests.CraftingProfessionType;
import com.runicrealms.runicquests.quests.PlayerClassType;
import com.runicrealms.runicquests.quests.QuestRewards;
import org.bukkit.inventory.ItemStack;

public class RunicCoreHook {

	/*
	 * This is meant to contain static methods that check with other RR plugins for player requirements
	 */

	public static boolean isRequiredLevel(Player player, int reqLevel) {
		int level = RunicCore.getCacheManager().getPlayerCache(player.getUniqueId()).getClassLevel();
		return level >= reqLevel;
	}

	public static boolean hasCompletedRequiredQuests(Player player, List<Integer> quests) {
		QuestProfile profile = Plugin.getQuestProfile(player.getUniqueId().toString());
		for (Integer questID : quests) {
			if (profile.getSavedData().getConfig().get(RunicCharactersApi.getCurrentCharacterSlot(player.getUniqueId())).contains(questID + "")) {
				if (!profile.getSavedData().getConfig().get(RunicCharactersApi.getCurrentCharacterSlot(player.getUniqueId())).getBoolean(questID + ".completed")) {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}

	public static boolean isRequiredCraftingLevel(Player player, CraftingProfessionType profession, int level) {
		String playerProf = RunicCore.getCacheManager().getPlayerCache(player.getUniqueId()).getProfName().toLowerCase();
		int profLevel = RunicCore.getCacheManager().getPlayerCache(player.getUniqueId()).getProfLevel();
		if (profession == CraftingProfessionType.ANY && profLevel >= level) return true;
		return profession != CraftingProfessionType.ANY && playerProf.equals(profession.getName()) && profLevel >= level;
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