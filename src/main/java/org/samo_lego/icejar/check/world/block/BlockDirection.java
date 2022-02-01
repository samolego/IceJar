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
        // East and West
        final double deltaX = blockPos.getX() - player.getX();
        // North and South
        final double deltaZ = blockPos.getZ() - player.getZ();

        // Facing E / NE /SE but interacting with W
        if (deltaX < 0 && direction.equals(Direction.WEST))
            return false;

        // Facing W / NW / SW but interacting with E
        if (deltaX > 0 && direction.equals(Direction.EAST))
            return false;

        // Facing S / SE / SW but interacting with N
        if (deltaZ < 0 && direction.equals(Direction.NORTH))
            return false;

        // The last one, facing N / NE / NW but interacting with S
        return !(deltaZ > 0) || !direction.equals(Direction.SOUTH);
    }
}
