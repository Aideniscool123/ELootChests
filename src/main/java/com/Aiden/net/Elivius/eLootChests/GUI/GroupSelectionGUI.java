package com.Aiden.net.Elivius.eLootChests.GUI;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.Aiden.net.Elivius.eLootChests.BossManager;
import com.Aiden.net.Elivius.eLootChests.BossRegistry;
import com.Aiden.net.Elivius.eLootChests.LootChests;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Arrays;
import java.util.List;

public class GroupSelectionGUI extends LootChestGUI {
    private final BossRegistry bossRegistry;
    private final BossManager bossManager;
    private final LootChests plugin;
    private int currentPage = 0;
    private final int groupsPerPage = 21; // 3 rows of 7 groups
    private int totalPages;

    public GroupSelectionGUI(Player player, BossRegistry bossRegistry, BossManager bossManager, LootChests plugin) {
        super(player, "§8Loot Chest Groups", 54);
        this.bossRegistry = bossRegistry;
        this.bossManager = bossManager;
        this.plugin = plugin;

        List<String> allBosses = bossRegistry.getAllBossNames();
        this.totalPages = Math.max(1, (int) Math.ceil((double) allBosses.size() / groupsPerPage));
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

        List<String> allBosses = bossRegistry.getAllBossNames();

        // Calculate which groups to show on this page
        int startIndex = currentPage * groupsPerPage;
        int endIndex = Math.min(startIndex + groupsPerPage, allBosses.size());

        // FIXED: Proper slot calculation for 3 rows of 7 items
        int[] groupSlots = {
                // Row 1: slots 10-16 (7 slots)
                10, 11, 12, 13, 14, 15, 16,
                // Row 2: slots 19-25 (7 slots)
                19, 20, 21, 22, 23, 24, 25,
                // Row 3: slots 28-34 (7 slots) - FIXED: No shift!
                28, 29, 30, 31, 32, 33, 34
        };

        // Add group items for this page using fixed slot positions
        for (int i = startIndex; i < endIndex; i++) {
            int slotIndex = i - startIndex;
            if (slotIndex >= groupSlots.length) break;

            int slot = groupSlots[slotIndex];
            String groupName = allBosses.get(i);

            ItemStack groupItem = new ItemStack(Material.CHEST);
            ItemMeta meta = groupItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§e" + groupName);

                int chestCount = bossManager.getChestCount(groupName);
                int itemCount = bossManager.getItemCount(groupName);

                String displayWorldName = "No coordinates";
                List<Location> locations = bossManager.getSavedChestLocations(groupName);
                if (locations != null && !locations.isEmpty()) {
                    String firstWorld = locations.get(0).getWorld().getName();
                    boolean sameWorld = true;
                    for (Location loc : locations) {
                        if (!loc.getWorld().getName().equals(firstWorld)) {
                            sameWorld = false;
                            break;
                        }
                    }
                    displayWorldName = sameWorld ? firstWorld : "Multiple Worlds";
                }

                if (chestCount == 0) {
                    meta.setLore(Arrays.asList(
                            "§7Chest locations: §c" + chestCount,
                            "§7Total items: §e" + itemCount,
                            "§7World: §cNo coordinates set",
                            "",
                            "§aLeft-click: §7Manage group",
                            "§cRight-click: §7Add locations first!"
                    ));
                } else {
                    meta.setLore(Arrays.asList(
                            "§7Chest locations: §a" + chestCount,
                            "§7Total items: §e" + itemCount,
                            "§7World: §e" + displayWorldName,
                            "",
                            "§aLeft-click: §7Manage group",
                            "§cRight-click: §7Spawn chests!"
                    ));
                }
                groupItem.setItemMeta(meta);
            }

            inventory.setItem(slot, groupItem);
        }

        setupNavigationButtons(allBosses.size());

