package org.samo_lego.icejar.check.combat;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
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
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.icejar.check.Check;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.mixin.accessor.ALivingEntity;
import org.samo_lego.icejar.util.DataFaker;
import org.samo_lego.icejar.util.IceJarPlayer;

import java.util.Set;

import static org.samo_lego.icejar.check.CheckCategory.COMBAT;
import static org.samo_lego.icejar.check.CheckCategory.category2checks;

public abstract class CombatCheck extends Check {

    /** The maximum distance allowed to interact with an entity in creative mode. */
    public static final double CREATIVE_DISTANCE = 6D;

    public CombatCheck(CheckType checkType, ServerPlayer player) {
        super(checkType, player);
    }

    public abstract boolean checkCombat(Level world, InteractionHand hand, Entity targetEntity, EntityHitResult hitResult);

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
        final Set<CheckType> checks = category2checks.get(COMBAT);
        if (player instanceof ServerPlayer pl && checks != null) {
            if (hitResult == null) {
                hitResult = new EntityHitResult(targetEntity);
            }
            // Loop through all combat checks
            for (CheckType type : checks) {
                if (Permissions.check(player, type.getBypassPermission(), false)) continue;

                final CombatCheck check = ((IceJarPlayer) player).getCheck(type);
                System.out.println("Checking " + check.getType());

                // Check the hit
                if (!check.checkCombat(world, hand, targetEntity, hitResult)) {
                    System.out.println("Check failed");
                    // Hit was fake. Let's pretend we don't know though :)
                    if (!(targetEntity instanceof ArmorStand) && targetEntity instanceof LivingEntity le) {
                        // Send "hurt" to everyone but player that was attacked
                        DataFaker.broadcast(le, pl, new ClientboundAnimatePacket(targetEntity, ClientboundAnimatePacket.HURT));

                        // Play sound
                        DataFaker.sendSound(((ALivingEntity) le).getHurtSound(DamageSource.playerAttack(pl)), pl);
                    }

                    check.sendFakeHitData(world, hand, targetEntity, hitResult);

                    // Figure out damage modifier to know if magic critical hit packet should be sent as well
                    ItemStack stack = player.getItemInHand(hand);

                    float modifier = targetEntity instanceof LivingEntity ?
                        EnchantmentHelper.getDamageBonus(stack, ((LivingEntity) targetEntity).getMobType()) :
                        EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED);

                    modifier *= player.getAttackStrengthScale(0.5F);

                    if (modifier > 0.0f) {
                        player.magicCrit(targetEntity); // Magic Critical hit

                    }

                    // Flagging
                    if (check.increaseCheatAttempts() > check.getMaxAttemptsBeforeFlag())
                        check.flag();

                    return InteractionResult.FAIL;
                } else {
                    check.decreaseCheatAttempts();
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
        ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemStack.getItem() instanceof SwordItem) {
            // Send sweep attack sound
            DataFaker.sendSound(SoundEvents.PLAYER_ATTACK_SWEEP, player);
            player.sweepAttack();
        }
        DataFaker.sendSound(SoundEvents.PLAYER_ATTACK_STRONG, player);
    }


}
