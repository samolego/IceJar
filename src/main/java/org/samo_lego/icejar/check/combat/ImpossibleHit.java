package org.samo_lego.icejar.check.combat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.util.IceJarPlayer;


/**
 * A check to see if players try to attack entities while performing something else
 * or having obstructions in their way.
 * (i.e.: while blocking)
 */
public class ImpossibleHit extends CombatCheck {
    public ImpossibleHit(ServerPlayer player) {
        super(CheckType.COMBAT_IMPOSSIBLEHIT, player);
    }

    @Override
    public boolean checkCombat(Level world, InteractionHand hand, Entity targetEntity, EntityHitResult hitResult) {
        // Check if there's a wall in front of the player
        final double victimDistance = Math.sqrt(hitResult.distanceTo(player));
        final double dist = player.isCreative() ? CREATIVE_DISTANCE : IceJar.getInstance().getConfig().combat.maxSurvivalDistance;
        final BlockHitResult blockHit = (BlockHitResult) player.pick(Math.sqrt(dist * dist), 0, false);

        // Cannot hit targets with a wall in front of them, open gui, using item etc.
        boolean wall = Math.sqrt(blockHit.distanceTo(player)) + 0.5D >= victimDistance || player.isPassenger();
        return wall &&
                !((IceJarPlayer) player).ij$hasOpenGui() &&
                !player.isUsingItem() &&
                !player.isBlocking();
    }
}
