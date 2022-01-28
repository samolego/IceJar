package org.samo_lego.icejar.check.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.samo_lego.icejar.check.CheckType;

public class AirPlace extends BlockInteractCheck {

    public AirPlace(ServerPlayer player) {
        super(CheckType.WORLD_BLOCK_AIR_PLACE, player);
    }

    @Override
    protected boolean checkBlockInteract(Level level, InteractionHand hand, BlockPos blockPos, Direction direction) {
        final BlockState state = level.getBlockState(blockPos);
        // Check if block is liquid or air
        return !state.isAir() && !state.getMaterial().isLiquid();
    }
}
