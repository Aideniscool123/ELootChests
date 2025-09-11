package com.Aiden.net.Elivius.eLootChests.GUI;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import java.util.HashMap;
import java.util.Map;

public class GUIManager implements Listener {
    private final Map<Player, LootChestGUI> openGUIs = new HashMap<>();

    public void openGUI(Player player, LootChestGUI gui) {
        closeGUI(player); // Close any existing GUI
        openGUIs.put(player, gui);
        gui.open();
    }

    public void closeGUI(Player player) {
        if (openGUIs.containsKey(player)) {
            openGUIs.remove(player);
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            openGUIs.remove(player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (openGUIs.containsKey(player) && event.getInventory().equals(openGUIs.get(player).getInventory())) {
                event.setCancelled(true);

                // Handle the click in the specific GUI
                LootChestGUI gui = openGUIs.get(player);
                if (gui instanceof GroupSelectionGUI) {
                    ((GroupSelectionGUI) gui).handleClick(event.getSlot());
                }
                // Add other GUI type checks here as I create them
            }
        }
    }
}