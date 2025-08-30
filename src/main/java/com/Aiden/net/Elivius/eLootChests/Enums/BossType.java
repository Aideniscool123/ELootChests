package com.Aiden.net.Elivius.eLootChests.Enums;

public enum BossType {
    LUSH_SANCTUARY("LushSanctuary", "Main world bosses"),
    SANTA_BOSS("SantaBoss", "Christmas event boss"),
    KRAMPUS_BOSS("KrampusBoss", "Christmas event boss"),
    TEST("Test", "Testing environment");

    private final String configName;
    private final String description;

    BossType(String configName, String description) {
        this.configName = configName;
        this.description = description;
    }

    public String getConfigName() { return configName; }
    public String getDescription() { return description; }
    public String getFolderName() { return configName.toLowerCase(); }
}