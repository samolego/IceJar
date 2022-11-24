package org.samo_lego.icejar.check.movement.cancellable;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.icejar.casts.IceJarPlayer;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.check.movement.MovementCheck;

import java.util.Set;

import static org.samo_lego.icejar.check.CheckCategory.MOVEMENT_MUTABLE;
import static org.samo_lego.icejar.check.CheckCategory.category2checks;

public abstract class CancellableMovementCheck extends MovementCheck {

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
     * @param player player to check.
     * @param packet packet containing movement data.
     * @return whether player has moved correctly.
     */
    public static boolean performCheck(ServerPlayer player, ServerboundMovePlayerPacket packet) {
        // Loop through all movement checks
        final Set<CheckType> checks = category2checks.get(MOVEMENT_MUTABLE);
        if (checks != null) {
            for (CheckType type : checks) {
                if (Permissions.check(player, type.getBypassPermission(), false)) continue;

                final CancellableMovementCheck check = ((IceJarPlayer) player).getCheck(type);

                // Check movement
                if (!check.checkMovement(packet)) {
                    if (check.increaseCheatAttempts() > check.getMaxAttemptsBeforeFlag())
                        check.flag();

                    // Ruberband
                    return false;
                }
            }
        }
        return true;
    }
}
