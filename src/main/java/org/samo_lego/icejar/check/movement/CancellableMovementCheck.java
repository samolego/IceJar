package org.samo_lego.icejar.check.movement;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.util.IceJarPlayer;

import java.util.Iterator;
import java.util.Set;

public abstract class CancellableMovementCheck extends MovementCheck {

    public static final Set<CheckType> movementChecks = Set.of();

    public CancellableMovementCheck(CheckType checkType, ServerPlayer player) {
        super(checkType, player);
    }

    @Override
    public boolean check(Object ... params) {
        if (params.length != 1)
            throw new IllegalArgumentException("MovementCheck.check() requires 1 parameter");
        return this.checkMovement((ServerboundMovePlayerPacket) params[0]);
    }

    /**
     * Checks whether player has moved correctly.
     * @param packet packet containing movement data.
     * @return whether player has moved correctly.
     */
    public boolean checkMovement(ServerboundMovePlayerPacket packet) {
        boolean valid = true;

        // Loop through all movement checks
        Iterator<CheckType> it = movementChecks.iterator();
        while (it.hasNext() && valid) {
            final CancellableMovementCheck check = (CancellableMovementCheck) ((IceJarPlayer) player).getCheck(it.next());

            // Check movement
            valid = check.checkMovement(packet);
        }

        if (!valid) {
            // Ruberband
            this.flag();
            return false;
        }
        return true;

    }
}
