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
                    // Get boss names ONLY from registry file
                    List<String> bossNames = bossRegistry.getAllBossNames();
                    return StringUtil.copyPartialMatches(args[1], bossNames, completions);

                case "table":
                    // First argument after "table"
                    List<String> actions = new ArrayList<>();
                    actions.add("add");
                    actions.add("remove");
                    actions.add("list");
                    // Also allow direct group names for the old syntax: /eloot table <group> <rarity>
                    actions.addAll(bossRegistry.getAllBossNames());
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
                        // Boss names for wand selection
                        List<String> bossNames = bossRegistry.getAllBossNames();
                        return StringUtil.copyPartialMatches(args[2], bossNames, completions);
                    }
                    break;

                case "table":
                    // Second argument - depends on first argument
                    String firstArg = args[1].toLowerCase();

                    if (firstArg.equals("remove") || firstArg.equals("list")) {
                        // For remove/list: suggest boss names
                        List<String> bossNames = bossRegistry.getAllBossNames();
                        return StringUtil.copyPartialMatches(args[2], bossNames, completions);
                    }
                    else if (firstArg.equals("add")) {
                        // For add: suggest boss names
                        List<String> bossNames = bossRegistry.getAllBossNames();
                        return StringUtil.copyPartialMatches(args[2], bossNames, completions);
                    }
                    else {
                        // For direct group name (old syntax): suggest rarities
                        List<String> rarities = new ArrayList<>();
                        for (Rarity rarity : Rarity.values()) {
                            rarities.add(rarity.name().toLowerCase());
                        }
                        return StringUtil.copyPartialMatches(args[2], rarities, completions);
                    }

                case "new":
                    // Suggest world names for new boss creation
                    return StringUtil.copyPartialMatches(args[2], Arrays.asList(
                            "world", "world_nether", "world_the_end", "lobby", "arena"
                    ), completions);
            }
        }

        if (args.length == 4) {
            switch (args[0].toLowerCase()) {
                case "table":
                    // Third argument - depends on previous arguments
                    String firstArg = args[1].toLowerCase();

                    if (firstArg.equals("add")) {
                        // For add: suggest rarities after boss name
                        List<String> rarities = new ArrayList<>();
                        for (Rarity rarity : Rarity.values()) {
                            rarities.add(rarity.name().toLowerCase());
                        }
                        return StringUtil.copyPartialMatches(args[3], rarities, completions);
                    }
                    else if (!firstArg.equals("remove") && !firstArg.equals("list")) {
                        // For direct group + rarity (old syntax): suggest percentages
                        return StringUtil.copyPartialMatches(args[3], Arrays.asList(
                                "0.1", "0.5", "1.0", "5.0", "10.0", "15.0", "20.0", "25.0", "50.0", "75.0", "100.0"
                        ), completions);
                    }
                    break;

                case "edit":
                    // Config value suggestions for edit command
                    return StringUtil.copyPartialMatches(args[3], Arrays.asList(
                            "chest-spawn-count", "respawn-timer-minutes",
                            "hologram-text", "particles-enabled", "world-name"
                    ), completions);
            }
        }

        if (args.length == 5) {
            if (args[0].equalsIgnoreCase("table")) {
                // Fourth argument - only for add command with percentage
                String firstArg = args[1].toLowerCase();

                if (firstArg.equals("add")) {
                    // For add: suggest percentages after rarity
                    return StringUtil.copyPartialMatches(args[4], Arrays.asList(
                            "0.1", "0.5", "1.0", "5.0", "10.0", "15.0", "20.0", "25.0", "50.0", "75.0", "100.0"
                    ), completions);
                }
            }
        }

        return completions;
    }
}