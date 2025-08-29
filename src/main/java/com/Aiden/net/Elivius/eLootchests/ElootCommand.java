package com.Aiden.net.Elivius.eLootchests;

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
    private final com.Aiden.net.Elivius.eLootchests.LootChests plugin;
    private final com.Aiden.net.Elivius.eLootchests.BossManager bossManager;
    private final Map<Player, String> wandModes = new HashMap<>();

    public ElootCommand(com.Aiden.net.Elivius.eLootchests.LootChests plugin, com.Aiden.net.Elivius.eLootchests.BossManager bossManager) {
        this.plugin = plugin;
        this.bossManager = bossManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /eloot <wand|spawn|despawn|table|new|info|edit>");
            return true;
        }

        if (args[0].equalsIgnoreCase("wand")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cThis command can only be used by players!");
                return true;
            }
            return handleWandCommand((Player) sender, args);
        }

        sender.sendMessage("§cUnknown sub-command. Use: /eloot to see usage!");
        return true;
    }

    private boolean handleWandCommand(Player player, String[] args) {

        bossManager.stopPlayerParticles(player);

        if (args.length == 1) {
            giveWand(player);
            wandModes.put(player, "add");
            player.sendMessage("§aWand mode set to: §eADD§a");
            return true;
        }

        switch (args[1].toLowerCase()) {
            case "add":
                wandModes.put(player, "add");
                player.sendMessage("§aWand mode set to: §eADD§a - Right-click blocks to add chest locations");
                break;

            case "remove":
                wandModes.put(player, "remove");
                player.sendMessage("§aWand mode set to: §eREMOVE§a - Right-click to remove chest locations");
                showNearbyLocations(player, 50, true, "remove");
                break;

            case "show":
                wandModes.put(player, "show");
                player.sendMessage("§aWand mode set to: §eSHOW§a - Showing nearby chest locations");
                showNearbyLocations(player, 100, false, "show");
                break;

            default:
                player.sendMessage("§cUnknown wand mode. Use: add, remove, or show");
                return false;
        }

        return true;
    }

    private void showNearbyLocations(Player player, int radius, boolean showThroughBlocks, String mode) {
        List<Location> allLocations = bossManager.getSavedChestLocations();
        Location playerLoc = player.getLocation();

        int count = 0;
        for (Location loc : allLocations) {
            if (loc.getWorld().equals(playerLoc.getWorld()) &&
                    loc.distance(playerLoc) <= radius) {

                int durationTicks = mode.equals("remove") ? -1 : 600;
                bossManager.showLocationParticles(player, loc, Particle.HAPPY_VILLAGER, durationTicks, showThroughBlocks);
                count++;
            }
        }

        String durationMsg = mode.equals("remove") ? "until mode change" : "for 30 seconds";
        player.sendMessage("§aShowing §e" + count + "§a chest locations");
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
                    "§8Mode: ADD"
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

        event.setCancelled(true);
        Location clickedLocation = event.getClickedBlock().getLocation();
        String mode = wandModes.getOrDefault(player, "add");

        switch (mode) {
            case "add":
                plugin.showChestLocation(clickedLocation);
                bossManager.saveChestLocation(clickedLocation);
                player.sendMessage("§aLocation saved! Chest added to coordinates.");
                updateWandLore(player, "ADD");
                break;

            case "remove":
                // Create location with correct Y+1 for particle matching
                Location particleLocation = new Location(clickedLocation.getWorld(),
                        clickedLocation.getX(),
                        clickedLocation.getY() + 1, // +1 to match particle height
                        clickedLocation.getZ());

                if (bossManager.removeChestLocation(clickedLocation)) {
                    player.sendMessage("§aChest location removed!");
                    // Stop particles for this removed location (using Y+1 position)
                    bossManager.stopLocationParticles(player, particleLocation);
                    // Show confirmation particles
                    bossManager.showLocationParticles(player, clickedLocation, Particle.HAPPY_VILLAGER, 20, false);
                } else {
                    player.sendMessage("§cNo chest location found at this position!");
                }
                break;

            case "show":
                plugin.showChestLocation(clickedLocation);
                player.sendMessage("§aShowing this chest location");
                break;
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (wandModes.containsKey(player)) {
            wandModes.remove(player);
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

    private void updateWandLore(Player player, String mode) {
        ItemStack wand = player.getInventory().getItemInMainHand();
        if (wand.getType() == Material.STICK && wand.hasItemMeta()) {
            ItemMeta meta = wand.getItemMeta();
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

            if (lore.size() >= 4) {
                lore.set(3, "§8Mode: " + mode);
            } else {
                lore.add("§8Mode: " + mode);
            }

            meta.setLore(lore);
            wand.setItemMeta(meta);
        }
    }
}