package com.Aiden.net.Elivius.eLootChests.GUI;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.Aiden.net.Elivius.eLootChests.BossManager;
import com.Aiden.net.Elivius.eLootChests.BossRegistry;
import com.Aiden.net.Elivius.eLootChests.LootChests;
import java.util.Arrays;

public class GroupManagementGUI extends LootChestGUI {
    private final BossManager bossManager;
    private final BossRegistry bossRegistry;
    private final String groupName;
    private LootChests plugin = null;

    public GroupManagementGUI(Player player, String groupName, BossManager bossManager, LootChests plugin, BossRegistry bossRegistry) {
        super(player, "§8Managing: §e" + groupName, 54);
        this.bossManager = bossManager;
        this.bossRegistry = bossRegistry;
        this.groupName = groupName;
        this.plugin = this.plugin;
        setupGUI();
    }

    private void setupGUI() {
        // Fill border
        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);
        fillBorder(border);

        // Get real data
        int chestCount = bossManager.getChestCount(groupName);
        int itemCount = bossManager.getItemCount(groupName);
        String worldName = bossManager.getWorldName(groupName);

        // Group Info Item
        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§6§l" + groupName + " Info");
        infoMeta.setLore(Arrays.asList(
                "§7Chest locations: §e" + chestCount,
                "§7Total items: §e" + itemCount,
                "§7World: §e" + worldName,
                "",
                "§7Group configuration and statistics"
        ));
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

        // Spawn Chests Button
        ItemStack spawnButton = new ItemStack(Material.ENDER_CHEST);
        ItemMeta spawnMeta = spawnButton.getItemMeta();
        spawnMeta.setDisplayName("§bSpawn Chests");
        spawnMeta.setLore(Arrays.asList(
                "§7Spawn chests in the world",
                "",
                "§eClick to spawn chests now"
        ));
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
                player.sendMessage("§aOpening config editor for: §e" + groupName);
                // TODO: Open config editor GUI
                break;

            case 22: // Loot Table Manager
                player.sendMessage("§aOpening loot table manager for: §e" + groupName);
                // TODO: Open loot table GUI
                break;

            case 24: // Spawn Chests
                player.closeInventory();
                player.performCommand("eloot spawn " + groupName);
                player.sendMessage("§aSpawning chests for: §e" + groupName);
                break;

            case 49: // Back Button
                player.closeInventory();
                GroupSelectionGUI groupGUI = new GroupSelectionGUI(player, bossRegistry, bossManager, plugin);
                plugin.getGuiManager().openGUI(player, groupGUI);
                break;
        }
    }
}