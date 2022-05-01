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
 * Checks yaw deltas, if they are too big, it will cancel the movement.
 */
public class WrongRotation extends CancellableMovementCheck {
    private double diff;

    public WrongRotation(ServerPlayer player) {
        super(CheckType.CMOVEMENT_ROTATION, player);
    }

    @Override
    public boolean checkMovement(ServerboundMovePlayerPacket packet) {
        if (packet.hasRotation() && this.ijp.ij$getLast2Movement() != null) {
            final var cfg = IceJar.getInstance().getConfig();

            final float yaw = this.player.getYRot();
            // wrapDegrees returns an angle between -180 and 180
            final float packetYaw = Mth.wrapDegrees(packet.getYRot(yaw));
            // If player turns from yaw -180 to yaw +180, we must account for that as well
            final double yawDiff = Math.min(Math.abs(yaw - packetYaw), Math.abs(yaw + packetYaw));

            if (this.trainModeActive()) {
                cfg.movement.maxRotationDiff = Math.max(cfg.movement.maxRotationDiff, yawDiff);
            } else {
                this.diff = yawDiff;
                return yawDiff <= cfg.movement.maxRotationDiff;
            }
        }
        return true;
    }

    @Override
    public MutableComponent getAdditionalFlagInfo() {
        return new TextComponent("Rotation difference: ")
                .append(new TextComponent(String.format("%.2f", this.diff)).withStyle(ChatFormatting.RED));
    }
}
