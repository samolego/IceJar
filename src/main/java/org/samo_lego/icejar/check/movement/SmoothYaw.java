package org.samo_lego.icejar.check.movement;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.check.CheckType;

public class SmoothYaw extends MovementCheck {

    private double diff;

    public SmoothYaw(ServerPlayer player) {
        super(CheckType.MOVEMENT_SMOOTH_ROTATION, player);
    }

    @Override
    public boolean checkMovement(ServerboundMovePlayerPacket packet) {
        if (packet.hasRotation() && this.ijp.ij$getLast2Rotation() != null) {
            final var cfg = IceJar.getInstance().getConfig();

            final float yaw = this.player.getYRot();
            final float yawDiff = Math.abs(yaw - Mth.wrapDegrees(packet.getYRot(yaw)));

            final double delta = this.ijp.ij$getRotation().add(this.ijp.ij$getLastRotation().negated()).lengthSquared();
            final double deltaPrev = this.ijp.ij$getLastRotation().add(this.ijp.ij$getLast2Rotation().negated()).lengthSquared();

            //final double prv = this.diff;
            this.diff = Math.abs(delta - deltaPrev);

            //this.diff = Math.abs(player.getYHeadRot() - prv);

            if (cfg.trainMode) {
                cfg.movement.maxYawChange = (float) Math.max(cfg.movement.maxYawChange, this.diff);
            } else {
                return this.diff <= cfg.movement.maxYawChange;
            }
        }
        return true;
    }


    @Override
    public MutableComponent getAdditionalFlagInfo() {
        return new TranslatableComponent("Rotation: ")
                .append(new TextComponent(String.format("%.2f", this.diff)).withStyle(ChatFormatting.RED));
    }

}
