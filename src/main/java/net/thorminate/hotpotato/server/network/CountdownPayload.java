package net.thorminate.hotpotato.server.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static net.thorminate.hotpotato.HotPotato.MOD_ID;

public record CountdownPayload(int countdown) implements CustomPayload {
    public static final Identifier HOT_POTATO_PACKET_ID = Identifier.of(MOD_ID, "hot_potato_data_packet");
    public static final PacketCodec<RegistryByteBuf, CountdownPayload> CODEC = PacketCodec.of(CountdownPayload::write, CountdownPayload::read);

    public static void write(CountdownPayload payload, RegistryByteBuf buf) {
        buf.writeInt(payload.countdown);
    }

    public static CountdownPayload read(RegistryByteBuf buf) {
        int countdown = buf.readInt();
        return new CountdownPayload(countdown);
    }

    public static final CustomPayload.Id<CountdownPayload> ID = new CustomPayload.Id<>(HOT_POTATO_PACKET_ID);

    @Override
    public Id<CountdownPayload> getId() {
        return ID;
    }
}
