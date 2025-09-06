package com.Aiden.net.Elivius.eLootChests;

import org.bukkit.Location;
import org.bukkit.Material;
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

public class BossManager {
    private final JavaPlugin plugin;
    private final Map<UUID, Map<Location, BukkitRunnable>> playerParticles = new HashMap<>();
    private final Map<String, Set<Location>> activeChests = new HashMap<>();

    public BossManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public Map<String, Set<Location>> getActiveChests() {
        return activeChests;
    }

    private String locationToString(Location location) {
        return location.getWorld().getName() + " " +
                location.getBlockX() + " " +
                location.getBlockY() + " " +
                location.getBlockZ();
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

        File configFile = new File(bossFolder, "config.yml");
        YamlConfiguration config = new YamlConfiguration();

        // Basic settings
        config.set("chest-spawn-count", 10);
        config.set("respawn-timer-minutes", 60);
        config.set("hologram-text", bossName + " Chest");
        config.set("particles-enabled", true);
        config.set("world-name", "world");

        // NEW: Spawning rules
        config.set("min-items-per-chest", 12);
        config.set("max-items-per-chest", 17);
        config.set("prevent-duplicates", true);
        config.set("max-mythic-per-chest", 1);

        // NEW: Announcement settings
        config.set("announce-rarities", Arrays.asList("MYTHIC", "GODLIKE"));
        config.set("announce-message", "[Elivius] A %RARITY% loot chest has spawned at %X% %Y% %Z% in %GROUP%.");

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

    public int getMinItemsPerChest(String bossName) {
        File bossFolder = new File(plugin.getDataFolder(), bossName.toLowerCase());
        File configFile = new File(bossFolder, "config.yml");

        if (!configFile.exists()) {
            return 12; // Default
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        return config.getInt("min-items-per-chest", 12);
    }

    public int getMaxItemsPerChest(String bossName) {
        File bossFolder = new File(plugin.getDataFolder(), bossName.toLowerCase());
        File configFile = new File(bossFolder, "config.yml");

        if (!configFile.exists()) {
            return 17; // Default
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        return config.getInt("max-items-per-chest", 17);
    }

    public List<String> getAnnounceRarities(String bossName) {
        File bossFolder = new File(plugin.getDataFolder(), bossName.toLowerCase());
        File configFile = new File(bossFolder, "config.yml");

        if (!configFile.exists()) {
            return Arrays.asList("MYTHIC", "GODLIKE"); // Default
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        return config.getStringList("announce-rarities");
    }

    public int spawnChests(String bossName) {
        // Get group configuration
        YamlConfiguration config = getGroupConfig(bossName);
        int chestsToSpawn = config.getInt("chest-spawn-count", 10);

        // Get all saved locations for this group
        List<Location> allLocations = getSavedChestLocations(bossName);

        if (allLocations.isEmpty()) {
            plugin.getLogger().warning("No chest locations found for group: " + bossName);
            return 0;
        }

        if (chestsToSpawn > allLocations.size()) {
            plugin.getLogger().warning("Cannot spawn " + chestsToSpawn + " chests! Group '" + bossName + "' only has " + allLocations.size() + " locations.");
            return 0;
        }

        // Randomly select locations
        Collections.shuffle(allLocations);
        List<Location> selectedLocations = allLocations.subList(0, Math.min(chestsToSpawn, allLocations.size()));

        int spawnedCount = 0;
        for (Location location : selectedLocations) {
            if (spawnChestAtLocation(bossName, location)) {
                spawnedCount++;
            }
        }

        plugin.getLogger().info("Spawned " + spawnedCount + " chests for group: " + bossName);
        return spawnedCount;
    }

    private boolean spawnChestAtLocation(String bossName, Location location) {
        if (location.getWorld() == null) {
            return false;
        }

        // Check if block is already a chest or occupied
        if (location.getBlock().getType() != Material.AIR) {
            return false;
        }

        // Spawn the chest
        location.getBlock().setType(Material.CHEST);

        // Track this active chest - Use block coordinates for consistency
        Location blockLocation = new Location(
                location.getWorld(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );

        addActiveChest(bossName, blockLocation);
        return true;
    }

    public YamlConfiguration getGroupConfig(String bossName) {
        File bossFolder = new File(plugin.getDataFolder(), bossName.toLowerCase());
        File configFile = new File(bossFolder, "config.yml");

        if (!configFile.exists()) {
            // Return default config if file doesn't exist
            YamlConfiguration defaultConfig = new YamlConfiguration();
            defaultConfig.set("chest-spawn-count", 10);
            defaultConfig.set("respawn-timer-minutes", 60);
            defaultConfig.set("hologram-text", bossName + " Chest");
            defaultConfig.set("particles-enabled", true);
            defaultConfig.set("world-name", "world");
            return defaultConfig;
        }

        return YamlConfiguration.loadConfiguration(configFile);
    }


    public int despawnChests(String bossName) {
        int despawnedCount = 0;
        String key = bossName.toLowerCase();

        // Get active chests for this group
        Set<Location> chestLocations = activeChests.getOrDefault(key, new HashSet<>());

        for (Location location : chestLocations) {
            if (removeChestAtLocation(location)) {
                despawnedCount++;
            }
        }

        // Clear the active chests for this group
        activeChests.remove(key);

        plugin.getLogger().info("Despawned " + despawnedCount + " chests for group: " + bossName);
        return despawnedCount;
    }



    private boolean removeChestAtLocation(Location location) {
        if (location.getWorld() == null) return false;

        // Use block coordinates for consistency
        Location blockLocation = new Location(
                location.getWorld(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );

        // Remove the chest block
        if (blockLocation.getBlock().getType() == Material.CHEST ||
                blockLocation.getBlock().getType() == Material.TRAPPED_CHEST) {
            blockLocation.getBlock().setType(Material.AIR);
            return true;
        }

        return false;
    }

    // Method to track spawned chests
    public void addActiveChest(String bossName, Location location) {
        String key = bossName.toLowerCase();

        // Use block coordinates for consistency
        Location blockLocation = new Location(
                location.getWorld(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );

        activeChests.computeIfAbsent(key, k -> new HashSet<>()).add(blockLocation);
    }

    public void saveActiveChests() {
        File activeFile = new File(plugin.getDataFolder(), "activechests.yml");
        YamlConfiguration config = new YamlConfiguration();

        for (Map.Entry<String, Set<Location>> entry : activeChests.entrySet()) {
            List<String> locationStrings = new ArrayList<>();
            for (Location loc : entry.getValue()) {
                locationStrings.add(locToString(loc));
            }
            config.set(entry.getKey(), locationStrings);
        }

        try {
            config.save(activeFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save active chests: " + e.getMessage());
        }
    }

    // Load active chests on plugin enable
    public void loadActiveChests() {
        File activeFile = new File(plugin.getDataFolder(), "activechests.yml");
        if (!activeFile.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(activeFile);
        for (String bossName : config.getKeys(false)) {
            List<String> locationStrings = config.getStringList(bossName);
            Set<Location> locations = new HashSet<>();

            for (String locString : locationStrings) {
                Location loc = stringToLocation(locString);
                if (loc != null) {
                    locations.add(loc);
                }
            }

            activeChests.put(bossName, locations);
        }
    }

    // Helper methods for location serialization
    private String locToString(Location location) {
        return location.getWorld().getName() + "," +
                location.getX() + "," +
                location.getY() + "," +
                location.getZ();
    }

    private Location stringToLocation(String locationString) {
        String[] parts = locationString.split(",");
        if (parts.length == 4) {
            World world = plugin.getServer().getWorld(parts[0]);
            if (world != null) {
                return new Location(world,
                        Double.parseDouble(parts[1]),
                        Double.parseDouble(parts[2]),
                        Double.parseDouble(parts[3]));
            }
        }
        return null;
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

    public boolean removeItemFromLootTable(String bossName, ItemStack item) {
        File bossFolder = new File(plugin.getDataFolder(), bossName.toLowerCase());
        File lootFile = new File(bossFolder, "loottable.yml");

        if (!lootFile.exists()) {
            return false;
        }

        YamlConfiguration loot = YamlConfiguration.loadConfiguration(lootFile);
        boolean removed = false;

        // Convert item to base64 for comparison
        String itemString = itemStackToBase64(item);

        // Check all rarity groups for this item
        for (Rarity rarity : Rarity.values()) {
            String rarityKey = rarity.name().toLowerCase();

            if (loot.contains(rarityKey + ".items")) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) loot.getList(rarityKey + ".items");
                Iterator<Map<String, Object>> iterator = items.iterator();

                while (iterator.hasNext()) {
                    Map<String, Object> itemData = iterator.next();
                    if (itemData.get("item").equals(itemString)) {
                        iterator.remove();
                        removed = true;
                        break;
                    }
                }

                if (!items.isEmpty()) {
                    loot.set(rarityKey + ".items", items);
                } else {
                    loot.set(rarityKey + ".items", null);
                }
            }
        }

        if (removed) {
            try {
                loot.save(lootFile);
                return true;
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save loot table for boss: " + bossName);
            }
        }

        return false;
    }
    public Map<Rarity, Integer> getLootTableSummary(String bossName) {
        Map<Rarity, Integer> itemCounts = new HashMap<>();

        File bossFolder = new File(plugin.getDataFolder(), bossName.toLowerCase());
        File lootFile = new File(bossFolder, "loottable.yml");

        if (!lootFile.exists()) {
            return itemCounts;
        }

        YamlConfiguration loot = YamlConfiguration.loadConfiguration(lootFile);

        for (Rarity rarity : Rarity.values()) {
            String rarityKey = rarity.name().toLowerCase();
            int count = 0;

            if (loot.contains(rarityKey + ".items")) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) loot.getList(rarityKey + ".items");
                count = items != null ? items.size() : 0;
            }

            itemCounts.put(rarity, count);
        }

        return itemCounts;
    }
    public ItemStack getItemFromLootTable(String bossName, Rarity rarity, int index) {
        File bossFolder = new File(plugin.getDataFolder(), bossName.toLowerCase());
        File lootFile = new File(bossFolder, "loottable.yml");

        if (!lootFile.exists()) {
            return null;
        }

        YamlConfiguration loot = YamlConfiguration.loadConfiguration(lootFile);
        String rarityKey = rarity.name().toLowerCase();

        if (loot.contains(rarityKey + ".items")) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) loot.getList(rarityKey + ".items");
            if (items != null && index >= 0 && index < items.size()) {
                Map<String, Object> itemData = items.get(index);
                String base64Item = (String) itemData.get("item");
                return itemStackFromBase64(base64Item);
            }
        }

        return null;
    }

    public void sendLootTableInfo(Player player, String bossName) {
        File bossFolder = new File(plugin.getDataFolder(), bossName.toLowerCase());
        File lootFile = new File(bossFolder, "loottable.yml");

        if (!lootFile.exists()) {
            player.sendMessage("§cLoot table not found for group: §e" + bossName);
            return;
        }

        YamlConfiguration loot = YamlConfiguration.loadConfiguration(lootFile);

        player.sendMessage("§6=== Loot Table: §e" + bossName + " §6===");

        for (Rarity rarity : Rarity.values()) {
            String rarityKey = rarity.name().toLowerCase();
            int count = 0;

            if (loot.contains(rarityKey + ".items")) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) loot.getList(rarityKey + ".items");
                count = items != null ? items.size() : 0;
            }

            player.sendMessage("§7- " + rarity.getFormattedName() + "§7: §e" + count + " items");

            // Show individual items with indexes
            if (count > 0) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) loot.getList(rarityKey + ".items");
                for (int i = 0; i < Math.min(count, 5); i++) { // Show first 5 items max
                    Map<String, Object> itemData = items.get(i);
                    double percentage = (Double) itemData.getOrDefault("percentage", 0.0);
                    player.sendMessage("§8  [" + i + "] §7" + percentage + "%");
                }
                if (count > 5) {
                    player.sendMessage("§8  ... and " + (count - 5) + " more");
                }
            }
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
}