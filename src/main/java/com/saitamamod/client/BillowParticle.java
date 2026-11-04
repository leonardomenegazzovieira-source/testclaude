package com.saitamamod.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

/** Nuvem grande que cresce, gira devagar e some aos poucos: fumaça, poeira e clarão. */
public class BillowParticle extends TextureSheetParticle {
    public static final int SMOKE = 0;
    public static final int DUST = 1;
    public static final int GLOW = 2;

    private final SpriteSet sprites;
    private final int kind;
    private final float baseAlpha;
    private final float grow;
    private final float rotSpeed;
    private final float drag;
    private final float rise;

    protected BillowParticle(ClientLevel level, double x, double y, double z,
                             double vx, double vy, double vz, SpriteSet sprites, int kind) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.kind = kind;
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.hasPhysics = false;

        float tone = 0.8F + this.random.nextFloat() * 0.3F;
        switch (kind) {
            case SMOKE -> {
                float g = 0.20F + this.random.nextFloat() * 0.25F;
                this.setColor(g, g, g);
                this.quadSize = 1.4F + this.random.nextFloat() * 1.6F;
                this.lifetime = 45 + this.random.nextInt(45);
                this.grow = 2.4F;
                this.baseAlpha = 0.8F;
                this.drag = 0.90F;
                this.rise = 0.004F;
                this.rotSpeed = (this.random.nextFloat() - 0.5F) * 0.06F;
            }
            case DUST -> {
                this.setColor(0.64F * tone, 0.54F * tone, 0.42F * tone);
                this.quadSize = 1.1F + this.random.nextFloat() * 1.3F;
                this.lifetime = 30 + this.random.nextInt(35);
                this.grow = 2.6F;
                this.baseAlpha = 0.7F;
                this.drag = 0.88F;
                this.rise = 0.0F;
                this.rotSpeed = (this.random.nextFloat() - 0.5F) * 0.08F;
            }
            default -> {
                this.setColor(1.0F, 0.92F, 0.75F);
                this.quadSize = 2.5F + this.random.nextFloat() * 2.0F;
                this.lifetime = 5 + this.random.nextInt(5);
                this.grow = 0.4F;
                this.baseAlpha = 1.0F;
                this.drag = 1.0F;
                this.rise = 0.0F;
                this.rotSpeed = 0.0F;
            }
        }
        this.roll = this.random.nextFloat() * ((float) Math.PI * 2.0F);
        this.oRoll = this.roll;
        this.alpha = 0.0F;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.setSpriteFromAge(this.sprites);
        this.oRoll = this.roll;
        this.roll += this.rotSpeed;
        this.xd *= this.drag;
        this.yd = this.yd * this.drag + this.rise;
        this.zd *= this.drag;
        this.move(this.xd, this.yd, this.zd);

        float t = (float) this.age / (float) this.lifetime;
        float fadeIn = Math.min(1.0F, t / 0.08F);
        float fadeOut = 1.0F - t;
        this.alpha = this.baseAlpha * fadeIn * (this.kind == GLOW ? fadeOut * fadeOut : fadeOut);
    }

    /** Cresce rápido no começo e devagar no fim, como uma nuvem de verdade. */
    @Override
    public float getQuadSize(float partialTick) {
        float t = Mth.clamp((this.age + partialTick) / (float) this.lifetime, 0.0F, 1.0F);
        float ease = 1.0F - (1.0F - t) * (1.0F - t);
        return this.quadSize * (1.0F + (this.grow - 1.0F) * ease);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        return this.kind == GLOW ? 15728880 : super.getLightColor(partialTick);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final int kind;

        public Provider(SpriteSet sprites, int kind) {
            this.sprites = sprites;
            this.kind = kind;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y,
                                       double z, double vx, double vy, double vz) {
            return new BillowParticle(level, x, y, z, vx, vy, vz, this.sprites, this.kind);
        }
    }
}
