package net.thorminate.hotpotato.client.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static net.thorminate.hotpotato.HotPotato.MOD_ID;

public record RequestCountdownPayload() implements CustomPayload {
    public static final Identifier REQUEST_HOT_POTATO_PACKET_ID = Identifier.of(MOD_ID, "request_hot_potato_data_packet");

    public static final RequestCountdownPayload INSTANCE = new RequestCountdownPayload();

    public static final PacketCodec<RegistryByteBuf, RequestCountdownPayload> CODEC = PacketCodec.unit(INSTANCE);

    public static final CustomPayload.Id<RequestCountdownPayload> ID = new CustomPayload.Id<>(REQUEST_HOT_POTATO_PACKET_ID);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
