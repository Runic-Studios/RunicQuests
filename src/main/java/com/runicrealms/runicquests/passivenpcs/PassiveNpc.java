package com.runicrealms.runicquests.passivenpcs;

import com.runicrealms.plugin.RunicNpcs;
import com.runicrealms.runicquests.task.TaskQueue;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class PassiveNpc {
    public final Map<UUID, TaskQueue> TALKING;
    private final int npcID;
    private final boolean isRunic;
    private final String name;
    private final boolean overrideText;
    private final List<List<String>> dialogue;

    public PassiveNpc(int npcID, boolean isRunic, String name, boolean overrideText, List<List<String>> dialogue) {
        this.npcID = npcID;
        this.isRunic = isRunic;
        this.name = name;
        this.overrideText = overrideText;
        this.dialogue = dialogue;
        this.TALKING = new HashMap<>();
    }

    /**
     * Static method to get the corresponding RunicNpc location for the given quest npc
     *
     * @param passiveNpc the questNpc wrapper object
     * @return a Location object
     */
    public static Location getPassiveNpcLocation(PassiveNpc passiveNpc) {
        return RunicNpcs.getAPI().getNpcById(passiveNpc.getNpcID()).getLocation();
    }

    public List<String> getDialogue() {
        return this.dialogue.get(ThreadLocalRandom.current().nextInt(this.dialogue.size()));
    }

    public String getName() {
        return this.name;
    }

    public Object getNpc() {
        return RunicNpcs.getAPI().getNpcById(this.npcID);
    }

    public int getNpcID() {
        return this.npcID;
    }

    public List<List<String>> getRawDialogue() {
        return this.dialogue;
    }

    public boolean isOverrideText() {
        return this.overrideText;
    }

    public boolean isRunic() {
        return this.isRunic;
    }
}
