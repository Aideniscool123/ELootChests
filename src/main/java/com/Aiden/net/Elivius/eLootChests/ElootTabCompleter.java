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
            List<String> commands = new ArrayList<>(Arrays.asList(
                    "wand", "spawn", "despawn", "table", "new", "info", "edit", "reload", "testitem", "admin", "gui"
            ));
            return StringUtil.copyPartialMatches(args[0], commands, completions);
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
                case "reload":
                case "admin":
                case "gui":
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

                case "edit":
                    // Config key suggestions for edit command
                    List<String> configKeys = Arrays.asList(
                            "chest-spawn-count",
                            "announce-world",
                            "boss-display-name",
                            "announce-rarities"
                    );
                    return StringUtil.copyPartialMatches(args[2], configKeys, completions);

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
                    // Fourth argument - config value suggestions
                    String configKey = args[2].toLowerCase();

                    switch (configKey) {
                        case "chest-spawn-count":
                            return StringUtil.copyPartialMatches(args[3], Arrays.asList("1", "5", "10", "15", "20"), completions);

                        case "announce-world":
                            // Suggest world names
                            return StringUtil.copyPartialMatches(args[3], Arrays.asList(
                                    "world", "world_nether", "world_the_end"
                            ), completions);

                        case "announce-rarities":
                            // Suggest common rarity combinations
                            List<String> rarityCombinations = Arrays.asList(
                                    "MYTHIC,GODLIKE",
                                    "MYTHIC,GODLIKE,LEGENDARY",
                                    "GODLIKE",
                                    "MYTHIC",
                                    "LEGENDARY,EPIC"
                            );
                            return StringUtil.copyPartialMatches(args[3], rarityCombinations, completions);

                        case "boss-display-name":
                            // No specific suggestions for display names
                            break;
                    }
                    break;

                case "new":
                    // Config value suggestions for new command
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