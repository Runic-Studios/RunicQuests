package com.runicrealms.runicquests.ui;

import com.runicrealms.plugin.common.util.ChatUtils;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.utilities.GUIUtil;
import com.runicrealms.runicquests.RunicQuests;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.util.QuestsUtil;
import com.runicrealms.runicquests.util.RunicCoreHook;
import com.runicrealms.runicquests.util.StatusItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class QuestMenu implements InventoryHolder {
    public static final ItemStack ACTIVE_QUEST_ITEM = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
    public static final ItemStack TOGGLE_SHOW_REPEATABLE_QUESTS_ITEM = StatusItemUtil.blueStatusItem.clone();
    public static final ItemStack DISABLE_COMPASS_ITEM = new ItemStack(Material.COMPASS);
    public static final ItemStack TRACK_COMPASS_ITEM = new ItemStack(Material.WRITABLE_BOOK);
    private static final int INVENTORY_SIZE = 45;
    private static final int MAX_PAGES = 2;
    private static final int MAX_REPEATABLE_PAGES = 1;
    private static final int QUEST_INVENTORY_FIRST_INDEX = 9;
    private static final String QUEST_MENU_TITLE = ChatColor.GOLD + "Quests";

    static {
        ItemMeta repeatableMeta = TOGGLE_SHOW_REPEATABLE_QUESTS_ITEM.getItemMeta();
        assert repeatableMeta != null;
        repeatableMeta.setDisplayName(ChatColor.AQUA + "Toggle Repeatable Quests");
        repeatableMeta.setLore(ChatUtils.formattedText(ChatColor.GRAY + "If toggled, only repeatable quests will be shown!"));
        TOGGLE_SHOW_REPEATABLE_QUESTS_ITEM.setItemMeta(repeatableMeta);

        CompassMeta compassMeta = (CompassMeta) DISABLE_COMPASS_ITEM.getItemMeta();
        assert compassMeta != null;
        compassMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
        compassMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        compassMeta.setDisplayName(ChatColor.RED + "Disable Compass Tracking");
        compassMeta.setLore(ChatUtils.formattedText(ChatColor.GRAY + "&7Turn your compass back into the quest book! You can always click on a quest to track it."));
        DISABLE_COMPASS_ITEM.setItemMeta(compassMeta);

        ItemMeta compassItemMeta = TRACK_COMPASS_ITEM.getItemMeta();
        assert compassItemMeta != null;
        compassItemMeta.setDisplayName(ChatColor.AQUA + "Compass Tracking");
        List<String> lore = ChatUtils.formattedText(ChatColor.GRAY + "Click on a quest to begin compass tracking. " +
                "Your quest book will turn into a compass that leads you to your destination.");
        compassItemMeta.setLore(lore);
        TRACK_COMPASS_ITEM.setItemMeta(compassItemMeta);

        ItemMeta activeMeta = ACTIVE_QUEST_ITEM.getItemMeta();
        assert activeMeta != null;
        activeMeta.setDisplayName(ChatColor.GREEN + "Active Quests");
        activeMeta.setLore(ChatUtils.formattedText(ChatColor.GRAY + "Your active quests appear to the left!"));
        ACTIVE_QUEST_ITEM.setItemMeta(activeMeta);
    }

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
     * @param uuid            of the player
     * @param startedQuests   the number of quests the player has which are in-progress
     * @param completedQuests the number of quests the player has completed
     * @param totalQuests     the total number of quests in the category
     * @param repeatableMenu  toggle the category (e.g., non-repeatable or repeatable quests)
     * @return an ItemStack to use in a GUI
     */
    private static ItemStack infoPaper(UUID uuid, int startedQuests, int completedQuests, int totalQuests, boolean repeatableMenu) {
        ItemStack infoPaper = new ItemStack(Material.PAPER);
        ItemMeta meta = infoPaper.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.GOLD + "Quest menu");
        if (!repeatableMenu) {
            meta.setLore(Arrays.asList
                    (
                            ChatColor.YELLOW + "" + ChatColor.BOLD + "POINTS: " + ChatColor.WHITE + ChatColor.BOLD + QuestsUtil.calculateQuestPoints(uuid),
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

    public int getCurrentPage() {
        return this.currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
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

    public void openFirstPage() {
        this.setCurrentPage(1);
        Bukkit.getScheduler().runTaskAsynchronously(RunicQuests.getInstance(), () -> {
            this.updateMenu();
            Bukkit.getScheduler().runTask(RunicQuests.getInstance(), () -> player.openInventory(inventory));
        });
    }

    public void openNextPage() {
        if (showRepeatableQuests && ((currentPage + 1) > MAX_REPEATABLE_PAGES)) return;
        if (!showRepeatableQuests && ((currentPage + 1) > MAX_PAGES)) return;
        this.setCurrentPage(currentPage + 1);
        Bukkit.getScheduler().runTaskAsynchronously(RunicQuests.getInstance(), () -> {
            this.updateMenu();
            Bukkit.getScheduler().runTask(RunicQuests.getInstance(), () -> player.openInventory(inventory));
        });
    }

    /**
     * Returns a list of quests sorted by level, adjust for player class, and other parameters
     *
     * @return a list of sorted quests
     */
    private QuestList sortQuests() {
        int slot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(player.getUniqueId());
        List<Quest> questsFromProfile = RunicQuests.getAPI().getQuestProfile(player.getUniqueId()).getQuestsMap().get(slot);
        List<Quest> startedQuests = new ArrayList<>();
        List<Quest> notStartedQuests = new ArrayList<>();
        List<Quest> completedQuests = new ArrayList<>();
        for (Quest quest : questsFromProfile) {
            if (!showRepeatableQuests && quest.isRepeatable()) continue;
            if (showRepeatableQuests && !quest.isRepeatable()) continue;
            // Skip class quests that don't match class
            if (quest.getRequirements().hasClassTypeRequirement()) {
                if (!RunicCoreHook.isRequiredClass(quest.getRequirements().getClassTypeRequirement(), player)) {
                    continue;
                }
            }
            // Skip profession quests that don't match profession
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
                    || (quest.isRepeatable() && QuestsUtil.canStartRepeatableQuest(player.getUniqueId(), quest)))) {
                notStartedQuests.add(quest);
            }
            if (quest.getQuestState().isCompleted() || (quest.isRepeatable() && !QuestsUtil.canStartRepeatableQuest(player.getUniqueId(), quest))) {
                completedQuests.add(quest);
            }
        }
        // Add all quests to the sorted list
        startedQuests.sort(Comparator.comparing(a -> a.getRequirements().getClassLvReq()));
        // If there are any in-progress quests, we add a dummy quest to act as a divider placeholder
        if (startedQuests.size() > 0) {
            startedQuests.add(new Quest("dummy"));
        }
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
            this.inventory.setItem(slot, GUIUtil.BORDER_ITEM);
        }
        if (currentPage == 1)
            this.inventory.setItem(0, GUIUtil.CLOSE_BUTTON);
        else
            this.inventory.setItem(0, GUIUtil.BACK_BUTTON);
        this.inventory.setItem(4, infoPaper(this.player.getUniqueId(), questList.getStartedQuestCount(), questList.getCompletedQuestCount(), quests.size(), showRepeatableQuests));
        this.inventory.setItem(5, TOGGLE_SHOW_REPEATABLE_QUESTS_ITEM);
        this.inventory.setItem(8, GUIUtil.FORWARD_BUTTON);
        if (CompassManager.getCompasses().containsKey(player)
                && CompassManager.getCompasses().get(player) != null
                && CompassManager.getCompasses().get(player).getLocation() != null) {
            this.inventory.setItem(2, DISABLE_COMPASS_ITEM);
        } else {
            this.inventory.setItem(2, TRACK_COMPASS_ITEM);
        }

        int location = (currentPage - 1) * INVENTORY_SIZE; // holds our place in the list of pages
        try {
            for (int i = 0; i < INVENTORY_SIZE; i++) {
                if ((location + i) < quests.size()) {
                    if (this.getInventory().firstEmpty() == -1) continue;
                    if (quests.get(location + i).getQuestName().equalsIgnoreCase("dummy")) {
                        this.inventory.setItem(this.getInventory().firstEmpty(), ACTIVE_QUEST_ITEM);
                    } else {
                        this.inventory.setItem(this.getInventory().firstEmpty(), quests.get((location + i)).generateQuestIcon(player));
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
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

        public int getCompletedQuestCount() {
            return completedQuestCount;
        }

        public List<Quest> getSortedQuests() {
            return sortedQuests;
        }

        public int getStartedQuestCount() {
            return startedQuestCount;
        }
    }
}