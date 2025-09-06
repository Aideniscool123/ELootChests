package com.Aiden.net.Elivius.eLootChests.Enums;

public enum LootConstraint {
    MAX_COMMON_PER_CHEST(17, Rarity.COMMON),
    MAX_RARE_PER_CHEST(10, Rarity.RARE),
    MAX_EPIC_PER_CHEST(3, Rarity.EPIC),
    MAX_LEGENDARY_PER_CHEST(1, Rarity.LEGENDARY),
    MAX_MYTHIC_PER_CHEST(1, Rarity.MYTHIC),
    MAX_GODLIKE_PER_CHEST(1, Rarity.GODLIKE),
    MYTHIC_OR_GODLIKE_ONLY(1, Rarity.MYTHIC, Rarity.GODLIKE), // Only one of these
    NO_GODLIKE_WITH_LEGENDARY(0, Rarity.GODLIKE, Rarity.LEGENDARY); // Mutual exclusion

    private final int maxAmount;
    private final Rarity[] affectedRarities;

    LootConstraint(int maxAmount, Rarity... affectedRarities) {
        this.maxAmount = maxAmount;
        this.affectedRarities = affectedRarities;
    }

    public int getMaxAmount() { return maxAmount; }
    public Rarity[] getAffectedRarities() { return affectedRarities; }
    public boolean affectsRarity(Rarity rarity) {
        for (Rarity r : affectedRarities) {
            if (r == rarity) return true;
        }
        return false;
    }
}