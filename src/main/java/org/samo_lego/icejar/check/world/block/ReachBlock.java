package org.samo_lego.icejar.check.world.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.check.CheckType;

public class ReachBlock extends BlockCheck {
    private double distance;

    public ReachBlock(ServerPlayer player) {
        super(CheckType.WORLD_BLOCK_REACH, player);
    }

    @Override
    public boolean checkBlockAction(final Level level, final InteractionHand interactionHand, final BlockPos blockPos, final Direction direction) {
        this.distance = Math.sqrt(blockPos.distSqr(player.getEyePosition(), false));
        final double maxDist = IceJar.getInstance().getConfig().world.maxBlockReachDistance;

        return this.distance <= maxDist;
    }

    @Override
    public MutableComponent getAdditionalFlagInfo() {
        return new TextComponent("Distance: ")
                .append(new TextComponent(String.format("%.2f", this.distance)).withStyle(ChatFormatting.RED));
    }
}
