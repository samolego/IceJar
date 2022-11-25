package org.samo_lego.icejar.mixin.accessor;

import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundSectionBlocksUpdatePacket.class)
public interface AClientboundSectionBlocksUpdatePacket {
    @Accessor("sectionPos")
    SectionPos sectionPos();
}
