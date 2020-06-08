package com.runicrealms.runicquests;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import com.runicrealms.plugin.character.api.CharacterApi;
import com.runicrealms.runicquests.command.CompleteQuestCommand;
import com.runicrealms.runicquests.command.ResetQuestsCommand;
import com.runicrealms.runicquests.data.PlayerDataLoader;
import com.runicrealms.runicquests.data.QuestProfile;
import com.runicrealms.runicquests.event.*;
import com.runicrealms.runicquests.event.custom.RightClickNpcHandler;
import com.runicrealms.runicquests.listeners.JournalListener;
import com.runicrealms.runicquests.quests.FirstNpcState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.runicrealms.runicquests.command.QuestsCommand;
import com.runicrealms.runicquests.config.ConfigLoader;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.QuestItem;
import com.runicrealms.runicquests.quests.QuestObjectiveType;
import com.runicrealms.runicquests.quests.location.LocationToReach;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveLocation;
import com.runicrealms.runicquests.task.TaskQueue;

public class Plugin extends JavaPlugin {

	private static Plugin plugin; // Used for getInstance()
	private static volatile HashMap<Long, TaskQueue> npcTaskQueues = new HashMap<Long, TaskQueue>(); // List of NPC task queues
	private static Map<UUID, Map<Integer, Set<Integer>>> cooldowns = new HashMap<UUID, Map<Integer, Set<Integer>>>(); // List of quest cooldowns
	private static Long nextId = Long.MIN_VALUE; // This is used to give each NPC a new unique ID.
	/*
	 * This Map is meant to help with performance issues with checking the players location. It will just indicate
	 * when a player has a location objective on one of their quests
	 */
	private static volatile Map<Player, Map<Integer, LocationToReach>> cachedLocations = new HashMap<Player, Map<Integer, LocationToReach>>();

	public static double NPC_MESSAGE_DELAY; // Config value

	@Override
	public void onEnable() {
		plugin = this; // Used for getInstance()
		ConfigLoader.initDirs(); // Initialize directories that might not exist
		ConfigLoader.loadMainConfig(); // Initialize the main config file if it doesn't exist
		NPC_MESSAGE_DELAY = ConfigLoader.getMainConfig().getDouble("npc-message-delay"); // Get the config value
		this.getServer().getPluginManager().registerEvents(new EventKillMythicMob(), this); // Register events
		this.getServer().getPluginManager().registerEvents(new EventClickNpc(), this);
		this.getServer().getPluginManager().registerEvents(new EventBreakBlock(), this);
		this.getServer().getPluginManager().registerEvents(new EventPlayerJoinQuit(), this);
		this.getServer().getPluginManager().registerEvents(new EventPlayerLocation(), this);
		this.getServer().getPluginManager().registerEvents(new JournalListener(), this);
		this.getServer().getPluginManager().registerEvents(new EventInventory(), this);
		this.getServer().getPluginManager().registerEvents(new RightClickNpcHandler(), this);
		for (Player player : Bukkit.getOnlinePlayers()) { // Loop through online players (fixes bug with /reload)
			if (CharacterApi.getCurrentCharacterSlot(player) != null) {
				EventPlayerJoinQuit.runJoinEvent(player, CharacterApi.getCurrentCharacterSlot(player)); // Read PlayerJoinQuitEvent.runJoinEvent
			}
		}
		EventPlayerJoinQuit.displayQuestionMarks();
		registerCommand(new CompleteQuestCommand(), "completequest", "questcomplete", "cq", "qc");
		registerCommand(new QuestsCommand(), "quests", "quest", "objectives", "objective");
		registerCommand(new ResetQuestsCommand(), "resetquests", "questsreset", "resetquest", "questreset", "rq", "qr");
		for (Player player : Bukkit.getOnlinePlayers()) {
			updatePlayerCachedLocations(player);
		}
		registerMoveTask();
	}

	private static void registerCommand(CommandExecutor executor, String... aliases) {
		for (int i = 0; i < aliases.length; i++) {
			PluginCommand pluginCommand = getInstance().getCommand(aliases[i]);
			pluginCommand.setExecutor(executor);
		}
	}

