package com.Aiden.net.Elivius.eLootChests.Enums;

import org.bukkit.ChatColor;

public enum Rarity {
    COMMON("Common", ChatColor.GREEN),
    RARE("Rare", ChatColor.AQUA),
    EPIC("Epic", ChatColor.RED),
    LEGENDARY("Legendary", ChatColor.GOLD),
    MYTHIC("Mythic", ChatColor.DARK_PURPLE),
    GODLIKE("Godlike", ChatColor.DARK_RED);

    private final String name;
    private final ChatColor color;

    Rarity(String name, ChatColor color) {
        this.name = name;
        this.color = color;
    }

    public String getName() { return name; }
    public ChatColor getColor() { return color; }
    public String getFormattedName() { return color + name; }

    public double getDefaultPercentage() {
        return switch (this) {
            case COMMON -> 70.0;
            case RARE -> 20.0;
            case EPIC -> 7.0;
            case LEGENDARY -> 2.5;
            case MYTHIC -> 0.4;
            case GODLIKE -> 0.1;
            default -> 0.0;
        };
    }
}