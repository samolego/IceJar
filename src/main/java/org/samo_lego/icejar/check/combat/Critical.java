package org.samo_lego.icejar.check.combat;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.icejar.check.Check;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.util.IceJarPlayer;

public class Critical extends Check implements AttackEntityCallback {

    public Critical() {
        super(CheckType.COMBAT_CRITICAL);
    }

    @Override
    public boolean check(ServerPlayer player) {
        if (isCritical(player) && !isValid(player)) {
            ((IceJarPlayer) player).flag(this);
            return false;
        }
        return true;
    }

    private static boolean isCritical(final ServerPlayer player) {
        return player.fallDistance > 0.0f && !player.isOnGround() && !player.isPassenger()
                && !player.hasEffect(MobEffects.BLINDNESS)
                && !player.getLevel().getBlockState(new BlockPos(player.getEyePosition())).getBlock().equals(Blocks.LADDER);
    }

    private static boolean isValid(final ServerPlayer player) {
        final BlockState feetState = player.getFeetBlockState();
        final Block block = feetState.getBlock();
        return !block.equals(Blocks.SWEET_BERRY_BUSH) &&
                !block.equals(Blocks.COBWEB) &&
                !block.equals(Blocks.POWDER_SNOW) &&
                !block.equals(Blocks.LADDER) &&
                !((IceJarPlayer) player).isAboveFluid();
    }

    @Override
    public InteractionResult interact(Player player, Level world, InteractionHand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        if (player instanceof ServerPlayer pl)
            return this.check(pl) ? InteractionResult.PASS : InteractionResult.FAIL;
        return InteractionResult.PASS;
    }
}
