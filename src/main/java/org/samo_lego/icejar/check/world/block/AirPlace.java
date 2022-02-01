package org.samo_lego.icejar.check.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.check.CheckType;

public class AirPlace extends BlockInteractCheck {

    public AirPlace(ServerPlayer player) {
        super(CheckType.WORLD_BLOCK_PLACE_AIR, player);
    }

    @Override
    protected boolean checkBlockInteract(Level level, InteractionHand hand, BlockPos blockPos, Direction direction) {
        final BlockState state = level.getBlockState(blockPos);
        // Check if block is liquid or air
        if (state.isAir() || state.getMaterial().isLiquid()) {
            final double dist = IceJar.getInstance().getConfig().world.maxBlockReachDistance;

            final BlockHitResult blockHit = (BlockHitResult) player.pick(dist, 0, false);
            return blockHit.getType().equals(BlockHitResult.Type.BLOCK);
        }
        return !state.isAir() && !state.getMaterial().isLiquid();
    }
}
