package com.runicrealms.config;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

import com.runicrealms.Plugin;
import com.runicrealms.quests.Quest;
import com.runicrealms.quests.objective.QuestObjective;

public class DataFileConfiguration {
	
	/*
	 * This class is meant to bound a File and a FileConfiguration
	 */
	
	private FileConfiguration config;
	private File file;
	
	public DataFileConfiguration(FileConfiguration config, File file) {
		this.config = config;
		this.file = file;
	}
	
	// Just saves the FileConfiguration to the File
	public void saveToFile() {
		try {
			this.config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Writes a List<Quest> to the FileConfiguration, then writes the FileConfiguration to File
	public void saveToConfig(List<Quest> quests) {
		for (Quest quest : quests) {
			config.set(quest.getQuestID() + ".started", quest.getQuestState().hasStarted());
			config.set(quest.getQuestID() + ".completed", quest.getQuestState().isCompleted());
			config.set(quest.getQuestID() + ".first-npc-state", quest.getFirstNPC().getState().getName());
			for (QuestObjective objective : quest.getObjectives()) {
				config.set(quest.getQuestID() + ".objectives." + objective.getObjectiveNumber() + "", objective.isCompleted());
			}
		}
		this.saveToFile();
	}
	
	// Static method to get a File from file system
	public static DataFileConfiguration getFile(String fileName) {
		File folder = ConfigLoader.getSubFolder(Plugin.getInstance().getDataFolder(), "users");
		for (File file : folder.listFiles()) {
			if (file.getName().equalsIgnoreCase(fileName)) {
				return new DataFileConfiguration(ConfigLoader.getYamlConfigFile(file.getName(), folder), file);
			}
		}
		File file = new File(folder, fileName);
		try {
			file.createNewFile();
			return new DataFileConfiguration(ConfigLoader.getYamlConfigFile(file.getName(), folder), file);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}	
	}
	
	public FileConfiguration getConfig() {
		return this.config;
	}
	
	public File getFile() {
		return this.file;
	}
	
}
