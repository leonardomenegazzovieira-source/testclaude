package com.saitamamod;

import net.minecraftforge.common.ForgeConfigSpec;

public final class SaitamaConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue PUSHUPS;
    public static final ForgeConfigSpec.IntValue SITUPS;
    public static final ForgeConfigSpec.IntValue SQUATS;
    public static final ForgeConfigSpec.IntValue RUN_METERS;

    public static final ForgeConfigSpec.IntValue SERIOUS_CHARGE_TICKS;
    public static final ForgeConfigSpec.IntValue SERIOUS_LENGTH;
    public static final ForgeConfigSpec.IntValue SERIOUS_RADIUS;

    public static final ForgeConfigSpec.IntValue CONSECUTIVE_DURATION_TICKS;
    public static final ForgeConfigSpec.IntValue CONSECUTIVE_LENGTH;
    public static final ForgeConfigSpec.IntValue CONSECUTIVE_RADIUS;

    public static final ForgeConfigSpec.IntValue PLANETARY_CHARGE_TICKS;
    public static final ForgeConfigSpec.IntValue PLANETARY_MAX_RADIUS;
    public static final ForgeConfigSpec.IntValue PLANETARY_COLUMNS_PER_TICK;

    public static final ForgeConfigSpec.IntValue SLICES_PER_TICK;
    public static final ForgeConfigSpec.BooleanValue HURT_OTHER_PLAYERS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_PLANETARY;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("training");
        PUSHUPS = b.comment("Flexões (socos no ar)").defineInRange("pushups", 100, 1, 100000);
        SITUPS = b.comment("Abdominais (pulos)").defineInRange("situps", 100, 1, 100000);
        SQUATS = b.comment("Agachamentos (agachar e levantar)").defineInRange("squats", 100, 1, 100000);
        RUN_METERS = b.comment("Corrida em metros (sprint)").defineInRange("runMeters", 10000, 1, 10000000);
        b.pop();

        b.push("serious_punch");
        SERIOUS_CHARGE_TICKS = b.defineInRange("chargeTicks", 60, 0, 1200);
        SERIOUS_LENGTH = b.defineInRange("length", 300, 8, 2000);
        SERIOUS_RADIUS = b.defineInRange("radius", 24, 1, 128);
        b.pop();

        b.push("consecutive_punches");
        CONSECUTIVE_DURATION_TICKS = b.defineInRange("durationTicks", 80, 1, 1200);
        CONSECUTIVE_LENGTH = b.defineInRange("length", 80, 8, 1000);
        CONSECUTIVE_RADIUS = b.defineInRange("radius", 8, 1, 64);
        b.pop();

        b.push("planetary_punch");
        ENABLE_PLANETARY = b.comment("Desligue para impedir a destruição em massa do mundo")
                .define("enabled", true);
        PLANETARY_CHARGE_TICKS = b.defineInRange("chargeTicks", 100, 0, 2400);
        PLANETARY_MAX_RADIUS = b.defineInRange("maxRadius", 256, 16, 4096);
        PLANETARY_COLUMNS_PER_TICK = b.comment("Colunas destruídas por tick. Menor = menos lag, onda mais lenta")
                .defineInRange("columnsPerTick", 150, 10, 5000);
        b.pop();

        b.push("general");
        SLICES_PER_TICK = b.comment("Fatias do corredor destruídas por tick (soco sério e consecutivos)")
                .defineInRange("slicesPerTick", 3, 1, 50);
        HURT_OTHER_PLAYERS = b.comment("Os socos matam outros jogadores?").define("hurtOtherPlayers", true);
        b.pop();

        SPEC = b.build();
    }

    private SaitamaConfig() {}
}
