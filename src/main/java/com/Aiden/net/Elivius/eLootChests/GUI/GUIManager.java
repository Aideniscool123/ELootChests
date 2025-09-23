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
        closeGUI(player);
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

            // DEBUG: Tell us what's happening
            player.sendMessage("§7DEBUG: Clicked slot " + event.getSlot());

            if (openGUIs.containsKey(player) && event.getInventory().equals(openGUIs.get(player).getInventory())) {
                event.setCancelled(true);

                // DEBUG: Tell us which GUI we're in
                LootChestGUI gui = openGUIs.get(player);
                player.sendMessage("§7DEBUG: GUI type: " + gui.getClass().getSimpleName());

                // Detect click type
                boolean isRightClick = event.getClick().toString().contains("RIGHT");

                // Handle the click in the specific GUI
                if (gui instanceof GroupSelectionGUI) {
                    player.sendMessage("§7DEBUG: Calling GroupSelectionGUI handleClick");
                    ((GroupSelectionGUI) gui).handleClick(event.getSlot(), isRightClick);
                }
                else if (gui instanceof GroupManagementGUI) {
                    player.sendMessage("§7DEBUG: Calling GroupManagementGUI handleClick");
                    ((GroupManagementGUI) gui).handleClick(event.getSlot(), isRightClick);
                }
                else if (gui instanceof PaginatedLootTableGUI) {
                    player.sendMessage("§7DEBUG: Calling PaginatedLootTableGUI handleClick");
                    ((PaginatedLootTableGUI) gui).handleClick(event.getSlot(), isRightClick);
                }
                else if (gui instanceof ConfigEditorGUI) {
                    player.sendMessage("§7DEBUG: Calling ConfigEditorGUI handleClick");
                    ((ConfigEditorGUI) gui).handleClick(event.getSlot(), isRightClick);
                }
            } else {
                player.sendMessage("§7DEBUG: No open GUI found for player");
            }
        }
    }
}