	public static Plugin getInstance() { // Get the plugin instance
		return plugin;
	}

	private static void registerMoveTask() { // Schedule a task that will run for the cached location objectives
		Bukkit.getScheduler().scheduleSyncRepeatingTask(getInstance(), new Runnable() {
			@Override
			public void run() {
				for (Entry<Player, Map<Integer, LocationToReach>> entry : cachedLocations.entrySet()) {
					for (Entry<Integer, LocationToReach> questLocationToReach : entry.getValue().entrySet()) {
						if (questLocationToReach.getValue().hasReachedLocation(entry.getKey())) {
							EventPlayerLocation.playerCompleteLocationObjective(entry.getKey(), questLocationToReach.getKey());
							updatePlayerCachedLocations(entry.getKey());
							break;
						}
					}
				}
			}
		}, 10L, 10L);
	}

	public static void updatePlayerCachedLocations(Player player) { // Updates the cached location objectives for a player
		cachedLocations.put(player, new HashMap<Integer, LocationToReach>());
		for (Quest quest : PlayerDataLoader.getPlayerQuestData(player.getUniqueId()).getQuests()) {
			for (QuestObjective objective : quest.getObjectives()) {
				if (objective.getObjectiveType() != QuestObjectiveType.LOCATION) {
					continue;
				}
				if (objective.getObjectiveNumber() != 1) {
					if (QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() - 1).isCompleted() == false) {
						continue;
					}
				}
				if (objective.isCompleted()) {
					continue;
				}
				cachedLocations.get(player).put(quest.getQuestID(), ((QuestObjectiveLocation) objective).getLocation());
			}
		}
		if (cachedLocations.get(player).size() == 0) {
			cachedLocations.remove(player);
		}
	}

	public static Map<Player, Map<Integer, LocationToReach>> getCachedLocations() {
		return cachedLocations;
	}

	public static HashMap<Long, TaskQueue> getNpcTaskQueues() { // Get the NPC task queues
		return npcTaskQueues;
	}

	public static Map<UUID, Map<Integer, Set<Integer>>> getQuestCooldowns() { // Get the quest cooldowns
		return cooldowns;
	}

	public static Long getNextId() { // Get a new unique ID that can be used for NPCs
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
							ItemStack newItem = item.clone();
							newItem.setAmount(leftToRemove * -1);
							inventory.addItem(newItem);
						}
						return;
					}
				}
			}
		}
	}

	public static String[] getFirstUncompletedGoalMessageAndLocation(Quest quest, QuestProfile profile) {
		if (quest.getFirstNPC().getState() != FirstNpcState.ACCEPTED) {
			return new String[] {
					quest.getFirstNPC().hasGoalMessage() ? ChatColor.translateAlternateColorCodes('&', quest.getFirstNPC().getGoalMessage()) :
							"Speak with: " + quest.getFirstNPC().getNpcName() + " at " + quest.getFirstNPC().getLocation().getBlockX() + " " + quest.getFirstNPC().getLocation().getBlockY() + " " + quest.getFirstNPC().getLocation().getBlockZ(),
					quest.getFirstNPC().hasGoalLocation() ? ChatColor.translateAlternateColorCodes('&', quest.getFirstNPC().getGoalLocation()) : ""
			};
		}
		QuestObjective lowest = null;
		for (QuestObjective objective : quest.getObjectives()) {
			if (objective.isCompleted() == false) {
				if (lowest == null) {
					lowest = objective;
				} else if (objective.getObjectiveNumber() < lowest.getObjectiveNumber()) {
					lowest = objective;
				}
			}
		}
		if (lowest == null) {
			quest.getQuestState().setCompleted(true);
			profile.save();
		}
		return new String[] {lowest.getGoalMessage(), lowest.getGoalLocation()};
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
					command = outputString.substring(i + 1, outputString.length());
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

	public static QuestProfile getQuestProfile(String uuid) {
		return PlayerDataLoader.getPlayerQuestData(UUID.fromString(uuid));
	}

}
