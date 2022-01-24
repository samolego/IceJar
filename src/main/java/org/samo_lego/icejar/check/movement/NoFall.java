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
    private boolean hasJesus;

    public NoFall(ServerPlayer player) {
        super(CheckType.MOVEMENT_NOFALL, player);
        this.skipDamageEvent = false;
    }

    @Override
    public boolean checkMovement(ServerboundMovePlayerPacket packet) {
        if (packet.isOnGround()) {
            IceJarPlayer ij = (IceJarPlayer) this.player;
            ij.updateGroundStatus();
            ij.ij$setOnGround(checkOnGround(this.player, packet.getY(this.player.getY()) - player.getY(), true));

            // Player isn't on ground but client packet says it is
            if (!ij.isNearGround()) {
                ((AServerboundMovePlayerPacket) packet).setOnGround(false);

                this.setJesus(ij.aboveLiquid());

                return false;
            } else {
                this.decreaseCheatAttempts();
            }
        }

        return true;
    }

    private void setJesus(boolean onWater) {
        this.hasJesus = onWater;
    }

    public boolean hasJesus() {
        return this.hasJesus;
    }

    @Override
    public CheckType getType() {
        return this.hasJesus() ? CheckType.SPECIAL_JESUS : this.checkType;
    }

    @Override
    public void setCheatAttempts(int attempts) {
        if (this.hasJesus())
            super.setCheatAttempts(attempts);
    }

    @Override
    public int increaseCheatAttempts() {
        final int max2 = this.getMaxAttemptsBeforeFlag() * 2;
        if (++this.cheatAttempts > max2) {
            this.cheatAttempts = max2;
        }
        return this.cheatAttempts;
    }

    public void setHasFallen(boolean hasFallen) {
        this.hasFallen = hasFallen;
    }

    public boolean hasFallen() {
        return this.hasFallen;
    }

    public boolean hasNoFall() {
        return this.getCheatAttempts() > ((double) this.getMaxAttemptsBeforeFlag() / 2);
    }

    public boolean shouldSkipDamageEvent() {
        return this.skipDamageEvent;
    }

    public void setSkipDamageEvent(boolean skipDamageEvent) {
        this.skipDamageEvent = skipDamageEvent;
    }

    public static boolean checkOnGround(final Entity entity, final double deltaY, boolean checkLiquid) {
        final AABB bBox = entity
                .getBoundingBox()
                .expandTowards(0, -(deltaY + 0.25005D), 0);

        final Iterable<VoxelShape> collidingBlocks = entity.getLevel().getBlockCollisions(entity, bBox);

        if (collidingBlocks.iterator().hasNext()) {
            // Has collision with blocks
            return true;
        }

        if (checkLiquid && entity instanceof IceJarPlayer pl) {
            pl.setAboveLiquid(entity.getLevel().containsAnyLiquid(bBox));
        }

        // No block collisions found, check for entity collisions (e.g. standing on boat)
        List<VoxelShape> collidingEntities = entity.getLevel().getEntityCollisions(entity, bBox);

        return !collidingEntities.isEmpty();
    }
}
