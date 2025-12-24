package net.thorminate.hotpotato.client.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import static net.thorminate.hotpotato.HotPotato.MOD_ID;

public record RequestCountdownPayload() implements CustomPacketPayload {
    public static final Identifier PACKET_ID =
            Identifier.fromNamespaceAndPath(MOD_ID, "request_hot_potato_data_packet");

    public static final CustomPacketPayload.Type<RequestCountdownPayload> TYPE =
            new CustomPacketPayload.Type<>(PACKET_ID);

    // A codec for an empty packet: use the ZERO codec
    public static final StreamCodec<ByteBuf, RequestCountdownPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    buf -> 0,
                    (dummy) -> new RequestCountdownPayload()
            );

    @Override
    public CustomPacketPayload.@NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}