package com.saitamamod.network;

import com.saitamamod.client.ClientShake;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Servidor -> cliente: treme a tela. */
public class ShakePacket {
    private final float intensity;
    private final int ticks;

    public ShakePacket(float intensity, int ticks) {
        this.intensity = intensity;
        this.ticks = ticks;
    }

    public static void encode(ShakePacket msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.intensity);
        buf.writeVarInt(msg.ticks);
    }

    public static ShakePacket decode(FriendlyByteBuf buf) {
        return new ShakePacket(buf.readFloat(), buf.readVarInt());
    }

    public static void handle(ShakePacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientShake.start(msg.intensity, msg.ticks)));
        c.setPacketHandled(true);
    }
}
