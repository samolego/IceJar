package org.samo_lego.icejar.check.movement;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.icejar.check.CheckType;

public class Derp extends MovementCheck {
    public Derp(ServerPlayer player) {
        super(CheckType.MOVEMENT_DERP, player);
    }

    @Override
    public boolean checkMovement(ServerboundMovePlayerPacket packet) {
        final double yRot = Math.abs(packet.getYRot(player.getYRot()));
        boolean derpPacket = Math.abs(packet.getXRot(player.getXRot())) > 90 ||
                yRot >= 360 ||
                yRot == 0;
        if (derpPacket && this.increaseCheatAttempts() > this.getMaxAttemptsBeforeFlag()) {
            return false;
        }
        this.decreaseCheatAttempts();
        return true;
    }
}
