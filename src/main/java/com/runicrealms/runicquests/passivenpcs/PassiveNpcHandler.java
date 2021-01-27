package com.runicrealms.runicquests.passivenpcs;

import com.runicrealms.runicquests.Plugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public final class PassiveNpcHandler {
    private final Map<Integer, PassiveNpc> passiveNPCs;

    public PassiveNpcHandler() {
        this.passiveNPCs = new HashMap<>();

        File passivenpcs = new File(Plugin.getInstance().getDataFolder().getPath() + "/passivenpcs");

        if (!passivenpcs.isDirectory()) {
            Plugin.getInstance().getLogger().log(Level.SEVERE, "passivenpcs is not a directory!");
            return;
        }

        for (File file : Objects.requireNonNull(passivenpcs.listFiles())) {
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

    private PassiveNpc loadFromYML(File file) throws PassiveNpcException {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (!config.contains("npc-id") || !config.contains("npc-plugin") || !config.contains("dialogue.1")) {
            throw new PassiveNpcException("Invalid npc yml: " + config.getName());
        }

        if (!config.contains("npc-name") && config.contains("add-npc-name")) {
            throw new PassiveNpcException("Invalid npc name yml: " + config.getName());
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
            dialogue.add(config.getStringList(section));
        }

        PassiveNpc npc = new PassiveNpc(id, isRunic, name, overrideText, dialogue);
        this.passiveNPCs.put(id, npc);
        return npc;
    }
}
