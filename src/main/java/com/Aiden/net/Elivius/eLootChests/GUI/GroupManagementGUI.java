package com.Aiden.net.Elivius.eLootChests.GUI;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.Aiden.net.Elivius.eLootChests.BossManager;
import com.Aiden.net.Elivius.eLootChests.BossRegistry;
import com.Aiden.net.Elivius.eLootChests.LootChests;
import org.bukkit.Location; // ADD THIS IMPORT
import java.util.Arrays;
import java.util.List;

public class GroupManagementGUI extends LootChestGUI {
    private final BossManager bossManager;
    private final BossRegistry bossRegistry;
    private final String groupName;
    private final LootChests plugin;

    public GroupManagementGUI(Player player, String groupName, BossManager bossManager, BossRegistry bossRegistry, LootChests plugin) {
        super(player, "§8Managing: §e" + groupName, 54);
        this.bossManager = bossManager;
        this.bossRegistry = bossRegistry;
        this.groupName = groupName;
        this.plugin = plugin;
        setupGUI();
    }

    private void setupGUI() {
        // Fill border
        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);
        fillBorder(border);

        // Get real data with proper world detection
        int chestCount = bossManager.getChestCount(groupName);
        int itemCount = bossManager.getItemCount(groupName);
        String detectedWorldName = getWorldNameFromCoordinates(groupName); // CHANGED VARIABLE NAME

        // Group Info Item
        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§6§l" + groupName + " Info");

        // Dynamic lore based on whether coordinates exist
        if (chestCount == 0) {
            infoMeta.setLore(Arrays.asList(
                    "§7Chest locations: §c" + chestCount + " §7(No coordinates set!)",
                    "§7Total items: §e" + itemCount,
                    "§7World: §cSet coordinates first!",
                    "",
                    "§c⚠ Use /eloot wand to add chest locations",
                    "§7Group configuration and statistics"
            ));
        } else {
            infoMeta.setLore(Arrays.asList(
                    "§7Chest locations: §a" + chestCount,
                    "§7Total items: §e" + itemCount,
                    "§7World: §e" + detectedWorldName, // USE CHANGED VARIABLE NAME
                    "",
                    "§7Group configuration and statistics"
            ));
        }

        infoItem.setItemMeta(infoMeta);
        inventory.setItem(4, infoItem);

        // Config Editor Button
        ItemStack configButton = new ItemStack(Material.REDSTONE_TORCH);
        ItemMeta configMeta = configButton.getItemMeta();
        configMeta.setDisplayName("§cEdit Configuration");
        configMeta.setLore(Arrays.asList(
                "§7Edit group settings and options",
                "",
                "§eClick to open config editor"
        ));
        configButton.setItemMeta(configMeta);
        inventory.setItem(20, configButton);

        // Loot Table Manager Button
        ItemStack lootButton = new ItemStack(Material.CHEST);
        ItemMeta lootMeta = lootButton.getItemMeta();
        lootMeta.setDisplayName("§aManage Loot Tables");
        lootMeta.setLore(Arrays.asList(
                "§7Add/remove items from loot tables",
                "",
                "§eClick to manage loot items"
        ));
        lootButton.setItemMeta(lootMeta);
        inventory.setItem(22, lootButton);

        // Spawn Chests Button (Enable/disable based on coordinates)
        ItemStack spawnButton = new ItemStack(chestCount > 0 ? Material.ENDER_CHEST : Material.BARRIER);
        ItemMeta spawnMeta = spawnButton.getItemMeta();
        spawnMeta.setDisplayName(chestCount > 0 ? "§bSpawn Chests" : "§cNo Coordinates Set");

        if (chestCount > 0) {
            spawnMeta.setLore(Arrays.asList(
                    "§7Spawn chests in the world",
                    "§7World: §e" + detectedWorldName, // USE CHANGED VARIABLE NAME
                    "§7Chests to spawn: §e" + chestCount,
                    "",
                    "§eClick to spawn chests now"
            ));
        } else {
            spawnMeta.setLore(Arrays.asList(
                    "§cNo chest locations configured!",
                    "§7Use §e/eloot wand select " + groupName + "§7 first",
                    "§7to add chest locations",
                    "",
                    "§cCannot spawn chests without locations"
            ));
        }

        spawnButton.setItemMeta(spawnMeta);
        inventory.setItem(24, spawnButton);

        // Back Button
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§fBack to Groups");
        backMeta.setLore(Arrays.asList("§7Return to group selection"));
        backButton.setItemMeta(backMeta);
        inventory.setItem(49, backButton);
    }

    public void handleClick(int slot, boolean isRightClick) {
        switch (slot) {
            case 20: // Config Editor
                player.closeInventory();
                ConfigEditorGUI configGUI = new ConfigEditorGUI(player, groupName, bossManager, bossRegistry, plugin);
                plugin.getGuiManager().openGUI(player, configGUI);
                break;

            case 22: // Loot Table Manager
                player.closeInventory();
                PaginatedLootTableGUI lootGUI = new PaginatedLootTableGUI(player, groupName, bossManager, bossRegistry, plugin);
                plugin.getGuiManager().openGUI(player, lootGUI);
                break;

            case 24: // Spawn Chests
                handleSpawnChests();
                break;

            case 49: // Back Button
                handleBackButton();
                break;
        }
    }

    private void handleSpawnChests() {
        int chestCount = bossManager.getChestCount(groupName);

        if (chestCount == 0) {
            player.sendMessage("§cCannot spawn chests! No locations configured.");
            player.sendMessage("§7Use: §e/eloot wand select " + groupName);
            player.sendMessage("§7Then right-click blocks to add locations");
            return;
        }

        player.closeInventory();
        player.performCommand("eloot spawn " + groupName);
        player.sendMessage("§aSpawning chests for: §e" + groupName);
    }

    private void handleBackButton() {
        player.closeInventory();
        GroupSelectionGUI groupGUI = new GroupSelectionGUI(player, bossRegistry, bossManager, plugin);
        plugin.getGuiManager().openGUI(player, groupGUI);
    }

    private String getWorldNameFromCoordinates(String groupName) {
        List<Location> locations = bossManager.getSavedChestLocations(groupName);
        if (locations == null || locations.isEmpty()) {
            return "§cSet coordinates first!";
        }

        // Get world from first coordinate
        String worldName = locations.get(0).getWorld().getName();
        return worldName;
    }
}