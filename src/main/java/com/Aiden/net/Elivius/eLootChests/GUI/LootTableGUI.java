package com.Aiden.net.Elivius.eLootChests.GUI;

import com.Aiden.net.Elivius.eLootChests.BossRegistry;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.Aiden.net.Elivius.eLootChests.BossManager;
import com.Aiden.net.Elivius.eLootChests.LootChests;
import java.util.Arrays;

public class LootTableGUI extends LootChestGUI {
    private final BossManager bossManager;
    private final LootChests plugin;
    private final String groupName;
    private BossRegistry bossRegistry = null;

    public LootTableGUI(Player player, String groupName, BossManager bossManager, LootChests plugin, BossRegistry bossRegistry) {
        super(player, "§8Loot Table: §e" + groupName, 54);
        this.bossManager = bossManager;
        this.plugin = plugin;
        this.groupName = groupName;
        this.bossRegistry = bossRegistry;
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

        // Loot table info
        ItemStack infoItem = new ItemStack(Material.CHEST);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§6Loot Table: §e" + groupName);
            infoMeta.setLore(Arrays.asList(
                    "§7Loot table manager for " + groupName,
                    "§7This GUI is under development",
                    "",
                    "§eUse §7/eloot table §efor now"
            ));
            infoItem.setItemMeta(infoMeta);
        }
        inventory.setItem(4, infoItem);

        // Back button
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§fBack to Management");
            backMeta.setLore(Arrays.asList("§7Return to group management"));
            backButton.setItemMeta(backMeta);
        }
        inventory.setItem(49, backButton);
    }

    public void handleClick(int slot, boolean isRightClick) {
        if (slot == 49) { // Back Button
            player.closeInventory();
            GroupManagementGUI managementGUI = new GroupManagementGUI(player, groupName, bossManager, bossRegistry, plugin);
            plugin.getGuiManager().openGUI(player, managementGUI);
        }
    }
}