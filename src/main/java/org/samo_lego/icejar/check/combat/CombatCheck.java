package org.samo_lego.icejar.check.combat;

import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.icejar.check.Check;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.mixin.accessor.ALivingEntity;
import org.samo_lego.icejar.util.IceJarPlayer;

import static org.samo_lego.icejar.check.CheckCategory.COMBAT;
import static org.samo_lego.icejar.check.CheckCategory.category2checks;

public abstract class CombatCheck extends Check {

    public CombatCheck(CheckType checkType, ServerPlayer player) {
        super(checkType, player);
    }

    public abstract boolean checkCombat(Level world, InteractionHand hand, Entity targetEntity, @Nullable EntityHitResult hitResult);

    @Override
    public boolean check(Object ... params) {
        if (params.length != 4)
            throw new IllegalArgumentException("CombatCheck.check() requires 4 parameters");

        return this.checkCombat((Level) params[0], (InteractionHand) params[1], (Entity) params[2], (EntityHitResult) params[3]);
    }

    /**
     * Performs hit check.
     * @param player player that is attacking.
     * @param world world the player is in.
     * @param hand hand the player is swinging with.
     * @param targetEntity entity that was hit.
     * @param hitResult hit result.
     * @return {@link InteractionResult} of the hit.
     */
    public static InteractionResult performCheck(Player player, Level world, InteractionHand hand, Entity targetEntity, @Nullable EntityHitResult hitResult) {
        if (player instanceof ServerPlayer pl && category2checks.get(COMBAT) != null) {
            // Loop through all combat checks
            for (CheckType type : category2checks.get(COMBAT)) {
                final CombatCheck check = (CombatCheck) ((IceJarPlayer) player).getCheck(type);

                // Check the hit
                if (!check.checkCombat(world, hand, targetEntity, hitResult)) {
                    // Hit was fake. Let's pretend we don't know though :)
                    if (!(targetEntity instanceof ArmorStand) && targetEntity instanceof LivingEntity le) {
                        pl.connection.send(new ClientboundAnimatePacket(targetEntity, ClientboundAnimatePacket.HURT)); // Take damage

                        // Play sound
                        pl.connection.send(new ClientboundSoundPacket(((ALivingEntity) le).getHurtSound(DamageSource.playerAttack(pl)),
                                player.getSoundSource(),
                                player.getX(),
                                player.getY(),
                                player.getZ(),
                                1.0f,
                                1.0f));
                    }

                    check.sendFakeHitData(world, hand, targetEntity, hitResult);

                    // Figure out damage modifier to know if magic critical hit packet should be sent as well
                    ItemStack stack = player.getItemInHand(hand);

                    float modifier = targetEntity instanceof LivingEntity ?
                        EnchantmentHelper.getDamageBonus(stack, ((LivingEntity) targetEntity).getMobType()) :
                        EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED);

                    modifier *= player.getAttackStrengthScale(0.5F);

                    if (modifier > 0.0f) {
                        pl.connection.send(new ClientboundAnimatePacket(targetEntity, ClientboundAnimatePacket.MAGIC_CRITICAL_HIT)); // Magic Critical hit

                    }

                    return InteractionResult.FAIL;
                }
            }
        }

        return InteractionResult.PASS;
    }

    /**
     * Sends additional data to the client to make it show the fake hit.
     * @param world world the player is in.
     * @param hand hand the player is swinging with.
     * @param targetEntity entity that was hit.
     * @param hitResult hit result.
     */
    protected void sendFakeHitData(Level world, InteractionHand hand, Entity targetEntity, @Nullable EntityHitResult hitResult) {
        player.connection.send(new ClientboundSoundPacket(SoundEvents.PLAYER_ATTACK_STRONG,
                this.player.getSoundSource(),
                this.player.getX(),
                this.player.getY(),
                this.player.getZ(),
                1.0f,
                1.0f));
    }
}
