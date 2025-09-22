package com.Aiden.net.Elivius.eLootChests.GUI;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.Aiden.net.Elivius.eLootChests.BossManager;
import com.Aiden.net.Elivius.eLootChests.BossRegistry;
import com.Aiden.net.Elivius.eLootChests.LootChests;
import java.util.Arrays;

public class GroupSelectionGUI extends LootChestGUI {
    private final BossRegistry bossRegistry;
    private BossManager bossManager;
    private final LootChests plugin;

    // UPDATED CONSTRUCTOR: Now accepts 4 parameters
    public GroupSelectionGUI(Player player, BossRegistry bossRegistry, BossManager bossManager, LootChests plugin) {
        super(player, "§8Loot Chest Groups", 54);
        this.bossRegistry = bossRegistry;
        this.bossManager = bossManager;
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

        // Add group items with REAL data
        int slot = 19;
        for (String groupName : bossRegistry.getAllBossNames()) {
            if (slot >= 43) break;

            ItemStack groupItem = new ItemStack(Material.CHEST);
            ItemMeta meta = groupItem.getItemMeta();
            meta.setDisplayName("§e" + groupName);

            // Get real data
            int chestCount = bossManager.getChestCount(groupName);
            int itemCount = bossManager.getItemCount(groupName);
            String worldName = bossManager.getWorldName(groupName);

            meta.setLore(Arrays.asList(
                    "§7Click to manage this group",
                    "§7Chest locations: §e" + chestCount,
                    "§7Total items: §e" + itemCount,
                    "§7World: §e" + worldName,
                    "",
                    "§aLeft-click: §7Manage group",
                    "§cRight-click: §7Spawn chests"
            ));
            groupItem.setItemMeta(meta);

            inventory.setItem(slot, groupItem);
            slot++;

            // Row formatting (skip border slots)
            if (slot % 9 == 8) slot += 2;
        }

        // Add create new group button
        ItemStack createButton = new ItemStack(Material.EMERALD);
        ItemMeta createMeta = createButton.getItemMeta();
        createMeta.setDisplayName("§aCreate New Group");
        createMeta.setLore(Arrays.asList(
                "§7Click to create a new boss group",
                "",
                "§eCreates config, loottable, and coordinates files"
        ));
        createButton.setItemMeta(createMeta);
        inventory.setItem(49, createButton);
    }

    public void handleClick(int slot, boolean isRightClick) {
        if (slot == 49) { // Create New Group button
            player.closeInventory();
            player.performCommand("eloot new");
            return;
        }

        // Check if clicked on a group item (slots 19-43)
        if (slot >= 19 && slot <= 43 && slot % 9 != 8 && slot % 9 != 0) {
            ItemStack clicked = inventory.getItem(slot);
            if (clicked != null && clicked.getType() == Material.CHEST) {
                String groupName = clicked.getItemMeta().getDisplayName().replace("§e", "");
                player.closeInventory();

                if (isRightClick) {
                    // Right-click: Spawn chests
                    player.performCommand("eloot spawn " + groupName);
                    player.sendMessage("§aSpawning chests for group: §e" + groupName);
                } else {
                    // Left-click: Open management GUI
                    GroupManagementGUI managementGUI = new GroupManagementGUI(player, groupName, bossManager, plugin, bossRegistry);
                    plugin.getGuiManager().openGUI(player, managementGUI);
                }
            }
        }
    }
}