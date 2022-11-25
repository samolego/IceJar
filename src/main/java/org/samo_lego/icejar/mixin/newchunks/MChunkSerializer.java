package org.samo_lego.icejar.mixin.newchunks;

import com.mojang.datafixers.util.Either;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.casts.IJChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * Inspired by <a href="https://github.com/TheCSDev/mc-no-unused-chunks/blob/main/nounusedchunks-fabric-1.19/src/main/java/thecsdev/nounusedchunks/mixin/ChunkStatusMixin.java">No Unused Chunks mod</a>.
 */
//@Mixin(ChunkSerializer.class)
@Mixin(ChunkStatus.class)
public class MChunkSerializer {
	@Inject(method = "generate", at = @At("RETURN"))
	private void ij_generate(Executor executor, ServerLevel serverLevel, ChunkGenerator chunkGenerator, StructureTemplateManager structureTemplateManager, ThreadedLevelLightEngine threadedLevelLightEngine, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> function, List<ChunkAccess> list, boolean bl, CallbackInfoReturnable<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> cir) {
		if (IceJar.getInstance().getConfig().fixes.newChunks) {
			Runnable task = () -> {
				try {
					var returnValue = cir.getReturnValue();
					if (returnValue == null || !returnValue.isDone())
						return; // do not remove the isDone check, weird deadlocks occur when you do

					ChunkAccess chunk = returnValue.get().left().orElse(null);
					if (chunk == null) return;

					// Mark the chunk as new
					((IJChunkAccess) chunk).ij_setNewChunk(true);
				} catch (Exception ignored) {
				}
			};

			// Let the server execute the task once it is able to
			serverLevel.getServer().execute(task);
		}
	}

	/*@Inject(method = "read", at = @At("RETURN"))  // ChunkSerializer.class
	private static void ij_onChunkRead(ServerLevel serverLevel, PoiManager poiManager, ChunkPos chunkPos, CompoundTag compoundTag, CallbackInfoReturnable<ProtoChunk> cir) {
		((IJChunkAccess) cir.getReturnValue()).ij_setNewChunk(false);
	}*/
}
