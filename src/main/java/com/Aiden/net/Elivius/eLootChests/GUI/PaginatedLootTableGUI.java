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
import java.util.Map;

public class PaginatedLootTableGUI extends LootChestGUI {
    private final BossManager bossManager;
    private final BossRegistry bossRegistry;
    private final LootChests plugin;
    private final String groupName;
    private Rarity currentRarity = null; // null = overview page

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
        // Overview page showing all rarities
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

        // FIXED: Use exact slot positions for rarities
        int[] raritySlots = {
                // Row 1: 10-16
                10, 11, 12, 13, 14, 15, 16,
                // Row 2: 19-25
                19, 20, 21, 22, 23, 24, 25,
                // Row 3: 28-34
                28, 29, 30, 31, 32, 33, 34
        };

        Rarity[] rarities = Rarity.values();

        for (int i = 0; i < rarities.length && i < raritySlots.length; i++) {
            Rarity rarity = rarities[i];
            int itemCount = bossManager.getLootTableSummary(groupName).getOrDefault(rarity, 0);

            ItemStack rarityItem = getRarityDisplayItem(rarity, itemCount);
            inventory.setItem(raritySlots[i], rarityItem);
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

    private void setupRarityPage(Rarity rarity) {
        int itemCount = bossManager.getLootTableSummary(groupName).getOrDefault(rarity, 0);
        Map<ItemStack, Double> itemsWithPercentages = bossManager.getItemsWithPercentages(groupName, rarity);

        // Rarity header
        ItemStack headerItem = new ItemStack(getMaterialForRarity(rarity));
        ItemMeta headerMeta = headerItem.getItemMeta();
        if (headerMeta != null) {
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
    }

    private void setupNavigationButtons() {
        if (currentRarity == null) {
            // OVERVIEW PAGE LAYOUT
            ItemStack backButton = new ItemStack(Material.BARRIER);
            ItemMeta backMeta = backButton.getItemMeta();
            if (backMeta != null) {
                backMeta.setDisplayName("§cBack to Management");
                backMeta.setLore(Arrays.asList("§7Return to group management"));
                backButton.setItemMeta(backMeta);
            }
            inventory.setItem(48, backButton);

            ItemStack pageIndicator = new ItemStack(Material.MAP);
            ItemMeta pageMeta = pageIndicator.getItemMeta();
            if (pageMeta != null) {
                pageMeta.setDisplayName("§bLoot Table Overview");
                pageMeta.setLore(Arrays.asList(
                        "§7Click any rarity to view items",
                        "§7" + Rarity.values().length + " rarity tiers available"
                ));
                pageIndicator.setItemMeta(pageMeta);
            }
            inventory.setItem(49, pageIndicator);

            ItemStack refreshButton = new ItemStack(Material.EMERALD);
            ItemMeta refreshMeta = refreshButton.getItemMeta();
            if (refreshMeta != null) {
                refreshMeta.setDisplayName("§aRefresh");
                refreshMeta.setLore(Arrays.asList("§7Reload loot table data"));
                refreshButton.setItemMeta(refreshMeta);
            }
            inventory.setItem(50, refreshButton);

        } else {
            // RARITY PAGE LAYOUT
            ItemStack backButton = new ItemStack(Material.ARROW);
            ItemMeta backMeta = backButton.getItemMeta();
            if (backMeta != null) {
                backMeta.setDisplayName("§f← Back to Overview");
                backMeta.setLore(Arrays.asList("§7Return to rarity selection"));
                backButton.setItemMeta(backMeta);
            }
            inventory.setItem(45, backButton);

            ItemStack managementButton = new ItemStack(Material.BARRIER);
            ItemMeta managementMeta = managementButton.getItemMeta();
            if (managementMeta != null) {
                managementMeta.setDisplayName("§cBack to Management");
                managementMeta.setLore(Arrays.asList("§7Return to group management"));
                managementButton.setItemMeta(managementMeta);
            }
            inventory.setItem(49, managementButton);

            ItemStack refreshButton = new ItemStack(Material.EMERALD);
            ItemMeta refreshMeta = refreshButton.getItemMeta();
            if (refreshMeta != null) {
                refreshMeta.setDisplayName("§aRefresh");
                refreshMeta.setLore(Arrays.asList("§7Reload item data"));
                refreshButton.setItemMeta(refreshMeta);
            }
            inventory.setItem(53, refreshButton);
        }
    }

    public void handleClick(int slot, boolean isRightClick) {
        // DEBUG: Tell us what slot was clicked
        player.sendMessage("§7DEBUG: Clicked slot " + slot);

        if (currentRarity == null) {
            // OVERVIEW PAGE CLICKS
            switch (slot) {
                case 48: // Back to Management
                    openManagementGUI();
                    break;

                case 50: // Refresh
                    updateGUI();
                    player.sendMessage("§aLoot table refreshed!");
                    break;

                default:
                    // Handle rarity clicks - FIXED: Use exact slot mapping
                    handleRarityClick(slot);
                    break;
            }
        } else {
            // RARITY PAGE CLICKS
            switch (slot) {
                case 45: // Back to Overview
                    currentRarity = null;
                    updateGUI();
                    player.sendMessage("§aReturning to overview...");
                    break;

                case 49: // Back to Management
                    openManagementGUI();
                    break;

                case 53: // Refresh
                    updateGUI();
                    player.sendMessage("§aItem list refreshed!");
                    break;

                default:
                    // Handle item clicks
                    if (slot >= 10 && slot <= 43) {
                        handleItemClick(slot);
                    }
                    break;
            }
        }
    }

    private void handleRarityClick(int slot) {
        // FIXED: Exact slot mapping for rarities
        int[] raritySlots = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34};
        Rarity[] rarities = Rarity.values();

        player.sendMessage("§7DEBUG: Checking slot " + slot + " against " + raritySlots.length + " possible slots");

        for (int i = 0; i < raritySlots.length; i++) {
            if (slot == raritySlots[i]) {
                player.sendMessage("§7DEBUG: Found matching slot at index " + i);
                if (i < rarities.length) {
                    currentRarity = rarities[i];
                    updateGUI();
                    player.sendMessage("§aOpening " + currentRarity.getFormattedName() + "§a items...");
                    return;
                } else {
                    player.sendMessage("§cNo rarity found for slot index " + i);
                    return;
                }
            }
        }
        player.sendMessage("§7DEBUG: Slot " + slot + " not found in rarity slots");
    }

    private void handleItemClick(int slot) {
        ItemStack clicked = inventory.getItem(slot);
        if (clicked != null && clicked.getType() != Material.AIR) {
            player.sendMessage("§eItem: §7" + clicked.getType().name());
            player.sendMessage("§eRarity: " + currentRarity.getFormattedName());

            if (clicked.hasItemMeta() && clicked.getItemMeta().hasLore()) {
                for (String line : clicked.getItemMeta().getLore()) {
                    if (line.contains("Spawn chance:")) {
                        player.sendMessage("§e" + line.trim());
                        break;
                    }
                }
            }
        }
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