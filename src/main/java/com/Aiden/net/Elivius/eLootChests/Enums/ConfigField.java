package com.Aiden.net.Elivius.eLootChests.Enums;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum ConfigField {
    CHEST_SPAWN_COUNT("chest-spawn-count", 10),
    RESPAWN_TIMER_MINUTES("respawn-timer-minutes", 60),
    HOLOGRAM_TEXT("hologram-text", "Chest"),
    PARTICLES_ENABLED("particles-enabled", true),
    WORLD_NAME("world-name", "world"),
    ANNOUNCE_RARITIES("announce-rarities", java.util.Arrays.asList("MYTHIC", "GODLIKE")),
    ANNOUNCE_WORLD("announce-world", "world"),
    BOSS_DISPLAY_NAME("boss-display-name", "Boss");

    private final String key;
    private final Object defaultValue;

    ConfigField(String key, Object defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setValue(YamlConfiguration config, String bossName) {
        Object value = defaultValue;

        // Special handling for hologram text
        if (this == HOLOGRAM_TEXT) {
            value = bossName + " " + defaultValue;
        }
        // Special handling for boss display name
        else if (this == BOSS_DISPLAY_NAME) {
            value = bossName;
        }

        config.set(key, value);
    }
    public int getInt(YamlConfiguration config) {
        return config.getInt(key, (Integer) defaultValue);
    }

    public String getString(YamlConfiguration config) {
        return config.getString(key, (String) defaultValue);
    }

    public boolean getBoolean(YamlConfiguration config) {
        return config.getBoolean(key, (Boolean) defaultValue);
    }

    public List<String> getStringList(YamlConfiguration config) {
        List<String> defaultList = (List<String>) defaultValue;
        return config.getStringList(key).isEmpty() ? defaultList : config.getStringList(key);
    }
}