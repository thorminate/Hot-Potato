package net.thorminate.hotpotato.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import static net.thorminate.hotpotato.HotPotato.MOD_ID;

import java.util.UUID;

public record HotPotatoPayload(UUID player, int countdown) implements CustomPayload {
    public static final Identifier HOT_POTATO_PACKET_ID = Identifier.of(MOD_ID, "hot_potato_data_packet");
    public static final PacketCodec<RegistryByteBuf, HotPotatoPayload> CODEC = PacketCodec.of(
            (payload, buf) -> {
                // Write a boolean flag to indicate whether UUID is present
                buf.writeBoolean(payload.player != null);
                if (payload.player != null) {
                    Uuids.PACKET_CODEC.encode(buf, payload.player);
                }
                buf.writeInt(payload.countdown);
            },
            buf -> {
                // Read the boolean flag to check if UUID is present
                UUID player = buf.readBoolean() ? Uuids.PACKET_CODEC.decode(buf) : null;
                int countdown = buf.readInt();
                return new HotPotatoPayload(player, countdown);
            }
    );
    public static final CustomPayload.Id<HotPotatoPayload> ID = new CustomPayload.Id<>(HOT_POTATO_PACKET_ID);

    @Override
    public Id<HotPotatoPayload> getId() {
        return ID;
    }
}
