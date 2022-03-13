package com.runicrealms.runicquests.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class QuestLoadException extends Exception {

    /*
     * This exception is just meant to hold info on why quest loading doesn't work
     */

    private final List<String> messages = new ArrayList<>();
    private String error = null;

    public QuestLoadException(String... messages) {
        Collections.addAll(this.messages, messages);
    }

    public QuestLoadException setErrorMessage(String message) {
        error = message;
        return this;
    }

    public void addMessage(String... messages) {
        Collections.addAll(this.messages, messages);
    }

    public void displayToOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(ChatColor.RED + this.getMessage());
            if (this.error != null) {
                player.sendMessage(ChatColor.RED + "Check the console for a detailed QuestLoadException error trace.");
            }
        }
    }

    public void displayToConsole() {
        Bukkit.getLogger().log(Level.SEVERE, this.getMessage());
        if (this.error != null) {
            Bukkit.getLogger().log(Level.WARNING, this.error);
        }
    }

    public String getMessage() {
        StringBuilder builder = new StringBuilder();
        for (int i = this.messages.size() - 1; i >= 0; i--) {
            builder.append(this.messages.get(i));
            if (i != 0) {
                builder.append(" -> ");
            }
        }
        return builder.toString();
    }

}
