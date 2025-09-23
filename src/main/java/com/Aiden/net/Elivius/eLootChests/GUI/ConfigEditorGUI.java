package com.Aiden.net.Elivius.eLootChests.GUI;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.Aiden.net.Elivius.eLootChests.BossManager;
import com.Aiden.net.Elivius.eLootChests.BossRegistry;
import com.Aiden.net.Elivius.eLootChests.LootChests;
import com.Aiden.net.Elivius.eLootChests.Enums.ConfigField;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Arrays;
import java.util.List;

public class ConfigEditorGUI extends LootChestGUI {
    private final BossManager bossManager;
    private final BossRegistry bossRegistry;
    private final LootChests plugin;
    private final String groupName;
    private YamlConfiguration config;

    public ConfigEditorGUI(Player player, String groupName, BossManager bossManager, BossRegistry bossRegistry, LootChests plugin) {
        super(player, "§8Config: §e" + groupName, 54);
        this.bossManager = bossManager;
        this.bossRegistry = bossRegistry;
        this.plugin = plugin;
        this.groupName = groupName;
        this.config = bossManager.getGroupConfig(groupName);
        setupGUI();
    }

    private void setupGUI() {
        // Fill border
        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        if (borderMeta != null) {
            borderMeta.setDisplayName(" ");
            border.setItemMeta(borderMeta);
        }
        fillBorder(border);

        // Current values from config
        int chestSpawnCount = config.getInt(ConfigField.CHEST_SPAWN_COUNT.getKey(), 10);
        String worldName = config.getString(ConfigField.WORLD_NAME.getKey(), "world");
        boolean particlesEnabled = config.getBoolean(ConfigField.PARTICLES_ENABLED.getKey(), true);
        List<String> announceRarities = config.getStringList(ConfigField.ANNOUNCE_RARITIES.getKey());
        String hologramText = config.getString(ConfigField.HOLOGRAM_TEXT.getKey(), groupName + " Chest");
        String bossDisplayName = config.getString(ConfigField.BOSS_DISPLAY_NAME.getKey(), groupName);

        // Group Info Header
        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§6Editing: §e" + groupName);
            infoMeta.setLore(Arrays.asList(
                    "§7Configuration Editor",
                    "§7Click any option to modify it",
                    "",
                    "§aChanges save automatically"
            ));
            infoItem.setItemMeta(infoMeta);
        }
        inventory.setItem(4, infoItem);

        // Chest Spawn Count Editor (Slot 10)
        ItemStack spawnCountItem = new ItemStack(Material.CHEST);
        ItemMeta spawnCountMeta = spawnCountItem.getItemMeta();
        if (spawnCountMeta != null) {
            spawnCountMeta.setDisplayName("§eChest Spawn Count");
            spawnCountMeta.setLore(Arrays.asList(
                    "§7Current: §a" + chestSpawnCount,
                    "§7Maximum chests to spawn at once",
                    "",
                    "§aLeft-click: §7Increase by 1",
                    "§cRight-click: §7Decrease by 1"
            ));
            spawnCountItem.setItemMeta(spawnCountMeta);
        }
        inventory.setItem(10, spawnCountItem);

        // World Name Display (Slot 12)
        ItemStack worldItem = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta worldMeta = worldItem.getItemMeta();
        if (worldMeta != null) {
            worldMeta.setDisplayName("§bWorld Name");
            worldMeta.setLore(Arrays.asList(
                    "§7Current: §a" + worldName,
                    "§7World where chests will spawn",
                    "",
                    "§eNote: Set via coordinates",
                    "§7Use §e/eloot wand§7 to add locations"
            ));
            worldItem.setItemMeta(worldMeta);
        }
        inventory.setItem(12, worldItem);

        // Particles Toggle (Slot 14)
        ItemStack particlesItem = new ItemStack(particlesEnabled ? Material.REDSTONE_TORCH : Material.LEVER);
        ItemMeta particlesMeta = particlesItem.getItemMeta();
        if (particlesMeta != null) {
            particlesMeta.setDisplayName("§dParticles");
            particlesMeta.setLore(Arrays.asList(
                    "§7Current: " + (particlesEnabled ? "§aEnabled" : "§cDisabled"),
                    "§7Show particle effects around chests",
                    "",
                    "§eClick to toggle"
            ));
            particlesItem.setItemMeta(particlesMeta);
        }
        inventory.setItem(14, particlesItem);

        // Hologram Text Display (Slot 16)
        ItemStack hologramItem = new ItemStack(Material.OAK_SIGN);
        ItemMeta hologramMeta = hologramItem.getItemMeta();
        if (hologramMeta != null) {
            hologramMeta.setDisplayName("§6Hologram Text");
            hologramMeta.setLore(Arrays.asList(
                    "§7Current: §a" + hologramText,
                    "§7Text displayed above chests",
                    "",
                    "§eUse §7/eloot edit " + groupName + " hologram-text <text>"
            ));
            hologramItem.setItemMeta(hologramMeta);
        }
        inventory.setItem(16, hologramItem);

