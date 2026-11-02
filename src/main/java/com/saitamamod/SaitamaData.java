package com.saitamamod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/** Progresso do treino, guardado no NBT persistente do jogador. */
public final class SaitamaData {
    private static final String ROOT = "saitamamod";

    public static final String PUSHUPS = "pushups";
    public static final String SITUPS = "situps";
    public static final String SQUATS = "squats";
    public static final String RUN = "run";
    public static final String AWAKENED = "saitama";

    private SaitamaData() {}

    private static CompoundTag tag(Player p) {
        CompoundTag data = p.getPersistentData();
        if (!data.contains(ROOT, 10)) {
            data.put(ROOT, new CompoundTag());
        }
        return data.getCompound(ROOT);
    }

    public static double get(Player p, String key) {
        return tag(p).getDouble(key);
    }

    public static int getInt(Player p, String key) {
        return (int) get(p, key);
    }

    /** Soma ao contador sem passar do alvo e devolve o novo valor. */
    public static double add(Player p, String key, double amount, double cap) {
        CompoundTag t = tag(p);
        double v = Math.min(cap, t.getDouble(key) + amount);
        t.putDouble(key, v);
        return v;
    }

    public static boolean isSaitama(Player p) {
        return tag(p).getBoolean(AWAKENED);
    }

    public static void setSaitama(Player p, boolean value) {
        tag(p).putBoolean(AWAKENED, value);
    }

    public static boolean trainingDone(Player p) {
        return getInt(p, PUSHUPS) >= SaitamaConfig.PUSHUPS.get()
                && getInt(p, SITUPS) >= SaitamaConfig.SITUPS.get()
                && getInt(p, SQUATS) >= SaitamaConfig.SQUATS.get()
                && get(p, RUN) >= SaitamaConfig.RUN_METERS.get();
    }

    public static void reset(Player p) {
        p.getPersistentData().put(ROOT, new CompoundTag());
    }

    /** Mantém o progresso depois de morrer. */
    public static void copy(Player from, Player to) {
        CompoundTag old = from.getPersistentData();
        if (old.contains(ROOT, 10)) {
            to.getPersistentData().put(ROOT, old.getCompound(ROOT).copy());
        }
    }
}
