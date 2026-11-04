package com.saitamamod.client;

import com.saitamamod.SaitamaMod;
import com.saitamamod.vfx.ModParticles;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Liga cada tipo de partícula à sua classe de renderização (só no cliente). */
@Mod.EventBusSubscriber(modid = SaitamaMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ParticleSetup {
    private ParticleSetup() {}

    @SubscribeEvent
    public static void register(RegisterParticleProvidersEvent e) {
        e.registerSpriteSet(ModParticles.BIG_SMOKE.get(),
                sprites -> new BillowParticle.Provider(sprites, BillowParticle.SMOKE));
        e.registerSpriteSet(ModParticles.BIG_DUST.get(),
                sprites -> new BillowParticle.Provider(sprites, BillowParticle.DUST));
        e.registerSpriteSet(ModParticles.GLOW.get(),
                sprites -> new BillowParticle.Provider(sprites, BillowParticle.GLOW));
    }
}
