package org.samo_lego.icejar.check.movement.cancellable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.check.CheckType;


/**
 * Checks for invalid deceleration of speed while rotating.
 * Doesn't catch strafe.
 * Inspired by <a href="https://www.youtube.com/watch?v=-SiqszHE9rQ">https://www.youtube.com/watch?v=-SiqszHE9rQ</a>
 */
public class NoDeceleration extends CancellableMovementCheck {

    private double diff;

    public NoDeceleration(ServerPlayer player) {
        super(CheckType.CMOVEMENT_NO_DECELERATION, player);
    }

    @Override
    public boolean checkMovement(ServerboundMovePlayerPacket packet) {
        if (packet.hasRotation() && this.ijp.ij$getLast2Movement() != null && !this.player.isPassenger() && !player.isFallFlying()) {
            final var cfg = IceJar.getInstance().getConfig();

            final float yaw = this.player.getYRot();
            // wrapDegrees returns an angle between -180 and 180
            final float packetYaw = Mth.wrapDegrees(packet.getYRot(yaw));
            final double yawDiff = Math.abs(yaw - packetYaw);

            // Activate only if yaw difference is bigger than config
            if (yawDiff > cfg.movement.speed.minYawDifference) {
                final double xzDelta = this.ijp.ij$getMovement().subtract(this.ijp.ij$getLastMovement()).horizontalDistance();

                // Has player moved enough?
                if (xzDelta > 0.15D) {
                    final double xzDeltaPrev = this.ijp.ij$getLastMovement().subtract(this.ijp.ij$getLast2Movement()).horizontalDistance();
                    this.diff = Math.abs(xzDelta - xzDeltaPrev);

                    if (this.trainModeActive()) {
                        cfg.movement.speed.minDeceleration = Math.min(cfg.movement.speed.minDeceleration, diff);
                    } else {
                        return this.diff >= cfg.movement.speed.minDeceleration;
                    }
                }
            }
        }

        return true;
    }


    @Override
    public MutableComponent getAdditionalFlagInfo() {
        return Component.literal("Deceleration while rotating: ")
                .append(Component.literal(String.format("x * 10^-%d", (int) Math.log10(1/(this.diff)))).withStyle(ChatFormatting.RED));
    }
}
