package com.runicrealms.runicquests.event;

import com.runicrealms.runicquests.data.PlayerDataLoader;
import com.runicrealms.runicquests.data.QuestProfile;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.util.RunicCoreHook;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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

    private static final int INVENTORY_SIZE = 54;
    private static final String QUEST_MENU_TITLE = ChatColor.GOLD + "Quests";
    private static final Map<UUID, QuestMenuWrapper> playersInQuestGui = new HashMap<>();

    /**
     *
     * @param player
     * @param page
     */
    public static void openQuestGui(Player player, Integer page) {
        boolean showRepeatableQuests = playersInQuestGui.get(player.getUniqueId()) != null && playersInQuestGui.get(player.getUniqueId()).isShowingRepeatableQuests();
        Map<Integer, ItemStack> items = new HashMap<>();
        List<Quest> quests = getSortedNonRepeatableQuests(player, showRepeatableQuests);
        items.put(0, backButton());
        items.put(4, infoPaper());
        items.put(5, toggleShowRepeatableQuestsItem());
        items.put(8, forwardArrow());
        for (int i = ((page - 1) * (INVENTORY_SIZE - 1)) + 9; i < page * INVENTORY_SIZE; i++) {
            if (i + 1 <= quests.size()) {
                items.put((i - (page - 1) * (INVENTORY_SIZE - 1)), quests.get(i).generateQuestIcon(player));
            }
        }
        Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE,
                ChatColor.translateAlternateColorCodes('&', QUEST_MENU_TITLE + " (pg. " + page + ")"));
        for (Map.Entry<Integer, ItemStack> item : items.entrySet()) {
            inventory.setItem(item.getKey(), item.getValue());
        }
        player.closeInventory();
        player.openInventory(inventory);
        playersInQuestGui.put(player.getUniqueId(), new QuestMenuWrapper(page, showRepeatableQuests));
    }

    /**
     * Returns a list of quests sorted by level, adjust for player class, and other parameters
     *
     * @param player
     * @param showRepeatableQuests whether to display repeatable quests or not
     * @return a list of sorted quests
     */
    private static List<Quest> getSortedNonRepeatableQuests(Player player, boolean showRepeatableQuests) {
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
        return sorted;
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
        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().equals(backButton())) {
            playersInQuestGui.put(uuid, new QuestMenuWrapper(1, false));
            openQuestGui(player, 1);
        } else if (event.getCurrentItem().equals(forwardArrow())) {
            openQuestGui(player, playersInQuestGui.get(uuid).getCurrentPage() + 1);
        } else if (event.getCurrentItem().equals(toggleShowRepeatableQuestsItem())) {
            playersInQuestGui.get((uuid)).setShowingRepeatableQuests(!playersInQuestGui.get(uuid).isShowingRepeatableQuests());
            openQuestGui(player, playersInQuestGui.get(uuid).getCurrentPage());
        }
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

    private static ItemStack infoPaper() {
        ItemStack infoPaper = new ItemStack(Material.PAPER);
        ItemMeta meta = infoPaper.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.GOLD + "Quest menu");
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "Visit the next page"));
        infoPaper.setItemMeta(meta);
        return infoPaper;
    }

    private static ItemStack toggleShowRepeatableQuestsItem() {
        ItemStack infoPaper = new ItemStack(Material.LIGHT_BLUE_DYE);
        ItemMeta meta = infoPaper.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.GOLD + "Toggle Repeatable Quests");
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "If toggled, only repeatable quests will be shown!"));
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
}
