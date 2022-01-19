package org.samo_lego.icejar.check.movement;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.mixin.accessor.AServerboundMovePlayerPacket;
import org.samo_lego.icejar.util.IceJarPlayer;

import java.util.List;

public class NoFall extends MovementCheck {

    private boolean skipDamageEvent;
    private boolean hasFallen;

    public NoFall(ServerPlayer player) {
        super(CheckType.MOVEMENT_NOFALL, player);
        this.skipDamageEvent = false;
    }

    @Override
    public boolean checkMovement(ServerboundMovePlayerPacket packet) {
        if (packet.isOnGround()) {
            IceJarPlayer ij = (IceJarPlayer) this.player;
            ij.updateGroundStatus();

            // Get bottom vehicle bounding box.
            // Todo: fp when standing on boat and falling
            Entity bottomEntity = this.player.getRootVehicle();
            if (bottomEntity == null) {
                bottomEntity = this.player;
            }
            final AABB bBox = bottomEntity
                    .getBoundingBox()
                    .inflate(0, 0.25005D, 0).move(0, packet.getY(this.player.getY()) - this.player.getY() - 0.25005D, 0);

            final Iterable<VoxelShape> collidingBlocks = this.player.getLevel().getBlockCollisions(bottomEntity, bBox);

            if (collidingBlocks.iterator().hasNext()) {
                // Preferring block collisions over entity ones
                ij.ij$setOnGround(true);
            } else {
                // No block collisions found, check for entity collisions
                Entity finalBottomEntity = bottomEntity;
                List<Entity> collidingEntities = this.player.getLevel().getEntities(bottomEntity, bBox, entity -> !finalBottomEntity.equals(entity));

                final boolean noCollisions = collidingEntities.isEmpty();
                ij.ij$setOnGround(!noCollisions);
            }

            // Player isn't on ground but client packet says it is
            if (!ij.isNearGround()) {
                ((AServerboundMovePlayerPacket) packet).setOnGround(false);

                int max2 = this.getMaxAttemptsBeforeFlag() * 2;
                if (this.increaseCheatAttempts() > max2) {
                    // we can use this later to lie to player's client
                    this.setCheatAttempts(max2);
                }
                return false;
            } else {
                this.decreaseCheatAttempts();
            }
        }

        return true;
    }


    public void setHasFallen(boolean hasFallen) {
        this.hasFallen = hasFallen;
    }

    public boolean hasFallen() {
        return this.hasFallen;
    }

    public boolean hasNoFall() {
        return this.getCheatAttempts() > this.getMaxAttemptsBeforeFlag();
    }

    public boolean shouldSkipDamageEvent() {
        return this.skipDamageEvent;
    }

    public void setSkipDamageEvent(boolean skipDamageEvent) {
        this.skipDamageEvent = skipDamageEvent;
    }
}
