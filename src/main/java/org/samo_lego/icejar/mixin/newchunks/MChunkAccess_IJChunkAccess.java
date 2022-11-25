package org.samo_lego.icejar.mixin.newchunks;

import net.minecraft.world.level.chunk.ChunkAccess;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.casts.IJChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChunkAccess.class)
public class MChunkAccess_IJChunkAccess implements IJChunkAccess {
    @Unique
    private boolean newChunk = false;

    @Override
    public boolean ij_isNewChunk() {
        return this.newChunk && IceJar.getInstance().getConfig().fixes.newChunks;
    }

    @Override
    public void ij_setNewChunk(boolean newChunk) {
        this.newChunk = newChunk;
    }
}
