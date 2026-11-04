package com.saitamamod;

import com.saitamamod.network.SaitamaNetwork;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SaitamaMod.MODID)
public class SaitamaMod {
    public static final String MODID = "saitamamod";

    public SaitamaMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SaitamaConfig.SPEC);
        com.saitamamod.vfx.ModParticles.PARTICLES.register(
                FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus()
                .addListener((net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent e) ->
                        e.enqueueWork(SaitamaNetwork::register));
    }
}
