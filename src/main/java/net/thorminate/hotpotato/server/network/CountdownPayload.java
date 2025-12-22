package net.thorminate.hotpotato.server.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import static net.thorminate.hotpotato.HotPotato.MOD_ID;

public record CountdownPayload(int countdown) implements CustomPacketPayload {
    public static final Identifier PACKET_ID = Identifier.fromNamespaceAndPath(MOD_ID, "countdown_payload");

    public static final CustomPacketPayload.Type<CountdownPayload> TYPE =
            new CustomPacketPayload.Type<>(PACKET_ID);

    public static final StreamCodec<ByteBuf, CountdownPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,             // codec for an int
                    CountdownPayload::countdown,   // getter
                    CountdownPayload::new          // constructor
            );

    @Override
    public CustomPacketPayload.@NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
