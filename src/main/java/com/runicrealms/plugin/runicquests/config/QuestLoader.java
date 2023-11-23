package com.runicrealms.plugin.runicquests.config;

import com.runicrealms.plugin.common.RunicCommon;
import com.runicrealms.plugin.npcs.RunicNpcs;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicquests.RunicQuests;
import com.runicrealms.plugin.runicquests.exception.QuestLoadException;
import com.runicrealms.plugin.runicquests.quests.CraftingProfessionType;
import com.runicrealms.plugin.runicquests.quests.PlayerClassType;
import com.runicrealms.plugin.runicquests.quests.Quest;
import com.runicrealms.plugin.runicquests.quests.QuestFirstNpc;
import com.runicrealms.plugin.runicquests.quests.QuestIdleMessage;
import com.runicrealms.plugin.runicquests.quests.QuestIdleMessageConditions;
import com.runicrealms.plugin.runicquests.quests.QuestItem;
import com.runicrealms.plugin.runicquests.quests.QuestNpc;
import com.runicrealms.plugin.runicquests.quests.QuestObjectiveType;
import com.runicrealms.plugin.runicquests.quests.QuestRequirements;
import com.runicrealms.plugin.runicquests.quests.QuestRewards;
import com.runicrealms.plugin.runicquests.quests.location.BoxLocation;
import com.runicrealms.plugin.runicquests.quests.location.RadiusLocation;
import com.runicrealms.plugin.runicquests.quests.objective.QuestObjective;
import com.runicrealms.plugin.runicquests.quests.objective.QuestObjectiveCast;
import com.runicrealms.plugin.runicquests.quests.objective.QuestObjectiveCraft;
import com.runicrealms.plugin.runicquests.quests.objective.QuestObjectiveGather;
import com.runicrealms.plugin.runicquests.quests.objective.QuestObjectiveLocation;
import com.runicrealms.plugin.runicquests.quests.objective.QuestObjectiveSlay;
import com.runicrealms.plugin.runicquests.quests.objective.QuestObjectiveTalk;
import com.runicrealms.plugin.runicquests.quests.objective.QuestObjectiveTrigger;
import com.runicrealms.plugin.runicquests.quests.trigger.Trigger;
import com.runicrealms.plugin.runicquests.quests.trigger.TriggerObjectiveHandler;
import com.runicrealms.plugin.runicquests.quests.trigger.TriggerType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestLoader {

    public static List<Quest> cachedQuests;

    /*
    Static block to load our quest list into memory from file storage on startup
     */
    static {
        cachedQuests = new ArrayList<>();
        File folder = RunicCommon.getConfigAPI().getSubFolder(RunicQuests.getInstance().getDataFolder(), "quests");
        for (File quest : folder.listFiles()) {
            if (!quest.isDirectory()) {
                Quest loadedQuest = null;
                try {
                    loadedQuest = QuestLoader.loadQuest(RunicCommon.getConfigAPI().getYamlConfigFromFile(quest.getName(), folder));
                } catch (QuestLoadException exception) {
                    exception.addMessage("Error loading quest: " + quest.getName());
                    exception.displayToConsole();
                    exception.displayToOnlinePlayers();
                } finally {
                    cachedQuests.add(loadedQuest);
                }
            }
        }
    }

    /**
     * Gets an immutable List<Quest> which can only be used as a reference!
     *
     * @return a reference to our default list of quests
     */
    public static List<Quest> getBlankQuestListRef() {
        return Collections.unmodifiableList(cachedQuests);
    }

    /**
     * Gets a List<Quest> from the quest folder, but the quests will contain no user info (like completed, or started)
     * This is not just a reference, and it will return a mutable list
     */
    public static List<Quest> getQuestListNoUserData() {
        return obtainNewNpcIds(cachedQuests);
    }

    /**
     * Method for creating a new List<Quest> with new NPC IDs and reset FirstNpcState
     */
    public static List<Quest> obtainNewNpcIds(List<Quest> quests) {
        List<Quest> newQuests = new ArrayList<>();
        for (Quest quest : quests) {
            if (quest != null) {
                Quest newQuest = quest.clone();
                newQuests.add(newQuest);
            } else {
                newQuests.add(null);
            }
        }
        return newQuests;
    }

    /**
     * Uses all methods from this class in order to load a quest from file. Will not have any player info.
     */
    public static Quest loadQuest(FileConfiguration config) throws QuestLoadException {
        try {
            int uniqueQuestId = checkValueNull(config.getInt("unique-id"), "unique-id");
            ArrayList<QuestObjective> objectives = new ArrayList<>();
            int numberOfObjectives = checkValueNull(config.getConfigurationSection("objectives"), "objectives").getKeys(false).size();
            for (int objectiveNumber = 1; objectiveNumber <= numberOfObjectives; objectiveNumber++) {
                QuestObjective objective;
                try {
                    ConfigObjective configObjective = new ConfigObjective
                            (
                                    config.getConfigurationSection("objectives." + objectiveNumber),
                                    uniqueQuestId,
                                    numberOfObjectives,
                                    objectiveNumber);
                    objective = loadObjective(configObjective);
                } catch (QuestLoadException exception) {
                    exception.addMessage(objectiveNumber + "", "objective: " + objectiveNumber);
                    throw exception;
                }
                objectives.add(objective);
            }
            QuestFirstNpc firstNPC;
            try {
                firstNPC = loadFirstNpc(config.getConfigurationSection("first-npc"), numberOfObjectives);
            } catch (QuestLoadException exception) {
                exception.addMessage("first-npc");
                throw exception;
            }
            QuestRewards rewards;
            try {
                rewards = loadRewards(config.getConfigurationSection("rewards"));
            } catch (QuestLoadException exception) {
                exception.addMessage("rewards");
                throw exception;
            }
            QuestRequirements requirements;
            try {
                requirements = loadRequirements(config.getConfigurationSection("requirements"));
            } catch (QuestLoadException exception) {
                exception.addMessage("requirements");
                throw exception;
            }
            return new Quest(
                    checkValueNull(config.getString("name"), "name"),
                    firstNPC,
                    objectives,
                    rewards,
                    uniqueQuestId,
                    requirements,
                    checkValueNull(config.getBoolean("side-quest"), "side-quest"),
                    checkValueNull(config.getBoolean("repeatable"), "repeatable"),
                    (config.getBoolean("repeatable") ? checkValueNull(config.getInt("cooldown"), "cooldown") : null),
                    (config.contains("tutorial") && config.getBoolean("tutorial")));
        } catch (QuestLoadException exception) {
            throw exception;
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new QuestLoadException("unknown syntax error").setErrorMessage(exception.getMessage());
        }
    }

    /**
     * Loads idle messages from config
     *
     * @param section            of the config
     * @param numberOfObjectives in the quest
     * @return a QuestObjectiveCast object
     * @throws QuestLoadException if section is improperly configured
     */
    public static List<QuestIdleMessage> loadIdleMessages(ConfigurationSection section, int numberOfObjectives) throws QuestLoadException {
        try {
            List<QuestIdleMessage> idleMessages = new ArrayList<>();
            for (String key : section.getKeys(false)) {
                List<Boolean> objectives = null;
                if (section.contains(key + ".condition.objectives")) {
                    objectives = new ArrayList<>();
                    for (int i = 0; i <= numberOfObjectives; i++) {
                        objectives.add(null);
                    }
                    for (String objectiveNumber : section.getConfigurationSection(key + ".condition.objectives").getKeys(false)) {
                        objectives.set(Integer.parseInt(objectiveNumber), section.getConfigurationSection(key + ".condition.objectives").getBoolean(objectiveNumber));
                    }
                }
                QuestIdleMessageConditions conditions = new QuestIdleMessageConditions(
                        (section.contains(key + ".condition.quest.started") ? section.getBoolean(key + ".condition.quest.started") : null),
                        (section.contains(key + ".condition.quest.completed") ? section.getBoolean(key + ".condition.quest.completed") : null),
                        objectives,
                        (section.contains(key + ".condition.quest-items") ? section.getBoolean(key + ".condition.quest-items") : null));
                idleMessages.add(new QuestIdleMessage(conditions, getStringList(section, key + ".speech")));
            }
            return idleMessages;
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new QuestLoadException("unknown syntax error", "idle-messages");
        }
    }

    /**
     * Loads quest requirements from config
     */
    public static QuestRequirements loadRequirements(ConfigurationSection configSec) throws QuestLoadException {
        try {
            Integer levelReq = checkValueNull(configSec.getInt("level"), "level");
            List<String> levelNotMet = configSec.contains("level-not-met") ? getStringList(configSec, "level-not-met") : null;
            Integer craftingReq = null;
            List<String> craftingNotMet = null;
            List<CraftingProfessionType> professionType = new ArrayList<>();
            if (configSec.contains("blacksmith-level")) {
                craftingNotMet = configSec.contains("crafting-level-not-met") ? getStringList(configSec, "crafting-level-not-met") : null;
                craftingReq = checkValueNull(configSec.getInt("blacksmith-level"), "blacksmith-level");
                professionType.add(CraftingProfessionType.BLACKSMITH);
            }
            if (configSec.contains("jeweling-level")) {
                craftingNotMet = configSec.contains("crafting-level-not-met") ? getStringList(configSec, "crafting-level-not-met") : null;
                craftingReq = checkValueNull(configSec.getInt("enchanter-level"), "enchanter-level");
                professionType.add(CraftingProfessionType.JEWELER);
            }
            if (configSec.contains("hunter-level")) {
                craftingNotMet = configSec.contains("crafting-level-not-met") ? getStringList(configSec, "crafting-level-not-met") : null;
                craftingReq = checkValueNull(configSec.getInt("hunter-level"), "hunter-level");
                professionType.add(CraftingProfessionType.HUNTER);
            }
            if (configSec.contains("alchemist-level")) {
                craftingNotMet = configSec.contains("crafting-level-not-met") ? getStringList(configSec, "crafting-level-not-met") : null;
                craftingReq = checkValueNull(configSec.getInt("alchemist-level"), "alchemist-level");
                professionType.add(CraftingProfessionType.ALCHEMIST);
            }
            if (configSec.contains("enchanter-level")) {
                craftingNotMet = configSec.contains("crafting-level-not-met") ? getStringList(configSec, "crafting-level-not-met") : null;
                craftingReq = checkValueNull(configSec.getInt("enchanter-level"), "enchanter-level");
                professionType.add(CraftingProfessionType.ENCHANTER);
            }
            if (configSec.contains("crafting-level")) {
                craftingNotMet = configSec.contains("crafting-level-not-met") ? getStringList(configSec, "crafting-level-not-met") : null;
                craftingReq = checkValueNull(configSec.getInt("crafting-level"), "crafting-level");
                professionType.add(CraftingProfessionType.ANY);
            }
            List<Integer> requiredQuests = new ArrayList<>();
            List<String> requiredQuestsNotMet = null;
            if (configSec.contains("required-quests")) {
                requiredQuestsNotMet = configSec.contains("required-quests-not-met") ? getStringList(configSec, "required-quests-not-met") : null;
                if (configSec.isInt("required-quests")) {
                    requiredQuests.add(configSec.getInt("required-quests"));
                } else {
                    requiredQuests = configSec.getIntegerList("required-quests");
                }
            }
            PlayerClassType classType = configSec.contains("class") ? checkValueNull(PlayerClassType.getFromString(configSec.getString("class")), "class") : null;
            List<String> classTypeNotMet = configSec.contains("class-not-met") ? getStringList(configSec, "class-not-met") : null;
            return new QuestRequirements(levelReq, craftingReq, professionType, requiredQuests, levelNotMet, craftingNotMet, requiredQuestsNotMet, classType, classTypeNotMet);
        } catch (QuestLoadException exception) {
            throw exception;
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new QuestLoadException("unknown syntax error").setErrorMessage(exception.getMessage());
        }
    }

    /**
     * Loads quest rewards from config
     */
    public static QuestRewards loadRewards(ConfigurationSection configSec) throws QuestLoadException {
        try {
            List<String> execute = null;
            if (configSec.contains("execute")) {
                execute = new ArrayList<>();
                if (configSec.isString("execute")) {
                    execute.add(configSec.getString("execute"));
                } else {
                    execute = configSec.getStringList("execute");
                }
            }
            Map<String, Integer> items = new HashMap<>();
            if (configSec.contains("items")) {
                ConfigurationSection itemsSec = configSec.getConfigurationSection("items");
                for (String itemName : itemsSec.getKeys(false)) {
                    if (!RunicItemsAPI.isTemplate(itemName))
                        throw new QuestLoadException("items." + itemName + " is not a valid template");
                    items.put(itemName, itemsSec.getInt(itemName));
                }
            }
            return new QuestRewards(
                    checkValueNull(configSec.getInt("exp"), "exp"),
                    checkValueNull(configSec.getInt("quest-points"), "quest-points"),
                    checkValueNull(configSec.getInt("money"), "money"),
                    execute,
                    items);
        } catch (QuestLoadException exception) {
            throw exception;
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new QuestLoadException("unknown syntax error").setErrorMessage(exception.getMessage());
        }
    }

    /**
     * Loads an objective from config file and converts to an objective object wrapper
     *
     * @param configObjective a wrapper which contains info about a quest and a specific objective
     * @return a QuestObjective object
     * @throws QuestLoadException if the config is improperly configured
     */
    public static QuestObjective loadObjective(ConfigObjective configObjective) throws QuestLoadException {
        ConfigurationSection section = configObjective.getSection();
        int numberOfObjectives = configObjective.getNumberOfObjectives();
        Integer objectiveNumber = configObjective.getObjectiveNumber();
        try {
            String goalMessage = checkValueNull(section.getString("goal-message"), "goal-message");
            String goalLocation = section.contains("goal-location") ? section.getString("goal-location") : "";
            boolean displayNextTitle = !section.contains("display-next-title") || section.getBoolean("display-next-title");
            if (section.getString("requirement.type").equalsIgnoreCase(QuestObjectiveType.CAST.getIdentifier())) {
                return castObjectiveFromConfig(configObjective, goalMessage, goalLocation, displayNextTitle);
            } else if (section.getString("requirement.type").equalsIgnoreCase(QuestObjectiveType.SLAY.getIdentifier())) {
                return new QuestObjectiveSlay(
                        checkValueNull(getStringList(section, "requirement.mob-names"), "mob-names"),
                        checkValueNull(section.getInt("requirement.amount"), "amount"),
                        section.contains("requirement.requires") ? loadQuestItems(section.getConfigurationSection("requirement.requires")) : null,
                        goalMessage,
                        (section.contains("execute") ? getStringList(section, "execute") : null),
                        objectiveNumber,
                        (section.contains("completed-message") ? getStringList(section, "completed-message") : null),
                        goalLocation,
                        displayNextTitle);
            } else if (section.getString("requirement.type").equalsIgnoreCase("talk")) {
                QuestNpc npc;
                try {
                    npc = loadNpc(section.getConfigurationSection("requirement.npc"), numberOfObjectives);
                } catch (QuestLoadException exception) {
                    exception.addMessage("npc");
                    throw exception;
                }
                return new QuestObjectiveTalk(
                        npc,
                        section.contains("requirement.requires") ? loadQuestItems(section.getConfigurationSection("requirement.requires")) : null,
                        goalMessage,
                        (section.contains("execute") ? getStringList(section, "execute") : null),
                        objectiveNumber,
                        (section.contains("completed-message") ? getStringList(section, "completed-message") : null),
                        goalLocation,
                        displayNextTitle);
            } else if (section.getString("requirement.type").equalsIgnoreCase("location")) {
                if (checkValueNull(section.getString("requirement.location-type")).equalsIgnoreCase("radius")) {
                    return new QuestObjectiveLocation(
                            new RadiusLocation(checkValueNull(parseLocation(section.getString("requirement.location"))), checkValueNull(section.getInt("requirement.radius"))),
                            section.contains("requirement.requires") ? loadQuestItems(section.getConfigurationSection("requirement.requires")) : null,
                            goalMessage,
                            (section.contains("execute") ? getStringList(section, "execute") : null),
                            objectiveNumber,
                            (section.contains("completed-message") ? getStringList(section, "completed-message") : null),
                            goalLocation,
                            displayNextTitle);
                } else {
                    return new QuestObjectiveLocation(
                            new BoxLocation(checkValueNull(parseLocation(section.getString("requirement.corner-one"))), checkValueNull(parseLocation(section.getString("requirement.corner-two")))),
                            section.contains("requirement.requires") ? loadQuestItems(section.getConfigurationSection("requirement.requires")) : null,
                            goalMessage,
                            (section.contains("execute") ? getStringList(section, "execute") : null),
                            objectiveNumber,
                            (section.contains("completed-message") ? getStringList(section, "completed-message") : null),
                            goalLocation,
                            displayNextTitle);

                }
            } else if (section.getString("requirement.type").equalsIgnoreCase("gather")) {
                return gatherObjectiveFromConfig(configObjective, goalMessage, goalLocation, displayNextTitle);
            } else if (section.getString("requirement.type").equalsIgnoreCase("trigger")) {
                return triggerObjectiveFromConfig(configObjective, goalMessage, goalLocation, displayNextTitle);
            } else if (section.getString("requirement.type").equalsIgnoreCase("craft")) {
                return craftObjectiveFromConfig(configObjective, goalMessage, goalLocation, displayNextTitle);
            }
        } catch (QuestLoadException exception) {
            throw exception;
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new QuestLoadException("unknown syntax error").setErrorMessage(exception.getMessage());
        }
        throw new QuestLoadException("invalid objective type");
    }

    /**
     * Loads a quest first NPC
     */
    public static QuestFirstNpc loadFirstNpc(ConfigurationSection configSec, int numberOfObjectives) throws QuestLoadException {
        try {
            return new QuestFirstNpc(
                    checkValueNull(configSec.getInt("npc-id"), "npc-id"),
                    RunicNpcs.getNpcs().get(configSec.getInt("npc-id")).getLocation(),
                    checkValueNull(getStringList(configSec, "speech"), "npc-speech"),
                    !configSec.contains("add-npc-name") || configSec.getBoolean("add-npc-name"),
                    (configSec.contains("idle-messages") ? loadIdleMessages(configSec.getConfigurationSection("idle-messages"), numberOfObjectives) : null),
                    (configSec.contains("quest-completed-message") ? getStringList(configSec, "quest-completed-message") : null),
                    checkValueNull(configSec.getString("npc-name"), "npc-name"),
                    (configSec.contains("execute") ? getStringList(configSec, "execute") : null),
                    configSec.contains("goal-message") ? configSec.getString("goal-message") : null,
                    configSec.contains("goal-location") ? configSec.getString("goal-location") : null);
        } catch (QuestLoadException exception) {
            throw exception;
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new QuestLoadException("unknown syntax error").setErrorMessage(exception.getMessage());
        }
    }

    /**
     * Loads a quest objective NPC
     */
    public static QuestNpc loadNpc(ConfigurationSection configSec, int numberOfObjectives) throws QuestLoadException {
        try {
            return new QuestNpc(
                    checkValueNull(configSec.getInt("npc-id"), "npc-id"),
                    checkValueNull(getStringList(configSec, "speech"), "npc-speech"),
                    !configSec.contains("add-npc-name") || configSec.getBoolean("add-npc-name"),
                    (configSec.contains("idle-messages") ? loadIdleMessages(configSec.getConfigurationSection("idle-messages"), numberOfObjectives) : null),
                    checkValueNull(configSec.getString("npc-name"), "npc-name"),
                    (configSec.contains("denied-message") ? getStringList(configSec, "denied-message") : null));
        } catch (QuestLoadException exception) {
            throw exception;
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new QuestLoadException("unknown syntax error").setErrorMessage(exception.getMessage());
        }
    }

    /**
     * Loads required quest items
     */
    public static List<QuestItem> loadQuestItems(ConfigurationSection configSec) throws QuestLoadException {
        try {
            List<QuestItem> questItems = new ArrayList<>();
            for (String key : configSec.getKeys(false)) {
                questItems.add(new QuestItem(
                        checkValueNull(configSec.getString(key + ".item-name"), "item-name", key, "requires"),
                        checkValueNull(configSec.getString(key + ".item-type"), "item-type", key, "requires"),
                        checkValueNull(configSec.getInt(key + ".item-count"), "item-count", key, "requires")));
            }
            return questItems;
        } catch (QuestLoadException exception) {
            throw exception;
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new QuestLoadException("unknown syntax error").setErrorMessage(exception.getMessage());
        }
    }

    /**
     * Throws an exception if the given value is null
     */
    private static <T> T checkValueNull(T obj, String... message) throws QuestLoadException {
        if (obj == null) {
            throw new QuestLoadException(message);
        }
        return obj;
    }

    /**
     * Gets a string/string list from config file
     */
    private static List<String> getStringList(ConfigurationSection configSec, String path) {
        List<String> list = new ArrayList<>();
        if (configSec.isString(path)) {
            list.add(configSec.getString(path));
        } else {
            list = configSec.getStringList(path);
        }
        return list;
    }

    /**
     * Parses a location from a string
     */
    private static Location parseLocation(String str) {
        World world;
        double x, y, z;
        try {
            world = Bukkit.getWorld(str.split(",")[0].replaceAll(",", ""));
            x = Double.parseDouble(str.split(",")[1].replaceAll(",", ""));
            y = Double.parseDouble(str.split(",")[2].replaceAll(",", ""));
            z = Double.parseDouble(str.split(",")[3].replaceAll(",", ""));
        } catch (Exception exception) {
            return null;
        }
        return new Location(world, x, y, z);
    }

    /**
     * Builds a QuestObjectiveCast object from a config section
     *
     * @param configObjective  the wrapper for relevant info from the section of config
     * @param goalMessage      the message of the objective
     * @param goalLocation     the location of the objective
     * @param displayNextTitle true if a title message should be displayed
     * @return a QuestObjectiveCast object
     * @throws QuestLoadException if section is improperly configured
     */
    private static QuestObjectiveCast castObjectiveFromConfig(
            ConfigObjective configObjective,
            String goalMessage,
            String goalLocation,
            boolean displayNextTitle) throws QuestLoadException {
        ConfigurationSection section = configObjective.getSection();
        Integer objectiveNumber = configObjective.getObjectiveNumber();
        return new QuestObjectiveCast(
                checkValueNull(getStringList(section, "requirement.spell-names"), "spell-names"),
                checkValueNull(section.getInt("requirement.amount"), "amount"),
                section.contains("requirement.requires") ? loadQuestItems(section.getConfigurationSection("requirement.requires")) : null,
                goalMessage,
                (section.contains("execute") ? getStringList(section, "execute") : null),
                objectiveNumber,
                (section.contains("completed-message") ? getStringList(section, "completed-message") : null),
                goalLocation,
                displayNextTitle);
    }

    /**
     * Builds a QuestObjectiveGather object from a config section
     */
    private static QuestObjectiveGather gatherObjectiveFromConfig(
            ConfigObjective configObjective,
            String goalMessage,
            String goalLocation,
            boolean displayNextTitle) throws QuestLoadException {
        ConfigurationSection section = configObjective.getSection();
        Integer objectiveNumber = configObjective.getObjectiveNumber();
        return new QuestObjectiveGather(
                checkValueNull(getStringList(section, "requirement.resource-ids"), "resource-ids"),
                (section.contains("requirement.amount") ? section.getInt("requirement.amount") : 1),
                section.contains("requirement.requires") ? loadQuestItems(section.getConfigurationSection("requirement.requires")) : null,
                goalMessage,
                (section.contains("execute") ? getStringList(section, "execute") : null),
                objectiveNumber,
                (section.contains("completed-message") ? getStringList(section, "completed-message") : null),
                goalLocation,
                displayNextTitle);
    }

    /**
     * Builds a QuestObjectiveTrigger object from a config section
     */
    private static QuestObjectiveTrigger triggerObjectiveFromConfig(
            ConfigObjective configObjective,
            String goalMessage,
            String goalLocation,
            boolean displayNextTitle) throws QuestLoadException {
        ConfigurationSection section = configObjective.getSection();
        List<String> triggerIds = checkValueNull(getStringList(section, "requirement.trigger-id"));
        int questId = configObjective.getQuestId();
        Integer objectiveNumber = configObjective.getObjectiveNumber();
        for (String triggerId : triggerIds) {
            TriggerObjectiveHandler.addTrigger(new Trigger(triggerId, questId, objectiveNumber), triggerId);
        }
        return new QuestObjectiveTrigger(
                triggerIds,
                checkValueNull(getStringList(section, "requirement.speech")),
                section.contains("requirement.trigger-type") ? TriggerType.getFromIdentifier(checkValueNull((section.getString("requirement.trigger-type")))) : TriggerType.ALL, // Requires all triggers by default
                section.contains("requirement.requires") ? loadQuestItems(section.getConfigurationSection("requirement.requires")) : null,
                goalMessage,
                (section.contains("execute") ? getStringList(section, "execute") : null),
                objectiveNumber,
                (section.contains("completed-message") ? getStringList(section, "completed-message") : null),
                goalLocation,
                displayNextTitle);
    }

    /**
     * Builds a QuestObjectiveCraft object from a config section
     */
    private static QuestObjectiveCraft craftObjectiveFromConfig(
            @NotNull ConfigObjective configObjective,
            String goalMessage,
            String goalLocation,
            boolean displayNextTitle) throws QuestLoadException {
        ConfigurationSection section = configObjective.getSection();

        return new QuestObjectiveCraft(
                checkValueNull(getStringList(section, "requirement.resource-ids"), "resource-ids"),
                (section.contains("requirement.amount") ? section.getInt("requirement.amount") : 1),
                section.contains("requirement.requires") ? loadQuestItems(section.getConfigurationSection("requirement.requires")) : null,
                goalMessage,
                (section.contains("execute") ? getStringList(section, "execute") : null),
                configObjective.getObjectiveNumber(),
                (section.contains("completed-message") ? getStringList(section, "completed-message") : null),
                goalLocation,
                displayNextTitle);
    }
}
