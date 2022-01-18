package org.samo_lego.icejar.mixin.fake_data.no_fall;

import net.minecraft.world.entity.LivingEntity;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.check.movement.NoFall;
import org.samo_lego.icejar.util.IceJarPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin_NoFallSound {

    @Unique
    private final LivingEntity self = (LivingEntity) (Object) this;

    @Inject(method = "playBlockFallSound", at = @At("HEAD"), cancellable = true)
    private void skipNoFallSound(CallbackInfo ci) {
        if (self instanceof IceJarPlayer player) {
            final boolean noFallEnabled = ((NoFall) player.getCheck(CheckType.MOVEMENT_NOFALL)).hasNoFall();

            if (noFallEnabled) {
                System.out.println("[IceJar] Skipping no fall sound");
                ci.cancel();
            }
        }
    }
}