        // Create New Group button (always visible)
        ItemStack createButton = new ItemStack(Material.EMERALD);
        ItemMeta createButtonMeta = createButton.getItemMeta();
        if (createButtonMeta != null) {
            createButtonMeta.setDisplayName("§aCreate New Group");
            createButtonMeta.setLore(Arrays.asList(
                    "§7Click to create a new boss group",
                    "",
                    "§aClick to start creation"
            ));
            createButton.setItemMeta(createButtonMeta);
        }
        inventory.setItem(49, createButton);
    }

    private void setupNavigationButtons(int totalGroups) {
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

        // Page indicator (always visible)
        ItemStack pageIndicator = new ItemStack(Material.MAP);
        ItemMeta pageMeta = pageIndicator.getItemMeta();
        if (pageMeta != null) {
            pageMeta.setDisplayName("§ePage " + (currentPage + 1) + " of " + totalPages);
            pageMeta.setLore(Arrays.asList(
                    "§7Groups: §e" + totalGroups,
                    "§7Groups per page: §e" + groupsPerPage
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
    }

    public void handleClick(int slot, boolean isRightClick) {
        if (slot == 49) { // Create New Group button
            handleCreateNewGroup();
            return;
        }

        if (slot == 45) { // Previous Page
            if (currentPage > 0) {
                currentPage--;
                updateGUI();
            }
            return;
        }

        if (slot == 53) { // Next Page
            if (currentPage < totalPages - 1) {
                currentPage++;
                updateGUI();
            }
            return;
        }

        // FIXED: Check if clicked on a valid group slot using our fixed array
        int[] validSlots = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34};
        for (int validSlot : validSlots) {
            if (slot == validSlot) {
                ItemStack clicked = inventory.getItem(slot);
                if (clicked != null && clicked.getType() == Material.CHEST && clicked.hasItemMeta()) {
                    String groupName = clicked.getItemMeta().getDisplayName().replace("§e", "");
                    handleGroupClick(groupName, isRightClick);
                    return;
                }
            }
        }
    }

    private void handleGroupClick(String groupName, boolean isRightClick) {
        player.closeInventory();

        if (isRightClick) {
            handleSpawnChests(groupName);
        } else {
            handleManageGroup(groupName);
        }
    }

    private void handleSpawnChests(String groupName) {
        int chestCount = bossManager.getChestCount(groupName);

        if (chestCount == 0) {
            player.sendMessage("");
            player.sendMessage("§6§l" + groupName + " - No Chest Locations");
            player.sendMessage("§7This group has no chest locations configured.");
            player.sendMessage("§7You need to add locations before spawning chests.");
            player.sendMessage("");
            player.sendMessage("§aNext steps:");
            player.sendMessage("§71. §e/eloot wand select " + groupName + " §7- Select this group");
            player.sendMessage("§72. §eRight-click blocks §7- Add chest locations");
            player.sendMessage("§73. §eCome back here §7- Spawn chests!");
            player.sendMessage("");
        } else {
            player.performCommand("eloot spawn " + groupName);
            player.sendMessage("§aSpawning chests for group: §e" + groupName);
        }
    }

    private void handleManageGroup(String groupName) {
        GroupManagementGUI managementGUI = new GroupManagementGUI(player, groupName, bossManager, bossRegistry, plugin);
        plugin.getGuiManager().openGUI(player, managementGUI);
    }

    private void handleCreateNewGroup() {
        player.closeInventory();
        startGroupNameInput();
    }

    private void startGroupNameInput() {
        player.sendMessage("");
        player.sendMessage("§6§lCreate New Boss Group");
        player.sendMessage("§7Please enter the name for your new boss group in chat.");
        player.sendMessage("§7Allowed: letters, numbers, underscores (3-20 characters)");
        player.sendMessage("§7Example: §eDragonBoss§7, §eChristmasEvent§7, §eDungeon1");
        player.sendMessage("§cType 'cancel' to cancel creation.");
        player.sendMessage("");

        ChatListener listener = new ChatListener(player, groupName -> {
            handleGroupNameInput(groupName);
        });

        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    private void handleGroupNameInput(String groupName) {
        if (groupName.equalsIgnoreCase("cancel")) {
            player.sendMessage("§cGroup creation cancelled.");
            reopenGUI();
            return;
        }

        if (groupName.equalsIgnoreCase("gui")) {
            reopenGUI();
            return;
        }

        if (!isValidGroupName(groupName)) {
            player.sendMessage("§cInvalid group name! Use only letters, numbers, and underscores (3-20 characters).");
            player.sendMessage("§7Please try again or type 'cancel':");
            startGroupNameInput();
            return;
        }

        if (bossRegistry.bossExists(groupName)) {
            player.sendMessage("§cA group named '" + groupName + "' already exists!");
            player.sendMessage("§7Please choose a different name or type 'cancel':");
            startGroupNameInput();
            return;
        }

        createNewGroup(groupName);
    }

    private boolean isValidGroupName(String name) {
        return name.matches("^[a-zA-Z0-9_]{3,20}$");
    }

    private void createNewGroup(String groupName) {
        if (bossManager.createBossGroup(groupName)) {
            if (bossRegistry.addBoss(groupName)) {
                player.sendMessage("§a✅ Successfully created new boss group: §e" + groupName);
                player.sendMessage("§7Next steps:");
                player.sendMessage("§71. §e/eloot wand select " + groupName + " §7- Select this group");
                player.sendMessage("§72. §eRight-click blocks §7- Add chest locations");
                player.sendMessage("§73. §e/eloot table add " + groupName + " <rarity> §7- Add loot items");
            } else {
                player.sendMessage("§c❌ Failed to register group in registry!");
            }
        } else {
            player.sendMessage("§c❌ Failed to create group files!");
        }

        reopenGUI();
    }

    private void updateGUI() {
        setupGUI();
        player.updateInventory();
    }

    private void reopenGUI() {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                GroupSelectionGUI newGUI = new GroupSelectionGUI(player, bossRegistry, bossManager, plugin);
                plugin.getGuiManager().openGUI(player, newGUI);
            }
        }, 10L);
    }

    // Chat listener inner class
    private class ChatListener implements Listener {
        private final Player targetPlayer;
        private final java.util.function.Consumer<String> callback;
        private boolean consumed = false;

        public ChatListener(Player targetPlayer, java.util.function.Consumer<String> callback) {
            this.targetPlayer = targetPlayer;
            this.callback = callback;
        }

        @EventHandler
        public void onPlayerChat(AsyncPlayerChatEvent event) {
            if (consumed) return;

            if (event.getPlayer().equals(targetPlayer)) {
                consumed = true;
                event.setCancelled(true);

                String message = event.getMessage().trim();

                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    callback.accept(message);
                    event.getHandlers().unregister(this);
                });
            }
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            if (event.getPlayer().equals(targetPlayer)) {
                consumed = true;
                event.getHandlers().unregister(this);
            }
        }
    }
}