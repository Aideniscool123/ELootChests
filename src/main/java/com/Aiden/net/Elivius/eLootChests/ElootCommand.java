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

        if (args[0].equalsIgnoreCase("wand")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cThis command can only be used by players!");
                return true;
            }
            return handleWandCommand((Player) sender, args);
        }

        sender.sendMessage("§cUnknown sub-command. Use: /eloot wand");
        return true;
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
                player.sendMessage("§aSelected group: §e" + groupName);
                wandModes.put(player, WandMode.ADD);
                break;

            default:
                player.sendMessage("§cUnknown wand mode. Use: add, remove, or show");
                return false;
        }

        return true;
    }

    private void showNearbyLocations(Player player, int radius, WandMode mode) {
        List<Location> allLocations = bossManager.getSavedChestLocations();
        Location playerLoc = player.getLocation();

        int count = 0;
        for (Location loc : allLocations) {
            if (loc.getWorld().equals(playerLoc.getWorld()) &&
                    loc.distance(playerLoc) <= radius) {

                bossManager.showLocationParticles(player, loc, mode.getParticleType(),
                        mode.getDurationTicks(), mode.getParticleType().canSeeThroughBlocks());
                count++;
            }
        }

        String durationMsg = mode.getDurationTicks() == -1 ? "until mode change" : "for 30 seconds";
        player.sendMessage("§aShowing §e" + count + "§a chest locations within §e" + radius + "§a blocks (§7" + durationMsg + "§a)");
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
                    "§8Mode: " + WandMode.ADD.getDisplayName()
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
        WandMode mode = wandModes.getOrDefault(player, WandMode.ADD);

        switch (mode) {
            case ADD:
                plugin.showChestLocation(clickedLocation);
                bossManager.saveChestLocation(clickedLocation);
                player.sendMessage("§aLocation saved! Chest added to coordinates.");
                updateWandLore(player, mode.getDisplayName());
                break;

            case REMOVE:
                // Create location with correct Y+1 for particle matching
                Location particleLocation = new Location(clickedLocation.getWorld(),
                        clickedLocation.getX(),
                        clickedLocation.getY() + 1,
                        clickedLocation.getZ());

                if (bossManager.removeChestLocation(clickedLocation)) {
                    player.sendMessage("§aChest location removed!");
                    // Stop particles for this removed location (using Y+1 position)
                    bossManager.stopLocationParticles(player, particleLocation);
                    // Show confirmation particles
                    bossManager.showLocationParticles(player, clickedLocation, ParticleType.SMOKE_LARGE, 20, false);
                } else {
                    player.sendMessage("§cNo chest location found at this position!");
                }
                break;

            case SHOW:
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