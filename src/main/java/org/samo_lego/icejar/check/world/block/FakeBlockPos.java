package org.samo_lego.icejar.check.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import org.samo_lego.icejar.check.CheckType;

/**
 * Vanilla client always sends {@link net.minecraft.core.BlockPos.MutableBlockPos} for block interactions.
 * We simply check if interaction was indeed a mutable one - if not, it's fake.
 */
public class FakeBlockPos extends BlockCheck {
    public FakeBlockPos(ServerPlayer player) {
        super(CheckType.WORLD_BLOCK_FAKEPOS, player);
    }

    @Override
    public boolean checkBlockAction(Level level, InteractionHand hand, BlockPos blockPos, Direction direction) {
        return !(blockPos instanceof BlockPos.MutableBlockPos);
    }
}
