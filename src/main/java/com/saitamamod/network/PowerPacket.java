package com.saitamamod.network;

import com.saitamamod.power.PowerManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Cliente -> servidor: o jogador apertou a tecla de um poder. */
public class PowerPacket {
    private final int power;

    public PowerPacket(int power) {
        this.power = power;
    }

    public static void encode(PowerPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.power);
    }

    public static PowerPacket decode(FriendlyByteBuf buf) {
        return new PowerPacket(buf.readVarInt());
    }

    public static void handle(PowerPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            ServerPlayer player = c.getSender();
            if (player != null) {
                PowerManager.request(player, msg.power);
            }
        });
        c.setPacketHandled(true);
    }
}
