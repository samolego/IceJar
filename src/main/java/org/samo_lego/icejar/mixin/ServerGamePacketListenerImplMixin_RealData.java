package org.samo_lego.icejar.mixin;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.mixin.accessor.AServerboundMovePlayerPacket;
import org.samo_lego.icejar.util.IceJarPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin_RealData {
    @Shadow
    public ServerPlayer player;

    /**
     * Checks the real onGround value of the movement packet.
     *
     * @param packet
     * @param ci
     */
    @Inject(
            method = "handleMovePlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;isPassenger()Z"
            )
    )
    private void checkOnGround(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        if (packet.isOnGround() && player.fallDistance > 0.0f) {
            final IceJarPlayer ijPlayer = (IceJarPlayer) this.player;

            ((AServerboundMovePlayerPacket) packet).setOnGround(false);

            if(!(ijPlayer).isNearGround()) {
                // Player isn't on ground but client packet says it is
                ijPlayer.getAdditionalData().setOnGround(false);
                // Flag the player
                ijPlayer.flag(CheckType.MOVEMENT_NOFALL);
            }
            ijPlayer.getAdditionalData().updateGroundStatus();
        }
    }
}
