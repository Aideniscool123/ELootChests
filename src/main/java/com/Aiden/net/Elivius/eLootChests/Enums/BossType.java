package com.Aiden.net.Elivius.eLootChests.Enums;

public enum BossType {;

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