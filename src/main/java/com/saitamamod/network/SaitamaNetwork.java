package com.saitamamod.network;

import com.saitamamod.SaitamaMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class SaitamaNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SaitamaMod.MODID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals);

    private SaitamaNetwork() {}

    public static void register() {
        CHANNEL.registerMessage(0, PowerPacket.class,
                PowerPacket::encode, PowerPacket::decode, PowerPacket::handle);
    }
}
