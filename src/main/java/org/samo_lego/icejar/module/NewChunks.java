package org.samo_lego.icejar.module;

import net.minecraft.world.level.ChunkPos;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class NewChunks {
    public static final Set<ChunkPos> NEW_CHUNKS = new ConcurrentSkipListSet<>((chunkPos1, chunkPos2) -> {
        if (chunkPos1.x == chunkPos2.x) {
            return chunkPos1.z - chunkPos2.z;
        }
        return chunkPos1.x - chunkPos2.x;
    });
}
