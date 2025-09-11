package com.Aiden.net.Elivius.eLootChests.GUI;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.Aiden.net.Elivius.eLootChests.Enums.Rarity;

public class LootChestGUI {
    protected Inventory inventory;
    protected Player player;
    protected String groupName;
    protected Rarity currentRarity;

    public LootChestGUI(Player player, String title, int size) {
        this.player = player;
        this.inventory = Bukkit.createInventory(null, size, title);
    }

    public void open() {
        player.openInventory(inventory);
    }

    public Inventory getInventory() {
        return inventory;
    }

    protected void fillBorder(ItemStack item) {
        int[] borderSlots = {0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,49,50,51,52,53};
        for (int slot : borderSlots) {
            if (slot < inventory.getSize()) {
                inventory.setItem(slot, item);
            }
        }
    }
}