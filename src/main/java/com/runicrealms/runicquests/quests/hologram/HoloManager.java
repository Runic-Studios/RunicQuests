package com.runicrealms.runicquests.quests.hologram;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.api.QuestCompleteEvent;
import com.runicrealms.runicquests.api.RunicQuestsAPI;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.util.RunicCoreHook;
import com.runicrealms.runicrestart.api.RunicRestartApi;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.inventory.ItemStack;

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
                if (q.isRepeatable()) {
                    firstNpcHoloType = FirstNpcHoloType.BLUE;
                    hologram.appendItemLine(new ItemStack(firstNpcHoloType.getMaterial()));
                } else if (q.isSideQuest()) {
                    firstNpcHoloType = FirstNpcHoloType.YELLOW;
                    hologram.appendItemLine(new ItemStack(firstNpcHoloType.getMaterial()));
                } else {
                    firstNpcHoloType = FirstNpcHoloType.GOLD;
                    hologram.appendItemLine(new ItemStack(firstNpcHoloType.getMaterial()));
                }

                hologramMap.put(q.getQuestID(), new HashMap<>());
                hologramMap.get(q.getQuestID()).put(firstNpcHoloType, hologram);

                Hologram hologramGreen = HologramsAPI.createHologram(Plugin.getInstance(), loc);
                hologramGreen.getVisibilityManager().setVisibleByDefault(false);
                hologramGreen.appendItemLine(new ItemStack(Material.GREEN_DYE));
                hologramMap.get(q.getQuestID()).put(FirstNpcHoloType.GREEN, hologramGreen);

                Hologram hologramRed = HologramsAPI.createHologram(Plugin.getInstance(), loc);
                hologramRed.getVisibilityManager().setVisibleByDefault(false);
                hologramRed.appendItemLine(new ItemStack(Material.RED_DYE));
                hologramMap.get(q.getQuestID()).put(FirstNpcHoloType.RED, hologramRed);
            }

            RunicRestartApi.markPluginLoaded("quests");
        }, 20L); // delay to wait for quests to load
    }

    @EventHandler
    public void onLoad(CharacterLoadEvent e) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(Plugin.getInstance(), () -> refreshHolograms(e.getPlayer()), 20L);
    }

    @EventHandler
    public void onLevel(PlayerLevelChangeEvent e) {
        refreshHolograms(e.getPlayer());
    }

    @EventHandler
    public void onQuestComplete(QuestCompleteEvent e) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(Plugin.getInstance(), () -> refreshHolograms(e.getPlayer()), 20L);
    }

    /**
     * This method does determines which color of hologram to activate for each quest in a quest profile
     * @param player The player of the quest profile
     * @param quest A quest in the player's quest profile
     * @return A hologram to activate
     */
    private Hologram determineHoloByStatus(Player player, Quest quest) {
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

    private void refreshHolograms(Player player) {
        for (Quest q : RunicQuestsAPI.getQuestProfile(player).getQuests()) {
            if (hologramMap.get(q.getQuestID()) != null) {
                for (Hologram hologram : hologramMap.get(q.getQuestID()).values()) { // reset previous holograms
                    hologram.getVisibilityManager().hideTo(player);
                }
                determineHoloByStatus(player, q).getVisibilityManager().showTo(player);
            }
        }
    }
}
