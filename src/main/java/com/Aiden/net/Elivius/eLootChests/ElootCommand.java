package com.Aiden.net.Elivius.eLootChests;

import com.Aiden.net.Elivius.eLootChests.Enums.*;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ElootCommand implements CommandExecutor, Listener {
    private final LootChests plugin;
    private final BossManager bossManager;
    private final BossRegistry bossRegistry;
    private final Map<Player, WandMode> wandModes = new HashMap<>();
    private final Map<Player, String> selectedBossGroups = new HashMap<>();

    public ElootCommand(LootChests plugin, BossManager bossManager, BossRegistry bossRegistry) {
        this.plugin = plugin;
        this.bossManager = bossManager;
        this.bossRegistry = bossRegistry;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /eloot <wand|spawn|despawn|table|new|info|edit>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "wand":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cThis command can only be used by players!");
                    return true;
                }
                return handleWandCommand((Player) sender, args);

            case "new":
                return handleNewCommand(sender, args);

            case "table":
                return handleTableCommand(sender, args);


            default:
                sender.sendMessage("§cUnknown sub-command. Use: /eloot wand|new|table");
                return true;
        }
    }

    private boolean handleWandCommand(Player player, String[] args) {
        // Stop all particles when switching modes
        bossManager.stopPlayerParticles(player);

        if (args.length == 1) {
            giveWand(player);
            wandModes.put(player, WandMode.ADD);
            player.sendMessage("§aWand mode set to: §e" + WandMode.ADD.getDisplayName() + "§a (default)");
            return true;
        }

        switch (args[1].toLowerCase()) {
            case "add":
                wandModes.put(player, WandMode.ADD);
                player.sendMessage("§aWand mode set to: §e" + WandMode.ADD.getDisplayName() + "§a - " + WandMode.ADD.getDescription());
                break;

            case "remove":
                wandModes.put(player, WandMode.REMOVE);
                player.sendMessage("§aWand mode set to: §e" + WandMode.REMOVE.getDisplayName() + "§a - " + WandMode.REMOVE.getDescription());
                showNearbyLocations(player, 50, WandMode.REMOVE);
                break;

            case "show":
                wandModes.put(player, WandMode.SHOW);
                player.sendMessage("§aWand mode set to: §e" + WandMode.SHOW.getDisplayName() + "§a - " + WandMode.SHOW.getDescription());
                showNearbyLocations(player, 100, WandMode.SHOW);
                break;

            case "select":
                if (args.length < 3) {
                    player.sendMessage("§cUsage: /eloot wand select <group>");
                    return false;
                }
                String groupName = args[2];

                // Validate the group exists
                if (!bossRegistry.bossExists(groupName)) {
                    player.sendMessage("§cBoss group '" + groupName + "' does not exist!");
                    player.sendMessage("§7Available groups: " + String.join(", ", bossRegistry.getAllBossNames()));
                    return false;
                }

                selectedBossGroups.put(player, groupName);
                wandModes.put(player, WandMode.ADD);
                player.sendMessage("§aSelected group: §e" + groupName);
                player.sendMessage("§7Wand mode set to: §e" + WandMode.ADD.getDisplayName());
                updateWandLore(player, WandMode.ADD.getDisplayName(), groupName);
                break;

            default:
                player.sendMessage("§cUnknown wand mode. Use: add, remove, or show");
                return false;
        }

        return true;
    }

    private boolean handleNewCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("eloot.admin") && !sender.isOp()) {
            sender.sendMessage("§cYou don't have permission to create new boss groups!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /eloot new <bossName>");
            sender.sendMessage("§7Available templates: " + getBossTypeNames());
            return true;
        }

        String bossName = args[1];

        // Check if boss already exists
        if (bossRegistry.bossExists(bossName)) {
            sender.sendMessage("§cA boss with that name already exists!");
            return true;
        }

        // Create the new boss group
        if (bossManager.createBossGroup(bossName)) {
            // Register the boss in the registry
            if (bossRegistry.addBoss(bossName)) {
                sender.sendMessage("§aSuccessfully created new boss group: §e" + bossName);
                sender.sendMessage("§7Use §e/eloot wand select " + bossName + " §7to start adding chest locations");
            } else {
                sender.sendMessage("§cFailed to register boss in registry!");
            }
        } else {
            sender.sendMessage("§cFailed to create boss group files!");
        }

        return true;
    }

    private boolean handleTableCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("eloot.admin") && !player.isOp()) {
            player.sendMessage("§cYou don't have permission to modify loot tables!");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /eloot table <add|remove|list> [arguments]");
            player.sendMessage("§7- /eloot table <group> <rarity> [percentage]");
            player.sendMessage("§7- /eloot table remove <group>");
            player.sendMessage("§7- /eloot table list <group>");
            return true;
        }

        // Handle sub-commands: remove and list
        if (args.length >= 2) {
            switch (args[1].toLowerCase()) {
                case "remove":
                    return handleTableRemoveCommand(sender, args);
                case "list":
                    return handleTableListCommand(sender, args);
                case "add":
                    // Handle add specifically if they use "table add" instead of just "table"
                    if (args.length < 4) {
                        player.sendMessage("§cUsage: /eloot table add <group> <rarity> [percentage]");
                        player.sendMessage("§7Rarities: " + getRarityNames());
                        return true;
                    }
                    // Shift arguments to handle as regular table command
                    String[] newArgs = new String[args.length - 1];
                    newArgs[0] = args[0]; // "table"
                    System.arraycopy(args, 2, newArgs, 1, args.length - 2);
                    return handleTableAddCommand(sender, newArgs);
                default:
                    // Handle regular table add command (without "add" keyword)
                    return handleTableAddCommand(sender, args);
            }
        }

        return true;
    }

    private boolean handleTableAddCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length < 3) {
            player.sendMessage("§cUsage: /eloot table <group> <rarity> [percentage]");
            player.sendMessage("§7Rarities: " + getRarityNames());
            return true;
        }

        String groupName = args[1];
        String rarityName = args[2].toUpperCase();
        double percentage = -1;

        // Parse percentage if provided
        if (args.length >= 4) {
            try {
                percentage = Double.parseDouble(args[3]);
                if (percentage < 0.1 || percentage > 100){
                    player.sendMessage("§cPercentage must be between 0.1 and 100!");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid percentage! Must be a number between 0.1 and 100.");
                return true;
            }
        }

        // Validate group exists
        if (!bossRegistry.bossExists(groupName)) {
            player.sendMessage("§cBoss group '" + groupName + "' does not exist!");
            player.sendMessage("§7Available groups: " + String.join(", ", bossRegistry.getAllBossNames()));
            return true;
        }

        // Validate rarity
        Rarity rarity;
        try {
            rarity = Rarity.valueOf(rarityName);
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid rarity! Available: " + getRarityNames());
            return true;
        }

        // Check if player is holding an item
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem == null || heldItem.getType() == Material.AIR) {
            player.sendMessage("§cYou must be holding an item to add to the loot table!");
            return true;
        }

        // Add item to loot table
        if (bossManager.addItemToLootTable(groupName, rarity, heldItem, percentage)) {
            player.sendMessage("§aSuccessfully added item to §e" + rarity.getFormattedName() + "§a rarity for group: §e" + groupName);
            if (percentage > 0) {
                player.sendMessage("§7Spawn percentage: §e" + percentage + "%");
            } else {
                player.sendMessage("§7Using default percentage: §e" + rarity.getDefaultPercentage() + "%");
            }
        } else {
            player.sendMessage("§cFailed to add item to loot table! It might already exist.");
        }

        return true;
    }

    private boolean handleTableListCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /eloot table list <group>");
            return true;
        }

        String groupName = args[2];

        // Validate group exists
        if (!bossRegistry.bossExists(groupName)) {
            sender.sendMessage("§cBoss group '" + groupName + "' does not exist!");
            sender.sendMessage("§7Available groups: " + String.join(", ", bossRegistry.getAllBossNames()));
            return true;
        }

        // Get loot table summary
        Map<Rarity, Integer> itemCounts = bossManager.getLootTableSummary(groupName);

        sender.sendMessage("§6=== Loot Table Summary: §e" + groupName + " §6===");
        for (Rarity rarity : Rarity.values()) {
            int count = itemCounts.getOrDefault(rarity, 0);
            sender.sendMessage("§7- " + rarity.getFormattedName() + "§7: §e" + count + " items");
        }

        int totalItems = itemCounts.values().stream().mapToInt(Integer::intValue).sum();
        sender.sendMessage("§7Total items: §e" + totalItems);

        return true;
    }

    private boolean handleTableRemoveCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("eloot.admin") && !player.isOp()) {
            player.sendMessage("§cYou don't have permission to modify loot tables!");
            return true;
        }

        if (args.length < 3) {
            player.sendMessage("§cUsage: /eloot table remove <group>");
            return true;
        }

        String groupName = args[2];

        // Validate group exists
        if (!bossRegistry.bossExists(groupName)) {
            player.sendMessage("§cBoss group '" + groupName + "' does not exist!");
            player.sendMessage("§7Available groups: " + String.join(", ", bossRegistry.getAllBossNames()));
            return true;
        }

        // Check if player is holding an item
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem == null || heldItem.getType() == Material.AIR) {
            player.sendMessage("§cYou must be holding an item to remove from the loot table!");
            return true;
        }

        // Remove item from loot table
        if (bossManager.removeItemFromLootTable(groupName, heldItem)) {
            player.sendMessage("§aSuccessfully removed item from loot table for group: §e" + groupName);
        } else {
            player.sendMessage("§cItem not found in loot table for group: §e" + groupName);
        }

        return true;
    }


    private void showNearbyLocations(Player player, int radius, WandMode mode) {
        String selectedGroup = selectedBossGroups.get(player);
        if (selectedGroup == null) {
            player.sendMessage("§cYou must select a boss group first!");
            return;
        }

        List<Location> groupLocations = bossManager.getSavedChestLocations(selectedGroup);
        Location playerLoc = player.getLocation();

        int count = 0;
        for (Location loc : groupLocations) {
            if (loc.getWorld().equals(playerLoc.getWorld()) &&
                    loc.distance(playerLoc) <= radius) {

                bossManager.showLocationParticles(player, loc, mode.getParticleType(),
                        mode.getDurationTicks(), mode.getParticleType().canSeeThroughBlocks());
                count++;
            }
        }

        String durationMsg = mode.getDurationTicks() == -1 ? "until mode change" : "for 30 seconds";
        player.sendMessage("§aShowing §e" + count + "§a chest locations from group '§e" + selectedGroup + "§a' within §e" + radius + "§a blocks (§7" + durationMsg + "§a)");
    }

    private boolean giveWand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("eloot.wand.use") && !player.isOp()) {
            player.sendMessage("§cYou don't have permission to use this!");
            return true;
        }

        ItemStack wand = new ItemStack(Material.STICK);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Loot Chest Wand");
            meta.setLore(Arrays.asList(
                    "§7Right-click blocks to set",
                    "§7chest locations for loot",
                    "§7chest configuration.",
                    "§8Mode: " + WandMode.ADD.getDisplayName(),
                    "§8Group: None"
            ));
            wand.setItemMeta(meta);
        }

        player.getInventory().addItem(wand);
        player.sendMessage("§aYou have received the Loot Chest Wand!");
        player.sendMessage("§7Right-click blocks to set chest locations.");

        return true;
    }

    @EventHandler
    public void onWandUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.STICK ||
                !item.hasItemMeta() || !item.getItemMeta().hasDisplayName() ||
                !item.getItemMeta().getDisplayName().equals("§6Loot Chest Wand")) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }

        if (!player.hasPermission("eloot.wand.use") && !player.isOp()) {
            player.sendMessage("§cYou don't have permission to use the wand!");
            return;
        }

        // Check if a group is selected
        String selectedGroup = selectedBossGroups.get(player);
        if (selectedGroup == null) {
            player.sendMessage("§cYou must first select a boss group with §e/eloot wand select <group>");
            return;
        }

        event.setCancelled(true);
        Location clickedLocation = event.getClickedBlock().getLocation();
        WandMode mode = wandModes.getOrDefault(player, WandMode.ADD);

        switch (mode) {
            case ADD:
                plugin.showChestLocation(clickedLocation);
                bossManager.saveChestLocation(clickedLocation, selectedGroup);
                player.sendMessage("§aLocation saved to group: §e" + selectedGroup);
                updateWandLore(player, mode.getDisplayName(), selectedGroup);
                break;

            case REMOVE:
                // Use the exact same location format as stored
                Location storedFormatLocation = new Location(clickedLocation.getWorld(),
                        clickedLocation.getBlockX(),
                        clickedLocation.getBlockY() + 1, // +1 to match storage format
                        clickedLocation.getBlockZ());

                Location particleLocation = new Location(clickedLocation.getWorld(),
                        clickedLocation.getX(),
                        clickedLocation.getY() + 1,
                        clickedLocation.getZ());

                if (bossManager.removeChestLocation(clickedLocation, selectedGroup)) {
                    player.sendMessage("§aChest location removed from group: §e" + selectedGroup);
                    // Stop particles for this specific location using the stored format
                    bossManager.stopLocationParticles(player, storedFormatLocation);
                    bossManager.showLocationParticles(player, clickedLocation, ParticleType.SMOKE_LARGE, 20, false);
                } else {
                    player.sendMessage("§cNo chest location found in group '" + selectedGroup + "' at this position!");
                }
                break;

            case SHOW:
                plugin.showChestLocation(clickedLocation);
                player.sendMessage("§aShowing chest location for group: §e" + selectedGroup);
                break;
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (wandModes.containsKey(player)) {
            wandModes.remove(player);
            selectedBossGroups.remove(player);
            bossManager.stopPlayerParticles(player);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();
        if (isWandItem(item) && wandModes.containsKey(player)) {
            bossManager.stopPlayerParticles(player);
        }
    }

    @EventHandler
    public void onInventoryChange(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            ItemStack currentItem = event.getCurrentItem();
            ItemStack cursorItem = event.getCursor();

            boolean isWandLeaving = (isWandItem(currentItem) || isWandItem(cursorItem));

            if (isWandLeaving && wandModes.containsKey(player)) {
                bossManager.stopPlayerParticles(player);
            }
        }
    }

    @EventHandler
    public void onItemSwap(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if ((isWandItem(event.getMainHandItem()) || isWandItem(event.getOffHandItem())) &&
                wandModes.containsKey(player)) {
            bossManager.stopPlayerParticles(player);
        }
    }

    private boolean isWandItem(ItemStack item) {
        return item != null &&
                item.getType() == Material.STICK &&
                item.hasItemMeta() &&
                item.getItemMeta().getDisplayName().equals("§6Loot Chest Wand");
    }

    private void updateWandLore(Player player, String mode, String group) {
        ItemStack wand = player.getInventory().getItemInMainHand();
        if (wand.getType() == Material.STICK && wand.hasItemMeta()) {
            ItemMeta meta = wand.getItemMeta();
            List<String> lore = new ArrayList<>(Arrays.asList(
                    "§7Right-click blocks to set",
                    "§7chest locations for loot",
                    "§7chest configuration.",
                    "§8Mode: " + mode,
                    "§8Group: " + group
            ));

            meta.setLore(lore);
            wand.setItemMeta(meta);
        }
    }

    private String getBossTypeNames() {
        StringBuilder names = new StringBuilder();
        for (BossType bossType : BossType.values()) {
            names.append(bossType.getConfigName()).append(", ");
        }
        return names.length() > 0 ? names.substring(0, names.length() - 2) : "None";
    }

    private String getRarityNames() {
        StringBuilder names = new StringBuilder();
        for (Rarity rarity : Rarity.values()) {
            names.append(rarity.name().toLowerCase()).append(", ");
        }
        return names.length() > 0 ? names.substring(0, names.length() - 2) : "None";
    }
}