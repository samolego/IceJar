package org.samo_lego.icejar.mixin.accessor;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Player.class)
public interface APlayer {
    @Accessor("DATA_PLAYER_ABSORPTION_ID")
    static EntityDataAccessor<Float> DATA_PLAYER_ABSORPTION_ID() {
        throw new AssertionError();
    }
}
