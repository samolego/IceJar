package org.samo_lego.icejar.check.movement.cancellable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.util.IceJarPlayer;


public class BoatFly extends CancellableVehicleMovementCheck {
    public BoatFly(ServerPlayer player) {
        super(CheckType.VEHICLE_MOVE_BOATFLY, player);
    }

    @Override
    public boolean checkVehicleMovement(ServerboundMoveVehiclePacket packet, Entity vehicle) {
        final Vec3 lastVM = ((IceJarPlayer) player).getLastVehicleMovement();
        final Vec3 vm = ((IceJarPlayer) player).getVehicleMovement();

        if (IceJar.getInstance().getConfig().trainMode) {
            IceJar.getInstance().getConfig().movement.vehicleYThreshold = Math.max(IceJar.getInstance().getConfig().movement.vehicleYThreshold, vm.y() - lastVM.y());
        }
        if (vm.horizontalDistanceSqr() - lastVM.horizontalDistanceSqr() <= 0.0D ||
            vm.y() - lastVM.y() <= 1E-3 ||
            vehicle.getDeltaMovement().y() <= 0.0D ||
            player.isFallFlying() ||
            player.isCreative() ||
            player.isSpectator() ||
            player.hasEffect(MobEffects.LEVITATION)) {
            return true;
        }

        if (vehicle.getType() == EntityType.BOAT) {
            final BlockState bottom = player.getLevel().getBlockState(new BlockPos(packet.getX(), packet.getY(), packet.getZ()));
            return ((IceJarPlayer) player).isNearGround() || !bottom.getMaterial().isLiquid();
        }
        return true;
    }
}
