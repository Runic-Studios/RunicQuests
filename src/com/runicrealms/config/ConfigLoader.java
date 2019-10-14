package com.runicrealms.config;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.runicrealms.Plugin;

public class ConfigLoader {
	
	private static FileConfiguration mainConfig;

	public static FileConfiguration getYamlConfigFile(String fileName, File folder) {
		FileConfiguration config;
		File file;
		file = new File(folder, fileName);
		config = new YamlConfiguration();
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			config.load(file);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return config;
	}

	public static File getSubFolder(File folder, String subfolder) {
		for (File file : folder.listFiles()) {
			if (file.getName().equalsIgnoreCase(subfolder)) {
				return file;
			}
		}
		return null;
	}
	
	public static void loadMainConfig() {
		mainConfig = getYamlConfigFile("config.yml", Plugin.getInstance().getDataFolder());
		if (!mainConfig.contains("npc-message-delay")) {
			mainConfig.set("npc-message-delay", 3);
			try {
				mainConfig.save(new File(Plugin.getInstance().getDataFolder(), "config.yml"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (!mainConfig.contains("cache-player-data")) {
			mainConfig.set("cache-player-data", true);
			try {
				mainConfig.save(new File(Plugin.getInstance().getDataFolder(), "config.yml"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void initDirs() {
		if (!Plugin.getInstance().getDataFolder().exists()) {
			Plugin.getInstance().getDataFolder().mkdir();
		}
		File folder = ConfigLoader.getSubFolder(Plugin.getInstance().getDataFolder(), "users");
		if (folder == null) {
			folder = new File(Plugin.getInstance().getDataFolder(), "users");
			folder.mkdir();
		}
		folder = ConfigLoader.getSubFolder(Plugin.getInstance().getDataFolder(), "quests");
		if (folder == null) {
			folder = new File(Plugin.getInstance().getDataFolder(), "quests");
			folder.mkdir();
		}
	}
	
	public static FileConfiguration getMainConfig() {
		return mainConfig;
	}

}
