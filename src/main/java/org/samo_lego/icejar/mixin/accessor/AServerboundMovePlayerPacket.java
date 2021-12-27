package org.samo_lego.icejar.mixin.accessor;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerboundMovePlayerPacket.class)
public interface AServerboundMovePlayerPacket {
    @Accessor("onGround")
    void setOnGround(boolean onGround);
}
