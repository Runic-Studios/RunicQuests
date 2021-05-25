package com.runicrealms.runicquests.listeners.questwriterlisteners;

import com.runicrealms.runicitems.item.RunicItemGeneric;
import com.runicrealms.runicitems.item.event.RunicItemGenericTriggerEvent;
import com.runicrealms.runicitems.item.util.ClickTrigger;
import com.runicrealms.runicquests.data.PlayerDataLoader;
import com.runicrealms.runicquests.data.QuestProfile;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.QuestState;
import com.runicrealms.runicquests.quests.location.BoxLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

public class AncientWhalePowderListener implements Listener {

    /*
    This listener is for the quest "Seeking Revenge", when the player clicks the item-
    "Ancient Whale Powder", they will be teleported to a specific location
     */

    private static final Location LOCATION = new Location(Bukkit.getWorld("Alterra"), -2285.700, 41,-1006.300);
    private static final List<PotionEffect> EFFECTS = Arrays.asList(new PotionEffect(PotionEffectType.CONFUSION, 60, 0, false, false, false),
            new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, false, false, false));
    private static final BoxLocation BOX = new BoxLocation(new Location(Bukkit.getWorld("Alterra"), -1811, 29, -706), new Location(Bukkit.getWorld("Alterra"), -1879, 52, -755));

    @EventHandler
    public void onRunicItemGenericTrigger(RunicItemGenericTriggerEvent event) {
        RunicItemGeneric item = event.getItem();

        if (event.getTrigger() != ClickTrigger.RIGHT_CLICK) {
            return;
        }

        if (!item.getTriggers().containsValue("seaking-revenge:ancient-whale-powder")) {
            return;
        }

        Player player = event.getPlayer();

        QuestProfile profile = PlayerDataLoader.getPlayerQuestData(player.getUniqueId());

        boolean isCorrectStage = false;
        for (Quest quest : profile.getQuests()) {
            if (quest.getQuestID() != 05222021) { //05222021 is the quest id of seeking revenge
                continue;
            }

            QuestState state = quest.getQuestState();

            if (!state.hasStarted() || state.isCompleted()) {
                continue;
            }

            isCorrectStage = true;
            break;
        }

        if (!isCorrectStage) {
            return;
        }

        if (!BOX.hasReachedLocation(player)) {
            return;
        }

        player.addPotionEffects(EFFECTS);
        player.teleport(LOCATION);

        item.setCount(item.getCount() - 1);
    }
}