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

public class PaginatedLootTableGUI extends LootChestGUI {
    private final BossManager bossManager;
    private final BossRegistry bossRegistry;
    private final LootChests plugin;
    private final String groupName;
    private int currentPage = 0;
    private final int itemsPerPage = 28; // 7x4 grid
    private final int totalPages;

    public PaginatedLootTableGUI(Player player, String groupName, BossManager bossManager, BossRegistry bossRegistry, LootChests plugin) {
        super(player, "§8Loot Table: §e" + groupName, 54);
        this.bossManager = bossManager;
        this.bossRegistry = bossRegistry;
        this.plugin = plugin;
        this.groupName = groupName;

        // Calculate total pages needed (one page per rarity + overview)
        this.totalPages = Rarity.values().length + 1; // +1 for overview page
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

        if (currentPage == 0) {
            setupOverviewPage();
        } else {
            setupRarityPage(currentPage - 1); // Page 1 = Rarity index 0, etc.
        }

        setupNavigationButtons();
    }

    private void setupOverviewPage() {
        // Overview page showing all rarities
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§6Loot Table Overview: §e" + groupName);
            infoMeta.setLore(Arrays.asList(
                    "§7Click on a rarity to view/edit items",
                    "§7Total items: §e" + bossManager.getItemCount(groupName),
                    "",
                    "§eComing soon: Item management GUI"
            ));
            infoItem.setItemMeta(infoMeta);
        }
        inventory.setItem(4, infoItem);

        // Display each rarity as a clickable item
        int slot = 10; // Start slot for rarities
        Rarity[] rarities = Rarity.values();

        for (int i = 0; i < rarities.length && slot <= 43; i++) {
            Rarity rarity = rarities[i];
            int itemCount = bossManager.getLootTableSummary(groupName).getOrDefault(rarity, 0);

            ItemStack rarityItem = new ItemStack(Material.CHEST);
            ItemMeta rarityMeta = rarityItem.getItemMeta();
            if (rarityMeta != null) {
                rarityMeta.setDisplayName(rarity.getFormattedName());
                rarityMeta.setLore(Arrays.asList(
                        "§7Items: §e" + itemCount,
                        "§7Default chance: §e" + rarity.getDefaultPercentage() + "%",
                        "",
                        "§eClick to view/edit " + rarity.getName() + " items"
                ));
                rarityItem.setItemMeta(rarityMeta);
            }

            inventory.setItem(slot, rarityItem);
            slot++;

            // Move to next row every 7 slots
            if (slot % 9 == 7) slot += 2;
        }
    }

    private void setupRarityPage(int rarityIndex) {
        Rarity[] rarities = Rarity.values();
        if (rarityIndex < 0 || rarityIndex >= rarities.length) return;

        Rarity rarity = rarities[rarityIndex];
        int itemCount = bossManager.getLootTableSummary(groupName).getOrDefault(rarity, 0);

        // Rarity header
        ItemStack headerItem = new ItemStack(Material.PAPER);
        ItemMeta headerMeta = headerItem.getItemMeta();
        if (headerMeta != null) {
            headerMeta.setDisplayName(rarity.getFormattedName() + " §7Items");
            headerMeta.setLore(Arrays.asList(
                    "§7Total items: §e" + itemCount,
                    "§7Default chance: §e" + rarity.getDefaultPercentage() + "%",
                    "",
                    "§eComing soon: Item editing"
            ));
            headerItem.setItemMeta(headerMeta);
        }
        inventory.setItem(4, headerItem);

        // Placeholder for future item display
        ItemStack comingSoonItem = new ItemStack(Material.BARRIER);
        ItemMeta soonMeta = comingSoonItem.getItemMeta();
        if (soonMeta != null) {
            soonMeta.setDisplayName("§cItem Management Coming Soon");
            soonMeta.setLore(Arrays.asList(
                    "§7Use §e/eloot table add " + groupName + " " + rarity.name().toLowerCase(),
                    "§7to add items for now",
                    "",
                    "§7GUI item management will be",
                    "§7added in a future update"
            ));
            comingSoonItem.setItemMeta(soonMeta);
        }
        inventory.setItem(22, comingSoonItem);
    }

    private void setupNavigationButtons() {
        // Previous Page button (only show if not on first page)
        if (currentPage > 0) {
            ItemStack prevButton = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevButton.getItemMeta();
            if (prevMeta != null) {
                prevMeta.setDisplayName("§fPrevious Page");
                prevMeta.setLore(Arrays.asList("§7Go back to previous page"));
                prevButton.setItemMeta(prevMeta);
            }
            inventory.setItem(45, prevButton);
        }

        // Page indicator
        ItemStack pageIndicator = new ItemStack(Material.MAP);
        ItemMeta pageMeta = pageIndicator.getItemMeta();
        if (pageMeta != null) {
            String pageName = currentPage == 0 ? "Overview" : Rarity.values()[currentPage - 1].getFormattedName();
            pageMeta.setDisplayName("§ePage " + (currentPage + 1) + " of " + totalPages);
            pageMeta.setLore(Arrays.asList(
                    "§7Current: §e" + pageName,
                    "§7Use arrows to navigate"
            ));
            pageIndicator.setItemMeta(pageMeta);
        }
        inventory.setItem(49, pageIndicator);

        // Next Page button (only show if not on last page)
        if (currentPage < totalPages - 1) {
            ItemStack nextButton = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextButton.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName("§fNext Page");
                nextMeta.setLore(Arrays.asList("§7Go to next page"));
                nextButton.setItemMeta(nextMeta);
            }
            inventory.setItem(53, nextButton);
        }

        // Back Button (always visible)
        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§cBack to Management");
            backMeta.setLore(Arrays.asList("§7Return to group management"));
            backButton.setItemMeta(backMeta);
        }
        inventory.setItem(48, backButton);
    }

    public void handleClick(int slot, boolean isRightClick) {
        switch (slot) {
            case 45: // Previous Page
                if (currentPage > 0) {
                    currentPage--;
                    updateGUI();
                }
                break;

            case 53: // Next Page
                if (currentPage < totalPages - 1) {
                    currentPage++;
                    updateGUI();
                }
                break;

            case 48: // Back to Management
                openManagementGUI();
                break;

            default:
                // Handle rarity clicks on overview page
                if (currentPage == 0 && slot >= 10 && slot <= 43) {
                    handleRarityClick(slot);
                }
                break;
        }
    }

    private void handleRarityClick(int slot) {
        // Calculate which rarity was clicked based on slot position
        int baseSlot = 10;
        int slotsPerRow = 7;
        int clickedIndex = (slot - baseSlot) % 9;
        if (clickedIndex >= slotsPerRow) return; // Not a valid rarity slot

        int row = (slot - baseSlot) / 9;
        int rarityIndex = row * slotsPerRow + clickedIndex;

        if (rarityIndex < Rarity.values().length) {
            // Go to the specific rarity page
            currentPage = rarityIndex + 1; // +1 because page 0 is overview
            updateGUI();
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