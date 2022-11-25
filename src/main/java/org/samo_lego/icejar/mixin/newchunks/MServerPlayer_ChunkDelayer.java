package org.samo_lego.icejar.mixin.newchunks;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class MServerPlayer_ChunkDelayer {
    @Shadow
    public abstract ServerLevel getLevel();

    @Inject(method = "trackChunk", at = @At("HEAD"), cancellable = true)
    private void ij_trackChunk(ChunkPos chunkPos, Packet<?> packet, CallbackInfo ci) {
        /*final LevelChunk chunk = this.getLevel().getChunk(chunkPos.x, chunkPos.z);
        if (((IJChunkAccess) chunk).ij_isNewChunk()) {
            // Delay the packet due to possible fluid spreading
            ((IceJarPlayer) this).ij_getDelayedPackets().put(chunkPos, Pair.of(packet, 8));
            ci.cancel();
        }*/
    }
}
