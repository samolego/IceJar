package org.samo_lego.icejar.check.world.block;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import static org.samo_lego.icejar.check.CheckCategory.WORLD_BLOCK_INTERACT;

public class BlockInteractCheck {

    public static InteractionResult performCheck(final Player player, final Level level,
                                                 final InteractionHand interactionHand, final BlockHitResult blockHitResult) {

        return BlockCheck.performCheck(WORLD_BLOCK_INTERACT, player, level, interactionHand, blockHitResult.getBlockPos(), blockHitResult.getDirection());
    }
}
