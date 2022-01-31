package com.runicrealms.runicquests.passivenpcs;

import com.runicrealms.runicnpcs.api.RunicNpcsAPI;
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
        return RunicNpcsAPI.getNpcById(passiveNpc.getNpcID()).getLocation();
    }

    public int getNpcID() {
        return this.npcID;
    }

    public boolean isRunic() {
        return this.isRunic;
    }

    public String getName() {
        return this.name;
    }

    public boolean isOverrideText() {
        return this.overrideText;
    }

    public List<List<String>> getRawDialogue() {
        return this.dialogue;
    }

    public List<String> getDialogue() {
        return this.dialogue.get(ThreadLocalRandom.current().nextInt(this.dialogue.size()));
    }

    public Object getNpc() {
        return RunicNpcsAPI.getNpcById(this.npcID);
    }
}
