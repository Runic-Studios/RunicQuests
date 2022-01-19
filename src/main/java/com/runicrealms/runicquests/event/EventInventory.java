package com.runicrealms.runicquests.event;

import com.runicrealms.plugin.utilities.GUIUtil;
import com.runicrealms.runicquests.data.PlayerDataLoader;
import com.runicrealms.runicquests.data.QuestProfile;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.util.RunicCoreHook;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class EventInventory implements Listener {

    private static final int INVENTORY_SIZE = 45;
    private static final int QUEST_INVENTORY_FIRST_INDEX = 9;
    private static final String QUEST_MENU_TITLE = ChatColor.GOLD + "Quests";
    private static final Map<UUID, QuestMenuWrapper> playersInQuestGui = new HashMap<>();

    /**
     * This menu displays a list of available quests to the player
     *
     * @param player to display gui to
     * @param page   of the quest menu to display
     */
    public static void openQuestGui(Player player, Integer page) {
        boolean showRepeatableQuests = playersInQuestGui.get(player.getUniqueId()) != null && playersInQuestGui.get(player.getUniqueId()).isShowingRepeatableQuests();
        Map<Integer, ItemStack> items = new HashMap<>();
        QuestList questList = getSortedNonRepeatableQuests(player, showRepeatableQuests);
        List<Quest> quests = questList.getSortedQuests();
        if (page == 1)
            items.put(0, GUIUtil.closeButton());
        else
            items.put(0, backButton());
        items.put(4, infoPaper(questList.getStartedQuestCount(), questList.getCompletedQuestCount(), quests.size(), showRepeatableQuests));
        items.put(5, toggleShowRepeatableQuestsItem());
        items.put(8, forwardArrow());

        int location = (page - 1) * INVENTORY_SIZE; // holds our place in the list
        try {
            for (int i = 0; i < INVENTORY_SIZE; i++) {
                if ((location + i) < quests.size()) {
                    items.put(i + QUEST_INVENTORY_FIRST_INDEX, quests.get((location + i)).generateQuestIcon(player));
                }
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE + QUEST_INVENTORY_FIRST_INDEX,
                ChatColor.translateAlternateColorCodes('&', QUEST_MENU_TITLE + " (pg. " + page + ")"));
        for (Map.Entry<Integer, ItemStack> item : items.entrySet()) {
            inventory.setItem(item.getKey(), item.getValue());
        }
        player.openInventory(inventory);
        playersInQuestGui.put(player.getUniqueId(), new QuestMenuWrapper(page, showRepeatableQuests));
    }

    /**
     * Returns a list of quests sorted by level, adjust for player class, and other parameters
     *
     * @param player               to sort list for
     * @param showRepeatableQuests whether to display repeatable quests or not
     * @return a list of sorted quests
     */
    private static QuestList getSortedNonRepeatableQuests(Player player, boolean showRepeatableQuests) {
        QuestProfile profile = PlayerDataLoader.getPlayerQuestData(player.getUniqueId());
        List<Quest> startedQuests = new ArrayList<>();
        List<Quest> unstartedQuests = new ArrayList<>();
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
            if (!quest.getQuestState().hasStarted() && !quest.getQuestState().isCompleted()) {
                unstartedQuests.add(quest);
            }
            if (quest.getQuestState().isCompleted()) {
                completedQuests.add(quest);
            }
        }
        startedQuests.sort(Comparator.comparing(a -> a.getRequirements().getClassLvReq()));
        unstartedQuests.sort(Comparator.comparing(a -> a.getRequirements().getClassLvReq()));
        completedQuests.sort(Comparator.comparing(a -> a.getRequirements().getClassLvReq()));
        List<Quest> sorted = new ArrayList<>(startedQuests);
        sorted.addAll(unstartedQuests);
        sorted.addAll(completedQuests);
        return new QuestList(startedQuests.size(), completedQuests.size(), sorted);
    }

    private static ItemStack backButton() {
        ItemStack back = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = back.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.RED + "Return");
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "Return to the first page"));
        back.setItemMeta(meta);
        return back;
    }

    /**
     * This...
     *
     * @param startedQuests
     * @param completedQuests
     * @param totalQuests
     * @param repeatableMenu
     * @return
     */
    private static ItemStack infoPaper(int startedQuests, int completedQuests, int totalQuests, boolean repeatableMenu) {
        ItemStack infoPaper = new ItemStack(Material.PAPER);
        ItemMeta meta = infoPaper.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.GOLD + "Quest menu");
        meta.setLore(Arrays.asList
                (
                        ChatColor.GRAY + "Here you can view available quests!",
                        "",
                        ChatColor.WHITE + "" + startedQuests + " " + ChatColor.YELLOW + "quests in progress",
                        ChatColor.GREEN + "(" + ChatColor.WHITE + completedQuests + ChatColor.GREEN + "/" + totalQuests + ") Quests Completed",
                        "",
                        ChatColor.GRAY + "" + ChatColor.BOLD + "KEY:",
                        ChatColor.GOLD + "Gold" + ChatColor.GRAY + " quests are main story",
                        ChatColor.YELLOW + "Yellow" + ChatColor.GRAY + " quests are side quests",
                        ChatColor.AQUA + "Blue" + ChatColor.GRAY + " quests are repeatable",
                        ChatColor.RED + "Red" + ChatColor.GRAY + " quests are missing requirements",
                        ChatColor.GREEN + "Green" + ChatColor.GRAY + " quests are complete"
                ));
        infoPaper.setItemMeta(meta);
        return infoPaper;
    }

    private static ItemStack toggleShowRepeatableQuestsItem() {
        ItemStack infoPaper = new ItemStack(Material.LIGHT_BLUE_DYE);
        ItemMeta meta = infoPaper.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.GOLD + "Toggle Repeatable Quests");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "If toggled, only repeatable quests", ChatColor.GRAY + "will be shown!"));
        infoPaper.setItemMeta(meta);
        return infoPaper;
    }

    private static ItemStack forwardArrow() {
        ItemStack arrow = new ItemStack(Material.BROWN_STAINED_GLASS_PANE);
        ItemMeta meta = arrow.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.GOLD + "Next Page");
        arrow.setItemMeta(meta);
        return arrow;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        playersInQuestGui.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerJoinEvent event) {
        playersInQuestGui.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!playersInQuestGui.containsKey(event.getWhoClicked().getUniqueId())) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        UUID uuid = player.getUniqueId();
        event.setCancelled(true);
        if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR)
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().equals(GUIUtil.closeButton())) {
            player.closeInventory();
        } else if (event.getCurrentItem().equals(backButton())) {
            playersInQuestGui.put(uuid, new QuestMenuWrapper(1, false));
            openQuestGui(player, 1);
        } else if (event.getCurrentItem().equals(forwardArrow())) {
            openQuestGui(player, playersInQuestGui.get(uuid).getCurrentPage() + 1);
        } else if (event.getCurrentItem().equals(toggleShowRepeatableQuestsItem())) {
            playersInQuestGui.get((uuid)).setShowingRepeatableQuests(!playersInQuestGui.get(uuid).isShowingRepeatableQuests());
            openQuestGui(player, 1);
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
