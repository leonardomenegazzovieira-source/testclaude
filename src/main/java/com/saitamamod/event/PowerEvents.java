package com.saitamamod.event;

import com.saitamamod.SaitamaConfig;
import com.saitamamod.SaitamaData;
import com.saitamamod.SaitamaMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Poderes passivos do Saitama: invencível, soco que mata tudo, quebra blocos na hora. */
@Mod.EventBusSubscriber(modid = SaitamaMod.MODID)
public final class PowerEvents {
    private PowerEvents() {}

    /** Nada machuca o Saitama (exceto /kill e o void). */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttack(LivingAttackEvent e) {
        if (e.getEntity() instanceof Player p
                && SaitamaData.isSaitama(p)
                && !e.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            e.setCanceled(true);
        }
    }

    /** Um soco normal do Saitama: dano absurdo, arremesso e pressão de vento. */
    @SubscribeEvent
    public static void onHurt(LivingHurtEvent e) {
        if (!(e.getSource().getEntity() instanceof ServerPlayer p)
                || e.getSource().getDirectEntity() != p
                || !SaitamaData.isSaitama(p)) {
            return;
        }
        LivingEntity target = e.getEntity();
        if (target instanceof Player && !SaitamaConfig.HURT_OTHER_PLAYERS.get()) {
            e.setCanceled(true);
            return;
        }
        e.setAmount(1.0E7F);

        target.setDeltaMovement(p.getLookAngle().scale(6.0).add(0, 0.8, 0));
        target.hurtMarked = true;

        // pressão de vento: empurra quem estiver perto do alvo (sem dano, para não encadear)
        AABB box = target.getBoundingBox().inflate(8.0);
        for (LivingEntity other : p.level().getEntitiesOfClass(LivingEntity.class, box,
                o -> o != p && o != target)) {
            Vec3 away = other.position().subtract(p.position());
            if (away.lengthSqr() > 1.0E-4) {
                other.setDeltaMovement(away.normalize().scale(2.5).add(0, 0.6, 0));
                other.hurtMarked = true;
            }
        }
        p.level().playSound(null, target.blockPosition(), SoundEvents.GENERIC_EXPLODE,
                SoundSource.PLAYERS, 1.0F, 1.5F);
    }

    /** Quebra qualquer bloco instantaneamente. */
    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed e) {
        if (SaitamaData.isSaitama(e.getEntity())) {
            e.setNewSpeed(100000.0F);
        }
    }

    /** Não precisa de ferramenta certa para o bloco dropar. */
    @SubscribeEvent
    public static void onHarvestCheck(PlayerEvent.HarvestCheck e) {
        if (SaitamaData.isSaitama(e.getEntity())) {
            e.setCanHarvest(true);
        }
    }

    /** Super velocidade e pulo, reaplicados a cada segundo. */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer p)
                || p.tickCount % 20 != 0 || !SaitamaData.isSaitama(p)) {
            return;
        }
        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 4, false, false, false));
        p.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, 3, false, false, false));
    }
}
