package com.runicrealms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.runicrealms.config.ConfigLoader;
import com.runicrealms.event.MythicMobsKillEvent;
import com.runicrealms.event.NpcClickEvent;
import com.runicrealms.event.PlayerBreakBlockEvent;
import com.runicrealms.event.PlayerJoinQuitEvent;
import com.runicrealms.event.PlayerTripwireEvent;
import com.runicrealms.player.QuestProfile;
import com.runicrealms.quests.Quest;
import com.runicrealms.quests.QuestObjective;
import com.runicrealms.task.TaskQueue;

public class Plugin extends JavaPlugin {

	private static Plugin plugin;
	private static List<QuestProfile> questProfiles = new ArrayList<QuestProfile>();
	private static volatile HashMap<Integer, TaskQueue> npcTaskQueues = new HashMap<Integer, TaskQueue>();
	private static Map<String, List<Integer>> cooldowns = new HashMap<String, List<Integer>>();
	private static Integer nextId = 0;

	public static String WORLD_NAME;
	public static double NPC_MESSAGE_DELAY;
	public static boolean CACHE_PLAYER_DATA;

	@Override
	public void onEnable() {
		plugin = this;
		ConfigLoader.initDirs();
		ConfigLoader.loadMainConfig();
		WORLD_NAME = ConfigLoader.getMainConfig().getString("world-name");
		NPC_MESSAGE_DELAY = ConfigLoader.getMainConfig().getDouble("npc-message-delay");
		CACHE_PLAYER_DATA = ConfigLoader.getMainConfig().getBoolean("cache-player-data");
		this.getServer().getPluginManager().registerEvents(new MythicMobsKillEvent(), this);
		this.getServer().getPluginManager().registerEvents(new NpcClickEvent(), this);
		this.getServer().getPluginManager().registerEvents(new PlayerBreakBlockEvent(), this);
		this.getServer().getPluginManager().registerEvents(new PlayerJoinQuitEvent(), this);
		this.getServer().getPluginManager().registerEvents(new PlayerTripwireEvent(), this);
	}

	public static Plugin getInstance() {
		return plugin;
	}
	
	public static HashMap<Integer, TaskQueue> getNpcTaskQueues() {
		return npcTaskQueues;
	}
	
	public static Map<String, List<Integer>> getQuestCooldowns() {
		return cooldowns;
	}

	public static List<QuestProfile> getQuestProfiles() {
		return questProfiles;
	}

	public static QuestProfile getQuestProfile(String uuid) {
		for (QuestProfile profile : questProfiles) {
			if (profile.getPlayerUUID().equalsIgnoreCase(uuid)) {
				return profile;
			}
		}
		return null;
	}

	public static Integer getNextId() {
		nextId++;
		return nextId - 1;
	}

	public static void removeItem(Inventory inventory, String name, String type, int amount) {
		int leftToRemove = amount;
		for (ItemStack item : inventory.getContents()) {
			if (item != null) {
				if (item.getType().name().equalsIgnoreCase(type) &&
						getItemName(item).equalsIgnoreCase(ChatColor.stripColor(name))) {
					inventory.remove(item);
					leftToRemove -= item.getAmount();
					if (leftToRemove <= 0) {
						if (leftToRemove < 0) {
							inventory.addItem(item.asQuantity(leftToRemove * -1));
						}
						return;
					}
				}
			}
		}
	}

	public static String getItemName(ItemStack item) {
		if (item.getItemMeta().getDisplayName() == "" || item.getItemMeta().getDisplayName() == null) {
			return ChatColor.stripColor(item.getType().toString());
		} else {
			return ChatColor.stripColor(item.getItemMeta().getDisplayName());
		}
	}
	
	public static boolean allObjectivesComplete(Quest quest) {
		for (QuestObjective objective : quest.getObjectives()) {
			if (objective.isCompleted() == false) {
				return false;
			}
		}
		return true;
	}

}
