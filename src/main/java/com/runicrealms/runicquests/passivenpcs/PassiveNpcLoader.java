package com.runicrealms.runicquests.passivenpcs;

import com.runicrealms.runicquests.Plugin;
import com.runicrealms.runicquests.config.ConfigLoader;
import com.runicrealms.runicquests.config.QuestLoader;
import com.runicrealms.runicquests.exception.QuestLoadException;
import com.runicrealms.runicquests.quests.Quest;
import com.runicrealms.runicquests.quests.QuestFirstNpc;
import com.runicrealms.runicquests.quests.QuestRequirements;
import com.runicrealms.runicquests.quests.QuestRewards;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PassiveNpcLoader {
    private static List<PassiveNpc> cachedPassiveNpcs = null;
    public static List<PassiveNpc> getUnusedPassiveNpcList() {
    File folder = ConfigLoader.getSubFolder(Plugin.getInstance().getDataFolder(), "passive");
		for (File passive : folder.listFiles()) {
            if (!passive.isDirectory()) {
                PassiveNpc loadedPassiveNpc = null;

                try {
                    loadedPassiveNpc = PassiveNpcLoader.loadPassiveNpc(ConfigLoader.getYamlConfigFile(passive.getName(), folder));
                } catch (Exception exception) {
                  
                }

            }
        }
		return null;
    }
    public static PassiveNpc loadPassiveNpc(FileConfiguration config)  {
        try {

        } catch (Exception exception) {

        }
        return null;
    }
}
