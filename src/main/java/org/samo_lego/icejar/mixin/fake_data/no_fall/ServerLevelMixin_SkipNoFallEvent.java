package org.samo_lego.icejar.mixin.fake_data.no_fall;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.check.movement.NoFall;
import org.samo_lego.icejar.util.IceJarPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public class ServerLevelMixin_SkipNoFallEvent {

    /**
     * Works together with {@link LivingEntityMixin_FallObserver#skipNoFallDamageEvent(DamageSource, float, CallbackInfoReturnable)}
     * @param entity entity to broadcast event for.
     * @param state state of the entity.
     * @param ci callback info.
     */
    @Inject(method = "broadcastEntityEvent", at = @At("HEAD"), cancellable = true)
    private void skipNoFallEvent(Entity entity, byte state, CallbackInfo ci) {
        if (entity instanceof IceJarPlayer player && ((NoFall) player.getCheck(CheckType.MOVEMENT_NOFALL)).shouldSkipDamageEvent()) {
            ((NoFall) player.getCheck(CheckType.MOVEMENT_NOFALL)).setSkipDamageEvent(false);
            ((NoFall) player.getCheck(CheckType.MOVEMENT_NOFALL)).setHasFallen(true);
            ci.cancel();
        }
    }
}
