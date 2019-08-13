package com.runicrealms;

import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin {

	/*
	 * 
	 * TODO - Make quest objectives have an execute
	 * TODO - Add extra function in ConfigLoader to load firstNPCs (with execute)
	 * 
	 */
	
	private static Plugin plugin;
	
	public static final String WORLD_NAME = "Alterra";
	
	@Override
	public void onEnable() {
		plugin = this;
	}
	
	public static Plugin getInstance() {
		return plugin;
	}
	
}
