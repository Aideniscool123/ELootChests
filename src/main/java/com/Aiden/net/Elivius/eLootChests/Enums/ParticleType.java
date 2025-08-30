package com.Aiden.net.Elivius.eLootChests.Enums;

import org.bukkit.Particle;

public enum ParticleType {
    VILLAGER_HAPPY(Particle.HAPPY_VILLAGER, false),
    GLOW(Particle.GLOW, true),
    SMOKE_LARGE(Particle.LARGE_SMOKE, false),
    HEART(Particle.HEART, false),
    FLAME(Particle.FLAME, false);

    private final Particle particle;
    private final boolean throughBlocks;

    ParticleType(Particle particle, boolean throughBlocks) {
        this.particle = particle;
        this.throughBlocks = throughBlocks;
    }

    public Particle getParticle() {
        return particle;
    }

    public boolean canSeeThroughBlocks() {
        return throughBlocks;
    }
}