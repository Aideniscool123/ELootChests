package com.Aiden.net.Elivius.eLootChests;

import com.Aiden.net.Elivius.eLootChests.Enums.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ElootTabCompleter implements TabCompleter {

    private final BossRegistry bossRegistry;

    public ElootTabCompleter(BossManager bossManager, BossRegistry bossRegistry) {
        this.bossRegistry = bossRegistry;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Main command completions
            return StringUtil.copyPartialMatches(args[0], Arrays.asList(
                    "wand", "spawn", "despawn", "table", "new", "info", "edit"
            ), completions);
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "wand":
                    return StringUtil.copyPartialMatches(args[1], Arrays.asList(
                            "add", "remove", "show", "select"
                    ), completions);

                case "spawn":
                case "despawn":
                case "info":
                case "edit":
                    List<String> bossNames = bossRegistry.getAllBossNames();
                    return StringUtil.copyPartialMatches(args[1], bossNames, completions);
                case "table":
                    List<String> actions = new ArrayList<>();
                    for (ChestAction action : ChestAction.values()) {
                        actions.add(action.getCommand());
                    }
                    actions.add("remove");
                    actions.add("list");
                    return StringUtil.copyPartialMatches(args[1], actions, completions);

                case "new":
                    // Suggest default boss types for creation
                    List<String> defaultBosses = new ArrayList<>();
                    for (BossType boss : BossType.values()) {
                        defaultBosses.add(boss.getConfigName());
                    }
                    return StringUtil.copyPartialMatches(args[1], defaultBosses, completions);
            }
        }

        if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "wand":
                    if (args[1].equalsIgnoreCase("select")) {
                        List<String> bossNames = bossRegistry.getAllBossNames();
                        return StringUtil.copyPartialMatches(args[2], bossNames, completions);
                    }
                    break;

                case "table":
                    if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove")) {
                        // Rarity types for loot table operations
                        List<String> rarities = new ArrayList<>();
                        for (Rarity rarity : Rarity.values()) {
                            rarities.add(rarity.name().toLowerCase());
                        }
                        return StringUtil.copyPartialMatches(args[2], rarities, completions);
                    }
                    break;

                case "new":
                    // Suggest world names for new boss creation
                    return StringUtil.copyPartialMatches(args[2], Arrays.asList(
                            "world", "world_nether", "world_the_end", "lobby", "arena"
                    ), completions);
            }
        }

        if (args.length == 4) {
            if (args[0].equalsIgnoreCase("table") && args[1].equalsIgnoreCase("add")) {
                // Percentage suggestions for loot table add
                return StringUtil.copyPartialMatches(args[3], Arrays.asList(
                        "0.1", "0.5", "1.0", "5.0", "10.0", "15.0", "20.0", "25.0", "50.0", "75.0", "100.0"
                ), completions);
            }

            if (args[0].equalsIgnoreCase("edit")) {
                // Config value suggestions for edit command
                return StringUtil.copyPartialMatches(args[3], Arrays.asList(
                        "chest-spawn-count", "respawn-timer-minutes",
                        "hologram-text", "particles-enabled", "world-name"
                ), completions);
            }
        }

        return completions;
    }
}