package org.samo_lego.icejar.check.movement.cancellable;

import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Saddleable;
import org.samo_lego.icejar.check.CheckType;

// not working, just idea
public class ForceSaddle extends CancellableVehicleMovementCheck {
    public ForceSaddle(ServerPlayer player) {
        super(CheckType.VEHICLE_MOVE_FORCE_SADDLE, player);
    }

    @Override
    public boolean checkVehicleMovement(ServerboundMoveVehiclePacket packet, Entity vehicle) {
        if (vehicle instanceof PathfinderMob mob && vehicle instanceof Saddleable saddleable && !saddleable.isSaddled()) {
            boolean pathFinding = mob.isPathFinding();
            return !pathFinding;
        }
        return true;
    }
}
