package com.runicrealms.config;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.runicrealms.Plugin;

public class ConfigLoader {
	
	public static FileConfiguration getYamlConfigFile(String fileName) {
		FileConfiguration config;
		File file;
		file = new File(Plugin.getInstance().getDataFolder(), fileName);
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
	
}
