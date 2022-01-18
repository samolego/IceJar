package org.samo_lego.icejar.mixin.fake_data.no_fall;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.check.movement.NoFall;
import org.samo_lego.icejar.util.IceJarPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin_FallObserver {

    @Unique
    private final LivingEntity self = (LivingEntity) (Object) this;

    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;broadcastEntityEvent(Lnet/minecraft/world/entity/Entity;B)V"))
    protected void skipNoFallDamageEvent(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (self instanceof IceJarPlayer player) {
            final boolean noFallEnabled = ((NoFall) player.getCheck(CheckType.MOVEMENT_NOFALL)).hasNoFall();
            ((NoFall) player.getCheck(CheckType.MOVEMENT_NOFALL)).setSkipDamageEvent(noFallEnabled);
        }
    }
}
