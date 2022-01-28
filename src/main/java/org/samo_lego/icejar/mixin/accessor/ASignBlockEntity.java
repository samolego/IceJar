package org.samo_lego.icejar.mixin.accessor;

import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SignBlockEntity.class)
public interface ASignBlockEntity {
    @Accessor("RAW_TEXT_FIELD_NAMES")
    static String[] RAW_TEXT_FIELD_NAMES() {
        throw new AssertionError();
    }

    @Accessor("FILTERED_TEXT_FIELD_NAMES")
    static String[] FILTERED_TEXT_FIELD_NAMES() {
        throw new AssertionError();
    }
}
