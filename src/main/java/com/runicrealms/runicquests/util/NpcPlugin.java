package com.runicrealms.runicquests.util;

public enum NpcPlugin {

    CITIZENS("Citizens"), RUNICNPCS("RunicNpcs");

    private String name;

    NpcPlugin(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static NpcPlugin getFromString(String name, NpcPlugin def) {
        for (NpcPlugin plugin : NpcPlugin.values()) {
            if (plugin.getName().equalsIgnoreCase(name)) {
                return plugin;
            }
        }
        return def;
    }

}
