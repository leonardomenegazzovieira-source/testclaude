package com.saitamamod.power;

import com.saitamamod.SaitamaConfig;
import com.saitamamod.SaitamaData;
import com.saitamamod.SaitamaMod;
import com.saitamamod.vfx.VfxHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Controla a carga e a execução dos três socos, e roda as tarefas de destruição tick a tick. */
@Mod.EventBusSubscriber(modid = SaitamaMod.MODID)
public final class PowerManager {
    public static final int SERIOUS = 0;
    public static final int CONSECUTIVE = 1;
    public static final int PLANETARY = 2;

    private static final int BURST_INTERVAL = 4;

    private static final class Action {
        final int power;
        int ticksLeft;
        final int total;

        Action(int power, int ticks) {
            this.power = power;
            this.ticksLeft = ticks;
            this.total = ticks;
        }
    }

    private static final Map<UUID, Action> ACTIONS = new HashMap<>();
    private static final List<Task> TASKS = new ArrayList<>();

    private PowerManager() {}

    /** Chamado pelo pacote da tecla (já no servidor). */
    public static void request(ServerPlayer p, int power) {
        if (!SaitamaData.isSaitama(p)) {
            p.displayClientMessage(Component.literal("§cVocê ainda não terminou o treino."), true);
            return;
        }
        if (ACTIONS.containsKey(p.getUUID())) {
            return;
        }
        switch (power) {
            case SERIOUS -> {
                ACTIONS.put(p.getUUID(), new Action(SERIOUS, SaitamaConfig.SERIOUS_CHARGE_TICKS.get()));
                p.sendSystemMessage(Component.literal("§c§lSoco Sério..."));
            }
            case CONSECUTIVE -> {
                ACTIONS.put(p.getUUID(), new Action(CONSECUTIVE, SaitamaConfig.CONSECUTIVE_DURATION_TICKS.get()));
                p.sendSystemMessage(Component.literal("§e§lSocos Normais Consecutivos!"));
            }
            case PLANETARY -> {
                if (!SaitamaConfig.ENABLE_PLANETARY.get()) {
                    p.displayClientMessage(Component.literal("§cO Soco Planetário está desligado na config."), true);
                    return;
                }
                ACTIONS.put(p.getUUID(), new Action(PLANETARY, SaitamaConfig.PLANETARY_CHARGE_TICKS.get()));
                p.sendSystemMessage(Component.literal("§4§lSoco Sério Planetário..."));
            }
            default -> { }
        }
    }

    /** Remove um bloco (bedrock fica). Sem drops e sem atualizar vizinhos, para ser barato. */
    public static void erase(ServerLevel level, BlockPos pos) {
        erase(level, pos, 0);
    }

    /** Igual ao erase, mas com 1 chance em debrisOneIn de soltar detritos do bloco (0 = nunca). */
    public static void erase(ServerLevel level, BlockPos pos, int debrisOneIn) {
        BlockState st = level.getBlockState(pos);
        if (st.isAir() || st.is(Blocks.BEDROCK)) {
            return;
        }
        if (debrisOneIn > 0 && level.random.nextInt(debrisOneIn) == 0) {
            VfxHelper.debris(level, st, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2, 0.5);
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2 | 16 | 32);
    }

    /** Mata a entidade e a arremessa na direção do soco. */
    public static void obliterate(ServerPlayer owner, LivingEntity target, Vec3 dir) {
        if (target == owner) {
            return;
        }
        target.hurt(owner.level().damageSources().playerAttack(owner), Float.MAX_VALUE);
        if (target.isAlive()) {
            target.hurt(owner.level().damageSources().genericKill(), Float.MAX_VALUE);
        }
        if (target.isAlive() && !(target instanceof Player)) {
            target.kill();
        }
        if (target.isAlive()) {
            target.setDeltaMovement(dir.scale(8.0).add(0, 1.0, 0));
            target.hurtMarked = true;
        }
    }

    private static void fireCorridor(ServerPlayer p, int length, int radius, int slicesPerTick) {
        ServerLevel level = p.serverLevel();
        Vec3 start = p.getEyePosition();
        Vec3 dir = p.getLookAngle().normalize();
        TASKS.add(new CorridorTask(level, p, start, dir, length, radius, slicesPerTick));
    }

    private static void clearWeather(ServerLevel level) {
        level.setWeatherParameters(6000, 0, false, false);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) {
            return;
        }
        tickActions(e.getServer());
        for (Iterator<Task> it = TASKS.iterator(); it.hasNext(); ) {
            if (it.next().tick()) {
                it.remove();
            }
        }
    }

    private static void tickActions(net.minecraft.server.MinecraftServer server) {
        for (Iterator<Map.Entry<UUID, Action>> it = ACTIONS.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, Action> en = it.next();
            ServerPlayer p = server.getPlayerList().getPlayer(en.getKey());
            Action a = en.getValue();
            if (p == null || !p.isAlive()) {
                it.remove();
                continue;
            }
            a.ticksLeft--;
            ServerLevel level = p.serverLevel();

            if (a.power == CONSECUTIVE) {
                int elapsed = a.total - a.ticksLeft;
                if (elapsed % BURST_INTERVAL == 0) {
                    fireCorridor(p, SaitamaConfig.CONSECUTIVE_LENGTH.get(),
                            SaitamaConfig.CONSECUTIVE_RADIUS.get(), SaitamaConfig.SLICES_PER_TICK.get() * 2);
                    p.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
                    level.playSound(null, p.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 2.0F, 1.4F);
                    VfxHelper.releaseBlast(level, p, 0.5F);
                    clearWeather(level);
                }
                if (a.ticksLeft <= 0) {
                    it.remove();
                }
                continue;
            }

            // carga (sério e planetário): fica parado e solta faíscas
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 6, false, false, false));
            float progress = 1.0F - (float) a.ticksLeft / Math.max(1, a.total);
            VfxHelper.chargeTick(level, p, progress);
            if (a.ticksLeft % 4 == 0) {
                VfxHelper.shake(level, p.getX(), p.getY(), p.getZ(), 48.0, 0.25F + progress, 6);
            }
            if (a.ticksLeft % 2 == 0) {
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.getX(), p.getY() + 1.0, p.getZ(),
                        12, 0.6, 0.9, 0.6, 0.2);
            }
            if (a.ticksLeft % 20 == 0 && a.ticksLeft > 0) {
                level.playSound(null, p.blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 2.0F, 0.5F);
            }
            if (a.ticksLeft > 0) {
                continue;
            }

            // solta o soco
            p.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
            level.playSound(null, p.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 4.0F, 0.5F);
            VfxHelper.releaseBlast(level, p, a.power == PLANETARY ? 3.0F : 1.5F);
            clearWeather(level);
            if (a.power == SERIOUS) {
                fireCorridor(p, SaitamaConfig.SERIOUS_LENGTH.get(), SaitamaConfig.SERIOUS_RADIUS.get(),
                        SaitamaConfig.SLICES_PER_TICK.get());
            } else {
                TASKS.add(new WaveTask(level, p));
            }
            it.remove();
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent e) {
        ACTIONS.clear();
        TASKS.clear();
    }
}
