package com.runicrealms.plugin.runicquests.quests.hologram;

import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.rdb.event.CharacterLoadedEvent;
import com.runicrealms.plugin.runicquests.RunicQuests;
import com.runicrealms.plugin.runicquests.api.QuestCompleteEvent;
import com.runicrealms.plugin.runicquests.model.QuestProfileData;
import com.runicrealms.plugin.runicquests.quests.Quest;
import com.runicrealms.plugin.runicquests.util.RunicCoreHook;
import com.runicrealms.plugin.runicquests.util.StatusItemUtil;
import com.runicrealms.plugin.runicrestart.RunicRestart;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    @NotNull
    public FirstNpcHoloType determineStatus(Player player, Quest quest) {
        if (quest.getQuestState().isCompleted())
            return FirstNpcHoloType.GREEN;
        if (!RunicCoreHook.hasCompletedRequiredQuests(player, quest.getRequirements().getCompletedQuestsRequirement())
                || !RunicCoreHook.hasCompletedLevelRequirement(player, quest.getRequirements().getClassLvReq())
                || (quest.getRequirements().hasCraftingRequirement() && !RunicCoreHook.hasProfession(player, quest.getRequirements().getCraftingProfessionType()))
                || (quest.getRequirements().hasClassTypeRequirement() && !RunicCoreHook.isRequiredClass(quest.getRequirements().getClassTypeRequirement(), player)))
            return FirstNpcHoloType.RED;
        if (quest.isRepeatable())
            return FirstNpcHoloType.BLUE;
        if (quest.isSideQuest())
            return FirstNpcHoloType.YELLOW;
        return FirstNpcHoloType.GOLD;
    }

    /**
     * This method determines which color of hologram to activate for each quest in a quest profile
     *
     * @param status The status of the quest
     * @param quest  A quest in the player's quest profile
     * @return A hologram to activate
     */
    @Nullable
    public Hologram determineHoloByStatus(FirstNpcHoloType status, Quest quest) {
        Map<FirstNpcHoloType, Hologram> types = hologramMap.get(quest.getQuestID());
        return types.get(status);
    }

    /**
     * This method determines which color of hologram to activate for each quest in a quest profile
     *
     * @param player The player of the quest profile
     * @param quest  A quest in the player's quest profile
     * @return A hologram to activate
     */
    @Nullable
    public Hologram determineHoloByStatus(Player player, Quest quest) {
        return this.determineHoloByStatus(this.determineStatus(player, quest), quest);
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
                Hologram hologram = HolographicDisplaysAPI.get(RunicQuests.getInstance()).createHologram(loc);
                hologram.getVisibilitySettings().setGlobalVisibility(VisibilitySettings.Visibility.HIDDEN);

                // main hologram based on quest type
                FirstNpcHoloType firstNpcHoloType;
                if (quest.isRepeatable())
                    firstNpcHoloType = FirstNpcHoloType.BLUE;
                else if (quest.isSideQuest())
                    firstNpcHoloType = FirstNpcHoloType.YELLOW;
                else
                    firstNpcHoloType = FirstNpcHoloType.GOLD;

                hologram.getLines().appendItem(firstNpcHoloType.getItemStack());
                hologramMap.put(quest.getQuestID(), new HashMap<>());
                hologramMap.get(quest.getQuestID()).put(firstNpcHoloType, hologram);

                Hologram hologramGreen = HolographicDisplaysAPI.get(RunicQuests.getInstance()).createHologram(loc);
                hologramGreen.getVisibilitySettings().setGlobalVisibility(VisibilitySettings.Visibility.HIDDEN);
                hologramGreen.getLines().appendItem(StatusItemUtil.greenStatusItem);
                hologramMap.get(quest.getQuestID()).put(FirstNpcHoloType.GREEN, hologramGreen);

                Hologram hologramRed = HolographicDisplaysAPI.get(RunicQuests.getInstance()).createHologram(loc);
                hologramRed.getVisibilitySettings().setGlobalVisibility(VisibilitySettings.Visibility.VISIBLE);
                hologramRed.getLines().appendItem(StatusItemUtil.redStatusItem);
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
    public void refreshStatusHolograms(@NotNull Player player) {
        Runnable task = () -> {
            int slot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(player.getUniqueId());
            QuestProfileData profileData = RunicQuests.getAPI().getQuestProfile(player.getUniqueId());
            if (profileData == null) return;
            Map<Integer, List<Quest>> questsMap = profileData.getQuestsMap();
            if (questsMap == null) return;
            List<Quest> quests = questsMap.get(slot);
            if (quests == null) return; // Something did not load

            Map<Integer, Pair<Quest, FirstNpcHoloType>> display = new HashMap<>();

            for (Quest quest : quests) {
                if (hologramMap.get(quest.getQuestID()) == null) {
                    continue;
                }

                for (Hologram hologram : hologramMap.get(quest.getQuestID()).values()) { // reset previous holograms
                    hologram.getVisibilitySettings().setIndividualVisibility(player, VisibilitySettings.Visibility.HIDDEN);
                }

                FirstNpcHoloType status = this.determineStatus(player, quest);

                Hologram hologram = determineHoloByStatus(status, quest);

                if (hologram == null) {
                    continue;
                }

                Pair<Quest, FirstNpcHoloType> questData = display.get(quest.getFirstNPC().getNpcId());

                if (questData == null || ((questData.second == FirstNpcHoloType.GREEN || questData.second == FirstNpcHoloType.RED) || questData.first.getRequirements().getClassLvReq() > quest.getRequirements().getClassLvReq())) {
                    display.put(quest.getFirstNPC().getNpcId(), new Pair<>(quest, status));
                }
            }

            if (display.isEmpty()) {
                return;
            }

            for (Pair<Quest, FirstNpcHoloType> pair : display.values()) {
                Hologram hologram = this.determineHoloByStatus(pair.second, pair.first);

                if (hologram == null) {
                    throw new IllegalStateException("There was no hologram for the " + pair.second.name() + " type on the " + pair.first.getQuestName() + " quest!");
                }

                hologram.getVisibilitySettings().setIndividualVisibility(player, VisibilitySettings.Visibility.VISIBLE);
            }
        };

        if (Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTaskAsynchronously(RunicQuests.getInstance(), task);
        } else {
            task.run();
        }
    }
}
