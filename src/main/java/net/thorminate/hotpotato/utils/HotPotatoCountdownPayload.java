package net.thorminate.hotpotato.utils;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.thorminate.hotpotato.HotPotato;

public record HotPotatoCountdownPayload(int countdown) implements CustomPayload {
    public static final Id<HotPotatoCountdownPayload> ID = new Id<>(Identifier.of(HotPotato.MOD_ID, "hot_potato_timer"));

    public HotPotatoCountdownPayload(PacketByteBuf buf) {
        this(buf.readInt());
    }

    public void write(PacketByteBuf buf) {
        buf.writeInt(countdown);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}