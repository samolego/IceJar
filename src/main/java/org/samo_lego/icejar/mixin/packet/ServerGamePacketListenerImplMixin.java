package org.samo_lego.icejar.mixin.packet;

import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.TextFilter;
import org.samo_lego.icejar.check.combat.NoSwing;
import org.samo_lego.icejar.check.world.block.AutoSign;
import org.samo_lego.icejar.util.IceJarPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    @Shadow public ServerPlayer player;

    @Inject(method = "handleAnimate", at = @At("TAIL"))
    private void onHandSwing(ServerboundSwingPacket packet, CallbackInfo ci) {
        ((IceJarPlayer) this.player).getCheck(NoSwing.class).onSwing();
    }

    @Inject(method = "updateSignText",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/SignBlockEntity;isEditable()Z"),
            cancellable = true)
    private void onSignUpdate(ServerboundSignUpdatePacket packet, List<TextFilter.FilteredText> list, CallbackInfo ci) {
       if (!((IceJarPlayer) this.player).getCheck(AutoSign.class).allowPlace(packet)) {
           ci.cancel();
       }
    }
}
