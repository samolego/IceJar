package org.samo_lego.icejar.check.movement;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.icejar.check.Check;
import org.samo_lego.icejar.check.CheckType;

public abstract class MovementCheck extends Check {

    public MovementCheck(CheckType checkType, ServerPlayer player) {
        super(checkType, player);
    }

    @Override
    public boolean check(Object ... params) {
        if (params.length != 1)
            throw new IllegalArgumentException("MovementCheck.check() requires 1 parameter");
        return this.checkMovement((ServerboundMovePlayerPacket) params[0]);
    }

    public abstract boolean checkMovement(ServerboundMovePlayerPacket packet);
}
