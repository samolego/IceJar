package org.samo_lego.icejar.check.movement.cancellable.vehicle;

import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.check.movement.NoFall;
import org.samo_lego.icejar.util.IceJarPlayer;

/**
 * Can still be improved, as this triggers false positive using e.g. slime block launcher.
 */
public class BoatFly extends CancellableVehicleMovementCheck {
    public BoatFly(ServerPlayer player) {
        super(CheckType.VEHICLE_MOVE_BOATFLY, player);
    }

    @Override
    public boolean checkVehicleMovement(ServerboundMoveVehiclePacket packet, Entity vehicle) {
        final Vec3 lastVM = ((IceJarPlayer) player).ij$getLastVehicleMovement();
        final Vec3 vm = ((IceJarPlayer) player).ij$getVehicleMovement();

        if (lastVM == null || vm == null)
            return true;

        if (this.trainModeActive()) {
            IceJar.getInstance().getConfig().movement.vehicleYThreshold = Math.max(IceJar.getInstance().getConfig().movement.vehicleYThreshold, vm.y() - lastVM.y());
        }
        Vec3 nMove = new Vec3(packet.getX(), packet.getY(), packet.getZ());
        final double pDeltaY = vm.y() - lastVM.y();
        final double deltaY = nMove.y() - vm.y();

        if (/*vm.horizontalDistanceSqr() - lastVM.horizontalDistanceSqr() <= 0.0D ||*/
            /*pDeltaY <= 1E-3 ||*/
            ((pDeltaY < deltaY ||
                pDeltaY < 0.0D ||
                deltaY < 0.0D) &&
                pDeltaY != deltaY) ||
            player.isFallFlying()) {
            return true;
        }

        if (vehicle.getType() == EntityType.BOAT) {
            final AABB bBox = vehicle.getBoundingBox().expandTowards(0.0D, -(pDeltaY + 0.25D), 0.0D);

            return NoFall.checkOnGround(vehicle, packet.getY() - vehicle.getY(), false) ||
                    vehicle.getLevel().containsAnyLiquid(bBox) ||
                    pDeltaY < deltaY;
        }
        return true;
    }
}
