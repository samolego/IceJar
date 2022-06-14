package org.samo_lego.icejar.check.world.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import org.samo_lego.icejar.check.CheckType;

public class BlockDirection extends BlockCheck {
    private Direction blockDirection;
    private Direction lookingDirection;

    public BlockDirection(ServerPlayer player) {
        super(CheckType.WORLD_BLOCK_DIRECTION, player);
    }

    @Override
    public boolean checkBlockAction(Level level, InteractionHand hand, BlockPos blockPos, Direction direction) {
        // East and West
        final double deltaX = blockPos.getX() - player.getX();
        this.blockDirection = direction;

        // North and South
        final double deltaZ = blockPos.getZ() - player.getZ();

        if (deltaZ > 0.7D && direction.equals(Direction.SOUTH)) {
            this.lookingDirection = Direction.SOUTH;
            return false;
        }

        // Y deltas
        final double deltaY = blockPos.getY() - player.getEyeY();

        // Facing down but interacting with up
        if (player.getXRot() > 0.0F && (deltaY > 0.8D || direction.equals(Direction.DOWN))) {
            this.lookingDirection = Direction.DOWN;
            return false;
        }

        this.lookingDirection = Direction.UP;
        return !(player.getXRot() < 0.0F) || (!(deltaY < -0.8D) && !direction.equals(Direction.UP));
    }

    @Override
    public MutableComponent getAdditionalFlagInfo() {
        return Component.translatable("Looking: %s\nBlock: %s",
                Component.literal(this.lookingDirection.getName()).withStyle(ChatFormatting.GREEN),
                Component.literal(this.blockDirection.getName()).withStyle(ChatFormatting.RED));
    }
}
