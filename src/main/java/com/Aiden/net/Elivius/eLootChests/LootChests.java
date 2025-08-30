package com.Aiden.net.Elivius.eLootChests;

import org.bukkit.*;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class LootChests extends JavaPlugin implements Listener {
    private BossManager bossManager;
    private final BossRegistry bossRegistry;

    public LootChests(BossRegistry bossRegistry) {
        this.bossRegistry = bossRegistry;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);

        this.bossManager = new BossManager(this);

        getCommand("eloot").setExecutor(new ElootCommand(this, bossManager, bossRegistry));
        getCommand("eloot").setTabCompleter(new ElootTabCompleter(bossManager));

        getLogger().info("LootChests has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("LootChests has been disabled!");
    }

    public void showChestLocation(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        new BukkitRunnable() {
            double time = 0;

            @Override
            public void run() {
                if (time > 1.0) {
                    this.cancel();
                    return;
                }

                drawBlockOutline(location);
                time += 0.05;
            }
        }.runTaskTimer(this, 0, 1);
    }

    private void drawBlockOutline(Location location) {
        World world = location.getWorld();
        double x = location.getX();
        double y = location.getY() + 1; // +1 to raise above the clicked block
        double z = location.getZ();

        Particle particle = Particle.HAPPY_VILLAGER;

        // Draw bottom square (one block higher)
        for (double i = 0; i <= 1; i += 0.1) {
            world.spawnParticle(particle, x + i, y, z, 1);
            world.spawnParticle(particle, x, y, z + i, 1);
            world.spawnParticle(particle, x + 1, y, z + i, 1);
            world.spawnParticle(particle, x + i, y, z + 1, 1);
        }

        // Draw top square (two blocks higher)
        for (double i = 0; i <= 1; i += 0.1) {
            world.spawnParticle(particle, x + i, y + 1, z, 1);
            world.spawnParticle(particle, x, y + 1, z + i, 1);
            world.spawnParticle(particle, x + 1, y + 1, z + i, 1);
            world.spawnParticle(particle, x + i, y + 1, z + 1, 1);
        }

        // Draw vertical lines
        for (double i = 0; i <= 1; i += 0.1) {
            world.spawnParticle(particle, x, y + i, z, 1);
            world.spawnParticle(particle, x + 1, y + i, z, 1);
            world.spawnParticle(particle, x, y + i, z + 1, 1);
            world.spawnParticle(particle, x + 1, y + i, z + 1, 1);
        }
    }

    public BossManager getBossManager() {
        return bossManager;
    }
}