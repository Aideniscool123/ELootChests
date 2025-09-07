package com.Aiden.net.Elivius.eLootChests;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import com.Aiden.net.Elivius.eLootChests.Enums.Rarity;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles holograms with soft dependency on DecentHolograms
 */


public class HologramManager {
    private static JavaPlugin plugin = null;
    private static final Map<Location, Object> activeHolograms = new HashMap<>();
    private boolean decentHologramsEnabled = false;
    private Method createHologramMethod;
    private Method addLineMethod;
    private static Method deleteMethod;

    public HologramManager(JavaPlugin plugin) {
        this.plugin = plugin;
        setupDecentHolograms();
    }

    public static void removeAllHolograms() {
        for (Object hologram : activeHolograms.values()) {
            try {
                deleteMethod.invoke(hologram);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to remove hologram: " + e.getMessage());
            }
        }
        activeHolograms.clear();
        plugin.getLogger().info("Removed all holograms");
    }

    private void setupDecentHolograms() {
        try {
            // Check if DecentHolograms is available
            Class<?> dhapiClass = Class.forName("eu.decentsoftware.holograms.api.DHAPI");
            Class<?> hologramClass = Class.forName("eu.decentsoftware.holograms.api.holograms.Hologram");

            // Get methods via reflection
            createHologramMethod = dhapiClass.getMethod("createHologram", String.class, Location.class);
            addLineMethod = dhapiClass.getMethod("addHologramLine", hologramClass, String.class);
            deleteMethod = hologramClass.getMethod("delete");

            decentHologramsEnabled = true;
            plugin.getLogger().info("DecentHolograms integration enabled");

        } catch (Exception e) {
            decentHologramsEnabled = false;
            plugin.getLogger().warning("DecentHolograms not found. Holograms will be disabled.");
        }
    }

    public void createChestHologram(String bossName, Location chestLocation, Rarity highestRarity) {
        if (!decentHologramsEnabled) return;

        try {
            Location hologramLocation = chestLocation.clone().add(0.5, 2.2, 0.5);
            String hologramId = "lootchest_" + System.currentTimeMillis();

            // Create hologram using reflection
            Object hologram = createHologramMethod.invoke(null, hologramId, hologramLocation);

            // Add lines
            addLineMethod.invoke(null, hologram, "§6§l" + bossName + " Chest");
            addLineMethod.invoke(null, hologram, highestRarity.getFormattedName() + " §7Loot");
            addLineMethod.invoke(null, hologram, "§eRight-click to open");

            try {
                Class<?> hologramClass = Class.forName("eu.decentsoftware.holograms.api.holograms.Hologram");
                Method setInteractionEnabledMethod = hologramClass.getMethod("setInteractionEnabled", boolean.class);
                setInteractionEnabledMethod.invoke(hologram, false); // Disable interaction
            } catch (Exception e) {
                plugin.getLogger().warning("Could not disable hologram interaction: " + e.getMessage());
            }

            activeHolograms.put(chestLocation, hologram);

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create hologram: " + e.getMessage());
        }
    }

    public void removeChestHologram(Location chestLocation) {
        if (!decentHologramsEnabled) return;

        Object hologram = activeHolograms.remove(chestLocation);
        if (hologram != null) {
            try {
                deleteMethod.invoke(hologram);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to remove hologram: " + e.getMessage());
            }
        }
    }

    public boolean isDecentHologramsEnabled() {
        return decentHologramsEnabled;
    }
}