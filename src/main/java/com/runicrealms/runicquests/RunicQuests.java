package com.runicrealms.runicquests;

import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.PaperCommandManager;
import com.runicrealms.libs.taskchain.BukkitTaskChainFactory;
import com.runicrealms.libs.taskchain.TaskChain;
import com.runicrealms.libs.taskchain.TaskChainFactory;
import com.runicrealms.runicquests.command.admin.QuestTriggerCMD;
import com.runicrealms.runicquests.command.system.TutorialWeaponCMD;
import com.runicrealms.runicquests.config.ConfigLoader;
import com.runicrealms.runicquests.listeners.*;
import com.runicrealms.runicquests.model.MongoTask;
import com.runicrealms.runicquests.model.QuestProfileManager;
import com.runicrealms.runicquests.passivenpcs.PassiveNpcClickListener;
import com.runicrealms.runicquests.passivenpcs.PassiveNpcHandler;
import com.runicrealms.runicquests.quests.FirstNpcState;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.QuestItem;
import com.runicrealms.runicquests.quests.hologram.HoloManager;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import com.runicrealms.runicquests.task.TaskQueue;
import com.runicrealms.runicquests.task.TaskQueueCleanupListener;
import com.runicrealms.runicquests.ui.CompassManager;
import com.runicrealms.runicquests.ui.QuestMenuListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RunicQuests extends JavaPlugin {
    private static final HashMap<Long, TaskQueue> npcTaskQueues = new HashMap<>(); // List of NPC task queues
    private static final Map<UUID, Map<Integer, Date>> cooldowns = new HashMap<>(); // List of quest cooldowns
    public static double NPC_MESSAGE_DELAY; // Config value
    private static RunicQuests instance;
    private static TaskChainFactory taskChainFactory;
    private static HoloManager holoManager;
    private static PassiveNpcHandler passiveNpcHandler;
    private static PaperCommandManager commandManager;
    private static QuestProfileManager questsAPI;
    private static MongoTask mongoTask;
    private static LocationManager locationManager;
    private static Long nextId = Long.MIN_VALUE; // This is used to give each NPC a new unique ID.

    public static RunicQuests getInstance() { // Get the plugin instance
        return instance;
    }

    public static HoloManager getHoloManager() {
        return holoManager;
    }

    public static PassiveNpcHandler getPassiveNpcHandler() {
        return passiveNpcHandler;
    }

    public static PaperCommandManager getCommandManager() {
        return commandManager;
    }

    public static QuestProfileManager getAPI() {
        return questsAPI;
    }

    public static MongoTask getMongoTask() {
        return mongoTask;
    }

    public static LocationManager getLocationManager() {
        return locationManager;
    }

    public static HashMap<Long, TaskQueue> getNpcTaskQueues() { // Get the NPC task queues
        return npcTaskQueues;
    }

    public static Map<UUID, Map<Integer, Date>> getQuestCooldowns() { // Get the quest cooldowns
        return cooldowns;
    }

    public static Long getNextId() { // Get a new unique ID that can be used for NPCs
        nextId++;
        return nextId - 1;
    }

    /**
     * ?
     *
     * @param <T>
     * @return
     */
    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }

    /**
     * ?
     *
     * @param name
     * @param <T>
     * @return
     */
    public static <T> TaskChain<T> newSharedChain(String name) {
        return taskChainFactory.newSharedChain(name);
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

    public static String[] getFirstUncompletedGoalMessageAndLocation(Quest quest) {
        if (quest.getFirstNPC().getState() != FirstNpcState.ACCEPTED) {
            return new String[]{
                    quest.getFirstNPC().hasGoalMessage() ? ChatColor.translateAlternateColorCodes('&', quest.getFirstNPC().getGoalMessage()) :
                            "Speak with " + quest.getFirstNPC().getNpcName() + " at " + quest.getFirstNPC().getLocation().getBlockX() + ", " + quest.getFirstNPC().getLocation().getBlockY() + ", " + quest.getFirstNPC().getLocation().getBlockZ(),
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
        }
        if (lowest != null) {
            return new String[]{lowest.getGoalMessage(), lowest.getGoalLocation()};
        } else {
            return new String[]{"", ""}; // usually means something broke
        }
    }

    public static String getItemName(ItemStack item) { // Get the name of an ItemStack
        if (item.getItemMeta() == null || item.getItemMeta().getDisplayName().equals("")) {
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
                    if (RunicQuests.getItemName(item).equalsIgnoreCase(ChatColor.stripColor(questItem.getItemName())) &&
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

    @Override
    public void onDisable() {
        instance = null;
        taskChainFactory = null;
        holoManager = null;
        commandManager = null;
        questsAPI = null;
        mongoTask = null;
    }

    @Override
    public void onEnable() {
        instance = this;
        taskChainFactory = BukkitTaskChainFactory.create(this);
        holoManager = new HoloManager();
        passiveNpcHandler = new PassiveNpcHandler();
        commandManager = new PaperCommandManager(this);
        questsAPI = new QuestProfileManager();
        mongoTask = new MongoTask();
        locationManager = new LocationManager();
        ConfigLoader.initDirs(); // Initialize directories that might not exist
        ConfigLoader.loadMainConfig(); // Initialize the main config file if it doesn't exist
        NPC_MESSAGE_DELAY = ConfigLoader.getMainConfig().getDouble("npc-message-delay"); // Get the config value
        /*
        Register Events
         */
        this.getServer().getPluginManager().registerEvents(new CastSpellListener(), this);
        this.getServer().getPluginManager().registerEvents(new GatherListener(), this);
        this.getServer().getPluginManager().registerEvents(new MythicMobDeathListener(), this);
        this.getServer().getPluginManager().registerEvents(new RightClickNpcListener(), this);
        this.getServer().getPluginManager().registerEvents(new LocationManager(), this);
        this.getServer().getPluginManager().registerEvents(new JournalListener(), this);
        this.getServer().getPluginManager().registerEvents(new QuestMenuListener(), this);
        this.getServer().getPluginManager().registerEvents(new PassiveNpcClickListener(), this);
        this.getServer().getPluginManager().registerEvents(new QuestItemListener(), this);
        this.getServer().getPluginManager().registerEvents(new QuestCompleteListener(), this);
        this.getServer().getPluginManager().registerEvents(new CompassManager(), this);
        this.getServer().getPluginManager().registerEvents(new TaskQueueCleanupListener(), this);
        this.getServer().getPluginManager().registerEvents(new RepeatableQuestListener(), this);
        this.getServer().getPluginManager().registerEvents(new IdleMessageListener(), this);

        /*
        ACF Commands
         */
        registerACFCommands();
        commandManager.getCommandConditions().addCondition("is-console-or-op", context -> {
            if (!(context.getIssuer().getIssuer() instanceof ConsoleCommandSender) && !context.getIssuer().getIssuer().isOp()) // ops can execute console commands
                throw new ConditionFailedException("Only the console may run this command!");
        });
    }

    private void registerACFCommands() {
        if (commandManager == null) {
            Bukkit.getLogger().info(ChatColor.DARK_RED + "ERROR: FAILED TO INITIALIZE ACF COMMANDS");
            return;
        }
        commandManager.registerCommand(new TutorialWeaponCMD());
        commandManager.registerCommand(new QuestTriggerCMD());
    }

}
