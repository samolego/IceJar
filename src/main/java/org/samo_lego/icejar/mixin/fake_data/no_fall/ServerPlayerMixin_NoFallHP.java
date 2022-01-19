package org.samo_lego.icejar.mixin.fake_data.no_fall;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.check.movement.NoFall;
import org.samo_lego.icejar.util.IceJarPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin_NoFallHP {

    @Shadow private float lastSentHealth;
    @Shadow private int lastSentFood;
    @Shadow private boolean lastFoodSaturationZero;
    @Unique
    private final ServerPlayer player = (ServerPlayer) (Object) this;

    /**
     * Skips sending health and food to the client if the player has taken fall damage but is using NoFall.
     * @param ci mixin callback info.
     */
    @Inject(method = "doTick", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/server/level/ServerPlayer;getHealth()F", ordinal = 0))
    private void setFakedHealth(CallbackInfo ci) {
        final NoFall check = (NoFall) ((IceJarPlayer) player).getCheck(CheckType.MOVEMENT_NOFALL);
        if (check.hasFallen()) {
            // Damage source is null-ified after about 2 seconds, then we check if no fall is still enabled.
            if (player.getLastDamageSource() == DamageSource.FALL || (player.getLastDamageSource() == null && check.hasNoFall())) {
                this.lastSentHealth = player.getHealth();
                this.lastSentFood = player.getFoodData().getFoodLevel();
                this.lastFoodSaturationZero = player.getFoodData().getSaturationLevel() == 0.0F;
            } else {
                check.setHasFallen(false);
            }
        }
    }
}
