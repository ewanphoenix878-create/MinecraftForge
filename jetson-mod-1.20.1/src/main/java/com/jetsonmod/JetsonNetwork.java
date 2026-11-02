package com.jetsonmod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public final class JetsonNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(JetsonMod.MODID, "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private JetsonNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(0, ExitMessage.class, (msg, buf) -> { }, buf -> new ExitMessage(), ExitMessage::handle);
    }

    /** Sent by the client when the pilot presses the exit key. */
    public static final class ExitMessage {
        private static void handle(ExitMessage msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    JetsonEntity.handleExitRequest(player);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
