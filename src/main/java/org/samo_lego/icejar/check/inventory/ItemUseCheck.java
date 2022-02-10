package org.samo_lego.icejar.check.inventory;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.samo_lego.icejar.check.Check;
import org.samo_lego.icejar.check.CheckCategory;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.util.IceJarPlayer;

import java.util.Set;

import static org.samo_lego.icejar.check.CheckCategory.category2checks;

public abstract class ItemUseCheck extends Check {
    public ItemUseCheck(CheckType checkType, ServerPlayer player) {
        super(checkType, player);
    }

    @Override
    public boolean check(Object... params) {
        if (params.length != 3)
            throw new IllegalArgumentException("ItemUse.check(...) requires 3 params, got " + params.length);
        return this.checkItemUse((Level) params[0], (ItemStack) params[1], (InteractionHand) params[2]);
    }

    public abstract boolean checkItemUse(Level level, ItemStack handStack, InteractionHand hand);

    public static InteractionResultHolder<ItemStack> performCheck(final Player player, final Level level, final InteractionHand hand) {
        final ItemStack handStack = player.getItemInHand(hand);

        final Set<CheckType> checks = category2checks.get(CheckCategory.INVENTORY);
        if (checks != null && player instanceof IceJarPlayer ij) {
            for (CheckType type : checks) {
                if (Permissions.check(player, type.getBypassPermission(), false)) continue;

                final ItemUseCheck check = ij.getCheck(type);

                // Check
                if (!check.checkItemUse(level, handStack, hand)) {
                    if (check.increaseCheatAttempts() > check.getMaxAttemptsBeforeFlag())
                        check.flag();
                    return InteractionResultHolder.fail(handStack);
                }
            }
        }
        return InteractionResultHolder.pass(handStack);
    }

}
