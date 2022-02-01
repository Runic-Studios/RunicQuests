package com.runicrealms.runicquests.task;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.runicrealms.plugin.utilities.ChatUtils;
import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.hologram.FirstNpcHoloType;
import com.runicrealms.runicquests.util.SpeechParser;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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
    public HologramTaskQueue(QuestResponse questResponse, @Nullable Quest quest, Location npcLocation, Player player, List<String> messages) {
        super();
        this.questResponse = questResponse;
        this.quest = quest;
        this.npcLocation = npcLocation;
        this.hologram = HologramsAPI.createHologram(Plugin.getInstance(), npcLocation);
        this.hologram.getVisibilityManager().setVisibleByDefault(false);
        this.player = player;
        this.speechParser = new SpeechParser(player);
        this.messages = messages;
        getTasks().addAll(createHologramRunnables());
    }

    /**
     *
     */
    private void hideQuestStatusHologram() {
        Map<Integer, Map<FirstNpcHoloType, Hologram>> hologramMap = Plugin.getHoloManager().getHologramMap();
        for (Hologram hologram : hologramMap.get(quest.getQuestID()).values()) {
            hologram.getVisibilityManager().hideTo(player);
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
            hideQuestStatusHologram();
        hologram.getVisibilityManager().showTo(player);
        List<Runnable> runnables = new ArrayList<>();
        List<String> messagesCloned = new ArrayList<>(messages); // all strings in Java are pass-by-reference, so we're cloning here to prevent side effects
        messagesCloned.replaceAll(s -> questResponse.getChatColor() + "&o" + s);
        int totalSpeechMessages = (int) messagesCloned.stream().filter(x -> !x.startsWith(questResponse.getChatColor() + "&o" + "//")).count(); // remove command-only lines from text count
        for (String message : messagesCloned) {
            runnables.add(() -> {
                hologram.clearLines();
                speechParser.updateParsedMessage("&7[" + (messagesCloned.indexOf(message) + 1) + "/" + totalSpeechMessages + "] " + message);

                if (speechParser.isChatMessage()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', questResponse.getChatColor() + speechParser.getParsedMessage()));
                } else {
                    List<String> formatted = ChatUtils.formattedText(speechParser.getParsedMessage(), 35);
                    hologram.teleport(this.npcLocation.clone().add(0, 2.5 + (0.25 * formatted.size()), 0)); // needs to sit 3 above ? 2.5 + 0.5 (
                    for (String s : formatted) {
                        hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', questResponse.getChatColor() + s));
                    }
                }

                speechParser.executeCommands();
            });
        }
        // todo: search queue.addTasks and setCompletedTask
        return runnables;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public enum QuestResponse {
        REQUIREMENTS_NOT_MET("&c"),
        STARTED("&6"),
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
