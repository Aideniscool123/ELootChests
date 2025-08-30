package com.Aiden.net.Elivius.eLootChests;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Particle;
import com.Aiden.net.Elivius.eLootChests.Enums.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BossManager {
    private final JavaPlugin plugin;
    private final File testFolder;
    private final Map<UUID, Map<Location, BukkitRunnable>> playerParticles = new HashMap<>();

    public BossManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.testFolder = new File(plugin.getDataFolder(), "test");
        createTestFolder();
    }

    private void createTestFolder() {
        if (!testFolder.exists()) {
            testFolder.mkdirs();
            createDefaultFiles();
        }
    }

    private void createDefaultFiles() {
        createDefaultConfig();
        createDefaultCoordinates();
        createDefaultLootTable();
    }

    private void createDefaultConfig() {
        File configFile = new File(testFolder, "config.yml");
        YamlConfiguration config = new YamlConfiguration();

        config.set("chest-spawn-count", 10);
        config.set("respawn-timer-minutes", 60);
        config.set("hologram-text", "Test Loot Chest");
        config.set("particles-enabled", true);

        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to create config for test folder");
        }
    }

    private void createDefaultCoordinates() {
        File coordsFile = new File(testFolder, "coordinates.yml");
        YamlConfiguration coords = new YamlConfiguration();

        coords.set("chest-locations", new ArrayList<String>());

        try {
            coords.save(coordsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to create coordinates for test folder");
        }
    }

    private void createDefaultLootTable() {
        File lootFile = new File(testFolder, "loottable.yml");
        YamlConfiguration loot = new YamlConfiguration();

        loot.set("common.items", new ArrayList<String>());
        loot.set("common.spawn-percentage", 70.0);
        loot.set("rare.items", new ArrayList<String>());
        loot.set("rare.spawn-percentage", 20.0);
        loot.set("epic.items", new ArrayList<String>());
        loot.set("epic.spawn-percentage", 7.0);
        loot.set("legendary.items", new ArrayList<String>());
        loot.set("legendary.spawn-percentage", 2.5);
        loot.set("mythic.items", new ArrayList<String>());
        loot.set("mythic.spawn-percentage", 0.4);
        loot.set("godlike.items", new ArrayList<String>());
        loot.set("godlike.spawn-percentage", 0.1);

        try {
            loot.save(lootFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to create loot table for test folder");
        }
    }

    public void saveChestLocation(Location location) {
        YamlConfiguration coords = getCoordinates();
        List<String> locations = coords.getStringList("chest-locations");

        String locationString = location.getWorld().getName() + "," +
                location.getBlockX() + "," +
                (location.getBlockY() + 1) + "," + // +1 to Y value
                location.getBlockZ();

        if (!locations.contains(locationString)) {
            locations.add(locationString);
            coords.set("chest-locations", locations);
            saveCoordinates(coords);
            plugin.getLogger().info("Saved chest location: " + locationString);
        }
    }

    public boolean removeChestLocation(Location location) {
        YamlConfiguration coords = getCoordinates();
        List<String> locations = coords.getStringList("chest-locations");

        // Use Y+1 to match how locations are stored
        String locationString = location.getWorld().getName() + "," +
                location.getBlockX() + "," +
                (location.getBlockY() + 1) + "," + // +1 to match storage
                location.getBlockZ();

        boolean removed = locations.remove(locationString);
        if (removed) {
            coords.set("chest-locations", locations);
            saveCoordinates(coords);
            plugin.getLogger().info("Removed chest location: " + locationString);
        }

        return removed;
    }

    public List<Location> getSavedChestLocations() {
        YamlConfiguration coords = getCoordinates();
        List<String> locationStrings = coords.getStringList("chest-locations");
        List<Location> locations = new ArrayList<>();

        for (String locationString : locationStrings) {
            String[] parts = locationString.split(",");
            if (parts.length == 4) {
                World world = plugin.getServer().getWorld(parts[0]);
                if (world != null) {
                    locations.add(new Location(
                            world,
                            Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[2]),
                            Integer.parseInt(parts[3])
                    ));
                }
            }
        }
        return locations;
    }

    public void showLocationParticles(Player player, Location location, Particle particle, int durationTicks, boolean throughBlocks) {

        World world = location.getWorld();
        double x = location.getX() + 0.5;
        double y = location.getY() + 0.2;
        double z = location.getZ() + 0.5;

        BukkitRunnable runnable = new BukkitRunnable() {
            int duration = 0;

            @Override
            public void run() {
                if (duration > durationTicks && durationTicks != -1) {
                    stopLocationParticles(player, location);
                    return;
                }

                if (player.isOnline()) {
                    if (throughBlocks) {
                        player.spawnParticle(Particle.GLOW, x, y, z, 3, 0.1, 0.1, 0.1, 0.05);
                    } else {
                        player.spawnParticle(Particle.HAPPY_VILLAGER, x, y, z, 3, 0.1, 0.1, 0.1, 0.05);
                    }
                } else {
                    this.cancel();
                }
                duration++;
            }
        };

        playerParticles.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .put(location, runnable);
        runnable.runTaskTimer(plugin, 0, 1);
    }

    public void stopPlayerParticles(Player player) {
        Map<Location, BukkitRunnable> particles = playerParticles.remove(player.getUniqueId());
        if (particles != null) {
            for (BukkitRunnable runnable : particles.values()) {
                runnable.cancel();
            }
        }
    }
    public void stopLocationParticles(Player player, Location location) {
        Map<Location, BukkitRunnable> particles = playerParticles.get(player.getUniqueId());
        if (particles != null) {
            BukkitRunnable runnable = particles.remove(location);
            if (runnable != null) {
                runnable.cancel();
            }
        }
    }

    public void stopAllParticles() {
        for (Map<Location, BukkitRunnable> particles : playerParticles.values()) {
            for (BukkitRunnable runnable : particles.values()) {
                runnable.cancel();
            }
        }
        playerParticles.clear();
    }

    public YamlConfiguration getConfig() {
        return YamlConfiguration.loadConfiguration(new File(testFolder, "config.yml"));
    }

    public YamlConfiguration getCoordinates() {
        return YamlConfiguration.loadConfiguration(new File(testFolder, "coordinates.yml"));
    }

    public YamlConfiguration getLootTable() {
        return YamlConfiguration.loadConfiguration(new File(testFolder, "loottable.yml"));
    }

    public void saveConfig(YamlConfiguration config) {
        try {
            config.save(new File(testFolder, "config.yml"));
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save config for test folder");
        }
    }

    public void saveCoordinates(YamlConfiguration coords) {
        try {
            coords.save(new File(testFolder, "coordinates.yml"));
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save coordinates for test folder");
        }
    }

    public void saveLootTable(YamlConfiguration loot) {
        try {
            loot.save(new File(testFolder, "loottable.yml"));
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save loot table for test folder");
        }
    }

    public File getTestFolder() {
        return testFolder;
    }
}