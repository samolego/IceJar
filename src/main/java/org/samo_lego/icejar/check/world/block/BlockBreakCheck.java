package org.samo_lego.icejar.check.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import static org.samo_lego.icejar.check.CheckCategory.WORLD_BLOCK_BREAK;

public abstract class BlockBreakCheck {
    public static InteractionResult performCheck(final Player player, final Level level, final InteractionHand interactionHand,
                                                 final BlockPos blockPos, final Direction direction) {
        return BlockCheck.performCheck(WORLD_BLOCK_BREAK, player, level, interactionHand, blockPos, direction);
    }
}
