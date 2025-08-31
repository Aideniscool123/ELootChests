package com.Aiden.net.Elivius.eLootChests;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import com.Aiden.net.Elivius.eLootChests.Enums.*;

public class BossRegistry {
    private final JavaPlugin plugin;
    private final File registryFile;

    public BossRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
        this.registryFile = new File(plugin.getDataFolder(), "bosses.yml");
        createRegistryFile();
    }

    private void createRegistryFile() {
        if (!registryFile.exists()) {
            try {
                // First try to copy from resources
                InputStream resourceStream = plugin.getResource("bosses.yml");
                if (resourceStream != null) {
                    Files.copy(resourceStream, registryFile.toPath());
                    plugin.getLogger().info("Successfully copied bosses.yml from resources");
                } else {
                    // If not in resources, create a new one with enum values
                    registryFile.getParentFile().mkdirs();
                    registryFile.createNewFile();

                    YamlConfiguration config = new YamlConfiguration();
                    List<String> defaultBosses = new ArrayList<>();

                    // Add all BossType enum values
                    for (BossType bossType : BossType.values()) {
                        defaultBosses.add(bossType.getConfigName());
                    }

                    config.set("bosses", defaultBosses);
                    config.save(registryFile);
                    plugin.getLogger().info("Created new bosses.yml with enum values: " + defaultBosses);
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to create bosses registry file: " + e.getMessage());
            }
        }
    }

    public List<String> getAllBossNames() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(registryFile);
        if (!registryFile.exists()) {
            return new ArrayList<>();
        }
        return config.getStringList("bosses");
    }

    public boolean addBoss(String bossName) {
        List<String> bosses = getAllBossNames();
        if (!bosses.contains(bossName)) {
            bosses.add(bossName);

            YamlConfiguration config = YamlConfiguration.loadConfiguration(registryFile);
            config.set("bosses", bosses);

            try {
                config.save(registryFile);
                return true;
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save boss to registry: " + bossName);
            }
        }
        return false;
    }

    public boolean removeBoss(String bossName) {
        List<String> bosses = getAllBossNames();
        if (bosses.remove(bossName)) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(registryFile);
            config.set("bosses", bosses);

            try {
                config.save(registryFile);
                return true;
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to remove boss from registry: " + bossName);
            }
        }
        return false;
    }

    public boolean bossExists(String bossName) {
        return getAllBossNames().contains(bossName);
    }
}