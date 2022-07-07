package com.runicrealms.runicquests.quests.hologram;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.api.QuestCompleteEvent;
import com.runicrealms.runicquests.api.RunicQuestsAPI;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.util.RunicCoreHook;
import com.runicrealms.runicquests.util.StatusItemUtil;
import com.runicrealms.runicrestart.api.RunicRestartApi;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;

import java.util.HashMap;
import java.util.Map;

public class HoloManager implements Listener {

    private final Map<Integer, Map<FirstNpcHoloType, Hologram>> hologramMap;

    public HoloManager() {
        hologramMap = new HashMap<>();
        loadHolograms();
    }

    /**
     * This method creates invisible holograms above all quest-givers on server startup
     */
    private void loadHolograms() {

        Bukkit.getScheduler().scheduleSyncDelayedTask(Plugin.getInstance(), () -> {
            for (Quest q : RunicQuestsAPI.getBlankQuestList()) {

                if (q == null)
                    continue;

                Location loc = q.getFirstNPC().getLocation().clone().add(0, 3.25, 0);
                Hologram hologram = HologramsAPI.createHologram(Plugin.getInstance(), loc);
                hologram.getVisibilityManager().setVisibleByDefault(false);

                // main hologram based on quest type
                FirstNpcHoloType firstNpcHoloType;
                if (q.isRepeatable())
                    firstNpcHoloType = FirstNpcHoloType.BLUE;
                else if (q.isSideQuest())
                    firstNpcHoloType = FirstNpcHoloType.YELLOW;
                else
                    firstNpcHoloType = FirstNpcHoloType.GOLD;

                hologram.appendItemLine(firstNpcHoloType.getItemStack());
                hologramMap.put(q.getQuestID(), new HashMap<>());
                hologramMap.get(q.getQuestID()).put(firstNpcHoloType, hologram);

                Hologram hologramGreen = HologramsAPI.createHologram(Plugin.getInstance(), loc);
                hologramGreen.getVisibilityManager().setVisibleByDefault(false);
                hologramGreen.appendItemLine(StatusItemUtil.greenStatusItem);
                hologramMap.get(q.getQuestID()).put(FirstNpcHoloType.GREEN, hologramGreen);

                Hologram hologramRed = HologramsAPI.createHologram(Plugin.getInstance(), loc);
                hologramRed.getVisibilityManager().setVisibleByDefault(false);
                hologramRed.appendItemLine(StatusItemUtil.redStatusItem);
                hologramMap.get(q.getQuestID()).put(FirstNpcHoloType.RED, hologramRed);
            }

            RunicRestartApi.markPluginLoaded("quests");
        }, 20L); // delay to wait for quests to load
    }

    /**
     * No need for a character select event, since PlayerLevelChangeEvent is always triggered on character select
     */
    @EventHandler
    public void onLevel(PlayerLevelChangeEvent e) {
        refreshStatusHolograms(e.getPlayer());
    }

    @EventHandler
    public void onQuestComplete(QuestCompleteEvent e) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(Plugin.getInstance(), () -> refreshStatusHolograms(e.getPlayer()), 20L);
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
                || !RunicCoreHook.isReqClassLv(player, quest.getRequirements().getClassLvReq()))
            return types.get(FirstNpcHoloType.RED);
        if (quest.isRepeatable())
            return types.get(FirstNpcHoloType.BLUE);
        if (quest.isSideQuest())
            return types.get(FirstNpcHoloType.YELLOW);
        return types.get(FirstNpcHoloType.GOLD);
    }

    /**
     * @param player
     */
    private void refreshStatusHolograms(Player player) {
        for (Quest quest : RunicQuestsAPI.getQuestProfile(player).getQuests()) {
            if (hologramMap.get(quest.getQuestID()) != null) {
                for (Hologram hologram : hologramMap.get(quest.getQuestID()).values()) { // reset previous holograms
                    hologram.getVisibilityManager().hideTo(player);
                }
                determineHoloByStatus(player, quest).getVisibilityManager().showTo(player);
            }
        }
    }

    public Map<Integer, Map<FirstNpcHoloType, Hologram>> getHologramMap() {
        return hologramMap;
    }
}
