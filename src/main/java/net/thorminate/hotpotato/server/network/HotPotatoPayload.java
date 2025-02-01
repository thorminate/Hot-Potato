package net.thorminate.hotpotato.server.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static net.thorminate.hotpotato.HotPotato.MOD_ID;

public record HotPotatoPayload(int countdown) implements CustomPayload {
    public static final Identifier HOT_POTATO_PACKET_ID = Identifier.of(MOD_ID, "hot_potato_data_packet");
    public static final PacketCodec<RegistryByteBuf, HotPotatoPayload> CODEC = PacketCodec.of(HotPotatoPayload::write, HotPotatoPayload::read);

    public static void write(HotPotatoPayload payload, RegistryByteBuf buf) {
        buf.writeInt(payload.countdown);
    }

    public static HotPotatoPayload read(RegistryByteBuf buf) {
        int countdown = buf.readInt();
        return new HotPotatoPayload(countdown);
    }

    public static final CustomPayload.Id<HotPotatoPayload> ID = new CustomPayload.Id<>(HOT_POTATO_PACKET_ID);

    @Override
    public Id<HotPotatoPayload> getId() {
        return ID;
    }
}
