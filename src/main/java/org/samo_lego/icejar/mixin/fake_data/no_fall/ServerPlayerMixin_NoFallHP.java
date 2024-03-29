package org.samo_lego.icejar.mixin.fake_data.no_fall;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.samo_lego.icejar.casts.IceJarPlayer;
import org.samo_lego.icejar.check.movement.NoFall;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.icejar.check.CheckType.MOVEMENT_NOFALL;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin_NoFallHP {

    @Shadow private float lastSentHealth;
    @Shadow private int lastSentFood;
    @Shadow private boolean lastFoodSaturationZero;
    @Unique private final ServerPlayer ij$player = (ServerPlayer) (Object) this;

    /**
     * Skips sending health and food to the client if the player has taken fall damage but is using NoFall.
     * @param ci mixin callback info.
     */
    @Inject(method = "doTick", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/server/level/ServerPlayer;getHealth()F", ordinal = 0))
    private void setFakedHealth(CallbackInfo ci) {
        if (MOVEMENT_NOFALL.isEnabled()) {
            final NoFall check = ((IceJarPlayer) ij$player).getCheck(NoFall.class);
            if (check.hasFallen()) {
                // Damage source is null-ified after about 2 seconds, then we check if no fall is still enabled.
                if (ij$player.getLastDamageSource() == DamageSource.FALL || (ij$player.getLastDamageSource() == null && check.hasNoFall())) {
                    this.lastSentHealth = ij$player.getHealth();
                    this.lastSentFood = ij$player.getFoodData().getFoodLevel();
                    this.lastFoodSaturationZero = ij$player.getFoodData().getSaturationLevel() == 0.0F;
                } else {
                    check.setHasFallen(false);
                }
            }
        }
    }
}
