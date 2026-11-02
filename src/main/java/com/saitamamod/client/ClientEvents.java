package com.saitamamod.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.saitamamod.SaitamaMod;
import com.saitamamod.network.PowerPacket;
import com.saitamamod.network.SaitamaNetwork;
import com.saitamamod.power.PowerManager;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

public final class ClientEvents {
    private static final String CATEGORY = "key.categories.saitamamod";

    public static final KeyMapping SERIOUS = new KeyMapping("key.saitamamod.serious",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, CATEGORY);
    public static final KeyMapping CONSECUTIVE = new KeyMapping("key.saitamamod.consecutive",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, CATEGORY);
    public static final KeyMapping PLANETARY = new KeyMapping("key.saitamamod.planetary",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY);

    private ClientEvents() {}

    @Mod.EventBusSubscriber(modid = SaitamaMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModBus {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(SERIOUS);
            event.register(CONSECUTIVE);
            event.register(PLANETARY);
        }
    }

    @Mod.EventBusSubscriber(modid = SaitamaMod.MODID, value = Dist.CLIENT)
    public static final class ForgeBus {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END || Minecraft.getInstance().player == null) {
                return;
            }
            while (SERIOUS.consumeClick()) {
                send(PowerManager.SERIOUS);
            }
            while (CONSECUTIVE.consumeClick()) {
                send(PowerManager.CONSECUTIVE);
            }
            while (PLANETARY.consumeClick()) {
                send(PowerManager.PLANETARY);
            }
        }

        private static void send(int power) {
            SaitamaNetwork.CHANNEL.sendToServer(new PowerPacket(power));
        }
    }
}
