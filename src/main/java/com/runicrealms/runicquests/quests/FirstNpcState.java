package com.runicrealms.runicquests.quests;

public enum FirstNpcState {

    /*
     * Represents the first NPC status:
     * neutral - the player has not yet talked to the NPC
     * accepted - the player has accepted the quest and started it
     */

    NEUTRAL("neutral"), ACCEPTED("accepted");

    private final String name;

    private FirstNpcState(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static FirstNpcState fromString(String str) {
        if (str.equalsIgnoreCase("neutral")) {
            return FirstNpcState.NEUTRAL;
        } else if (str.equalsIgnoreCase("accepted")) {
            return FirstNpcState.ACCEPTED;
        }
        return null;
    }

}
