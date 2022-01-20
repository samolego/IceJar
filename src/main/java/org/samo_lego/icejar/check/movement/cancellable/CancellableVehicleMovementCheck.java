package org.samo_lego.icejar.check.movement.cancellable;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.samo_lego.icejar.check.Check;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.util.IceJarPlayer;

import java.util.Set;

import static org.samo_lego.icejar.check.CheckCategory.VEHICLE_MOVEMENT;
import static org.samo_lego.icejar.check.CheckCategory.category2checks;

public abstract class CancellableVehicleMovementCheck extends Check {

    public CancellableVehicleMovementCheck(CheckType checkType, ServerPlayer player) {
        super(checkType, player);
    }

    @Override
    public boolean check(Object ... params) {
        if (params.length != 2)
            throw new IllegalArgumentException("CancellableVehicleMovementCheck.check() requires 2 parameters");
        return this.checkVehicleMovement((ServerboundMoveVehiclePacket) params[0], (Entity) params[1]);
    }


    /**
     * Checks whether player has moved correctly.
     * @param player player to check.
     * @param packet packet containing movement data.
     * @param vehicle vehicle that player is moving.
     * @return whether player has moved correctly.
     */
    public static boolean performCheck(ServerPlayer player, ServerboundMoveVehiclePacket packet, Entity vehicle) {
        // Loop through all movement checks
        final Set<CheckType> checks = category2checks.get(VEHICLE_MOVEMENT);
        if (checks != null) {
            for (CheckType type : checks) {
                if (Permissions.check(player, type.getBypassPermission(), false)) continue;

                final CancellableVehicleMovementCheck check = ((IceJarPlayer) player).getCheck(type);

                // Check movement
                if (!check.checkVehicleMovement(packet, vehicle)) {
                    if (check.increaseCheatAttempts() > check.getMaxAttemptsBeforeFlag())
                        check.flag();

                    // Ruberband
                    return false;
                } else {
                    check.decreaseCheatAttempts();
                }
            }
        }
        return true;
    }

    public abstract boolean checkVehicleMovement(ServerboundMoveVehiclePacket packet, Entity vehicle);
}
