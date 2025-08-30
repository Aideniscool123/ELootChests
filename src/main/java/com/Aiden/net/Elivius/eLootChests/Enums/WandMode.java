package com.Aiden.net.Elivius.eLootChests.Enums;

public enum WandMode {
    ADD("ADD", "Right-click blocks to add chest locations", ParticleType.VILLAGER_HAPPY, 1),
    REMOVE("REMOVE", "Right-click to remove chest locations", ParticleType.GLOW, -1),
    SHOW("SHOW", "Showing nearby chest locations", ParticleType.VILLAGER_HAPPY, 600);

    private final String displayName;
    private final String description;
    private final ParticleType particleType;
    private final int durationTicks;

    WandMode(String displayName, String description, ParticleType particleType, int durationTicks) {
        this.displayName = displayName;
        this.description = description;
        this.particleType = particleType;
        this.durationTicks = durationTicks;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public ParticleType getParticleType() { return particleType; }
    public int getDurationTicks() { return durationTicks; }
}