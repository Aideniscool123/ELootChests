package com.Aiden.net.Elivius.eLootChests.Enums;

public enum ChestAction {
    SPAWN("spawn", "Spawns chests from a group"),
    DESPAWN("despawn", "Despawns chests from a group"),
    ADD_ITEM("add-item", "Adds item to loot table"),
    REMOVE_ITEM("remove-item", "Removes item from loot table"),
    INFO("info", "Shows chest information");

    private final String command;
    private final String description;

    ChestAction(String command, String description) {
        this.command = command;
        this.description = description;
    }

    public String getCommand() { return command; }
    public String getDescription() { return description; }
}