package org.samo_lego.icejar.check.movement.cancellable;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.check.CheckType;

public class PacketMovement extends CancellableMovementCheck {
    public PacketMovement(ServerPlayer player) {
        super(CheckType.CMOVEMENT_PACKET, player);
    }

    @Override
    public boolean checkMovement(ServerboundMovePlayerPacket packet) {
        final double x = packet.getX(player.getX());
        final double y = packet.getY(player.getY());
        final double z = packet.getZ(player.getZ());

        final Vec3 previous = player.getPosition(1);

        // Create location from new data
        final Vec3 current = new Vec3(x, y, z);
        // Only take horizontal distance
        final double distanceHorizontal = previous.horizontalDistanceSqr() - current.horizontalDistanceSqr();
        System.out.println("Hor : " + distanceHorizontal);
        final double distanceVertical = previous.y - current.y;
        System.out.println("Ver: " + distanceVertical);
        final double maxDistanceHorizontal = IceJar.getInstance().getConfig().movement.maxHorizontalDistance;

        // Get data (make sure to not use a hacked client while training)
        if (IceJar.getInstance().getConfig().trainMode) {
            IceJar.getInstance().getConfig().movement.maxHorizontalDistance = Math.max(maxDistanceHorizontal, distanceHorizontal);
        } else if (Math.abs(distanceHorizontal) > maxDistanceHorizontal) {
            return false;
        }

        return distanceVertical >= -4.0D;
    }
}
