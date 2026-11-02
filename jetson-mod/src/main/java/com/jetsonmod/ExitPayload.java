package com.jetsonmod;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Sent by the client when the pilot presses the exit key. */
public record ExitPayload() implements CustomPacketPayload {
    public static final ExitPayload INSTANCE = new ExitPayload();
    public static final Type<ExitPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(JetsonMod.MODID, "exit"));
    public static final StreamCodec<ByteBuf, ExitPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ExitPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> JetsonEntity.handleExitRequest(context.player()));
    }
}
