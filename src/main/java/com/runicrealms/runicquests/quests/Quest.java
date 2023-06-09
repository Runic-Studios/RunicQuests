package com.runicrealms.runicquests.quests;

import com.runicrealms.plugin.common.util.ChatUtils;
import com.runicrealms.runicquests.RunicQuests;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveTalk;
import com.runicrealms.runicquests.util.QuestsUtil;
import com.runicrealms.runicquests.util.RunicCoreHook;
import com.runicrealms.runicquests.util.StatusItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains all the quest values which are needed.
 * All values except "QuestState state" can be parsed from config
 *
 * @author Excel
 */
public class Quest implements Cloneable {
    private Integer questID;
    private List<QuestObjective> objectives;
    private QuestState state;
    private QuestRequirements requirements;
    private boolean sideQuest;
    private boolean repeatable;
    private Integer cooldown;
    private String questName;
    private QuestFirstNpc firstNPC;
    private QuestRewards rewards;
    private boolean tutorial;

    /**
     * A dummy constructor used as a placeholder in the ui menu
     *
     * @param questName "dummy"
     */
    public Quest(String questName) {
        this.questName = questName;
    }

    /**
     * Creates a quest object with the following fields
     *
     * @param questName    the name of the quest
     * @param firstNPC     the first NPC (quest giver)
     * @param objectives   a list of objectives inside the quest
     * @param rewards      the rewards of the quest
     * @param questID      the UNIQUE quest id
     * @param requirements the requirements to start the quest
     * @param sideQuest    whether the quest is a main story or side quest
     * @param repeatable   whether the quest is a repeatable quest
     * @param cooldown     the cooldown of the quest (if repeatable)
     */
    public Quest(String questName, QuestFirstNpc firstNPC, ArrayList<QuestObjective> objectives, QuestRewards rewards,
                 Integer questID, QuestRequirements requirements, boolean sideQuest, boolean repeatable, Integer cooldown, boolean tutorial) {
        this.questName = questName;
        this.firstNPC = firstNPC;
        this.objectives = objectives;
        this.rewards = rewards;
        this.state = new QuestState(false, false);
        this.questID = questID;
        this.requirements = requirements;
        this.sideQuest = sideQuest;
        this.repeatable = repeatable;
        this.cooldown = cooldown;
        this.tutorial = tutorial;
    }

    public Quest(Quest quest) {
        this.questName = quest.questName;
        this.firstNPC = quest.firstNPC;
        this.objectives = quest.objectives;
        this.rewards = quest.rewards;
        this.state = quest.state;
        this.questID = quest.questID;
        this.requirements = quest.requirements;
        this.sideQuest = quest.sideQuest;
        this.repeatable = quest.repeatable;
        this.cooldown = quest.cooldown;
        this.tutorial = quest.tutorial;
    }

    public static List<String> getRequirementsNotMetMsg(Quest quest, RequirementsResult result) {
        List<String> list = new ArrayList<>();
        switch (result) {
            case CLASS_TYPE_NOT_MET:
                return quest.getRequirements().getClassTypeNotMetMsg();
            case CRAFTING_LEVEL_NOT_MET:
                return quest.getRequirements().getCraftingLevelNotMetMsg();
            case LEVEL_NOT_MET:
                return quest.getRequirements().getLevelNotMetMsg();
            case REQUIRED_QUESTS_NOT_MET:
                return quest.getRequirements().getCompletedQuestsNotMetMsg();
            case ALL_REQUIREMENTS_MET:
                return list;
        }
        return list;
    }

