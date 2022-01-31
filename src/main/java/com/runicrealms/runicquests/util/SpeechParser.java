package com.runicrealms.runicquests.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * SpeechParser object to parse through quest text. Can create one object per list of speech, then call
 * 'update' method
 */
public class SpeechParser {

    /*
    if any message begins with this symbol, it will be sent as a chat message (instead of hologram)
     */
    private static final String CHAT_PREFIX = "$";

    private final Player player;
    private final List<String> commandsToExecute = Collections.synchronizedList(new ArrayList<>());
    private boolean chatMessage = false;
    private String parsedMessage;

    /**
     * Construct a speech parser without any parsed message
     *
     * @param player to display speech to
     */
    public SpeechParser(Player player) {
        this.player = player;
        this.parsedMessage = "";
    }

    /**
     * Construct a speech parser with the given message
     *
     * @param message to parse
     * @param player  to display message to
     */
    public SpeechParser(String message, Player player) {
        this.parsedMessage = parseMessage(message);
        this.player = player;
    }

    public boolean isChatMessage() {
        return chatMessage;
    }

    public String getParsedMessage() {
        return parsedMessage;
    }

    public void updateParsedMessage(String messageToParse) {
        this.chatMessage = false; // by default
        this.parsedMessage = parseMessage(messageToParse);
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * Parses the given string. Checks for a prefix string to determine whether this should be a chat message
     * or a hologram message. Also strips all commands from the message to be executed afterward.
     *
     * @param message to parse
     * @return the message without commands
     */
    private String parseMessage(String message) {
//        if (message.startsWith(CHAT_PREFIX)) {
//            message = message.replaceAll("\\$", ""); // remove it
//            this.chatMessage = true;
//        }
        String[] parts = message.replaceAll("%player%", player.getName()).split("//");
        if (parts.length != 1)
            commandsToExecute.addAll(Arrays.asList(parts).subList(1, parts.length));
        return parts[0];
    }

    /**
     * Executes all commands that were scraped from the chat message, then clears the commands queue
     */
    public void executeCommands() {
        for (String command : commandsToExecute) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
        commandsToExecute.clear();
    }
}
