package org.samo_lego.icejar.check.movement.cancellable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.check.CheckType;


/**
 * Checks for invalid deceleration of speed.
 * Doesn't catch strafe.
 * Inspired by https://www.youtube.com/watch?v=-SiqszHE9rQ
 */
public class DirectionSpeed extends CancellableMovementCheck {

    private double diff;

    public DirectionSpeed(ServerPlayer player) {
        super(CheckType.CMOVEMENT_DIRECTION_SPEED, player);
    }

    @Override
    public boolean checkMovement(ServerboundMovePlayerPacket packet) {
        if (packet.hasRotation()) {
            final var cfg = IceJar.getInstance().getConfig();

            final float yaw = this.player.getYRot();
            final double yawDiff = Math.abs(yaw - Mth.wrapDegrees(packet.getYRot(yaw)));

            // Activate only if yaw difference is bigger than config
            if (yawDiff > cfg.movement.speed.minYawDifference) {
                final double xzDelta = this.ijp.ij$getMovement().subtract(this.ijp.ij$getLastMovement()).horizontalDistance();

                // Has player moved enough?
                if (xzDelta > 0.15D) {
                    final double xzDeltaPrev = this.ijp.ij$getLastMovement().subtract(this.ijp.ij$getLast2Movement()).horizontalDistance();
                    this.diff = Math.abs(xzDelta - xzDeltaPrev);

                    if (cfg.trainMode) {
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
        return new TextComponent("Movement difference while rotating: ")
                .append(new TextComponent(String.format("%.4f", this.diff)).withStyle(ChatFormatting.RED));
    }
}