    /**
     * Checks all possible requirements and ensures the player has met all
     *
     * @return a RequirementsResult based on the missing requirement(s)
     */
    public static RequirementsResult hasMetRequirements(Player player, Quest quest) {
        // Check prior completed quests req
        if (quest.getRequirements().hasCompletedQuestRequirement()) {
            if (!RunicCoreHook.hasCompletedRequiredQuests(player, quest.getRequirements().getCompletedQuestsRequirement())) {
                return RequirementsResult.REQUIRED_QUESTS_NOT_MET;
            }
        }
        // Check level req
        if (!RunicCoreHook.hasCompletedLevelRequirement(player, quest.getRequirements().getClassLvReq())) {
            return RequirementsResult.LEVEL_NOT_MET;
        }
        // Check crafting level req
        if (quest.getRequirements().hasCraftingRequirement()) {
            for (CraftingProfessionType profession : quest.getRequirements().getCraftingProfessionType()) {
                if (!RunicCoreHook.isRequiredCraftingLevel(player, profession, quest.getRequirements().getCraftingRequirement())) {
                    return RequirementsResult.CRAFTING_LEVEL_NOT_MET;
                }
            }
        }
        // Check class type req
        if (quest.getRequirements().hasClassTypeRequirement()) {
            boolean isRequiredClass = RunicCoreHook.isRequiredClass(quest.getRequirements().getClassTypeRequirement(), player);
            if (!isRequiredClass)
                return RequirementsResult.CLASS_TYPE_NOT_MET;
        }
        return RequirementsResult.ALL_REQUIREMENTS_MET;
    }

    @Override
    public Quest clone() {
        ArrayList<QuestObjective> newObjectives = new ArrayList<>();
        for (QuestObjective objective : this.objectives) {
            if (objective instanceof QuestObjectiveTalk) {
                newObjectives.add(((QuestObjectiveTalk) objective).clone());
            } else {
                newObjectives.add(objective.clone());
            }
        }
        return new Quest(this.questName, this.firstNPC.clone(), newObjectives, this.rewards, this.questID, this.requirements, this.sideQuest, this.repeatable, this.cooldown, this.tutorial);
    }

