package org.samo_lego.icejar.mixin.newchunks;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.module.NewChunks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ChunkGenerator.class)
public class MChunkGenerator_NewChunkMarker {
	@Inject(method = "applyBiomeDecoration",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/SharedConstants;debugVoidTerrain(Lnet/minecraft/world/level/ChunkPos;)Z"),
			locals = LocalCapture.CAPTURE_FAILHARD)
	private void ij_generate(WorldGenLevel worldGenLevel, ChunkAccess chunkAccess, StructureManager structureManager, CallbackInfo ci, ChunkPos chunkPos) {
		if (IceJar.getInstance().getConfig().fixes.newChunks) {
			NewChunks.addChunk(worldGenLevel.getLevel(), chunkPos);
		}
	}
}
