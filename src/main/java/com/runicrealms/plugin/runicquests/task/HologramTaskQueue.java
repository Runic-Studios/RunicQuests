package com.runicrealms.plugin.runicquests.task;

import com.runicrealms.plugin.common.util.ChatUtils;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.runicquests.util.SpeechParser;
import com.runicrealms.plugin.runicquests.RunicQuests;
import com.runicrealms.plugin.runicquests.quests.Quest;
import com.runicrealms.plugin.runicquests.quests.hologram.FirstNpcHoloType;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Extends the TaskQueue system and, instead of sending the player chat messages,
 * HologramTaskQueue manipulates the contents of a client-sided hologram to the player
 */
public class HologramTaskQueue extends TaskQueue {

    private final QuestResponse questResponse;
    private final Quest quest;
    private final Hologram hologram;
    private final Integer npcId;
    private final Location npcLocation;
    private final Player player;
    private final SpeechParser speechParser;
    private final List<String> messages;

    /**
     * Create a HologramTaskQueue which extends the base TaskQueue to keep track of a hologram
     *
     * @param npcLocation   the location of the npc to display the hologram over
     * @param player        the player who triggered the queue
     * @param messages      the list of speech messages to display
     * @param questResponse determines the color of the hologram / text based on the state of the quest
     */
    public HologramTaskQueue(QuestResponse questResponse, @Nullable Quest quest, @Nullable Integer npcId,
                             Location npcLocation, Player player, List<String> messages) {
        super();
        this.questResponse = questResponse;
        this.quest = quest;
        this.npcId = npcId;
        this.npcLocation = npcLocation;
        this.hologram = HolographicDisplaysAPI.get(RunicQuests.getInstance()).createHologram(npcLocation);
        this.hologram.getVisibilitySettings().setGlobalVisibility(VisibilitySettings.Visibility.HIDDEN);
        this.player = player;
        this.speechParser = new SpeechParser(player);
        this.messages = messages;
        getTasks().addAll(createHologramRunnables());
        List<HologramTaskQueue> currentTaskQueues = TaskQueueCleanupListener.getCurrentQueuesForUuid(player.getUniqueId());
        currentTaskQueues.add(this);
        TaskQueueCleanupListener.CURRENT_TASK_QUEUES.putIfAbsent(player.getUniqueId(), currentTaskQueues);
    }

    @Override
    public void cancel() {
        super.cancel();
        if (!this.hologram.isDeleted())
            this.hologram.delete();
    }

    /**
     * Hides quest status holograms during dialogue
     */
    private void changeQuestStatusHolograms(boolean display) {
        if (npcId == null) return;
        int slot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(player.getUniqueId());
        List<Quest> quests = RunicQuests.getAPI().getQuestProfile(player.getUniqueId()).getQuestsMap().get(slot);
        for (Quest quest : quests) {
            if (!quest.getFirstNPC().getNpcId().equals(npcId)) continue;
            if (quest.equals(this.quest)) continue;
            cleanUpStatusHologram(display, quest);
        }
        cleanUpStatusHologram(display, this.quest);
    }

    /**
     * For the given quest, determines a status hologram if the status hologram should be displayed, otherwise hides all status holograms
     *
     * @param display whether to hide the status hologram or display it
     * @param quest   which quest to check
     */
    private void cleanUpStatusHologram(boolean display, Quest quest) {
        Map<Integer, Map<FirstNpcHoloType, Hologram>> hologramMap = RunicQuests.getHoloManager().getHologramMap();
        for (Hologram hologram : hologramMap.get(quest.getQuestID()).values()) {
            if (display)
                RunicQuests.getHoloManager().determineHoloByStatus(player, quest).getVisibilitySettings().setIndividualVisibility(player, VisibilitySettings.Visibility.VISIBLE);
            else
                hologram.getVisibilitySettings().setIndividualVisibility(player, VisibilitySettings.Visibility.HIDDEN);
        }
    }

    /**
     * Static method to add quest text to the given hologram as a list of runnables. Player can speed up the queue
     * by right-clicking the Npc again, and we add a 'delete' runnable at the end to remove the text hologram.
     *
     * @return a list of runnables to change the hologram contents
     */
    private List<Runnable> createHologramRunnables() {
        if (quest != null)
            changeQuestStatusHolograms(false);
        hologram.getVisibilitySettings().setIndividualVisibility(player, VisibilitySettings.Visibility.VISIBLE);
        List<Runnable> runnables = new ArrayList<>();
        List<String> messagesCloned = new ArrayList<>(messages); // all strings in Java are pass-by-reference, so we're cloning here to prevent side effects
        messagesCloned.replaceAll(s -> questResponse.getChatColor() + "&o" + s);
        int totalSpeechMessages = (int) messagesCloned.stream().filter(x -> !x.startsWith(questResponse.getChatColor() + "&o" + "//")).count(); // remove command-only lines from text count
        for (String message : messagesCloned) {
            runnables.add(() -> {
                hologram.getLines().clear();
                speechParser.updateParsedMessage("&7[" + (messagesCloned.indexOf(message) + 1) + "/" + totalSpeechMessages + "] " + message);
                if (speechParser.isChatMessage()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', questResponse.getChatColor() + speechParser.getParsedMessage()));
                } else {
                    if (!message.equals(messagesCloned.get(0))) // don't play sound on first click
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.25f, 1.5f);
                    List<String> formatted = ChatUtils.formattedText(speechParser.getParsedMessage(), 35);
                    hologram.setPosition(this.npcLocation.clone().add(0, 2.5 + (0.25 * formatted.size()), 0)); // needs to sit 3 above ? 2.5 + 0.5 (
                    for (String s : formatted) {
                        hologram.getLines().appendText(ChatColor.translateAlternateColorCodes('&', questResponse.getChatColor() + s));
                    }
                }

                speechParser.executeCommands();
            });
        }
        if (questResponse == QuestResponse.REQUIREMENTS_NOT_MET || questResponse == QuestResponse.IDLE)
            runnables.add(() -> {
                this.getHologram().delete();
                changeQuestStatusHolograms(true);
                TaskQueueCleanupListener.getCurrentQueuesForUuid(player.getUniqueId()).remove(this);
            });
        return runnables;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public enum QuestResponse {
        REQUIREMENTS_NOT_MET("&c"),
        STARTED("&6"),
        IDLE("&6"),
        COMPLETED("&a");

        private final String chatColor;

        QuestResponse(String chatColor) {
            this.chatColor = chatColor;
        }

        public String getChatColor() {
            return chatColor;
        }
    }
}
