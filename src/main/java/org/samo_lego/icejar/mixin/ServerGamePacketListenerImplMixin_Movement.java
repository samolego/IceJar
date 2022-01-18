package org.samo_lego.icejar.mixin;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.samo_lego.icejar.check.movement.CancellableMovementCheck;
import org.samo_lego.icejar.check.movement.MovementCheck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin_Movement {
    @Shadow
    public ServerPlayer player;

    @Shadow public abstract void teleport(double x, double y, double z, float yaw, float pitch);

    /**
     * Checks the real onGround value of the movement packet.
     *
     * @param packet player movement packet.
     * @param ci   callback info.
     */
    @Inject(
            method = "handleMovePlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;isPassenger()Z"
            ),
            cancellable = true
    )
    private void checkOnGround(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        MovementCheck.performCheck(player, packet);
        boolean canMove = CancellableMovementCheck.performCheck(player, packet);

        if (!canMove) {
            this.teleport(player.getX(), player.getY(), player.getZ(), player.getYHeadRot(), player.getXRot());
            ci.cancel();
        }
    }
}
