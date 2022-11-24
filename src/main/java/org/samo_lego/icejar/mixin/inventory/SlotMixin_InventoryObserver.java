package org.samo_lego.icejar.mixin.inventory;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.samo_lego.icejar.casts.IceJarPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Slot.class)
public class SlotMixin_InventoryObserver {

    @Inject(method = "onTake", at = @At("TAIL"))
    private void onTake(Player player, ItemStack stack, CallbackInfo ci) {
        if (player instanceof IceJarPlayer ij) {
            ij.ij$setOpenGUI(true);
        }
    }
}
