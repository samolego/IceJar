package org.samo_lego.icejar.module;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class NewChunks {
    private static final Map<Level, Set<ChunkPos>> NEW_CHUNKS = new ConcurrentHashMap<>();

    public static void addChunk(Level level, ChunkPos chunkPos) {
        NEW_CHUNKS.computeIfAbsent(level, k -> new ConcurrentSkipListSet<>(new ChunkCompare())).add(chunkPos);
    }

    public static boolean isNewChunk(Level level, ChunkPos chunkPos) {
        return NEW_CHUNKS.containsKey(level) && NEW_CHUNKS.get(level).contains(chunkPos);
    }

    public static void removeChunk(Level level, ChunkPos chunkPos) {
        if (NEW_CHUNKS.containsKey(level)) {
            NEW_CHUNKS.get(level).remove(chunkPos);
        }
    }

    public static boolean tryFastFluidSpread(Level level, BlockPos blockPos, BlockState blockState) {
        if (isNewChunk(level, new ChunkPos(blockPos))) {
            // If chunk is new, set lower fluid tick delay
            level.scheduleTick(blockPos, blockState.getFluidState().getType(), 0);

            return true;
        }
        return false;
    }

    private static class ChunkCompare implements Comparator<ChunkPos> {
        @Override
        public int compare(ChunkPos chunkPos1, ChunkPos chunkPos2) {
            if (chunkPos1.x == chunkPos2.x) {
                return chunkPos1.z - chunkPos2.z;
            }
            return chunkPos1.x - chunkPos2.x;
        }
    }
}
