package org.samo_lego.icejar.check.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.samo_lego.icejar.check.CheckType;

import static org.samo_lego.icejar.check.CheckCategory.WORLD_BLOCK_BREAK;

public abstract class BlockBreakCheck extends BlockCheck {
    public BlockBreakCheck(CheckType checkType, ServerPlayer player) {
        super(checkType, player);
    }

    public static InteractionResult performCheck(final Player player, final Level level, final InteractionHand interactionHand,
                                                 final BlockPos blockPos, final Direction direction) {
        return BlockCheck.performCheck(WORLD_BLOCK_BREAK, player, level, interactionHand, blockPos, direction);
    }


    @Override
    public boolean checkBlockAction(final Level level, final InteractionHand hand, final BlockPos blockPos, final Direction direction) {
        return this.checkBlockBreak(level, hand, blockPos, direction);
    }

    protected abstract boolean checkBlockBreak(final Level level, final InteractionHand hand, final BlockPos blockPos, final Direction direction);
}
