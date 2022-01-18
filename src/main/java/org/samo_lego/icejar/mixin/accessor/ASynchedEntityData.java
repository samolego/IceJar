package org.samo_lego.icejar.mixin.accessor;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SynchedEntityData.class)
public interface ASynchedEntityData {
    @Accessor("entity")
    Entity getEntity();
}
