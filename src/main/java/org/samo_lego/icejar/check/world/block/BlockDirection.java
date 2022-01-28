package org.samo_lego.icejar.check.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import org.samo_lego.icejar.check.CheckType;

public class BlockDirection extends BlockCheck {
    public BlockDirection(ServerPlayer player) {
        super(CheckType.WORLD_BLOCK_DIRECTION, player);
    }

    @Override
    public boolean checkBlockAction(Level level, InteractionHand hand, BlockPos blockPos, Direction direction) {
        // Player can't interact with the block facing the same way.
        // Very basic
        return !player.getDirection().equals(direction);
    }
}
