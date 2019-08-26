package com.runicrealms.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.runicrealms.Plugin;
import com.runicrealms.player.QuestProfile;

public class PlayerJoinQuitEvent implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (Plugin.CACHE_PLAYER_DATA) {
			for (QuestProfile profile : Plugin.getQuestProfiles()) {
				if (profile.getPlayerUUID().equalsIgnoreCase(event.getPlayer().getUniqueId().toString())) {
					return;
				}
			}
			Plugin.getQuestProfiles().add(new QuestProfile(event.getPlayer().getUniqueId().toString()));
		} else {
			Plugin.getQuestProfiles().add(new QuestProfile(event.getPlayer().getUniqueId().toString()));
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (!Plugin.CACHE_PLAYER_DATA) {
			Plugin.getQuestProfiles().remove(Plugin.getQuestProfiles().indexOf(Plugin.getQuestProfile(event.getPlayer().getUniqueId().toString())));
		}
	}

}
