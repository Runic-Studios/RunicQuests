package com.runicrealms.runicquests.quests.hologram;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.rdb.event.CharacterLoadedEvent;
import com.runicrealms.runicquests.RunicQuests;
import com.runicrealms.runicquests.api.QuestCompleteEvent;
import com.runicrealms.runicquests.model.QuestProfileData;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.util.RunicCoreHook;
import com.runicrealms.runicquests.util.StatusItemUtil;
import com.runicrealms.runicrestart.RunicRestart;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HoloManager implements Listener {
    private static final int DELAY = 3; // seconds
    private final Map<Integer, Map<FirstNpcHoloType, Hologram>> hologramMap;

    public HoloManager() {
        hologramMap = new HashMap<>();
        RunicQuests.getInstance().getServer().getPluginManager().registerEvents(this, RunicQuests.getInstance());
        loadHolograms();
    }

    /**
     * This method determines which color of hologram to activate for each quest in a quest profile
     *
     * @param player The player of the quest profile
     * @param quest  A quest in the player's quest profile
     * @return A hologram to activate
     */
    public Hologram determineHoloByStatus(Player player, Quest quest) {
        Map<FirstNpcHoloType, Hologram> types = hologramMap.get(quest.getQuestID());
        if (quest.getQuestState().isCompleted())
            return types.get(FirstNpcHoloType.GREEN);
        if (!RunicCoreHook.hasCompletedRequiredQuests(player, quest.getRequirements().getCompletedQuestsRequirement())
                || !RunicCoreHook.hasCompletedLevelRequirement(player, quest.getRequirements().getClassLvReq())
                || (quest.getRequirements().hasCraftingRequirement() && !RunicCoreHook.hasProfession(player, quest.getRequirements().getCraftingProfessionType()))
                || (quest.getRequirements().hasClassTypeRequirement() && !RunicCoreHook.isRequiredClass(quest.getRequirements().getClassTypeRequirement(), player)))
            return types.get(FirstNpcHoloType.RED);
        if (quest.isRepeatable())
            return types.get(FirstNpcHoloType.BLUE);
        if (quest.isSideQuest())
            return types.get(FirstNpcHoloType.YELLOW);
        return types.get(FirstNpcHoloType.GOLD);
    }

    public Map<Integer, Map<FirstNpcHoloType, Hologram>> getHologramMap() {
        return hologramMap;
    }

    /**
     * This method creates invisible holograms above all quest-givers on server startup for every type of status
     * (repeatable, side, main, can't yet start, etc.)
     * Then they are shown to each player based on the player's quest progress
     */
    private void loadHolograms() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(RunicQuests.getInstance(), () -> {
            for (Quest quest : RunicQuests.getAPI().getBlankQuestList()) {
                if (quest == null) continue;

                Location loc = quest.getFirstNPC().getLocation().clone().add(0, 3.25, 0);
                Hologram hologram = HologramsAPI.createHologram(RunicQuests.getInstance(), loc);
                hologram.getVisibilityManager().setVisibleByDefault(false);

                // main hologram based on quest type
                FirstNpcHoloType firstNpcHoloType;
                if (quest.isRepeatable())
                    firstNpcHoloType = FirstNpcHoloType.BLUE;
                else if (quest.isSideQuest())
                    firstNpcHoloType = FirstNpcHoloType.YELLOW;
                else
                    firstNpcHoloType = FirstNpcHoloType.GOLD;

                hologram.appendItemLine(firstNpcHoloType.getItemStack());
                hologramMap.put(quest.getQuestID(), new HashMap<>());
                hologramMap.get(quest.getQuestID()).put(firstNpcHoloType, hologram);

                Hologram hologramGreen = HologramsAPI.createHologram(RunicQuests.getInstance(), loc);
                hologramGreen.getVisibilityManager().setVisibleByDefault(false);
                hologramGreen.appendItemLine(StatusItemUtil.greenStatusItem);
                hologramMap.get(quest.getQuestID()).put(FirstNpcHoloType.GREEN, hologramGreen);

                Hologram hologramRed = HologramsAPI.createHologram(RunicQuests.getInstance(), loc);
                hologramRed.getVisibilityManager().setVisibleByDefault(false);
                hologramRed.appendItemLine(StatusItemUtil.redStatusItem);
                hologramMap.get(quest.getQuestID()).put(FirstNpcHoloType.RED, hologramRed);
            }

            RunicRestart.getAPI().markPluginLoaded("quests");
        }, 20L); // delay to wait for quests to load
    }

    @EventHandler(priority = EventPriority.HIGH) // late
    public void onCharacterSelect(CharacterLoadedEvent event) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicQuests.getInstance(),
                () -> refreshStatusHolograms(event.getPlayer()), DELAY * 20L);
    }

    @EventHandler(priority = EventPriority.HIGH) // late
    public void onLevel(PlayerLevelChangeEvent event) {
        if (!RunicDatabase.getAPI().getCharacterAPI().getLoadedCharacters().contains(event.getPlayer().getUniqueId()))
            return;
        if (RunicQuests.getAPI().getQuestProfile(event.getPlayer().getUniqueId()) == null)
            return; // Player not loaded yet (login level change)
        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicQuests.getInstance(),
                () -> refreshStatusHolograms(event.getPlayer()), DELAY * 20L);
    }

    @EventHandler(priority = EventPriority.HIGH) // late
    public void onQuestComplete(QuestCompleteEvent event) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicQuests.getInstance(),
                () -> refreshStatusHolograms(event.getPlayer()), DELAY * 20L);
    }

    /**
     * Updates all current floating quest status holograms to be accurate
     *
     * @param player to update holograms for
     */
    private void refreshStatusHolograms(Player player) {
        int slot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(player.getUniqueId());
        QuestProfileData profileData = RunicQuests.getAPI().getQuestProfile(player.getUniqueId());
        if (profileData == null) return;
        Map<Integer, List<Quest>> questsMap = profileData.getQuestsMap();
        if (questsMap == null) return;
        List<Quest> quests = questsMap.get(slot);
        if (quests == null) return; // Something did not load
        for (Quest quest : quests) {
            if (hologramMap.get(quest.getQuestID()) != null) {
                for (Hologram hologram : hologramMap.get(quest.getQuestID()).values()) { // reset previous holograms
                    hologram.getVisibilityManager().hideTo(player);
                }
                determineHoloByStatus(player, quest).getVisibilityManager().showTo(player);
            }
        }
    }
}
