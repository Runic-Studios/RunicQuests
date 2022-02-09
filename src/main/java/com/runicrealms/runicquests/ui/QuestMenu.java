package com.runicrealms.runicquests.ui;

import com.runicrealms.plugin.utilities.GUIUtil;
import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.data.PlayerDataLoader;
import com.runicrealms.runicquests.data.QuestProfile;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.util.RunicCoreHook;
import com.runicrealms.runicquests.util.StatusItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class QuestMenu implements InventoryHolder {

    private static final int INVENTORY_SIZE = 45;
    private static final int MAX_PAGES = 2;
    private static final int MAX_REPEATABLE_PAGES = 1;
    private static final int QUEST_INVENTORY_FIRST_INDEX = 9;
    private static final String QUEST_MENU_TITLE = ChatColor.GOLD + "Quests";
    private final Inventory inventory;
    private final Player player;
    private boolean showRepeatableQuests;
    private int currentPage;

    public QuestMenu(Player player) {
        this.showRepeatableQuests = false;
        this.currentPage = 1;
        this.inventory = Bukkit.createInventory
                (
                        this,
                        INVENTORY_SIZE + QUEST_INVENTORY_FIRST_INDEX,
                        ChatColor.translateAlternateColorCodes('&', QUEST_MENU_TITLE)
                );
        this.player = player;
        updateMenu();
    }

    /**
     * Creates the informational ItemStack used in the quest GUI
     *
     * @param startedQuests   the number of quests the player has which are in-progress
     * @param completedQuests the number of quests the player has completed
     * @param totalQuests     the total number of quests in the category
     * @param repeatableMenu  toggle the category (e.g., non-repeatable or repeatable quests)
     * @return an ItemStack to use in a GUI
     */
    private static ItemStack infoPaper(int startedQuests, int completedQuests, int totalQuests, boolean repeatableMenu) {
        ItemStack infoPaper = new ItemStack(Material.PAPER);
        ItemMeta meta = infoPaper.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.GOLD + "Quest menu");
        if (!repeatableMenu) {
            meta.setLore(Arrays.asList
                    (
                            ChatColor.GRAY + "Here you can view available quests!",
                            "",
                            ChatColor.WHITE + "" + startedQuests + " " + ChatColor.YELLOW + "quest(s) in progress",
                            ChatColor.GREEN + "(" + ChatColor.WHITE + completedQuests + ChatColor.GREEN + "/" + totalQuests + ") Quests Completed",
                            "",
                            ChatColor.GRAY + "" + ChatColor.BOLD + "KEY:",
                            ChatColor.GOLD + "● Gold" + ChatColor.GRAY + " quests are " + ChatColor.GOLD + "main story",
                            ChatColor.YELLOW + "● Yellow" + ChatColor.GRAY + " quests are " + ChatColor.YELLOW + "side quests",
                            ChatColor.AQUA + "● Blue" + ChatColor.GRAY + " quests are " + ChatColor.AQUA + "repeatable",
                            ChatColor.RED + "● Red" + ChatColor.GRAY + " quests are " + ChatColor.RED + "missing requirements",
                            ChatColor.GREEN + "● Green" + ChatColor.GRAY + " quests are " + ChatColor.GREEN + "complete!"
                    ));
        } else {
            meta.setLore(Arrays.asList
                    (
                            ChatColor.GRAY + "Here you can view repeatable quests!",
                            "",
                            ChatColor.WHITE + "" + startedQuests + " " + ChatColor.YELLOW + "quest(s) in progress",
                            ChatColor.GREEN + "(" + ChatColor.WHITE + completedQuests + ChatColor.GREEN + "/" + totalQuests + ") Quests Completed",
                            "",
                            ChatColor.GRAY + "" + ChatColor.BOLD + "KEY:",
                            ChatColor.AQUA + "● Blue" + ChatColor.GRAY + " quests are " + ChatColor.AQUA + "repeatable",
                            ChatColor.GREEN + "● Green" + ChatColor.GRAY + " quests are " + ChatColor.GREEN + "on cooldown"
                    ));
        }
        infoPaper.setItemMeta(meta);
        return infoPaper;
    }

    public static ItemStack toggleShowRepeatableQuestsItem() {
        ItemStack infoPaper = StatusItemUtil.blueStatusItem().clone();
        ItemMeta meta = infoPaper.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.AQUA + "Toggle Repeatable Quests");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "If toggled, only repeatable quests", ChatColor.GRAY + "will be shown!"));
        infoPaper.setItemMeta(meta);
        return infoPaper;
    }

    public static ItemStack forwardArrow() {
        ItemStack arrow = new ItemStack(Material.BROWN_STAINED_GLASS_PANE);
        ItemMeta meta = arrow.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.GOLD + "Next Page");
        arrow.setItemMeta(meta);
        return arrow;
    }

    /**
     * Returns a list of quests sorted by level, adjust for player class, and other parameters
     *
     * @return a list of sorted quests
     */
    private QuestList sortQuests() {
        QuestProfile profile = PlayerDataLoader.getPlayerQuestData(player.getUniqueId());
        List<Quest> startedQuests = new ArrayList<>();
        List<Quest> notStartedQuests = new ArrayList<>();
        List<Quest> completedQuests = new ArrayList<>();
        for (Quest quest : profile.getQuests()) {
            if (!showRepeatableQuests && quest.isRepeatable()) continue;
            if (showRepeatableQuests && !quest.isRepeatable()) continue;
            // skip class quests that don't match class
            if (quest.getRequirements().hasClassTypeRequirement()) {
                if (!RunicCoreHook.isRequiredClass(quest.getRequirements().getClassTypeRequirement(), player)) {
                    continue;
                }
            }
            // skip profession quests that don't match profession
            if (quest.getRequirements().hasCraftingRequirement()) {
                if (!RunicCoreHook.hasProfession(player, quest.getRequirements().getCraftingProfessionType())) {
                    continue;
                }
            }
            if (quest.getQuestState().hasStarted() && !quest.getQuestState().isCompleted()) {
                startedQuests.add(quest);
            }
            if (!quest.getQuestState().hasStarted()
                    && !quest.getQuestState().isCompleted()
                    && (!quest.isRepeatable()
                    || (quest.isRepeatable() && Plugin.canStartRepeatableQuest(player.getUniqueId(), quest.getQuestID())))) {
                notStartedQuests.add(quest);
            }
            if (quest.getQuestState().isCompleted() || (quest.isRepeatable() && !Plugin.canStartRepeatableQuest(player.getUniqueId(), quest.getQuestID()))) {
                completedQuests.add(quest);
            }
        }
        startedQuests.sort(Comparator.comparing(a -> a.getRequirements().getClassLvReq()));
        notStartedQuests.sort(Comparator.comparing(a -> a.getRequirements().getClassLvReq()));
        completedQuests.sort(Comparator.comparing(a -> a.getRequirements().getClassLvReq()));
        List<Quest> sorted = new ArrayList<>(startedQuests);
        sorted.addAll(notStartedQuests);
        sorted.addAll(completedQuests);
        return new QuestList(startedQuests.size(), completedQuests.size(), sorted);
    }

    /**
     * This menu displays a list of available quests to the player
     */
    public void updateMenu() {
        this.inventory.clear();
        QuestList questList = sortQuests();
        List<Quest> quests = questList.getSortedQuests();
        int[] slots = new int[]{1, 2, 3, 6, 7};
        for (int slot : slots) {
            this.inventory.setItem(slot, GUIUtil.borderItem());
        }
        if (currentPage == 1)
            this.inventory.setItem(0, GUIUtil.closeButton());
        else
            this.inventory.setItem(0, GUIUtil.backButton());
        this.inventory.setItem(4, infoPaper(questList.getStartedQuestCount(), questList.getCompletedQuestCount(), quests.size(), showRepeatableQuests));
        this.inventory.setItem(5, toggleShowRepeatableQuestsItem());
        this.inventory.setItem(8, forwardArrow());

        int location = (currentPage - 1) * INVENTORY_SIZE; // holds our place in the list
        try {
            for (int i = 0; i < INVENTORY_SIZE; i++) {
                if ((location + i) < quests.size()) {
                    this.inventory.setItem(i + QUEST_INVENTORY_FIRST_INDEX, quests.get((location + i)).generateQuestIcon(player));
                }
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    public void openFirstPage() {
        this.setCurrentPage(1);
        this.updateMenu();
        player.openInventory(inventory);
    }

    public void openNextPage() {
        if (showRepeatableQuests && ((currentPage + 1) > MAX_REPEATABLE_PAGES)) return;
        if (!showRepeatableQuests && ((currentPage + 1) > MAX_PAGES)) return;
        this.setCurrentPage(currentPage + 1);
        this.updateMenu();
        player.openInventory(inventory);
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public Player getPlayer() {
        return this.player;
    }

    public boolean getShowRepeatableQuests() {
        return this.showRepeatableQuests;
    }

    public void setShowRepeatableQuests(boolean showRepeatableQuests) {
        this.showRepeatableQuests = showRepeatableQuests;
    }

    public int getCurrentPage() {
        return this.currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    static class QuestList {

        private final int startedQuestCount;
        private final int completedQuestCount;
        private final List<Quest> sortedQuests;

        public QuestList(int startedQuestCount, int completedQuestCount, List<Quest> sortedQuests) {
            this.startedQuestCount = startedQuestCount;
            this.completedQuestCount = completedQuestCount;
            this.sortedQuests = sortedQuests;
        }

        public int getStartedQuestCount() {
            return startedQuestCount;
        }

        public int getCompletedQuestCount() {
            return completedQuestCount;
        }

        public List<Quest> getSortedQuests() {
            return sortedQuests;
        }
    }
}