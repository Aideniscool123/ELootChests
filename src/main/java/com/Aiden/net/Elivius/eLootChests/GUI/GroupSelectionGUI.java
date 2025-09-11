package com.Aiden.net.Elivius.eLootChests.GUI;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.Aiden.net.Elivius.eLootChests.BossRegistry;
import java.util.Arrays;

public class GroupSelectionGUI extends LootChestGUI {
    private final BossRegistry bossRegistry;

    public GroupSelectionGUI(Player player, BossRegistry bossRegistry) {
        super(player, "§8Loot Chest Groups", 54);
        this.bossRegistry = bossRegistry;
        setupGUI();
    }

    public void handleClick(int slot) {
        if (slot == 49) {
            player.closeInventory();
            player.performCommand("eloot new");
            return;
        }

        if (slot >= 19 && slot <43 && slot % 9 != 8 && slot %9 != 8) {
            ItemStack clicked = inventory.getItem(slot);
            if (clicked != null && clicked.getType() == Material.CHEST) {
                String groupname = clicked.getItemMeta().getDisplayName().replace("§e", "");
                player.closeInventory();
                player.sendMessage("§aOpening management for group: §e" + groupname);
            }
        }
    }

    private void setupGUI() {
        // Fill border
        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);
        fillBorder(border);

        // Add group items
        int slot = 19;
        for (String groupName : bossRegistry.getAllBossNames()) {
            if (slot >= 43) break;

            ItemStack groupItem = new ItemStack(Material.CHEST);
            ItemMeta meta = groupItem.getItemMeta();
            meta.setDisplayName("§e" + groupName);
            meta.setLore(Arrays.asList(
                    "§7Click to manage this group",
                    "§7Chest locations: §e" + "TODO", // Will update with real data next
                    "§7Total items: §e" + "TODO"      // Will update with real data next
            ));
            groupItem.setItemMeta(meta);

            inventory.setItem(slot, groupItem);
            slot++;
            if (slot % 9 == 8) slot += 2;
        }

        // Add create new group button
        ItemStack createButton = new ItemStack(Material.EMERALD);
        ItemMeta createMeta = createButton.getItemMeta();
        createMeta.setDisplayName("§aCreate New Group");
        createMeta.setLore(Arrays.asList("§7Click to create a new boss group"));
        createButton.setItemMeta(createMeta);
        inventory.setItem(49, createButton);
    }
}