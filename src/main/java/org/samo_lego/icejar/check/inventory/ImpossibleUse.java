package org.samo_lego.icejar.check.inventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.samo_lego.icejar.casts.IceJarPlayer;
import org.samo_lego.icejar.check.CheckType;

public class ImpossibleUse extends ItemUseCheck {
    public ImpossibleUse(ServerPlayer player) {
        super(CheckType.INVENTORY_IMPOSSIBLE_ITEM_USE, player);
    }

    @Override
    public boolean checkItemUse(final Level level, final ItemStack handStack, final InteractionHand hand) {
        return !((IceJarPlayer) player).ij$hasOpenGui();
    }
}
