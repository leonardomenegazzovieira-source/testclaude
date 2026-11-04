package com.saitamamod.client;

import com.saitamamod.SaitamaMod;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Tremor de tela: balança pitch, yaw e roll da câmera, diminuindo até parar. */
@Mod.EventBusSubscriber(modid = SaitamaMod.MODID, value = Dist.CLIENT)
public final class ClientShake {
    private static float intensity;
    private static int total = 1;
    private static int left = 0;

    private ClientShake() {}

    public static void start(float amount, int ticks) {
        if (left > 0 && amount < strength()) {
            return; // já tem um tremor mais forte rolando
        }
        intensity = amount;
        total = Math.max(1, ticks);
        left = total;
    }

    private static float strength() {
        return left <= 0 ? 0.0F : intensity * left / (float) total;
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.END && left > 0 && !Minecraft.getInstance().isPaused()) {
            left--;
        }
    }

    @SubscribeEvent
    public static void onCamera(ViewportEvent.ComputeCameraAngles e) {
        float s = strength();
        if (s <= 0.0F) {
            return;
        }
        e.setPitch(e.getPitch() + (float) (Math.random() * 2 - 1) * s);
        e.setYaw(e.getYaw() + (float) (Math.random() * 2 - 1) * s);
        e.setRoll(e.getRoll() + (float) (Math.random() * 2 - 1) * s * 1.5F);
    }
}
