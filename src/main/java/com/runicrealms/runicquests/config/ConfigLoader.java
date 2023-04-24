package com.runicrealms.runicquests.config;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.runicquests.RunicQuests;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigLoader {

    // Main config.yml file
    private static FileConfiguration mainConfig;

    // Set default values for config.yml
    public static void loadMainConfig() {
        mainConfig = RunicCore.getConfigAPI().getYamlConfigFromFile("config.yml", RunicQuests.getInstance().getDataFolder());
        if (!mainConfig.contains("npc-message-delay")) {
            mainConfig.set("npc-message-delay", 3);
            try {
                mainConfig.save(new File(RunicQuests.getInstance().getDataFolder(), "config.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Create users and quests folders if they have not been created
    public static void initDirs() {
        if (!RunicQuests.getInstance().getDataFolder().exists()) {
            RunicQuests.getInstance().getDataFolder().mkdir();
        }
        File folder = RunicCore.getConfigAPI().getSubFolder(RunicQuests.getInstance().getDataFolder(), "quests");
        if (folder == null) {
            folder = new File(RunicQuests.getInstance().getDataFolder(), "quests");
            folder.mkdir();
        }
        File folderPassive = RunicCore.getConfigAPI().getSubFolder(RunicQuests.getInstance().getDataFolder(), "passive");
        if (folderPassive == null) {
            folderPassive = new File(RunicQuests.getInstance().getDataFolder(), "passive");
            folderPassive.mkdir();
        }
    }

    public static FileConfiguration getMainConfig() {
        return mainConfig;
    }

}
