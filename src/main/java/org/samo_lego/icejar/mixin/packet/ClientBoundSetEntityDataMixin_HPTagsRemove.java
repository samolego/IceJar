package org.samo_lego.icejar.mixin.packet;

import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.icejar.mixin.accessor.ASynchedEntityData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static org.samo_lego.icejar.mixin.accessor.ALivingEntity.DATA_HEALTH_ID;
import static org.samo_lego.icejar.mixin.accessor.APlayer.DATA_PLAYER_ABSORPTION_ID;

/**
 * A simplified version of GolfIV's data remover.
 *
 * @see <a href="https://github.com/samolego/GolfIV/blob/golfive/src/main/java/org/samo_lego/golfiv/mixin/packets/EntityTrackerUpdateS2CPacketMixin_DataPatch.java">GolfIV's Mixin</a>
 */
@Mixin(ClientboundSetEntityDataPacket.class)
public class ClientBoundSetEntityDataMixin_HPTagsRemove {

    @Shadow
    @Final
    @Nullable
    private List<SynchedEntityData.DataItem<?>> packedItems;

    @Inject(method = "<init>(ILjava/util/List;)V", at = @At("TAIL"))
    private void removePlayerHP(int i, List<SynchedEntityData.DataValue<?>> data, CallbackInfo ci) {
        if (this.packedItems != null) {
            final Entity entity = ((ASynchedEntityData) data).getEntity();
            if (entity instanceof ServerPlayer) {
                this.packedItems.removeIf((SynchedEntityData.DataItem<?> dataItem) -> {
                    Object value = dataItem.getAccessor();
                    return value == DATA_HEALTH_ID() || value.equals(DATA_PLAYER_ABSORPTION_ID());
                });
            }
        }
    }
}
