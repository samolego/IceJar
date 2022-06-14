package org.samo_lego.icejar.check.world.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import org.samo_lego.icejar.IceJar;
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
        this.blockDirection = direction;

        // Y deltas
        final double deltaY = blockPos.getY() - player.getEyeY();

        final var cfg = IceJar.getInstance().getConfig().world;

        // Facing down but interacting with up
        if (player.getXRot() > 0.0F && (deltaY > cfg.direction || direction.equals(Direction.DOWN))) {
            if (this.trainModeActive() && deltaY > cfg.direction) {
                cfg.direction = deltaY;
                return true;
            }

            this.lookingDirection = Direction.DOWN;
            return false;
        }


        if (player.getXRot() < 0.0F && (deltaY < -cfg.direction || direction.equals(Direction.UP))) {
            if (this.trainModeActive() && deltaY < -cfg.direction) {
                cfg.direction = -deltaY;
                return true;
            }

            this.lookingDirection = Direction.UP;
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
