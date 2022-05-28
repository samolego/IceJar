package org.samo_lego.icejar.mixin;

import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.check.movement.MovementCheck;
import org.samo_lego.icejar.check.movement.cancellable.CancellableMovementCheck;
import org.samo_lego.icejar.check.movement.cancellable.CancellableVehicleMovementCheck;
import org.samo_lego.icejar.check.movement.cancellable.Timer;
import org.samo_lego.icejar.util.IceJarPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

import static org.samo_lego.icejar.check.CheckType.CMOVEMENT_TIMER;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin_Movement {
    @Shadow
    public ServerPlayer player;
    @Unique
    private Vec3 lastValidSpot;
    @Unique
    private int ij$validTickCount;
    @Unique
    private Vec2 lastValidRot;

    @Shadow public abstract void teleport(double x, double y, double z, float yaw, float pitch);

    /**
     * Checks the real onGround value of the movement packet.
     *
     * @param packet player movement packet.
     * @param ci   callback info.
     */
    @Inject(method = "handleMovePlayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isPassenger()Z"),
            cancellable = true
    )
    private void onPlayerMove(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        ((IceJarPlayer) player).ij$setMovement(packet);
        ((IceJarPlayer) player).ij$setRotation(packet);
        // Movement check only returns false if Jesus hack is active, while CMovementCheck returns false if any check fails.
        boolean valid = MovementCheck.performCheck(player, packet) && CancellableMovementCheck.performCheck(player, packet);

        if (!valid && !IceJar.getInstance().getConfig().debug) {
            this.ij$validTickCount = 0;
            // Teleport to last spot, just don't keep Y value
            Vec3 last = this.lastValidSpot;
            Vec2 lastRot = this.lastValidRot;

            if (this.lastValidSpot == null || this.lastValidRot == null) {
                this.lastValidSpot = new Vec3(player.getX(), player.getY(), player.getZ());
                this.lastValidRot = new Vec2(player.getYRot(), player.getXRot());
                last = this.lastValidSpot;
                lastRot = this.lastValidRot;
            }

            this.teleport(last.x(), packet.getY(player.getY()), last.z(), lastRot.x, lastRot.y);
            ci.cancel();
        } else {
            if (++this.ij$validTickCount >= 50) {
                this.lastValidSpot = this.player.getPacketCoordinates();
            }
        }
    }

    @Inject(method = "handleMoveVehicle", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"),
            cancellable = true
    )
    private void onVehicleMove(ServerboundMoveVehiclePacket packet, CallbackInfo ci) {
        final Entity vh = this.player.getRootVehicle();
        boolean canMove = CancellableVehicleMovementCheck.performCheck(player, packet, vh);

        if (!canMove && !IceJar.getInstance().getConfig().debug) {
            //todo
            // dismount?
            player.stopRiding();
            vh.teleportTo(vh.getX(), vh.getY(), vh.getZ());
            ci.cancel();
        } else {
            ((IceJarPlayer) player).ij$setVehicleMovement(packet);
        }
    }

    @Inject(method = "teleport(DDDFFLjava/util/Set;Z)V", at = @At(value = "TAIL"))
    private void onTeleport(double d, double e, double f, float g, float h,
                            Set<ClientboundPlayerPositionPacket.RelativeArgument> set, boolean bl, CallbackInfo ci) {
        if (CMOVEMENT_TIMER.isEnabled()) {
            ((IceJarPlayer) player).getCheck(Timer.class).rebalance();
        }
    }
}
