package com.runicrealms.runicquests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.runicrealms.runicquests.command.QuestsCommand;
import com.runicrealms.runicquests.config.ConfigLoader;
import com.runicrealms.runicquests.event.MythicMobsKillEvent;
import com.runicrealms.runicquests.event.NpcClickEvent;
import com.runicrealms.runicquests.event.PlayerBreakBlockEvent;
import com.runicrealms.runicquests.event.PlayerJoinQuitEvent;
import com.runicrealms.runicquests.event.PlayerTripwireEvent;
import com.runicrealms.runicquests.player.QuestProfile;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.QuestItem;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import com.runicrealms.runicquests.task.TaskQueue;

public class Plugin extends JavaPlugin {

	private static Plugin plugin; // Used for getInstance()
	private static List<QuestProfile> questProfiles = new ArrayList<QuestProfile>(); // List of player quest profiles
	private static volatile HashMap<Integer, TaskQueue> npcTaskQueues = new HashMap<Integer, TaskQueue>(); // List of NPC task queues
	private static Map<String, List<Integer>> cooldowns = new HashMap<String, List<Integer>>(); // List of quest cooldowns
	private static Integer nextId = 0; // This is used to give each NPC a new unique ID.

	public static double NPC_MESSAGE_DELAY; // Config value
	public static boolean CACHE_PLAYER_DATA; // Config value

	@Override
	public void onEnable() {
		plugin = this; // Used for getInstance()
		ConfigLoader.initDirs(); // Initialize directories that might not exist
		ConfigLoader.loadMainConfig(); // Intitialze the main config file if it doesn't exist
		NPC_MESSAGE_DELAY = ConfigLoader.getMainConfig().getDouble("npc-message-delay"); // Get the config value
		CACHE_PLAYER_DATA = ConfigLoader.getMainConfig().getBoolean("cache-player-data"); // Get the config value
		this.getServer().getPluginManager().registerEvents(new MythicMobsKillEvent(), this); // Register events
		this.getServer().getPluginManager().registerEvents(new NpcClickEvent(), this);
		this.getServer().getPluginManager().registerEvents(new PlayerBreakBlockEvent(), this);
		this.getServer().getPluginManager().registerEvents(new PlayerJoinQuitEvent(), this);
		this.getServer().getPluginManager().registerEvents(new PlayerTripwireEvent(), this);
		for (Player player : Bukkit.getOnlinePlayers()) { // Loop through online players (fixes bug with /reload)
			PlayerJoinQuitEvent.runJoinEvent(player); // Read PlayerJoinQuitEvent.runJoinEvent
		}
		QuestsCommand commandExecutor = new QuestsCommand(); // Register the /quests command
		String[] commands = new String[] {"quests", "quest", "objectives", "objective"};
		for (int i = 0; i < commands.length; i++) {
			PluginCommand pluginCommand = this.getCommand(commands[i]);
			pluginCommand.setExecutor(commandExecutor);
		}
	}

	public static Plugin getInstance() { // Get the plugin instance
		return plugin;
	}

	public static HashMap<Integer, TaskQueue> getNpcTaskQueues() { // Get the NPC task queues
		return npcTaskQueues;
	}

	public static Map<String, List<Integer>> getQuestCooldowns() { // Get the quest cooldowns
		return cooldowns;
	}

	public static List<QuestProfile> getQuestProfiles() { // Get the player quest profiles
		return questProfiles;
	}

	public static QuestProfile getQuestProfile(String uuid) { // Get a quest profile by player UUID
		for (QuestProfile profile : questProfiles) {
			if (profile.getPlayerUUID().equalsIgnoreCase(uuid)) {
				return profile;
			}
		}
		return null;
	}

	public static Integer getNextId() { // Get a new unique ID that can be used for NPCs
		nextId++;
		return nextId - 1;
	}

	public static void removeItem(Inventory inventory, String name, String type, int amount) { // Remove an item from a player's inventory
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

	public static String getItemName(ItemStack item) { // Get the name of an ItemStack
		if (item.getItemMeta().getDisplayName() == "" || item.getItemMeta().getDisplayName() == null) {
			return ChatColor.stripColor(item.getType().toString());
		} else {
			return ChatColor.stripColor(item.getItemMeta().getDisplayName());
		}
	}

	public static boolean allObjectivesComplete(Quest quest) { // Checks that all the objectives in a quest have been completed
		for (QuestObjective objective : quest.getObjectives()) {
			if (objective.isCompleted() == false) {
				return false;
			}
		}
		return true;
	}

	public static boolean hasQuestItems(QuestObjective objective, Player player) { // Checks that a player has the required quest items
		int aquiredQuestItems = 0;
		for (QuestItem questItem : objective.getQuestItems()) {
			int amount = 0;
			for (ItemStack item : player.getInventory().getContents()) {
				if (item != null) {
					if (Plugin.getItemName(item).equalsIgnoreCase(ChatColor.stripColor(questItem.getItemName())) &&
							item.getType().name().equalsIgnoreCase(questItem.getItemType())) {
						amount += item.getAmount();
						if (amount >= questItem.getAmount()) {
							aquiredQuestItems++;
							break;
						}
					}
				}
			}
		}
		return aquiredQuestItems == objective.getQuestItems().size();
	}

	public static String parseMessage(String msg, String playerName) { // Parse an NPC message and replace %player% with player name, and run commands
		String outputString = msg.replaceAll("%player%", playerName);
		String command = null;
		boolean lastCharSlash = false;
		String[] chars = msg.split("(?!^)");
		for (int i = 0; i < chars.length; i++) {
			String character = chars[i];
			if (character.equalsIgnoreCase("/")) {
				if (!lastCharSlash) {
					lastCharSlash = true;
				} else {
					command = outputString.substring(i, outputString.length());
					outputString = outputString.substring(0, i - 2);
					break;
				}
			} else {
				lastCharSlash = false;
			}
		}
		if (command != null) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
		}
		return outputString;
	}

}
