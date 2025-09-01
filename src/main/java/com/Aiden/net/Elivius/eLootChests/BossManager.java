package com.Aiden.net.Elivius.eLootChests;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Particle;
import com.Aiden.net.Elivius.eLootChests.Enums.*;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.bukkit.util.io.BukkitObjectInputStream;

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.*;

/**
 * @author AidenTheStitch
 * @file BossManager.java
 * @description Manages boss configurations, chest locations, and loot tables
 */
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

        for (Rarity rarity : Rarity.values()) {
            loot.set(rarity.name().toLowerCase() + ".items", new ArrayList<Map<String, Object>>());
        }

        try {
            loot.save(lootFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to create loot table for test folder");
        }
    }

    public boolean createBossGroup(String bossName) {
        File bossFolder = new File(plugin.getDataFolder(), bossName.toLowerCase());
        if (bossFolder.exists()) {
            plugin.getLogger().warning("Boss folder already exists: " + bossName);
            return false;
        }

        if (!bossFolder.mkdirs()) {
            plugin.getLogger().warning("Failed to create boss folder: " + bossName);
            return false;
        }

        // Create config.yml
        File configFile = new File(bossFolder, "config.yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("chest-spawn-count", 10);
        config.set("respawn-timer-minutes", 60);
        config.set("hologram-text", bossName + " Chest");
        config.set("particles-enabled", true);
        config.set("world-name", "world");

        // Create coordinates.yml
        File coordsFile = new File(bossFolder, "coordinates.yml");
        YamlConfiguration coords = new YamlConfiguration();
        coords.set("chest-locations", new ArrayList<String>());

        // Create loottable.yml - WITHOUT rarity-level percentages
        File lootFile = new File(bossFolder, "loottable.yml");
        YamlConfiguration loot = new YamlConfiguration();
        for (Rarity rarity : Rarity.values()) {
            String rarityKey = rarity.name().toLowerCase();
            loot.set(rarityKey + ".items", new ArrayList<Map<String, Object>>());
        }

        try {
            config.save(configFile);
            coords.save(coordsFile);
            loot.save(lootFile);
            plugin.getLogger().info("Created new boss group: " + bossName);
            return true;
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to create files for boss: " + bossName);
            // Clean up folder if creation failed
            if (bossFolder.exists()) {
                deleteFolder(bossFolder);
            }
            return false;
        }
    }

    private void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteFolder(file);
                }
            }
        }
        folder.delete();
    }

    public void saveChestLocation(Location location, String groupName) {
        File groupFolder = new File(plugin.getDataFolder(), groupName.toLowerCase());
        File coordsFile = new File(groupFolder, "coordinates.yml");
        YamlConfiguration coords = YamlConfiguration.loadConfiguration(coordsFile);

        List<String> locations = coords.getStringList("chest-locations");
        String locationString = location.getWorld().getName() + "," +
                location.getBlockX() + "," +
                (location.getBlockY() + 1) + "," +
                location.getBlockZ();

        if (!locations.contains(locationString)) {
            locations.add(locationString);
            coords.set("chest-locations", locations);
            try {
                coords.save(coordsFile);
                plugin.getLogger().info("Saved chest location for group " + groupName + ": " + locationString);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save coordinates for group: " + groupName);
            }
        }
    }

    public boolean removeChestLocation(Location location, String groupName) {
        File groupFolder = new File(plugin.getDataFolder(), groupName.toLowerCase());
        File coordsFile = new File(groupFolder, "coordinates.yml");
        YamlConfiguration coords = YamlConfiguration.loadConfiguration(coordsFile);

        List<String> locations = coords.getStringList("chest-locations");
        String locationString = location.getWorld().getName() + "," +
                location.getBlockX() + "," +
                (location.getBlockY() + 1) + "," +
                location.getBlockZ();

        boolean removed = locations.remove(locationString);
        if (removed) {
            coords.set("chest-locations", locations);
            try {
                coords.save(coordsFile);
                plugin.getLogger().info("Removed chest location from group " + groupName + ": " + locationString);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save coordinates for group: " + groupName);
            }
        }

        return removed;
    }

    public List<Location> getSavedChestLocations(String groupName) {
        File groupFolder = new File(plugin.getDataFolder(), groupName.toLowerCase());
        File coordsFile = new File(groupFolder, "coordinates.yml");

        if (!coordsFile.exists()) {
            return new ArrayList<>();
        }

        YamlConfiguration coords = YamlConfiguration.loadConfiguration(coordsFile);
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

    public boolean addItemToLootTable(String bossName, Rarity rarity, ItemStack item, double customPercentage) {
        File bossFolder = new File(plugin.getDataFolder(), bossName.toLowerCase());
        File lootFile = new File(bossFolder, "loottable.yml");

        if (!lootFile.exists()) {
            return false;
        }

        YamlConfiguration loot = YamlConfiguration.loadConfiguration(lootFile);
        String rarityKey = rarity.name().toLowerCase();

        // Get current items list
        List<Map<String, Object>> items = new ArrayList<>();
        if (loot.contains(rarityKey + ".items")) {
            items = (List<Map<String, Object>>) loot.getList(rarityKey + ".items");
        }

        // Convert item to base64 string
        String itemString = itemStackToBase64(item);

        // Check if item already exists in loot table
        for (Map<String, Object> itemData : items) {
            if (itemData.get("item").equals(itemString)) {
                return false; // Item already exists
            }
        }

        // Use custom percentage or default to the rarity's default if not provided
        double finalPercentage = (customPercentage > 0) ? customPercentage : rarity.getDefaultPercentage();

        // Create new item data with percentage
        Map<String, Object> newItem = new HashMap<>();
        newItem.put("item", itemString);
        newItem.put("percentage", finalPercentage);

        // Add the item
        items.add(newItem);
        loot.set(rarityKey + ".items", items);

        try {
            loot.save(lootFile);
            return true;
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save loot table for boss: " + bossName);
            return false;
        }
    }

    private String itemStackToBase64(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to serialize item to base64: " + e.getMessage());
            return null;
        }
    }

    public ItemStack itemStackFromBase64(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to deserialize item from base64: " + e.getMessage());
            return null;
        }
    }

    public Map<ItemStack, Double> getItemsWithPercentages(String bossName, Rarity rarity) {
        Map<ItemStack, Double> itemsWithPercentages = new HashMap<>();
        File bossFolder = new File(plugin.getDataFolder(), bossName.toLowerCase());
        File lootFile = new File(bossFolder, "loottable.yml");

        if (!lootFile.exists()) {
            return itemsWithPercentages;
        }

        YamlConfiguration loot = YamlConfiguration.loadConfiguration(lootFile);
        String rarityKey = rarity.name().toLowerCase();

        if (loot.contains(rarityKey + ".items")) {
            List<Map<String, Object>> itemDataList = (List<Map<String, Object>>) loot.getList(rarityKey + ".items");
            for (Map<String, Object> itemData : itemDataList) {
                String base64Item = (String) itemData.get("item");
                double percentage = (Double) itemData.getOrDefault("percentage", 50.0);
                ItemStack item = itemStackFromBase64(base64Item);
                if (item != null) {
                    itemsWithPercentages.put(item, percentage);
                }
            }
        }

        return itemsWithPercentages;
    }

    public void showLocationParticles(Player player, Location location, ParticleType particleType,
                                      int durationTicks, boolean throughBlocks) {
        World world = location.getWorld();
        double x = location.getX() + 0.5;
        double y = location.getY() + 1.2;
        double z = location.getZ() + 0.5;

        // Create a new Location object to ensure we have the exact reference
        Location particleLocation = new Location(world, x, y, z);

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
                        player.spawnParticle(ParticleType.GLOW.getParticle(), particleLocation.getX(),
                                particleLocation.getY(), particleLocation.getZ(), 3, 0.1, 0.1, 0.1, 0.05);
                    } else {
                        player.spawnParticle(particleType.getParticle(), particleLocation.getX(),
                                particleLocation.getY(), particleLocation.getZ(), 3, 0.1, 0.1, 0.1, 0.05);
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

    public void stopLocationParticles(Player player, Location locationToRemove) {
        Map<Location, BukkitRunnable> particles = playerParticles.get(player.getUniqueId());
        if (particles != null) {
            // Find the exact location key that matches the coordinates
            Location foundKey = null;
            for (Location loc : particles.keySet()) {
                if (loc.getWorld().equals(locationToRemove.getWorld()) &&
                        loc.getBlockX() == locationToRemove.getBlockX() &&
                        loc.getBlockY() == locationToRemove.getBlockY() &&
                        loc.getBlockZ() == locationToRemove.getBlockZ()) {
                    foundKey = loc;
                    break;
                }
            }

            if (foundKey != null) {
                BukkitRunnable runnable = particles.remove(foundKey);
                if (runnable != null) {
                    runnable.cancel();
                }
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

    public void validateRarityPercentages(String bossName) {
        File bossFolder = new File(plugin.getDataFolder(), bossName.toLowerCase());
        File lootFile = new File(bossFolder, "loottable.yml");

        if (!lootFile.exists()) {
            return;
        }

        YamlConfiguration loot = YamlConfiguration.loadConfiguration(lootFile);
        double total = 0;

        for (Rarity rarity : Rarity.values()) {
            total += loot.getDouble(rarity.name().toLowerCase() + ".spawn-percentage", rarity.getDefaultPercentage());
        }

        if (Math.abs(total - 100.0) > 0.1) {
            plugin.getLogger().warning("Rarity percentages for boss '" + bossName + "' don't sum to 100%! Total: " + total);
        }
    }

    public File getTestFolder() {
        return testFolder;
    }
}