package org.samo_lego.icejar.mixin.newchunks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import org.samo_lego.icejar.casts.IceJarPlayer;
import org.samo_lego.icejar.mixin.accessor.AClientboundSectionBlocksUpdatePacket;
import org.samo_lego.icejar.module.NewChunks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MServerGamePacketListener_ChunkDelayer {
    @Unique
    private static final Direction[] DIRECTIONS = {Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH};

    @Shadow
    public abstract ServerPlayer getPlayer();

    @Shadow
    public abstract void send(Packet<?> packet);

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void ij_sendPacket(Packet<?> packet, CallbackInfo ci) {
        LevelChunk chunk = null;
        final var level = getPlayer().getLevel();
        if (packet instanceof ClientboundBlockUpdatePacket blockPacket) {
            BlockPos pos = blockPacket.getPos();
            chunk = this.getPlayer().getLevel().getChunkAt(pos);

            // Check possible fluid spreading
            FluidState fluidState = chunk.getFluidState(pos);

            if (NewChunks.isNewChunk(level, chunk.getPos()) && !fluidState.isEmpty()) {
                // Reset neighbour update delay
                for (Direction dir : DIRECTIONS) {
                    BlockPos neighbourPos = pos.relative(dir, fluidState.getAmount());
                    System.out.println("Neighbour pos: " + neighbourPos + " fluid amount: " + fluidState.getAmount());

                    var chunkNeighbour = this.getPlayer().getLevel().getChunkAt(neighbourPos);
                    if (chunkNeighbour != chunk) {
                        //((IceJarPlayer) this.getPlayer()).ij_getDelayedPackets().put(chunkNeighbour.getPos(), fluidState.getAmount());
                        NewChunks.addChunk(level, chunkNeighbour.getPos());
                    }
                }
            }
        } else if (packet instanceof ClientboundLevelChunkWithLightPacket lightPacket) {
            chunk = this.getPlayer().getLevel().getChunk(lightPacket.getX(), lightPacket.getZ());
        } else if (packet instanceof ClientboundSectionBlocksUpdatePacket blocksUpdatePacket) {
            final var pos = ((AClientboundSectionBlocksUpdatePacket) blocksUpdatePacket).sectionPos();
            chunk = this.getPlayer().getLevel().getChunk(pos.z(), pos.z());
        }

        if (chunk != null && NewChunks.isNewChunk(level, chunk.getPos())) {
            // Reset delay
            ((IceJarPlayer) this.getPlayer()).ij_getDelayedPackets().put(chunk.getPos(), 2);
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void ij_onTick(CallbackInfo ci) {
        // Send the needed packets
        for (var entry : ((IceJarPlayer) this.getPlayer()).ij_getDelayedPackets().entrySet()) {
            ChunkPos chunkPos = entry.getKey();
            var delay = entry.getValue();

            if (delay <= 0) {
                // Send the packet
                var chunk = this.getPlayer().getLevel().getChunk(chunkPos.x, chunkPos.z);
                var packet = new ClientboundLevelChunkWithLightPacket(chunk, chunk.getLevel().getLightEngine(), null, null, true);

                NewChunks.removeChunk(this.getPlayer().getLevel(), chunkPos);
                ((IceJarPlayer) this.getPlayer()).ij_getDelayedPackets().remove(chunkPos);

                this.send(packet);
            } else {
                // Decrease delay
                entry.setValue(delay - 1);
            }
        }
    }
}
