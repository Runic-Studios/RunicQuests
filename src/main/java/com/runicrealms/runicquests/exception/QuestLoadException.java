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

	public QuestLoadException(String... messages) {
		for (String message : messages) {
			this.messages.add(message);
		}
	}

	public void addMessage(String... messages) {
		for (String message : messages) {
			this.messages.add(message);
		}
	}

	public void displayToOnlinePlayers() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.sendMessage(ChatColor.RED + this.getMessage());
		}
	}

	public void displayToConsole() {
		Bukkit.getLogger().log(Level.INFO, this.getMessage());
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