        // Announce Rarities Display (Slot 28)
        ItemStack announceItem = new ItemStack(Material.BELL);
        ItemMeta announceMeta = announceItem.getItemMeta();
        if (announceMeta != null) {
            announceMeta.setDisplayName("§5Announce Rarities");
            announceMeta.setLore(Arrays.asList(
                    "§7Current: §a" + String.join(", ", announceRarities),
                    "§7Rarities that trigger global announcements",
                    "",
                    "§eUse §7/eloot edit " + groupName + " announce-rarities <list>"
            ));
            announceItem.setItemMeta(announceMeta);
        }
        inventory.setItem(28, announceItem);

        // Boss Display Name Display (Slot 30)
        ItemStack displayNameItem = new ItemStack(Material.NAME_TAG);
        ItemMeta displayNameMeta = displayNameItem.getItemMeta();
        if (displayNameMeta != null) {
            displayNameMeta.setDisplayName("§3Display Name");
            displayNameMeta.setLore(Arrays.asList(
                    "§7Current: §a" + bossDisplayName,
                    "§7Name used in announcements",
                    "",
                    "§eUse §7/eloot edit " + groupName + " boss-display-name <name>"
            ));
            displayNameItem.setItemMeta(displayNameMeta);
        }
        inventory.setItem(30, displayNameItem);

        // Instructions Item (Slot 32)
        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        if (helpMeta != null) {
            helpMeta.setDisplayName("§bNeed More Options?");
            helpMeta.setLore(Arrays.asList(
                    "§7Some settings require commands:",
                    "§e/eloot edit " + groupName + " <setting> <value>",
                    "",
                    "§7Available settings:",
                    "§8- chest-spawn-count",
                    "§8- particles-enabled",
                    "§8- hologram-text",
                    "§8- announce-rarities",
                    "§8- boss-display-name"
            ));
            helpItem.setItemMeta(helpMeta);
        }
        inventory.setItem(32, helpItem);

        // Save Button (Slot 48)
        ItemStack saveButton = new ItemStack(Material.EMERALD);
        ItemMeta saveMeta = saveButton.getItemMeta();
        if (saveMeta != null) {
            saveMeta.setDisplayName("§aReload Config");
            saveMeta.setLore(Arrays.asList(
                    "§7Reload configuration from disk",
                    "§7Use if you edited files manually",
                    "",
                    "§eClick to reload"
            ));
            saveButton.setItemMeta(saveMeta);
        }
        inventory.setItem(48, saveButton);

        // Back Button (Slot 49)
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§fBack to Management");
            backMeta.setLore(Arrays.asList("§7Return to group management"));
            backButton.setItemMeta(backMeta);
        }
        inventory.setItem(49, backButton);

        // Close Button (Slot 50)
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("§cClose");
            closeMeta.setLore(Arrays.asList("§7Close this menu"));
            closeButton.setItemMeta(closeMeta);
        }
        inventory.setItem(50, closeButton);
    }

    public void handleClick(int slot, boolean isRightClick) {
        switch (slot) {
            case 10: // Chest Spawn Count
                handleSpawnCountChange(isRightClick);
                break;

            case 14: // Particles Toggle
                toggleParticles();
                break;

            case 48: // Reload Config
                reloadConfig();
                break;

            case 49: // Back to Management
                openManagementGUI();
                break;

            case 50: // Close
                player.closeInventory();
                break;

            case 12: // World Name (Read-only)
            case 16: // Hologram Text (Read-only)
            case 28: // Announce Rarities (Read-only)
            case 30: // Display Name (Read-only)
            case 32: // Help Item (Read-only)
                player.sendMessage("§eUse §7/eloot edit " + groupName + " §eto modify this setting");
                break;
        }
    }

    private void handleSpawnCountChange(boolean isRightClick) {
        int current = config.getInt(ConfigField.CHEST_SPAWN_COUNT.getKey(), 10);
        int newValue = isRightClick ? Math.max(1, current - 1) : Math.min(64, current + 1);

        // Update config
        config.set(ConfigField.CHEST_SPAWN_COUNT.getKey(), newValue);

        // Save to file
        if (bossManager.editGroupConfig(groupName, ConfigField.CHEST_SPAWN_COUNT.getKey(), String.valueOf(newValue))) {
            player.sendMessage("§aChest spawn count set to: §e" + newValue);
            updateGUI();
        } else {
            player.sendMessage("§cFailed to save configuration!");
        }
    }

    private void toggleParticles() {
        boolean current = config.getBoolean(ConfigField.PARTICLES_ENABLED.getKey(), true);
        boolean newValue = !current;

        // Update config
        config.set(ConfigField.PARTICLES_ENABLED.getKey(), newValue);

        // Save to file
        if (bossManager.editGroupConfig(groupName, ConfigField.PARTICLES_ENABLED.getKey(), String.valueOf(newValue))) {
            player.sendMessage("§7Particles " + (newValue ? "§aenabled" : "§cdisabled"));
            updateGUI();
        } else {
            player.sendMessage("§cFailed to save configuration!");
        }
    }

    private void reloadConfig() {
        this.config = bossManager.getGroupConfig(groupName);
        updateGUI();
        player.sendMessage("§aConfiguration reloaded from disk");
    }

    private void updateGUI() {
        setupGUI(); // Refresh the GUI
        player.updateInventory();
    }

    private void openManagementGUI() {
        player.closeInventory();
        GroupManagementGUI managementGUI = new GroupManagementGUI(player, groupName, bossManager, bossRegistry, plugin);
        plugin.getGuiManager().openGUI(player, managementGUI);
    }
}