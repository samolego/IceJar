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

        // Facing W / NW / SW *AND* interacting with W
        if (deltaX < 0 && direction.equals(Direction.WEST))
            return false;

        // Facing E / NE / SE *AND* interacting with E
        if (deltaX > 0 && direction.equals(Direction.EAST))
            return false;

        // North and South
        final double deltaZ = blockPos.getZ() - player.getZ();

        // Facing N / NE / NW *AND* interacting with N
        if (deltaZ < 0 && direction.equals(Direction.NORTH))
            return false;

        if (deltaZ > 0 && direction.equals(Direction.SOUTH))
            return false;

        // Y deltas
        final double deltaY = blockPos.getY() - player.getEyeY();

        // Facing down but interacting with up
        if (deltaY > 1 && player.getXRot() > 0.0F)
            return false;
        if (deltaY < -1 && player.getXRot() < 0.0F)
            return false;

        System.out.println(player.getXRot());
        System.out.println(player.getYHeadRot());

        // The last one, facing N / NE / NW but interacting with S
        return true;
    }
}
