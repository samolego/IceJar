package org.samo_lego.icejar.check.movement;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.mixin.accessor.AServerboundMovePlayerPacket;
import org.samo_lego.icejar.util.AdditionalData;
import org.samo_lego.icejar.util.IceJarPlayer;

import java.util.List;

public class NoFall extends MovementCheck {

    private int hasNoFallChance = 0;

    public NoFall(ServerPlayer player) {
        super(CheckType.MOVEMENT_NOFALL, player);
    }

    public boolean hasNoFall() {
        System.out.println(this.hasNoFallChance);
        return this.hasNoFallChance > 5;
    }

    @Override
    public boolean checkMovement(ServerboundMovePlayerPacket packet) {
        if (packet.isOnGround()) {
            final IceJarPlayer ijPlayer = (IceJarPlayer) this.player;
            final AdditionalData data = ijPlayer.getAdditionalData();

            data.updateGroundStatus();

            // Get bottom vehicle bounding box.
            Entity bottomEntity = this.player.getRootVehicle();
            if (bottomEntity == null) {
                bottomEntity = player;
            }
            final AABB bBox = bottomEntity
                    .getBoundingBox()
                    .inflate(0, 0.25005D, 0).move(0, packet.getY(player.getY()) - player.getY() - 0.25005D, 0);

            final Iterable<VoxelShape> collidingBlocks = player.getLevel().getBlockCollisions(bottomEntity, bBox);

            if (collidingBlocks.iterator().hasNext()) {
                // Preferring block collisions over entity ones
                data.setOnGround(true);
            } else {
                // No block collisions found, check for entity collisions
                Entity finalBottomEntity = bottomEntity;
                List<Entity> collidingEntities = player.getLevel().getEntities(bottomEntity, bBox, entity -> !finalBottomEntity.equals(entity));

                boolean noCollisions = collidingEntities.isEmpty();
                data.setOnGround(!noCollisions);

                if (noCollisions) {
                    final double fallDistance = packet.getY(player.getY()) - player.getY();
                    data.setFallDistance(fallDistance);
                }
            }

            // Player isn't on ground but client packet says it is
            if (!ijPlayer.isNearGround()) {
                data.setOnGround(false);
                ((AServerboundMovePlayerPacket) packet).setOnGround(false);
                // Flag the player
                this.flag();

                // we can use this later to lie to player's client
                if (++this.hasNoFallChance > 10) {
                    this.hasNoFallChance = 10;
                }
            }
        } else {
            if (--this.hasNoFallChance < 0) {
                this.hasNoFallChance = 0;
            }
            System.out.println(this.hasNoFallChance);
        }

        return true;
    }
}
