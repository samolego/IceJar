package org.samo_lego.icejar.check.combat;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
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
        final boolean crit = this.isCritical(player);
        final boolean valid = this.isValid(player);

        if (crit && !valid) {
            ((IceJarPlayer) player).flag(this);
            return false;
        }
        return true;
    }

    /**
     * Checks whether hit was a critical one.
     * @param player player performing the hit.
     * @return true if hit was a critical one, otherwise false.
     */
    private boolean isCritical(final ServerPlayer player) {
        final IceJarPlayer ijPlayer = (IceJarPlayer) player;
        return player.fallDistance > 0.0f &&
                !player.isOnGround() &&
                !player.isPassenger() &&
                !player.hasEffect(MobEffects.BLINDNESS) &&
                !player.isInWater() &&
                !player.onClimbable();
    }

    /**
     * Checks whether hit is a valid critical hit.
     * @param player player performing the hit.
     * @return true if hit is a valid critical hit, otherwise false.
     */
    private boolean isValid(final ServerPlayer player) {
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

    @Override
    public InteractionResult interact(Player player, Level world, InteractionHand hand, Entity targetEntity, @Nullable EntityHitResult hitResult) {
        if (player instanceof ServerPlayer pl && !this.check(pl)) {
            // Critical hit was fake. Let's pretend we didn't see it though :)
            pl.connection.send(new ClientboundAnimatePacket(targetEntity, ClientboundAnimatePacket.SWING_MAIN_HAND)); // Vanilla sends this
            if (!(targetEntity instanceof ArmorStand))
                pl.connection.send(new ClientboundAnimatePacket(targetEntity, ClientboundAnimatePacket.HURT)); // Take damage

            pl.connection.send(new ClientboundAnimatePacket(targetEntity, ClientboundAnimatePacket.CRITICAL_HIT)); // Critical hit

            // Sound event
            pl.connection.send(new ClientboundSoundPacket(SoundEvents.PLAYER_ATTACK_CRIT,
                            player.getSoundSource(),
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            1.0f,
                            1.0f));

            // Figure out damage modifier to know if magic critical hit packet should be sent as well
            float modifier;
            ItemStack stack = pl.getItemInHand(hand);

            if (targetEntity instanceof LivingEntity) {
                modifier = EnchantmentHelper.getDamageBonus(stack, ((LivingEntity)targetEntity).getMobType());
            } else {
                modifier = EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED);
            }
            modifier *= pl.getAttackStrengthScale(0.5F);

            if (modifier > 0.0f) {
                pl.connection.send(new ClientboundAnimatePacket(targetEntity, ClientboundAnimatePacket.MAGIC_CRITICAL_HIT)); // Magic Critical hit

            }
            return InteractionResult.FAIL;

        }
        return InteractionResult.PASS;
    }
}
