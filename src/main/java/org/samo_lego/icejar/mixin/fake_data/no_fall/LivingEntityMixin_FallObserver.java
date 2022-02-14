package org.samo_lego.icejar.mixin.fake_data.no_fall;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.samo_lego.icejar.check.movement.NoFall;
import org.samo_lego.icejar.util.IceJarPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.samo_lego.icejar.check.CheckType.MOVEMENT_NOFALL;

@Mixin(LivingEntity.class)
public class LivingEntityMixin_FallObserver {

    @Unique
    private final LivingEntity self = (LivingEntity) (Object) this;

    /**
     * Updates the {@link NoFall} hasFallen value.
     * @param source damage source, relevant only if it is {@link DamageSource#FALL}
     * @param amount amount of damage to be taken.
     * @param cir mixin callback info returnable.
     */
    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;broadcastEntityEvent(Lnet/minecraft/world/entity/Entity;B)V"))
    protected void skipNoFallDamageEvent(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (self instanceof IceJarPlayer player && source == DamageSource.FALL && MOVEMENT_NOFALL.isEnabled()) {
            final NoFall check = player.getCheck(NoFall.class);
            final boolean noFallEnabled = check.hasNoFall();
            check.setSkipDamageEvent(noFallEnabled);
            check.setHasFallen(noFallEnabled);
        }
    }
}
