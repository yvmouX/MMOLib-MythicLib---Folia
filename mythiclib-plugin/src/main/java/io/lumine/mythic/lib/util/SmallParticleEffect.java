package io.lumine.mythic.lib.util;

import io.lumine.mythic.lib.MythicLib;
import cn.yvmou.ylib.scheduler.UniversalTask;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;

public class SmallParticleEffect implements Runnable {
    private final Location loc;
    private final Particle particle;
    private final double r;
    private final UniversalTask task;

    private double t;

    public SmallParticleEffect(Entity entity, Particle particle) {
        this(entity, particle, .7);
    }

    public SmallParticleEffect(Entity entity, Particle particle, double r) {
        this.loc = entity.getLocation().add(0, .5, 0);
        this.particle = particle;
        this.r = r;

        task = MythicLib.getScheduler().runTimer(entity, this, 0, null, 1);
    }

    public void run() {
        if (t > Math.PI * 2) task.cancel();

        for (int k = 0; k < 3; k++) {
            t += Math.PI / 10;
            loc.getWorld().spawnParticle(particle, loc.clone().add(r * Math.cos(t), t / Math.PI / 2, r * Math.sin(t)), 0);
        }
    }
}
