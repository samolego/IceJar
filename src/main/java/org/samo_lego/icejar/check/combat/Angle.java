package org.samo_lego.icejar.check.combat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import org.samo_lego.icejar.check.CheckType;

/**
 * Taken from GolfIV, https://github.com/samolego/GolfIV/blob/61fe5f2e8684ddb2d768d8f2e79af3149c86c830/src/main/java/org/samo_lego/golfiv/event/combat/AngleCheck.java#L24
 */
public class Angle extends CombatCheck{

    public Angle(ServerPlayer player) {
        super(CheckType.COMBAT_ANGLE, player);
    }

    @Override
    public boolean checkCombat(Level world, InteractionHand hand, Entity targetEntity, EntityHitResult entityHitResult) {
        final double victimDistanceSquared = entityHitResult.distanceTo(player);
        final double victimDistance = Math.sqrt(victimDistanceSquared);

        // Get NSEW direction
        int xOffset = player.getDirection().getStepX();
        int zOffset = player.getDirection().getStepZ();

        final AABB bBox = targetEntity.getBoundingBox();

        // Checking if targetEntity is behind player ("dumb" check)
        if(xOffset * targetEntity.getX() + bBox.getXsize() / 2 - xOffset * player.getX() < 0 ||
            zOffset * targetEntity.getZ() + bBox.getZsize() / 2 - zOffset * player.getZ() < 0) {
            return false;
        }

        // Fine check
        final double deltaX = targetEntity.getX() - player.getX();
        final double deltaZ = targetEntity.getZ() - player.getZ();

        // Get the angle between the player and the targetEntity (in radians)
        final double beta = Math.atan2(deltaZ, deltaX) - Math.PI / 2;
        final double phi = beta - Math.toRadians(player.getYHeadRot());

        // Get diagonal distance of target bounding box
        final double allowedAttackSpace = Math.sqrt(bBox.getXsize() * bBox.getXsize() + bBox.getZsize() * bBox.getZsize());

        return Math.abs(victimDistance * Math.sin(phi)) <= allowedAttackSpace / 2 + 0.2D;
    }
}
