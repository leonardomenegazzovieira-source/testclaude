package com.saitamamod.power;

import com.saitamamod.SaitamaConfig;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Onda planetária: um anel que se expande a partir do Saitama, apagando todas as colunas
 * de blocos (do topo do mundo até a bedrock) e matando tudo que ela alcança.
 */
public final class WaveTask implements Task {
    private final ServerLevel level;
    private final ServerPlayer owner;
    private final int cx;
    private final int cz;
    private final double cy;
    private final int maxRadius;
    private final int columnsPerTick;

    private int ring = 0;
    private IntArrayList xs;
    private IntArrayList zs;
    private int idx = 0;
    private int age = 0;

    public WaveTask(ServerLevel level, ServerPlayer owner) {
        this.level = level;
        this.owner = owner;
        this.cx = owner.blockPosition().getX();
        this.cz = owner.blockPosition().getZ();
        this.cy = owner.getY();
        this.maxRadius = SaitamaConfig.PLANETARY_MAX_RADIUS.get();
        this.columnsPerTick = SaitamaConfig.PLANETARY_COLUMNS_PER_TICK.get();
    }

    /** Colunas (dx, dz) cuja distância ao centro está entre r e r+1. */
    private void buildRing(int r) {
        xs = new IntArrayList();
        zs = new IntArrayList();
        int lo = r * r;
        int hi = (r + 1) * (r + 1);
        for (int dx = -(r + 1); dx <= r + 1; dx++) {
            for (int dz = -(r + 1); dz <= r + 1; dz++) {
                int d = dx * dx + dz * dz;
                if (d >= lo && d < hi) {
                    xs.add(dx);
                    zs.add(dz);
                }
            }
        }
        idx = 0;
    }

    private void eraseColumn(int x, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, 0, z);
        if (!level.hasChunkAt(pos)) {
            return;
        }
        for (int y = level.getMaxBuildHeight() - 1; y >= level.getMinBuildHeight(); y--) {
            pos.setY(y);
            PowerManager.erase(level, pos);
        }
    }

    private void killEntitiesInside(int radius) {
        AABB box = new AABB(cx - radius, level.getMinBuildHeight(), cz - radius,
                cx + radius, level.getMaxBuildHeight(), cz + radius);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, x -> x != owner)) {
            if (e instanceof Player && !SaitamaConfig.HURT_OTHER_PLAYERS.get()) {
                continue;
            }
            double dx = e.getX() - cx;
            double dz = e.getZ() - cz;
            if (dx * dx + dz * dz <= (double) radius * radius) {
                Vec3 away = new Vec3(dx, 0, dz);
                PowerManager.obliterate(owner, e, away.lengthSqr() > 1.0E-4 ? away.normalize() : new Vec3(0, 1, 0));
            }
        }
    }

    @Override
    public boolean tick() {
        age++;
        int budget = columnsPerTick;
        while (budget > 0 && ring <= maxRadius) {
            if (xs == null) {
                buildRing(ring);
            }
            while (idx < xs.size() && budget > 0) {
                eraseColumn(cx + xs.getInt(idx), cz + zs.getInt(idx));
                idx++;
                budget--;
            }
            if (idx >= xs.size()) {
                ring++;
                xs = null;
                zs = null;
            }
        }

        if (age % 3 == 0 && ring > 0) {
            for (int i = 0; i < 16; i++) {
                double ang = i * (Math.PI * 2 / 16);
                level.sendParticles(ParticleTypes.EXPLOSION, cx + Math.cos(ang) * ring, cy + 1.0,
                        cz + Math.sin(ang) * ring, 1, 0, 0, 0, 0);
            }
        }
        if (age % 5 == 0) {
            killEntitiesInside(Math.min(ring, maxRadius));
        }
        return ring > maxRadius;
    }
}
