package com.Aiden.net.Elivius.eLootChests.GUI;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.Aiden.net.Elivius.eLootChests.BossManager;
import com.Aiden.net.Elivius.eLootChests.BossRegistry;
import com.Aiden.net.Elivius.eLootChests.LootChests;
import com.Aiden.net.Elivius.eLootChests.Enums.Rarity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PaginatedLootTableGUI extends LootChestGUI {
    private final BossManager bossManager;
    private final BossRegistry bossRegistry;
    private final LootChests plugin;
    private final String groupName;
    private Rarity currentRarity = null;

    public PaginatedLootTableGUI(Player player, String groupName, BossManager bossManager, BossRegistry bossRegistry, LootChests plugin) {
        super(player, "§8Loot Table: §e" + groupName, 54);
        this.bossManager = bossManager;
        this.bossRegistry = bossRegistry;
        this.plugin = plugin;
        this.groupName = groupName;
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

        if (currentRarity == null) {
            setupOverviewPage();
        } else {
            setupRarityPage(currentRarity);
        }

        setupNavigationButtons();
    }

    private void setupOverviewPage() {
        // FIRST: Clear the inventory before setting up overview
        clearInventoryContent();

        // THEN: Setup overview page
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§6Loot Table Overview: §e" + groupName);

            Map<Rarity, Integer> itemCounts = bossManager.getLootTableSummary(groupName);
            int totalItems = itemCounts.values().stream().mapToInt(Integer::intValue).sum();

            infoMeta.setLore(Arrays.asList(
                    "§7Click on a rarity to view items",
                    "§7Total items: §e" + totalItems,
                    "",
                    "§eRarity breakdown:",
                    getRarityBreakdown(itemCounts)
            ));
            infoItem.setItemMeta(infoMeta);
        }
        inventory.setItem(4, infoItem);

        // Display each rarity as a clickable item
        int[] raritySlots = {
                10,11,12,13,14,15,16,
                19,20,21,22,23,24,25,
                28,29,30,31,32,33,34
        };

        Rarity[] rarities = Rarity.values();

        for (int i = 0; i < rarities.length && i < raritySlots.length; i++) {
            Rarity rarity = rarities[i];
            int itemCount = bossManager.getLootTableSummary(groupName).getOrDefault(rarity, 0);

            ItemStack rarityItem = getRarityDisplayItem(rarity, itemCount);
            inventory.setItem(raritySlots[i], rarityItem);
        }

        // Setup navigation buttons for overview page
        setupNavigationButtons();
    }

    private void setupRarityPage(Rarity rarity) {
        // FIRST: Clear the entire inventory except borders
        clearInventoryContent();

        // THEN: Add the rarity header
        ItemStack headerItem = new ItemStack(getMaterialForRarity(rarity));
        ItemMeta headerMeta = headerItem.getItemMeta();
        if (headerMeta != null) {
            int itemCount = bossManager.getLootTableSummary(groupName).getOrDefault(rarity, 0);
            headerMeta.setDisplayName(rarity.getFormattedName() + " §7Items");
            headerMeta.setLore(Arrays.asList(
                    "§7Total items: §e" + itemCount,
                    "§7Default chance: §e" + rarity.getDefaultPercentage() + "%",
                    "",
                    "§eItems in this rarity:"
            ));
            headerItem.setItemMeta(headerMeta);
        }
        inventory.setItem(4, headerItem);

        // Display actual items from the loot table
        Map<ItemStack, Double> itemsWithPercentages = bossManager.getItemsWithPercentages(groupName, rarity);
        int itemCount = itemsWithPercentages.size();

        if (itemCount > 0) {
            int slot = 10; // Start displaying items
            int itemIndex = 0;

            for (Map.Entry<ItemStack, Double> entry : itemsWithPercentages.entrySet()) {
                if (slot > 43) break; // Don't overflow the inventory

                ItemStack lootItem = entry.getKey().clone();
                double percentage = entry.getValue();

                // Add lore to show percentage
                ItemMeta meta = lootItem.getItemMeta();
                if (meta != null) {
                    meta.setLore(Arrays.asList(
                            "§7Spawn chance: §e" + percentage + "%",
                            "§7Rarity: " + rarity.getFormattedName(),
                            "§7Index: §e" + itemIndex,
                            "",
                            "§8Use §7/eloot table remove §8to delete"
                    ));
                    lootItem.setItemMeta(meta);
                }

                inventory.setItem(slot, lootItem);
                slot++;
                itemIndex++;

                // Move to next row every 7 slots
                if (slot % 9 == 7) slot += 2;
            }
        } else {
            // No items in this rarity
            ItemStack emptyItem = new ItemStack(Material.BARRIER);
            ItemMeta emptyMeta = emptyItem.getItemMeta();
            if (emptyMeta != null) {
                emptyMeta.setDisplayName("§cNo Items in " + rarity.getName());
                emptyMeta.setLore(Arrays.asList(
                        "§7This rarity has no items yet",
                        "",
                        "§eHow to add items:",
                        "§71. Hold the item in your hand",
                        "§72. Use §e/eloot table add " + groupName + " " + rarity.name().toLowerCase() + " [percentage]",
                        "§73. The item will appear here!"
                ));
                emptyItem.setItemMeta(emptyMeta);
            }
            inventory.setItem(22, emptyItem);
        }

        // Setup navigation buttons for rarity page
        setupNavigationButtons();
    }

    private void clearInventoryContent() {
        // Clear all slots except borders
        int[] slotsToClear = {
                10,11,12,13,14,15,16,
                19,20,21,22,23,24,25,
                28,29,30,31,32,33,34,
                1,2,3,4,5,6,7, // Top row middle
                46,47,48,50,51,52 // Bottom row middle
        };

        for (int slot : slotsToClear) {
            if (slot < inventory.getSize()) {
                inventory.setItem(slot, null);
            }
        }
    }

    private ItemStack getRarityDisplayItem(Rarity rarity, int itemCount) {
        Material material = getMaterialForRarity(rarity);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(rarity.getFormattedName());
            meta.setLore(Arrays.asList(
                    "§7Items: §e" + itemCount,
                    "§7Default chance: §e" + rarity.getDefaultPercentage() + "%",
                    "",
                    "§eClick to view " + rarity.getName() + " items",
                    "§7" + (itemCount > 0 ? "§aContains " + itemCount + " items" : "§cNo items yet")
            ));
            item.setItemMeta(meta);
        }

        return item;
    }

    private Material getMaterialForRarity(Rarity rarity) {
        switch (rarity) {
            case COMMON: return Material.WHITE_WOOL;
            case RARE: return Material.LIGHT_BLUE_WOOL;
            case EPIC: return Material.PURPLE_WOOL;
            case LEGENDARY: return Material.ORANGE_WOOL;
            case MYTHIC: return Material.MAGENTA_WOOL;
            case GODLIKE: return Material.RED_WOOL;
            default: return Material.CHEST;
        }
    }

    // ADD THIS MISSING METHOD:
    private String getRarityBreakdown(Map<Rarity, Integer> itemCounts) {
        StringBuilder breakdown = new StringBuilder();
        for (Rarity rarity : Rarity.values()) {
            int count = itemCounts.getOrDefault(rarity, 0);
            breakdown.append("§8- ")
                    .append(rarity.getFormattedName())
                    .append("§7: §e")
                    .append(count)
                    .append("\n");
        }
        return breakdown.toString().trim();
    }

    private void setupNavigationButtons() {
        if (currentRarity == null) {
            // Overview page buttons
            addButton(48, Material.BARRIER, "§cBack to Management", "Return to group management");
            addButton(49, Material.MAP, "§bLoot Table Overview", "Click rarities to view items");
            addButton(50, Material.EMERALD, "§aRefresh", "Reload loot table");
        } else {
            // Rarity page buttons
            addButton(45, Material.ARROW, "§f← Back to Overview", "Return to rarity selection");
            addButton(49, Material.BARRIER, "§cBack to Management", "Return to group management");
            addButton(53, Material.EMERALD, "§aRefresh", "Reload items");
        }
    }

    private void addButton(int slot, Material material, String name, String lore) {
        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList("§7" + lore));
            button.setItemMeta(meta);
        }
        inventory.setItem(slot, button);
    }

    public void handleClick(int slot, boolean isRightClick) {
        // Navigation buttons first
        if (slot == 48) {
            openManagementGUI();
            return;
        }
        if (slot == 50 || slot == 53) {
            updateGUI();
            player.sendMessage("§aRefreshed!");
            return;
        }
        if (slot == 45 && currentRarity != null) {
            currentRarity = null;
            updateGUI();
            return;
        }

        // Rarity clicks
        if (currentRarity == null) {
            ItemStack clicked = inventory.getItem(slot);
            if (clicked != null && clicked.getType().name().contains("WOOL")) {
                Rarity clickedRarity = getRarityFromWoolColor(clicked.getType());
                if (clickedRarity != null) {
                    openRarityPage(clickedRarity);
                    return;
                }
            }
        }
    }

    private Rarity getRarityFromWoolColor(Material woolType) {
        switch (woolType) {
            case WHITE_WOOL: return Rarity.COMMON;
            case LIGHT_BLUE_WOOL: return Rarity.RARE;
            case PURPLE_WOOL: return Rarity.EPIC;
            case ORANGE_WOOL: return Rarity.LEGENDARY;
            case MAGENTA_WOOL: return Rarity.MYTHIC;
            case RED_WOOL: return Rarity.GODLIKE;
            default: return null;
        }
    }

    private void openRarityPage(Rarity rarity) {
        currentRarity = rarity;
        updateGUI();
        player.sendMessage("§aOpening " + rarity.getFormattedName() + "§a items...");
    }

    private void updateGUI() {
        setupGUI();
        player.updateInventory();
    }

    private void openManagementGUI() {
        player.closeInventory();
        GroupManagementGUI managementGUI = new GroupManagementGUI(player, groupName, bossManager, bossRegistry, plugin);
        plugin.getGuiManager().openGUI(player, managementGUI);
    }
}