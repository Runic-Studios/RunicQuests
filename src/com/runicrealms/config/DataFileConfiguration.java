package com.runicrealms.config;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

import com.runicrealms.Plugin;
import com.runicrealms.quests.Quest;
import com.runicrealms.quests.QuestObjective;

public class DataFileConfiguration {
	
	public FileConfiguration config;
	public File file;
	
	public DataFileConfiguration(FileConfiguration config, File file) {
		this.config = config;
		this.file = file;
	}
	
	public void save() {
		try {
			this.config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveToConfig(List<Quest> quests) {
		for (Quest quest : quests) {
			config.set(quest.questID + ".started", quest.state.started);
			config.set(quest.questID + ".completed", quest.state.completed);
			for (QuestObjective objective : quest.objectives.keySet()) {
				config.set(quest.questID + ".objectives." + objective.objectiveNumber, objective.completed);
			}
		}
		this.save();
	}
	
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
	
}
