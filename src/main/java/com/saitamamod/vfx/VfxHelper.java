package com.saitamamod.vfx;

import com.saitamamod.network.SaitamaNetwork;
import com.saitamamod.network.ShakePacket;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

/** Efeitos visuais dos poderes: fumaça, poeira, ondas de choque, detritos e tremor de tela. */
public final class VfxHelper {
    /** Distância máxima (em blocos) em que um jogador recebe as partículas. */
    private static final double RANGE = 160.0;

    private VfxHelper() {}

    /**
     * Envia partículas a todos os jogadores próximos, ignorando o limite padrão de 32 blocos.
     * Com count = 0, (dx, dy, dz) é a direção da velocidade e speed multiplica essa velocidade.
     * Com count > 0, dx/dy/dz espalham a posição e speed espalha a velocidade.
     */
    public static void emit(ServerLevel level, ParticleOptions p, double x, double y, double z,
                            int count, double dx, double dy, double dz, double speed) {
        double r2 = RANGE * RANGE;
        for (ServerPlayer pl : level.players()) {
            if (pl.distanceToSqr(x, y, z) <= r2) {
                level.sendParticles(pl, p, true, x, y, z, count, dx, dy, dz, speed);
            }
        }
    }

    /** Tremor de tela para quem estiver perto; quanto mais longe, mais fraco. */
    public static void shake(ServerLevel level, double x, double y, double z, double radius,
                             float intensity, int ticks) {
        double r2 = radius * radius;
        for (ServerPlayer pl : level.players()) {
            double d2 = pl.distanceToSqr(x, y, z);
            if (d2 <= r2) {
                float falloff = (float) (1.0 - Math.sqrt(d2) / radius);
                SaitamaNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> pl),
                        new ShakePacket(intensity * Math.max(0.15F, falloff), ticks));
            }
        }
    }

    /** Anel horizontal de partículas que se expande a partir do centro. */
    public static void ring(ServerLevel level, double cx, double cy, double cz, double radius,
                            int points, ParticleOptions p, double speed, double lift) {
        for (int i = 0; i < points; i++) {
            double a = Math.PI * 2 * i / points;
            double c = Math.cos(a);
            double s = Math.sin(a);
            emit(level, p, cx + c * radius, cy, cz + s * radius, 0, c, lift, s, speed);
        }
    }

    /** Anel num plano qualquer (u, v), usado nas fatias do corredor: a "parede" de pressão. */
    public static void planeRing(ServerLevel level, Vec3 center, Vec3 u, Vec3 v, Vec3 dir,
                                 double radius, int points, ParticleOptions p, double speed, double forward) {
        for (int i = 0; i < points; i++) {
            double a = Math.PI * 2 * i / points;
            Vec3 o = u.scale(Math.cos(a)).add(v.scale(Math.sin(a)));
            Vec3 pos = center.add(o.scale(radius));
            Vec3 vel = o.add(dir.scale(forward));
            emit(level, p, pos.x, pos.y, pos.z, 0, vel.x, vel.y, vel.z, speed);
        }
    }

    /** Pedaços do bloco voando para cima e para os lados. */
    public static void debris(ServerLevel level, BlockState state, double x, double y, double z,
                              int n, double power) {
        if (state.isAir() || !state.getFluidState().isEmpty()) {
            return;
        }
        BlockParticleOption opt = new BlockParticleOption(ParticleTypes.BLOCK, state);
        for (int i = 0; i < n; i++) {
            double vx = (level.random.nextDouble() - 0.5) * 1.2;
            double vy = 0.4 + level.random.nextDouble() * 0.9;
            double vz = (level.random.nextDouble() - 0.5) * 1.2;
            emit(level, opt, x, y, z, 0, vx, vy, vz, power);
        }
    }

    /** Impacto do soco normal no alvo. */
    public static void hitImpact(ServerLevel level, double x, double y, double z) {
        emit(level, ModParticles.GLOW.get(), x, y, z, 1, 0, 0, 0, 0);
        emit(level, ParticleTypes.EXPLOSION, x, y, z, 1, 0, 0, 0, 0);
        emit(level, ParticleTypes.POOF, x, y, z, 25, 0.5, 0.5, 0.5, 0.25);
        emit(level, ModParticles.BIG_DUST.get(), x, y, z, 5, 0.5, 0.5, 0.5, 0.2);
        emit(level, ModParticles.BIG_SMOKE.get(), x, y, z, 3, 0.4, 0.4, 0.4, 0.06);
        emit(level, ParticleTypes.SONIC_BOOM, x, y, z, 1, 0, 0, 0, 0);
    }

    /** Partículas durante a carga: faíscas sendo sugadas para o Saitama e poeira no chão. */
    public static void chargeTick(ServerLevel level, ServerPlayer p, float progress) {
        double x = p.getX();
        double y = p.getY();
        double z = p.getZ();
        int n = 4 + (int) (progress * 10);
        for (int i = 0; i < n; i++) {
            double a = level.random.nextDouble() * Math.PI * 2;
            double c = Math.cos(a);
            double s = Math.sin(a);
            double r = 2.5 + level.random.nextDouble() * 2.5;
            double h = level.random.nextDouble() * 2.2;
            emit(level, ParticleTypes.ELECTRIC_SPARK, x + c * r, y + h, z + s * r, 0,
                    -c, 0.0, -s, 0.25 + progress * 0.25);
        }
        if (p.tickCount % 6 == 0) {
            ring(level, x, y + 0.1, z, 0.6, 20, ParticleTypes.POOF, 0.12 + progress * 0.25, 0.0);
            BlockState under = level.getBlockState(p.blockPosition().below());
            debris(level, under, x, y + 0.2, z, 4, 0.12);
        }
    }

    /** Explosão na hora de soltar o soco. scale 0.5 = rajada leve, 1.5 = sério, 3 = planetário. */
    public static void releaseBlast(ServerLevel level, ServerPlayer p, float scale) {
        double x = p.getX();
        double y = p.getY();
        double z = p.getZ();
        Vec3 eye = p.getEyePosition();
        Vec3 look = p.getLookAngle();

        emit(level, ParticleTypes.FLASH, eye.x + look.x * 1.5, eye.y + look.y * 1.5, eye.z + look.z * 1.5,
                1, 0, 0, 0, 0);
        emit(level, ParticleTypes.SONIC_BOOM, eye.x + look.x * 2, eye.y + look.y * 2, eye.z + look.z * 2,
                1, 0, 0, 0, 0);
        emit(level, ModParticles.GLOW.get(), eye.x + look.x * 1.5, eye.y + look.y * 1.5, eye.z + look.z * 1.5,
                1, 0, 0, 0, 0);
        ring(level, x, y + 0.2, z, 1.0, (int) (32 * scale), ParticleTypes.CLOUD, 0.9 * scale, 0.05);
        ring(level, x, y + 0.4, z, 1.2, (int) (18 * scale), ModParticles.BIG_DUST.get(), 1.1 * scale, 0.03);
        ring(level, x, y + 0.6, z, 1.6, (int) (10 * scale), ModParticles.BIG_SMOKE.get(), 0.5 * scale, 0.04);
        emit(level, ModParticles.BIG_SMOKE.get(), x, y + 0.5, z, (int) (8 * scale), 1.0, 0.3, 1.0, 0.12);
        emit(level, ParticleTypes.LARGE_SMOKE, x, y + 0.3, z, (int) (30 * scale), 1.2, 0.2, 1.2, 0.08);
        for (int i = 0; i < Math.max(1, (int) scale); i++) {
            emit(level, ParticleTypes.EXPLOSION_EMITTER, x + look.x * (2 + i * 3), y + 1.0,
                    z + look.z * (2 + i * 3), 1, 0, 0, 0, 0);
        }
        shake(level, x, y, z, 64.0 * scale, 2.0F * scale, 30);
    }

    /** Uma fatia do corredor de destruição: anel de pressão, fumaça e estrondo. */
    public static void slice(ServerLevel level, Vec3 c, Vec3 u, Vec3 v, Vec3 dir, int radius, int index) {
        if (index % 3 == 0) {
            planeRing(level, c, u, v, dir, radius, Math.min(48, 16 + radius * 2),
                    ParticleTypes.CLOUD, 0.7, 0.4);
        }
        if (index % 4 == 0) {
            planeRing(level, c, u, v, dir, radius, Math.min(20, 8 + radius / 2),
                    ModParticles.BIG_DUST.get(), 0.5, 0.3);
        }
        if (index % 6 == 0) {
            emit(level, ParticleTypes.LARGE_SMOKE, c.x, c.y, c.z, 8,
                    radius * 0.4, radius * 0.4, radius * 0.4, 0.05);
        }
        if (index % 8 == 0) {
            emit(level, ModParticles.BIG_SMOKE.get(), c.x, c.y, c.z, 3,
                    radius * 0.5, radius * 0.5, radius * 0.5, 0.04);
        }
        if (index % 12 == 0) {
            emit(level, ParticleTypes.SONIC_BOOM, c.x, c.y, c.z, 1, 0, 0, 0, 0);
        }
    }

    /** A frente da onda planetária: parede de poeira e fumaça acompanhando o terreno. */
    public static void waveFront(ServerLevel level, double cx, double cy, double cz, int radius) {
        int points = Math.min(120, 24 + radius);
        for (int i = 0; i < points; i++) {
            double a = Math.PI * 2 * i / points;
            double c = Math.cos(a);
            double s = Math.sin(a);
            double x = cx + c * radius;
            double z = cz + s * radius;
            // olha um pouco à frente, onde o terreno ainda existe, para achar a altura do chão
            int gy = level.getHeight(Heightmap.Types.MOTION_BLOCKING,
                    (int) Math.floor(cx + c * (radius + 6)), (int) Math.floor(cz + s * (radius + 6)));
            double y = gy > level.getMinBuildHeight() ? gy : cy;

            emit(level, ParticleTypes.CLOUD, x, y, z, 0, c, 0.18, s, 0.8);
            if (i % 2 == 0) {
                emit(level, ParticleTypes.POOF, x, y + 0.5, z, 0, c, 0.3, s, 0.5);
            }
            if (i % 5 == 0) {
                emit(level, ParticleTypes.LARGE_SMOKE, x, y + 1.0, z, 2, 0.5, 1.0, 0.5, 0.05);
            }
            if (i % 3 == 0) {
                emit(level, ModParticles.BIG_DUST.get(), x, y + 0.8, z, 0, c, 0.15, s, 0.5);
            }
            if (i % 8 == 0) {
                emit(level, ModParticles.BIG_SMOKE.get(), x, y + 1.5, z, 1, 0.3, 0.6, 0.3, 0.06);
            }
            if (i % 12 == 0) {
                emit(level, ParticleTypes.EXPLOSION, x, y + 1.0, z, 1, 0, 0, 0, 0);
            }
        }
    }
}
