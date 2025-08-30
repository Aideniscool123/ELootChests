package com.Aiden.net.Elivius.eLootChests;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
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
                registryFile.getParentFile().mkdirs();
                registryFile.createNewFile();

                YamlConfiguration config = YamlConfiguration.loadConfiguration(registryFile);
                config.set("bosses", new ArrayList<String>());
                config.save(registryFile);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to create bosses registry file!");
            }
        }
    }

    public List<String> getAllBossNames() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(registryFile);
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