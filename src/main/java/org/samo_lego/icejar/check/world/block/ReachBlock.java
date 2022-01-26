package org.samo_lego.icejar.check.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.check.CheckType;

import static org.samo_lego.icejar.check.combat.CombatCheck.CREATIVE_DISTANCE;

public class ReachBlock extends BlockCheck {
    public ReachBlock(ServerPlayer player) {
        super(CheckType.WORLD_BLOCK_REACH, player);
    }

    @Override
    public boolean checkBlockAction(final Level level, final InteractionHand interactionHand, final BlockPos blockPos, final Direction direction) {
        final double distance = Math.sqrt(blockPos.distSqr(player.getEyePosition(), false));
        final double maxDist = player.isCreative() ?
                CREATIVE_DISTANCE :
                IceJar.getInstance().getConfig().combat.maxSurvivalDistance;

        return distance <= maxDist;
    }
}
