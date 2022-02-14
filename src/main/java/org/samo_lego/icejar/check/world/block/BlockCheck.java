package org.samo_lego.icejar.check.world.block;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.check.Check;
import org.samo_lego.icejar.check.CheckCategory;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.util.IceJarPlayer;

import java.util.Set;

import static org.samo_lego.icejar.check.CheckCategory.category2checks;

public abstract class BlockCheck extends Check {
    public BlockCheck(CheckType checkType, ServerPlayer player) {
        super(checkType, player);
    }

    @Override
    public boolean check(Object... params) {
        if(params.length != 4)
            throw new IllegalArgumentException("BlockCheck. check() requires 3 params, got " + params.length);
        return this.checkBlockAction((Level) params[0], (InteractionHand) params[1], (BlockPos) params[2], (Direction) params[3]);
    }

    public abstract boolean checkBlockAction(final Level level, final InteractionHand hand, final BlockPos blockPos, final Direction direction);


    public static InteractionResult performCheck(final CheckCategory category, final Player player, final Level level,
                                                 final InteractionHand interactionHand, final BlockPos blockPos, Direction direction) {

        // Loop through all block interact checks
        final Set<CheckType> checks = category2checks.get(category);
        if (checks != null && player instanceof IceJarPlayer ij) {
            for (CheckType type : checks) {
                if (Permissions.check(player, type.getBypassPermission(), false)) continue;

                final BlockCheck check = ij.getCheck(type);

                // Check
                if (!check.checkBlockAction(level, interactionHand, blockPos, direction)) {
                    if (check.increaseCheatAttempts() > check.getMaxAttemptsBeforeFlag())
                        check.flag();
                    return IceJar.getInstance().getConfig().debug ?
                            InteractionResult.PASS :
                            InteractionResult.FAIL;
                }
            }
        }
        return InteractionResult.PASS;
    }

}
