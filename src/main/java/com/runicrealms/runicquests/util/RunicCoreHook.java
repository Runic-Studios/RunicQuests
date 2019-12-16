package com.runicrealms.runicquests.util;

import java.util.List;

import org.bukkit.entity.Player;

import com.runicrealms.runiccharacters.api.RunicCharactersApi;
import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.player.QuestProfile;
import com.runicrealms.runicquests.quests.CraftingProfessionType;
import com.runicrealms.runicquests.quests.PlayerClassType;
import com.runicrealms.runicquests.quests.QuestRewards;

public class RunicCoreHook {

	/*
	 * This is meant to contain static methods that check with other RR plugins for player requirements
	 */
	
	public static boolean isRequiredLevel(Player player, int level) {
		return true; // TODO
	}
	
	public static boolean hasCompletedRequiredQuests(Player player, List<Integer> quests) {
		QuestProfile profile = Plugin.getQuestProfile(player.getUniqueId().toString());
		for (Integer questID : quests) {
			if (profile.getSavedData().getConfig().get(RunicCharactersApi.getCurrentCharacterSlot(player.getUniqueId()) + "").contains(questID + "")) {
				if (!profile.getSavedData().getConfig().get(RunicCharactersApi.getCurrentCharacterSlot(player.getUniqueId()) + "").getBoolean(questID + ".completed")) {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}
	
	public static boolean isRequiredCraftingLevel(Player player, CraftingProfessionType profession, int level) {
		return true; // TODO *please note that CraftingProfessionType also has an 'Any' value
	}
	
	public static void giveRewards(Player player, QuestRewards rewards) {
		// TODO *you do NOT have to execute the commands with QuestRewards, just give money, exp, and quest points
	}
	
	public static boolean isRequiredClass(PlayerClassType classType, Player player) {
		return true; // TODO
	}
	
}
