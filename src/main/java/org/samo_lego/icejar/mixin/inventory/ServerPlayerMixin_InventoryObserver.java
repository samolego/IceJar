package org.samo_lego.icejar.mixin.inventory;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import org.samo_lego.icejar.util.IceJarPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;

/**
 * Checks whether player has opened an inventory.
 */
@Mixin(ServerPlayer.class)
public class ServerPlayerMixin_InventoryObserver {

    @Unique
    private final IceJarPlayer player = (IceJarPlayer) this;

    @Inject(method = "openMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void onOpenMenu(MenuProvider menuProvider, CallbackInfoReturnable<OptionalInt> cir) {
        player.ij$setOpenGUI(true);
    }

    @Inject(method = "doCloseContainer", at = @At("HEAD"))
    private void onCloseMenu(CallbackInfo ci) {
        player.ij$setOpenGUI(false);
    }

    @Inject(method = "changeDimension", at = @At("HEAD"))
    private void onDimensionChange(ServerLevel destination, CallbackInfoReturnable<Entity> cir) {
        player.ij$setOpenGUI(false);
    }
}
