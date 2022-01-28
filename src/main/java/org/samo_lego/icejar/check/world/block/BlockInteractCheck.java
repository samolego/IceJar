package org.samo_lego.icejar.check.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.samo_lego.icejar.check.CheckType;

import static org.samo_lego.icejar.check.CheckCategory.WORLD_BLOCK_INTERACT;

public abstract class BlockInteractCheck extends BlockCheck {

    public BlockInteractCheck(CheckType checkType, ServerPlayer player) {
        super(checkType, player);
    }

    public static InteractionResult performCheck(final Player player, final Level level,
                                                 final InteractionHand interactionHand, final BlockHitResult blockHitResult) {

        return BlockCheck.performCheck(WORLD_BLOCK_INTERACT, player, level, interactionHand, blockHitResult.getBlockPos(), blockHitResult.getDirection());
    }

    @Override
    public boolean checkBlockAction(Level level, InteractionHand hand, BlockPos blockPos, Direction direction) {
        return this.checkBlockInteract(level, hand, blockPos, direction);
    }

    protected abstract boolean checkBlockInteract(Level level, InteractionHand hand, BlockPos blockPos, Direction direction);
}
