package com.Aiden.net.Elivius.eLootChests.Enums;

import org.bukkit.ChatColor;

public enum Rarity {
    COMMON("Common", ChatColor.GRAY, 70.0),
    RARE("Rare", ChatColor.BLUE, 20.0),
    EPIC("Epic", ChatColor.DARK_PURPLE, 7.0),
    LEGENDARY("Legendary", ChatColor.GOLD, 2.5),
    MYTHIC("Mythic", ChatColor.LIGHT_PURPLE, 0.4),
    GODLIKE("Godlike", ChatColor.RED, 0.1);

    private final String name;
    private final ChatColor color;
    private final double spawnPercentage;

    Rarity(String name, ChatColor color, double spawnPercentage) {
        this.name = name;
        this.color = color;
        this.spawnPercentage = spawnPercentage;
    }

    public String getName() { return name; }
    public ChatColor getColor() { return color; }
    public double getSpawnPercentage() { return spawnPercentage; }
    public String getFormattedName() { return color + name; }
}