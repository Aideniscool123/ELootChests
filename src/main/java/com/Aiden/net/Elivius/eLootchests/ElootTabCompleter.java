package com.Aiden.net.Elivius.eLootchests;

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

    public ElootTabCompleter(BossManager bossManager) {
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
                    // Add your group names here later
                    return StringUtil.copyPartialMatches(args[1], Arrays.asList(
                            "test", "lushsanctuary", "santaboss" // Example groups
                    ), completions);
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("wand") && args[1].equalsIgnoreCase("select")) {
            // Group selection for wand
            return StringUtil.copyPartialMatches(args[2], Arrays.asList(
                    "test", "lushsanctuary", "santaboss" // Example groups
            ), completions);
        }

        return completions;
    }
}