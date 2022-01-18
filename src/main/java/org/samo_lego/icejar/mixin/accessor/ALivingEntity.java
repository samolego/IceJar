package org.samo_lego.icejar.mixin.accessor;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface ALivingEntity {
    @Invoker("getHurtSound")
    SoundEvent getHurtSound(DamageSource damageSource);

    @Accessor("DATA_HEALTH_ID")
    static EntityDataAccessor<Float> DATA_HEALTH_ID() {
        throw new AssertionError();
    }
}
