package org.samo_lego.icejar.mixin.packet;

import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.check.combat.NoSwing;
import org.samo_lego.icejar.util.IceJarPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    @Shadow public ServerPlayer player;

    @Inject(method = "handleAnimate", at = @At("TAIL"))
    private void onHandSwing(ServerboundSwingPacket packet, CallbackInfo ci) {
        ((NoSwing) ((IceJarPlayer) this.player).getCheck(CheckType.COMBAT_NOSWING)).onSwing();
    }
}