    /**
     * Generates a dynamic quest icon for a player's quest
     *
     * @param player to populate info from
     * @return a menu item representing a quest with dynamic values
     */
    public ItemStack generateQuestIcon(Player player) {
        ItemStack item;
        ItemMeta meta;
        List<String> lore = new ArrayList<>();
        if (this.isRepeatable()) {
            boolean canStart = QuestsUtil.canStartRepeatableQuest(player.getUniqueId(), this);
            item = canStart ? StatusItemUtil.blueStatusItem : StatusItemUtil.greenStatusItem;
            meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.AQUA + this.getQuestName());
            lore.add(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "REPEATABLE QUEST");
            String[] messageLocation = RunicQuests.getFirstUncompletedGoalMessageAndLocation(this);
            lore.addAll(ChatUtils.formattedText(ChatColor.YELLOW + ChatColor.translateAlternateColorCodes('&', messageLocation[0])));
            if (!messageLocation[1].equalsIgnoreCase("")) {
                lore.add("");
                lore.addAll(ChatUtils.formattedText(ChatColor.DARK_AQUA + "Location/Tip: " + ChatColor.translateAlternateColorCodes('&', messageLocation[1])));
            }
            lore.add(canStart ? ChatColor.BLUE + "Can complete!" : ChatColor.GRAY + "On cooldown: " + ChatColor.WHITE + QuestsUtil.repeatableQuestTimeRemaining(player, this));
        } else if (this.getQuestState().isCompleted()) {
            item = StatusItemUtil.greenStatusItem;
            meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName((isTutorialQuest() ? ChatColor.LIGHT_PURPLE : ChatColor.GREEN) + this.getQuestName());
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "COMPLETE!");
        } else if (!RunicCoreHook.hasCompletedLevelRequirement(player, this.getRequirements().getClassLvReq())) {
            item = StatusItemUtil.redStatusItem;
            meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.RED + this.getQuestName());
            lore.add(ChatColor.DARK_RED + "You do not meet the level requirements!");
        } else if (this.isSideQuest()) {
            item = StatusItemUtil.yellowStatusItem;
            meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName((isTutorialQuest() ? ChatColor.LIGHT_PURPLE : ChatColor.YELLOW) + this.getQuestName());
            if (!isTutorialQuest()) {
                lore.add(ChatColor.GRAY + "" + ChatColor.BOLD + "SIDE QUEST");
            } else {
                lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "TUTORIAL QUEST");
            }
            String[] messageLocation = RunicQuests.getFirstUncompletedGoalMessageAndLocation(this);
            lore.addAll(ChatUtils.formattedText(ChatColor.YELLOW + ChatColor.translateAlternateColorCodes('&', messageLocation[0])));
            if (!messageLocation[1].equalsIgnoreCase("")) {
                lore.add("");
                lore.addAll(ChatUtils.formattedText(ChatColor.DARK_AQUA + "Location/Tip: " + ChatColor.translateAlternateColorCodes('&', messageLocation[1])));
            }
        } else {
            item = StatusItemUtil.goldStatusItem;
            meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName((isTutorialQuest() ? ChatColor.LIGHT_PURPLE : ChatColor.GOLD) + this.getQuestName());
            if (!isTutorialQuest()) {
                lore.add(ChatColor.GOLD + "" + ChatColor.BOLD + "MAIN STORY QUEST");
            } else {
                lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "TUTORIAL QUEST");
            }
            String[] messageLocation = RunicQuests.getFirstUncompletedGoalMessageAndLocation(this);
            lore.addAll(ChatUtils.formattedText(ChatColor.YELLOW + ChatColor.translateAlternateColorCodes('&', messageLocation[0])));
            if (!messageLocation[1].equalsIgnoreCase("")) {
                lore.add("");
                lore.addAll(ChatUtils.formattedText(ChatColor.DARK_AQUA + "Location/Tip: " + ChatColor.translateAlternateColorCodes('&', messageLocation[1])));
            }
        }
        lore.add("");
        lore.add(ChatColor.GRAY + "Level " + ChatColor.GRAY + "[" + this.getRequirements().getClassLvReq() + "]");
        lore.add("");
        lore.add(
                "" + ChatColor.GRAY + ChatColor.ITALIC + "Rewards " +
                        ChatColor.WHITE + ChatColor.ITALIC + this.getRewards().getExperienceReward() +
                        ChatColor.GRAY + ChatColor.ITALIC + " experience"
        );
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public Integer getCooldown() {
        return this.cooldown;
    }

    public void setCooldown(Integer cooldown) {
        this.cooldown = cooldown;
    }

    public QuestFirstNpc getFirstNPC() {
        return firstNPC;
    }

    public void setFirstNPC(QuestFirstNpc firstNPC) {
        this.firstNPC = firstNPC;
    }

    public List<QuestObjective> getObjectives() {
        return objectives;
    }

    public void setObjectives(List<QuestObjective> objectives) {
        this.objectives = objectives;
    }

    public Integer getQuestID() {
        return questID;
    }

    public void setQuestID(Integer questID) {
        this.questID = questID;
    }

    public String getQuestName() {
        return questName;
    }

    public void setQuestName(String questName) {
        this.questName = questName;
    }

    public QuestState getQuestState() {
        return state;
    }

    public QuestRequirements getRequirements() {
        return requirements;
    }

    public void setRequirements(QuestRequirements requirements) {
        this.requirements = requirements;
    }

    public QuestRewards getRewards() {
        return rewards;
    }

    public void setRewards(QuestRewards rewards) {
        this.rewards = rewards;
    }

    public QuestState getState() {
        return state;
    }

    public void setState(QuestState state) {
        this.state = state;
    }

    public boolean hasCooldown() {
        return this.cooldown != null;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }

    public boolean isSideQuest() {
        return sideQuest;
    }

    public void setSideQuest(boolean sideQuest) {
        this.sideQuest = sideQuest;
    }

    public boolean isTutorialQuest() {
        return this.tutorial;
    }

}