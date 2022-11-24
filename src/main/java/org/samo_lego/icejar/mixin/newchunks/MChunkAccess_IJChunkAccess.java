package org.samo_lego.icejar.mixin.newchunks;

import net.minecraft.world.level.chunk.LevelChunk;
import org.samo_lego.icejar.casts.IJChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LevelChunk.class)
public class MChunkAccess_IJChunkAccess implements IJChunkAccess {
    @Unique
    private boolean newChunk = false;

    @Override
    public boolean ij_isNewChunk() {
        return this.newChunk;
    }

    @Override
    public void ij_setNewChunk(boolean newChunk) {
        this.newChunk = newChunk;
    }
}
