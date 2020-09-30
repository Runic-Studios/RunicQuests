package com.runicrealms.runicquests.passivenpcs;

import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.config.ConfigLoader;
import com.runicrealms.runicquests.quests.Quest;

import java.io.File;
import java.util.List;

public class PassiveNpcLoader {

    public static List<PassiveNpc> getUnusedPassiveNpcList() {
    File folder = ConfigLoader.getSubFolder(Plugin.getInstance().getDataFolder(), "passive");
		for (File passive : folder.listFiles()) {
		    
        }
		return null;
    }

}
