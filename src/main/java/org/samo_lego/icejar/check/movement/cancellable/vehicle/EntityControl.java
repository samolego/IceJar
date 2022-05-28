package org.samo_lego.icejar.check.movement.cancellable.vehicle;

import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.animal.horse.Llama;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.config.IceConfig;
import org.samo_lego.icejar.mixin.accessor.AMob;

public class EntityControl extends CancellableVehicleMovementCheck {
    public EntityControl(ServerPlayer player) {
        super(CheckType.VEHICLE_MOVE_ENTITY_CONTROL, player);
    }

    @Override
    public boolean checkVehicleMovement(ServerboundMoveVehiclePacket packet, Entity vehicle) {
        if (vehicle instanceof Saddleable sd && !sd.isSaddled() && sd instanceof Mob mob) {
            final float diffY = Math.abs(packet.getYRot() - mob.getYRot()) % 360.0f;

            // We check mob's goals, if they are empty, player shouldn't be controlling it.
            // Mob can still be moved by external sources though (e.g. water, lead, etc.)
            if (mob instanceof Llama || ((AMob) mob).getGoalSelector().getRunningGoals().findAny().isEmpty()) {
                final IceConfig config = IceJar.getInstance().getConfig();
                if (diffY > config.movement.entityControl.diffY) {
                    if (this.trainModeActive()) {
                        config.movement.entityControl.diffY = Math.max(diffY, config.movement.entityControl.diffY);
                        return true;
                    }
                    return false;
                }
            }
        }
        return true;
    }
}
