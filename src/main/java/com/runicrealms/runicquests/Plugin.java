package com.runicrealms.runicquests;

import com.runicrealms.plugin.character.api.CharacterApi;
import com.runicrealms.runicquests.command.CompleteQuestCommand;
import com.runicrealms.runicquests.command.QuestTriggerCommand;
import com.runicrealms.runicquests.command.QuestsCommand;
import com.runicrealms.runicquests.command.ResetQuestsCommand;
import com.runicrealms.runicquests.config.ConfigLoader;
import com.runicrealms.runicquests.data.PlayerDataLoader;
import com.runicrealms.runicquests.data.QuestProfile;
import com.runicrealms.runicquests.event.EventBreakBlock;
import com.runicrealms.runicquests.event.EventClickNpc;
import com.runicrealms.runicquests.event.EventInventory;
import com.runicrealms.runicquests.event.EventKillMythicMob;
import com.runicrealms.runicquests.event.EventPlayerJoinQuit;
import com.runicrealms.runicquests.event.EventPlayerLocation;
import com.runicrealms.runicquests.event.custom.RightClickNpcHandler;
import com.runicrealms.runicquests.listeners.JournalListener;
import com.runicrealms.runicquests.quests.FirstNpcState;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.QuestItem;
import com.runicrealms.runicquests.quests.QuestObjectiveType;
import com.runicrealms.runicquests.quests.hologram.HoloManager;
import com.runicrealms.runicquests.quests.location.LocationToReach;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveLocation;
import com.runicrealms.runicquests.task.TaskQueue;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

public class Plugin extends JavaPlugin {

	private static Plugin plugin; // Used for getInstance()
	private static HoloManager holoManager;
	private static final HashMap<Long, TaskQueue> npcTaskQueues = new HashMap<>(); // List of NPC task queues
	private static final Map<UUID,  Map<Integer, Long>> cooldowns = new HashMap<>(); // List of quest cooldowns
	private static Long nextId = Long.MIN_VALUE; // This is used to give each NPC a new unique ID.
	/*
	 * This Map is meant to help with performance issues with checking the players location. It will just indicate
	 * when a player has a location objective on one of their quests
	 */
	private static final Map<Player, Map<Integer, LocationToReach>> cachedLocations = new HashMap<>();

	public static double NPC_MESSAGE_DELAY; // Config value

	@Override
	public void onEnable() {
		plugin = this; // Used for getInstance()
		holoManager = new HoloManager();
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
		this.getServer().getPluginManager().registerEvents(holoManager, this);
		for (Player player : Bukkit.getOnlinePlayers()) { // Loop through online players (fixes bug with /reload)
			if (CharacterApi.getCurrentCharacterSlot(player) != null) {
				EventPlayerJoinQuit.runJoinEvent(player, CharacterApi.getCurrentCharacterSlot(player)); // Read PlayerJoinQuitEvent.runJoinEvent
			}
		}
		registerCommand(new CompleteQuestCommand(), "completequest", "questcomplete", "cq", "qc");
		registerCommand(new QuestsCommand(), "quests", "quest", "objectives", "objective");
		registerCommand(new ResetQuestsCommand(), "resetquests", "questsreset", "resetquest", "questreset", "rq", "qr");
		registerCommand(new QuestTriggerCommand(), "questtrigger");
		for (Player player : Bukkit.getOnlinePlayers()) {
			updatePlayerCachedLocations(player);
		}
		registerMoveTask();
	}

	@Override
	public void onDisable() {
		plugin = null;
		holoManager = null;
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

	public static HoloManager getHoloManager() {
		return holoManager;
	}

	private static void registerMoveTask() { // Schedule a task that will run for the cached location objectives
		Bukkit.getScheduler().scheduleSyncRepeatingTask(getInstance(), () -> {
			for (Entry<Player, Map<Integer, LocationToReach>> entry : cachedLocations.entrySet()) {
				for (Entry<Integer, LocationToReach> questLocationToReach : entry.getValue().entrySet()) {
					if (questLocationToReach.getValue().hasReachedLocation(entry.getKey())) {
						EventPlayerLocation.playerCompleteLocationObjective(entry.getKey(), questLocationToReach.getKey());
						updatePlayerCachedLocations(entry.getKey());
						break;
					}
				}
			}
		}, 10L, 10L);
	}

	public static void updatePlayerCachedLocations(Player player) { // Updates the cached location objectives for a player
		cachedLocations.put(player, new HashMap<>());
		for (Quest quest : PlayerDataLoader.getPlayerQuestData(player.getUniqueId()).getQuests()) {
			for (QuestObjective objective : quest.getObjectives()) {
				if (objective.getObjectiveType() != QuestObjectiveType.LOCATION) {
					continue;
				}
				if (objective.getObjectiveNumber() != 1) {
					if (!QuestObjective.getObjective(quest.getObjectives(), objective.getObjectiveNumber() - 1).isCompleted()) {
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

	public static Map<UUID, Map<Integer, Long>> getQuestCooldowns() { // Get the quest cooldowns
		return cooldowns;
	}

	public static boolean canStartRepeatableQuest(UUID uuid, Integer questId) {
		if (!cooldowns.get(uuid).containsKey(questId)) {
			return true;
		}
		if (cooldowns.get(uuid).get(questId) <= System.currentTimeMillis()) {
			cooldowns.get(uuid).remove(questId);
			return true;
		}
		return false;
	}

	public static Long getNextId() { // Get a new unique ID that can be used for NPCs
		nextId++;
		return nextId - 1;
	}


	public static void removeItem(Player pl, String name, String type, int amount) {
		int to_take = amount;
		for (ItemStack player_item : pl.getInventory().getContents()) {
			if (player_item != null) {
				if (player_item.getType().name().equalsIgnoreCase(type) &&
						getItemName(player_item).equalsIgnoreCase(ChatColor.stripColor(name))) {
					int take_next = Math.min(to_take, player_item.getAmount());
					remove(pl, player_item, take_next);
					to_take -= take_next;
					if (to_take <= 0) { //Reached amount. Can stop!
						break;
					}
				}
			}
		}
	}

	private static void remove(Player p, ItemStack toR, int amount) {
		ItemStack i = toR.clone();
		i.setAmount(amount);
		p.getInventory().removeItem(i);
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
			if (!objective.isCompleted()) {
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
			if (!objective.isCompleted()) {
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
		String[] parts = msg.split("//");
		if (parts.length != 1) {
			for (int i = 1; i < parts.length; i++) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parts[i]);
			}
		}
		return parts[0];
	}

	public static QuestProfile getQuestProfile(String uuid) {
		return PlayerDataLoader.getPlayerQuestData(UUID.fromString(uuid));
	}

}
