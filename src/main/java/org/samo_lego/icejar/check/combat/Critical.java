package org.samo_lego.icejar.check.combat;

import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.icejar.check.CheckType;

public class Critical extends CombatCheck {

    public Critical(ServerPlayer player) {
        super(CheckType.COMBAT_CRITICAL, player);
    }

    /**
     * Checks whether hit was a critical one.
     * @return true if hit was a critical one, otherwise false.
     */
    private boolean isCritical() {
        return player.fallDistance > 0.0f &&
                !player.isOnGround() &&
                !player.isPassenger() &&
                !player.hasEffect(MobEffects.BLINDNESS) &&
                !player.isInWater() &&
                !player.onClimbable();
    }

    /**
     * Checks whether hit is a valid critical hit.
     * @return true if hit is a valid critical hit, otherwise false.
     */
    private boolean isValid() {
        final BlockState feetState = player.getFeetBlockState();
        final Block block = feetState.getBlock();

        // Really basic check, detects "packet" mode only
        // todo - config
        return player.position().y() % 1.0d > 0.001d &&
                player.position().y() % 0.5d > 0.001d &&

                // Blocks that disallow critical hits.
                // todo - unhardcode
                !block.equals(Blocks.SWEET_BERRY_BUSH) &&
                !block.equals(Blocks.COBWEB) &&
                !block.equals(Blocks.POWDER_SNOW);
    }


    /**
     * Performs a critical hit check.
     * @param _world world the player is in.
     * @param hand hand the player is using.
     * @param targetEntity entity being hit.
     * @param _hitResult hit result.
     * @return InteractionResult#PASS if action can continue, otherwise InteractionResult#FAIL.
     */
    @Override
    public boolean checkCombat(Level _world, InteractionHand hand, Entity targetEntity, @Nullable EntityHitResult _hitResult) {
        final boolean crit = this.isCritical();
        final boolean valid = this.isValid();

        if (crit && !valid) {
            return false;
        }
        return true;

    }

    @Override
    protected void sendFakeHitData(Level _world, InteractionHand hand, Entity targetEntity, @Nullable EntityHitResult _hitResult) {
        player.connection.send(new ClientboundAnimatePacket(targetEntity, ClientboundAnimatePacket.CRITICAL_HIT)); // Critical hit

        // Sound event for critical hit
        player.connection.send(new ClientboundSoundPacket(SoundEvents.PLAYER_ATTACK_CRIT,
                this.player.getSoundSource(),
                this.player.getX(),
                this.player.getY(),
                this.player.getZ(),
                1.0f,
                1.0f));
    }
}
