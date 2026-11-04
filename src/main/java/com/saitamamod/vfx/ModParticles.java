package com.saitamamod.vfx;

import com.saitamamod.SaitamaMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Partículas próprias do mod (as texturas ficam em assets/saitamamod/textures/particle). */
public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, SaitamaMod.MODID);

    /** Fumaça escura e grossa que cresce e sobe. */
    public static final RegistryObject<SimpleParticleType> BIG_SMOKE =
            PARTICLES.register("big_smoke", () -> new SimpleParticleType(true));

    /** Nuvem de poeira cor de terra, para ondas de choque no chão. */
    public static final RegistryObject<SimpleParticleType> BIG_DUST =
            PARTICLES.register("big_dust", () -> new SimpleParticleType(true));

    /** Clarão branco-amarelado, curto e sem sombra. */
    public static final RegistryObject<SimpleParticleType> GLOW =
            PARTICLES.register("glow", () -> new SimpleParticleType(true));

    private ModParticles() {}
}
