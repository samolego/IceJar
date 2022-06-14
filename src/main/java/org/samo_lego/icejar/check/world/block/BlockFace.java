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

public class BlockFace extends BlockCheck {
    private Direction blockDirection;
    private Direction lookingDirection;

    public BlockFace(ServerPlayer player) {
        super(CheckType.WORLD_BLOCK_FACE, player);
    }

    @Override
    public boolean checkBlockAction(Level level, InteractionHand hand, BlockPos blockPos, Direction direction) {
        if (player.getDirection().equals(direction)) {
            this.lookingDirection = player.getDirection();
            this.blockDirection = direction;
            return false;
        }

        // East and West
        final double deltaX = blockPos.getX() - player.getX();
        this.blockDirection = direction;

        // Facing W / NW / SW *AND* interacting with W
        if (deltaX < -0.7D && direction.equals(Direction.WEST)) {
            this.lookingDirection = Direction.WEST;
            return false;
        }
        // Facing E / NE / SE *AND* interacting with E
        if (deltaX > 0.7D && direction.equals(Direction.EAST)) {
            this.lookingDirection = Direction.EAST;
            return false;
        }

        // North and South
        final double deltaZ = blockPos.getZ() - player.getZ();

        // Facing N / NE / NW *AND* interacting with N
        if (deltaZ < -0.7D && direction.equals(Direction.NORTH)) {
            this.lookingDirection = Direction.NORTH;
            return false;
        }

        if (deltaZ > 0.7D && direction.equals(Direction.SOUTH)) {
            this.lookingDirection = Direction.SOUTH;
            return false;
        }

        return true;
    }

    @Override
    public MutableComponent getAdditionalFlagInfo() {
        return Component.translatable("Looking: %s\nBlock: %s",
                Component.literal(this.lookingDirection.getName()).withStyle(ChatFormatting.GREEN),
                Component.literal(this.blockDirection.getName()).withStyle(ChatFormatting.RED));
    }
}
