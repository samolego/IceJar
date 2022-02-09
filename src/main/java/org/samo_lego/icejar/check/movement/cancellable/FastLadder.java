package org.samo_lego.icejar.check.movement.cancellable;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.config.IceConfig;
import org.samo_lego.icejar.util.IceJarPlayer;

public class FastLadder extends CancellableMovementCheck {
    private boolean wasClimbing;

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
        final double maxUp = cf.movement.ladderSpeedUpMax;
        final double maxDown = cf.movement.ladderSpeedDownMax;

        final Vec3 lastMovement = ((IceJarPlayer) player).ij$getLastMovement();

        if (lastMovement == null) return true;

        final double deltaY = packet.getY(player.getY()) - lastMovement.y;
        System.out.println(deltaY);

        if (cf.trainMode) {
            if (deltaY > maxUp) {
                cf.movement.ladderSpeedUpMax = deltaY;
            } else if (deltaY < maxDown) {
                cf.movement.ladderSpeedDownMax = deltaY;
            }

            return true;
        }

        return !(deltaY > maxUp) && !(deltaY < maxDown);
    }
}