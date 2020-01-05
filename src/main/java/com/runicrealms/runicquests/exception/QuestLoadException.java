package com.runicrealms.runicquests.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@SuppressWarnings("serial")
public class QuestLoadException extends Exception {

	/*
	 * This exception is just meant to hold info on why quest loading doesn't work
	 */
	
	private List<String> messages = new ArrayList<String>();
	private String error = null;

	public QuestLoadException(String... messages) {
		for (String message : messages) {
			this.messages.add(message);
		}
	}

	public QuestLoadException setErrorMessage(String message) {
		error = message;
		return this;
	}

	public void addMessage(String... messages) {
		for (String message : messages) {
			this.messages.add(message);
		}
	}

	public void displayToOnlinePlayers() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.sendMessage(ChatColor.RED + this.getMessage());
			if (this.getMessage().contains("unknown")) {
				player.sendMessage(ChatColor.RED + "To get more info about this error, check the console for the QuestLoadException error trace.");
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
