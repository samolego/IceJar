package org.samo_lego.icejar.mixin.packet;

import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

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
        if (this.packedItems != null) {  // todo - 1.19.3
            /*final Entity entity = IceJar.getInstance().getServer().getEnt
            if (entity instanceof ServerPlayer) {
                this.packedItems.removeIf((SynchedEntityData.DataItem<?> dataItem) -> {
                    Object value = dataItem.getAccessor();
                    return value == DATA_HEALTH_ID() || value.equals(DATA_PLAYER_ABSORPTION_ID());
                });
            }*/
        }
    }
}
