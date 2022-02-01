package org.samo_lego.icejar.check.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.check.combat.ImpossibleHit;
import org.samo_lego.icejar.util.IceJarPlayer;

import static org.samo_lego.icejar.util.ChatColor.styleBoolean;

/**
 * Checks whether a player can break blocks.
 * Circumstances that disallow breaking blocks are similar
 * to {@link ImpossibleHit}.
 */
public class ImpossibleBreak extends BlockBreakCheck {
    public ImpossibleBreak(ServerPlayer player) {
        super(CheckType.WORLD_BLOCK_BREAK_IMPOSSIBLE, player);
    }

    @Override
    protected boolean checkBlockBreak(final Level level, final InteractionHand hand, final BlockPos blockPos, final Direction direction) {
        return !((IceJarPlayer) player).ij$hasOpenGui() &&
                !player.isUsingItem() &&
                !player.isBlocking();
    }

    @Override
    public MutableComponent getAdditionalFlagInfo() {
        return new TextComponent("GUI open: ")
                .append(styleBoolean(((IceJarPlayer) player).ij$hasOpenGui()))
                .append("\n")
                .append(new TextComponent("Using item: ")
                        .append(styleBoolean(player.isUsingItem())))
                .append("\n")
                .append(new TextComponent("Blocking: ")
                        .append(styleBoolean(player.isBlocking())));
    }
}
