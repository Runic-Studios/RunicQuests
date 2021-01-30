package com.runicrealms.runicquests.passivenpcs;

import com.runicrealms.runicquests.Plugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PassiveNpcHandler {
    private final Map<Integer, PassiveNpc> passiveNPCs;

    public PassiveNpcHandler() {
        this.passiveNPCs = new HashMap<>();

        File passivenpcs = new File(Plugin.getInstance().getDataFolder(), "passivenpcs");

        if (!passivenpcs.exists()) {
            passivenpcs.mkdir();
        }

        for (File file : passivenpcs.listFiles()) {
            try {
                this.loadFromYML(file);
            } catch (PassiveNpcException e) {
                e.printStackTrace();
            }
        }
    }

    public PassiveNpc getNPC(int id) {
        return this.passiveNPCs.get(id);
    }

    private void loadFromYML(File file) throws PassiveNpcException {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (!config.contains("npc-id") || !config.contains("npc-plugin") || !config.contains("npc-name") || !config.contains("dialogue.1")) {
            throw new PassiveNpcException("Invalid npc yml: " + config.getName());
        }

        int id = config.getInt("npc-id");

        boolean isRunic;
        String plugin = config.getString("npc-plugin");
        if (plugin.equalsIgnoreCase("Runic")) {
            isRunic = true;
        } else if (plugin.equalsIgnoreCase("Citizens")) {
            isRunic = false;
        } else {
            throw new PassiveNpcException("Invalid npc plugin in yml:" + config.getName());
        }

        String name = config.getString("npc-name");

        boolean overrideText;
        if (config.contains("add-npc-name")) {
            overrideText = config.getBoolean("add-npc-name");
        } else {
            overrideText = false;
        }

        List<List<String>> dialogue = new ArrayList<>();
        for (String section : config.getConfigurationSection("dialogue").getKeys(false)) {
            dialogue.add(config.getStringList("dialogue." + section));
        }

        this.passiveNPCs.put(id, new PassiveNpc(id, isRunic, name, overrideText, dialogue));
    }
}
