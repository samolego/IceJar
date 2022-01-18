package org.samo_lego.icejar.mixin.fake_data.no_fall;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.check.movement.NoFall;
import org.samo_lego.icejar.util.IceJarPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin_NoFallSound {
    @Shadow public abstract LivingEntity.Fallsounds getFallSounds();

    @Unique
    private final Player self = (Player) (Object) this;
    @Unique
    private final ResourceLocation SMALL_FALL = this.getFallSounds().small().getLocation();
    @Unique
    private final ResourceLocation BIG_FALL = this.getFallSounds().big().getLocation();

    @Inject(method = "playSound", at = @At("HEAD"), cancellable = true)
    private void skipNoFallDamageSound(SoundEvent sound, float volume, float pitch, CallbackInfo ci) {
        if (self instanceof ServerPlayer player) {
            final boolean hasNoFall = ((NoFall) ((IceJarPlayer) player).getCheck(CheckType.MOVEMENT_NOFALL)).hasNoFall();

            final ResourceLocation soundId = sound.getLocation();
            if (hasNoFall &&
                (soundId.equals(BIG_FALL) ||
                soundId.equals(SMALL_FALL)) ||
                soundId.equals(new ResourceLocation("minecraft:entity.player.hurt"))) {
                // Cancel the sound
                System.out.println("Cancelling sound: " + soundId);
                ci.cancel();
            }
        }
    }
}
