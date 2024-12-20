package com.runicrealms.plugin.runicquests.quests.trigger;

import java.util.HashMap;
import java.util.Map;

public class TriggerObjectiveHandler {

    private static final Map<String, Trigger> triggers = new HashMap<>();

    public static void addTrigger(Trigger trigger, String triggerId) {
        triggers.put(triggerId, trigger);
    }

    public static Trigger getTrigger(String triggerId) {
        if (triggers.containsKey(triggerId)) {
            return triggers.get(triggerId);
        }
        return null;
    }

}
