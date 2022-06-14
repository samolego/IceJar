package org.samo_lego.icejar.check.movement.cancellable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.config.IceConfig;
import org.samo_lego.icejar.util.IceJarPlayer;

public class FastLadder extends CancellableMovementCheck {
    private boolean wasClimbing;
    private double deltaY;

    public FastLadder(ServerPlayer player) {
        super(CheckType.CMOVEMENT_FAST_LADDER, player);
    }

    @Override
    public boolean checkMovement(ServerboundMovePlayerPacket packet) {

        if (!player.onClimbable() || ((IceJarPlayer) player).ij$aboveLiquid() || player.isFallFlying()) {
            this.wasClimbing = false;
            return true;
        } else if (!this.wasClimbing) {
            this.wasClimbing = true;
            return true;
        }

        final IceConfig cf = IceJar.getInstance().getConfig();
        final double maxUp = cf.movement.ladder.speedUpMax;
        final double maxDown = cf.movement.ladder.speedDownMax;

        final Vec3 lastMovement = ((IceJarPlayer) player).ij$getLastMovement();

        if (lastMovement == null) return true;

        this.deltaY = packet.getY(player.getY()) - lastMovement.y;

        if (this.trainModeActive()) {
            if (deltaY > maxUp) {
                cf.movement.ladder.speedUpMax = deltaY;
            } else if (deltaY < maxDown) {
                cf.movement.ladder.speedDownMax = deltaY;
            }

            return true;
        }

        return !(deltaY > maxUp) && !(deltaY < maxDown);
    }

    @Override
    public MutableComponent getAdditionalFlagInfo() {
        return Component.literal("Direction: ")
                .append(Component.translatable("gui." + (this.deltaY < 0 ? "down" : "up"))
                        .withStyle(ChatFormatting.GREEN))
                .append("\nMovement delta: ")
                .append(Component.literal(String.format("%.2f", deltaY)).withStyle(ChatFormatting.RED));
    }
}
