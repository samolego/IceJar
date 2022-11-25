package org.samo_lego.icejar.mixin.newchunks;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.samo_lego.icejar.casts.IJChunkAccess;
import org.samo_lego.icejar.casts.IceJarPlayer;
import org.samo_lego.icejar.mixin.accessor.AClientboundLevelChunkWithLightPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MServerGamePacketListener_ChunkDelayer {
    @Shadow
    public abstract ServerPlayer getPlayer();

    @Shadow
    public abstract void send(Packet<?> packet);

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void ij_sendPacket(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ClientboundBlockUpdatePacket blockPacket) {
            final LevelChunk chunk = this.getPlayer().getLevel().getChunkAt(blockPacket.getPos());

            if (((IceJarPlayer) this.getPlayer()).ij_getDelayedPackets().containsKey(chunk.getPos()) && !chunk.getFluidState(blockPacket.getPos()).isEmpty()) {
                // Set delay to back to 8 ticks
                ((IceJarPlayer) this.getPlayer()).ij_getDelayedPackets().put(chunk.getPos(), Pair.of(packet, 8));
                ci.cancel();
            }
        } else if (packet instanceof ClientboundLevelChunkWithLightPacket lightPacket) {
            final LevelChunk chunk = this.getPlayer().getLevel().getChunk(lightPacket.getX(), lightPacket.getZ());
            if (((IJChunkAccess) chunk).ij_isNewChunk()) {
                // Delay the packet due to possible fluid spreading
                ((IceJarPlayer) this).ij_getDelayedPackets().put(chunk.getPos(), Pair.of(packet, 8));
                ci.cancel();
            }

        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void ij_onTick(CallbackInfo ci) {
        // Send the needed packets
        for (var entry : ((IceJarPlayer) this.getPlayer()).ij_getDelayedPackets().entrySet()) {
            ChunkPos chunkPos = entry.getKey();
            var pair = entry.getValue();

            if (pair.getSecond() <= 0) {
                // Send the packet
                var chunk = this.getPlayer().getLevel().getChunk(chunkPos.x, chunkPos.z);
                ((IJChunkAccess) chunk).ij_setNewChunk(false);
                ((IceJarPlayer) this.getPlayer()).ij_getDelayedPackets().remove(chunkPos);

                Packet<?> packet = pair.getFirst();

                if (packet instanceof ClientboundLevelChunkWithLightPacket lightPacket) {
                    ((AClientboundLevelChunkWithLightPacket) lightPacket).setChunkData(new ClientboundLevelChunkPacketData(chunk));
                    this.send(lightPacket);
                }
            } else {
                // Decrease delay
                entry.setValue(entry.getValue().mapSecond(i -> i - 1));
            }
        }
    }
}
