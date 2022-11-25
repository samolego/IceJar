package org.samo_lego.icejar.mixin.accessor;

import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundLevelChunkWithLightPacket.class)
public interface AClientboundLevelChunkWithLightPacket {
    @Mutable
    @Accessor("chunkData")
    void setChunkData(ClientboundLevelChunkPacketData chunkData);
}
