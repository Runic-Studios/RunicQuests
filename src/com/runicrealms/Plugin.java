package com.runicrealms;

import org.bukkit.plugin.java.JavaPlugin;

import com.runicrealms.config.PlayerDataLoader;

public class Plugin extends JavaPlugin {

	private static Plugin plugin;
	
	public static final String WORLD_NAME = "Alterra";
	
	@Override
	public void onEnable() {
		plugin = this;
		PlayerDataLoader.initDirs();
	}
	
	public static Plugin getInstance() {
		return plugin;
	}
	
}
