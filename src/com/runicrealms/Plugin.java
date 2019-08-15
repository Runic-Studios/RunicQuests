package com.runicrealms;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import com.runicrealms.config.PlayerDataLoader;
import com.runicrealms.player.QuestProfile;

public class Plugin extends JavaPlugin {

	private static Plugin plugin;
	private static List<QuestProfile> questProfiles = new ArrayList<QuestProfile>();
	
	public static final String WORLD_NAME = "Alterra";
	
	@Override
	public void onEnable() {
		plugin = this;
		PlayerDataLoader.initDirs();
	}
	
	public static Plugin getInstance() {
		return plugin;
	}
	
	public static List<QuestProfile> getQuestProfiles() {
		return questProfiles;
	}
	
	public static QuestProfile getQuestProfile(String uuid) {
		for (QuestProfile profile : questProfiles) {
			if (profile.playerUUID.equalsIgnoreCase(uuid)) {
				return profile;
			}
		}
		return null;
	}
	
}
