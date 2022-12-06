package org.samo_lego.icejar.mixin.newchunks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.samo_lego.icejar.module.NewChunks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LiquidBlock.class)
public class MLiquidBlock {
    @Inject(method = "onPlace",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/Fluid;I)V"),
            cancellable = true)
    private void ij_onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl, CallbackInfo ci) {
        if (NewChunks.tryFastFluidSpread(level, blockPos, blockState)) {
            ci.cancel();
        }
    }

    @Inject(method = "neighborChanged",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/Fluid;I)V"),
            cancellable = true)
    private void ij_neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl, CallbackInfo ci) {
        if (NewChunks.tryFastFluidSpread(level, blockPos, blockState)) {
            ci.cancel();
        }
    }
}
