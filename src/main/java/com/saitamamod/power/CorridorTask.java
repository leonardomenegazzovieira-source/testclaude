package com.saitamamod.power;

import com.saitamamod.SaitamaConfig;
import com.saitamamod.vfx.VfxHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Corredor cilíndrico de destruição: o soco sério e cada rajada dos socos consecutivos. */
public final class CorridorTask implements Task {
    private static final double STEP = 0.8;

    private final ServerLevel level;
    private final Vec3 start;
    private final Vec3 dir;
    private final Vec3 u;
    private final Vec3 v;
    private final int radius;
    private final int slices;
    private final int slicesPerTick;
    private int index = 0;

    public CorridorTask(ServerLevel level, ServerPlayer owner, Vec3 start, Vec3 dir,
                        int length, int radius, int slicesPerTick) {
        this.level = level;
        this.start = start;
        this.dir = dir;
        this.radius = radius;
        this.slices = (int) (length / STEP);
        this.slicesPerTick = slicesPerTick;

        Vec3 ref = Math.abs(dir.y) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        this.u = dir.cross(ref).normalize();
        this.v = dir.cross(u).normalize();

        hitEntities(owner, length);
    }

    /** Mata na hora tudo que estiver dentro do corredor. */
    private void hitEntities(ServerPlayer owner, int length) {
        Vec3 end = start.add(dir.scale(length));
        AABB box = new AABB(start, end).inflate(radius + 1.0);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, x -> x != owner)) {
            if (e instanceof Player && !SaitamaConfig.HURT_OTHER_PLAYERS.get()) {
                continue;
            }
            Vec3 rel = e.position().subtract(start);
            double along = rel.dot(dir);
            if (along < -3.5 || along > length) {
                continue;
            }
            double perp = rel.subtract(dir.scale(along)).length();
            if (perp <= radius + 1.0) {
                PowerManager.obliterate(owner, e, dir);
            }
        }
    }

    @Override
    public boolean tick() {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        double r2 = (double) radius * radius;
        int done = 0;
        while (done < slicesPerTick && index <= slices) {
            Vec3 c = start.add(dir.scale(index * STEP));
            for (double a = -radius; a <= radius; a += STEP) {
                for (double b = -radius; b <= radius; b += STEP) {
                    if (a * a + b * b > r2) {
                        continue;
                    }
                    Vec3 w = c.add(u.scale(a)).add(v.scale(b));
                    pos.set(Mth.floor(w.x), Mth.floor(w.y), Mth.floor(w.z));
                    PowerManager.erase(level, pos, 500);
                }
            }
            VfxHelper.slice(level, c, u, v, dir, radius, index);
            index++;
            done++;
        }
        return index > slices;
    }
}
