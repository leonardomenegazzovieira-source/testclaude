package com.saitamamod.event;

import com.saitamamod.SaitamaConfig;
import com.saitamamod.SaitamaData;
import com.saitamamod.SaitamaMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Conta flexões (socos no ar), abdominais (pulos), agachamentos e corrida. */
@Mod.EventBusSubscriber(modid = SaitamaMod.MODID)
public final class TrainingEvents {
    private static final class State {
        boolean sneaking;
        Vec3 last;
        int lastSquat = -100;
        int lastSwing = -100;
    }

    private static final Map<UUID, State> STATES = new HashMap<>();

    private TrainingEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer p)) {
            return;
        }
        State s = STATES.computeIfAbsent(p.getUUID(), k -> new State());
        Vec3 pos = p.position();
        boolean sneak = p.isShiftKeyDown();

        if (!SaitamaData.isSaitama(p)) {
            checkPushup(p, s);
            // agachamento completo: agachou e levantou
            if (s.sneaking && !sneak && p.onGround() && p.tickCount - s.lastSquat >= 8) {
                s.lastSquat = p.tickCount;
                count(p, SaitamaData.SQUATS, 1, SaitamaConfig.SQUATS.get(), 10, "Agachamentos", "");
            }
            // corrida: só conta sprint no chão, ignora teleportes
            if (s.last != null && p.isSprinting() && p.onGround() && !p.isPassenger()) {
                double dx = pos.x - s.last.x;
                double dz = pos.z - s.last.z;
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d < 3.0) {
                    count(p, SaitamaData.RUN, d, SaitamaConfig.RUN_METERS.get(), 100, "Corrida", " m");
                }
            }
        }
        s.sneaking = sneak;
        s.last = pos;
    }

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent e) {
        if (e.getEntity() instanceof ServerPlayer p && !SaitamaData.isSaitama(p)) {
            count(p, SaitamaData.SITUPS, 1, SaitamaConfig.SITUPS.get(), 10, "Abdominais", "");
        }
    }

    /** Flexão = soco no ar: o braço começou a balançar e não há bloco na frente. */
    private static void checkPushup(ServerPlayer p, State s) {
        if (!p.swinging || p.swingTime != 0 || p.tickCount - s.lastSwing < 8) {
            return;
        }
        if (p.pick(5.0D, 1.0F, false).getType() == HitResult.Type.MISS) {
            s.lastSwing = p.tickCount;
            count(p, SaitamaData.PUSHUPS, 1, SaitamaConfig.PUSHUPS.get(), 10, "Flexões", "");
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone e) {
        SaitamaData.copy(e.getOriginal(), e.getEntity());
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        STATES.remove(e.getEntity().getUUID());
    }

    private static void count(ServerPlayer p, String key, double amount, int target, int step,
                              String label, String unit) {
        double before = SaitamaData.get(p, key);
        if (before >= target) {
            return;
        }
        double after = SaitamaData.add(p, key, amount, target);
        if ((int) (after / step) > (int) (before / step) || after >= target) {
            p.displayClientMessage(Component.literal("§e" + label + ": §f" + (int) after + "/" + target + unit), true);
        }
        if (after >= target) {
            p.sendSystemMessage(Component.literal("§a" + label + " completo!"));
            if (SaitamaData.trainingDone(p)) {
                awaken(p);
            }
        }
    }

    private static void awaken(ServerPlayer p) {
        SaitamaData.setSaitama(p, true);
        p.server.getPlayerList().broadcastSystemMessage(
                Component.literal("§6§l" + p.getGameProfile().getName()
                        + " terminou o treino e agora é o SAITAMA!"), false);
        p.sendSystemMessage(Component.literal(
                "§eZ = Soco Sério  |  X = Socos Consecutivos  |  V = Soco Sério Planetário"));
        p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                SoundSource.PLAYERS, 1.0F, 1.0F);
        p.serverLevel().sendParticles(ParticleTypes.FLASH, p.getX(), p.getY() + 1.0, p.getZ(), 1, 0, 0, 0, 0);
    }
}
