package com.runicrealms.runicquests.quests;

import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.data.QuestProfile;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import com.runicrealms.runicquests.quests.objective.QuestObjectiveTalk;
import com.runicrealms.runicquests.util.RunicCoreHook;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Quest implements Cloneable {

    /*
     * Contains all the quest values which are needed.
     * All values except "QuestState state" can be parsed from config
     */

    private final String questName;
    private final QuestFirstNpc firstNPC;
    private final List<QuestObjective> objectives;
    private final QuestRewards rewards;
    private final QuestState state;
    private final Integer questID;
    private final QuestRequirements requirements;
    private final boolean sideQuest;
    private final boolean repeatable;
    private final Integer cooldown;

    /**
     * This...
     *
     * @param questName
     * @param firstNPC
     * @param objectives
     * @param rewards
     * @param questID
     * @param requirements
     * @param sideQuest
     * @param repeatable
     * @param cooldown
     */
    public Quest(String questName, QuestFirstNpc firstNPC, ArrayList<QuestObjective> objectives, QuestRewards rewards,
                 Integer questID, QuestRequirements requirements, boolean sideQuest, boolean repeatable, Integer cooldown) {
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
    }

    public String getQuestName() {
        return questName;
    }

    public QuestFirstNpc getFirstNPC() {
        return firstNPC;
    }

    public List<QuestObjective> getObjectives() {
        return objectives;
    }

    public QuestRewards getRewards() {
        return rewards;
    }

    public QuestState getQuestState() {
        return state;
    }

    public Integer getQuestID() {
        return questID;
    }

    public boolean isSideQuest() {
        return sideQuest;
    }

    public QuestRequirements getRequirements() {
        return requirements;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public boolean hasCooldown() {
        return this.cooldown != null;
    }

    public Integer getCooldown() {
        return this.cooldown;
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
        return new Quest(this.questName, this.firstNPC.clone(), newObjectives, this.rewards, this.questID, this.requirements, this.sideQuest, this.repeatable, this.cooldown);
    }

    /**
     * @param player
     * @return
     */
    public ItemStack generateQuestIcon(Player player) {
        QuestProfile profile = Plugin.getQuestProfile(player.getUniqueId().toString());
        if (this.getQuestState().isCompleted()) {
            ItemStack item = new ItemStack(Material.SLIME_BALL);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            List<String> lore = new ArrayList<>();
            meta.setDisplayName(ChatColor.GREEN + this.getQuestName());
            lore.add(ChatColor.DARK_GREEN + "Completed");
            lore.add(ChatColor.GRAY + "Level " + this.getRequirements().getClassLvReq());
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        } else if (!RunicCoreHook.isReqClassLv(player, this.getRequirements().getClassLvReq())) {
            ItemStack item = new ItemStack(Material.BARRIER);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            List<String> lore = new ArrayList<>();
            meta.setDisplayName(ChatColor.RED + this.getQuestName());
            lore.add(ChatColor.DARK_RED + "You do not meet the level requirements!");
            lore.add(ChatColor.GRAY + "Level " + this.getRequirements().getClassLvReq());
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        } else if (this.isRepeatable()) {
            boolean canStart = Plugin.canStartRepeatableQuest(player.getUniqueId(), this.getQuestID());
            ItemStack item = new ItemStack(canStart ? Material.LIGHT_BLUE_DYE : Material.SLIME_BALL);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            List<String> lore = new ArrayList<>();
            meta.setDisplayName(ChatColor.AQUA + this.getQuestName());
            String[] messageLocation = Plugin.getFirstUncompletedGoalMessageAndLocation(this, profile);
            lore.add(ChatColor.YELLOW + ChatColor.translateAlternateColorCodes('&', messageLocation[0]));
            if (!messageLocation[1].equalsIgnoreCase("")) {
                lore.add(ChatColor.YELLOW + "Location: " + ChatColor.translateAlternateColorCodes('&', messageLocation[1]));
            }
            lore.add(ChatColor.BLUE + "Repeatable!");
            lore.add(ChatColor.GRAY + "Level " + this.getRequirements().getClassLvReq());
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        } else if (this.isSideQuest()) {
            ItemStack item = new ItemStack(Material.YELLOW_DYE);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            List<String> lore = new ArrayList<>();
            meta.setDisplayName(ChatColor.YELLOW + this.getQuestName());
            String[] messageLocation = Plugin.getFirstUncompletedGoalMessageAndLocation(this, profile);
            lore.add(ChatColor.YELLOW + ChatColor.translateAlternateColorCodes('&', messageLocation[0]));
            if (!messageLocation[1].equalsIgnoreCase("")) {
                lore.add(ChatColor.YELLOW + "Location: " + ChatColor.translateAlternateColorCodes('&', messageLocation[1]));
            }
            lore.add(ChatColor.GRAY + "Level " + this.getRequirements().getClassLvReq());
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        } else {
            ItemStack item = new ItemStack(Material.ORANGE_DYE);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            List<String> lore = new ArrayList<>();
            meta.setDisplayName(ChatColor.GOLD + this.getQuestName());
            String[] messageLocation = Plugin.getFirstUncompletedGoalMessageAndLocation(this, profile);
            lore.add(ChatColor.YELLOW + ChatColor.translateAlternateColorCodes('&', messageLocation[0]));
            if (!messageLocation[1].equalsIgnoreCase("")) {
                lore.add(ChatColor.YELLOW + "Location: " + ChatColor.translateAlternateColorCodes('&', messageLocation[1]));
            }
            lore.add(ChatColor.GRAY + "Level " + this.getRequirements().getClassLvReq());
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        }
    }

}