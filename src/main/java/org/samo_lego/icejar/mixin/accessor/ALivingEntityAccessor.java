package org.samo_lego.icejar.mixin.accessor;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface ALivingEntityAccessor {
    @Invoker("getHurtSound")
    SoundEvent getHurtSound(DamageSource damageSource);
}
