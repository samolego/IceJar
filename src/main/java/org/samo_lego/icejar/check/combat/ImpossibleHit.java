package org.samo_lego.icejar.check.combat;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.samo_lego.icejar.casts.IceJarPlayer;
import org.samo_lego.icejar.check.CheckType;

import static org.samo_lego.icejar.check.combat.Reach.getMaxDist;
import static org.samo_lego.icejar.util.ChatColor.styleBoolean;


/**
 * A check to see if players try to attack entities while performing something else
 * or having obstructions in their way.
 * (i.e.: while blocking)
 */
public class ImpossibleHit extends CombatCheck {
    private boolean noWall;

    public ImpossibleHit(ServerPlayer player) {
        super(CheckType.COMBAT_IMPOSSIBLEHIT, player);
    }

    @Override
    public boolean checkCombat(Level world, InteractionHand hand, Entity targetEntity, EntityHitResult hitResult) {
        // Check if there's a wall in front of the player
        final double victimDistance = Math.sqrt(hitResult.distanceTo(player));
        final double dist = player.isCreative() ?
                CREATIVE_DISTANCE :
                getMaxDist(targetEntity);

        final BlockHitResult blockHit = (BlockHitResult) player.pick(dist, 0, false);

        // Cannot hit targets with a wall in front of them, open gui, using item etc.
        this.noWall = blockHit.getType().equals(HitResult.Type.MISS) ||
                Math.sqrt(blockHit.distanceTo(player)) + 0.5D >= victimDistance ||
                player.isPassenger(); // ignore players riding other entities
        return noWall &&
                !((IceJarPlayer) player).ij$hasOpenGui() &&
                !player.isUsingItem() &&
                !player.isBlocking();
    }

    @Override
    public MutableComponent getAdditionalFlagInfo() {
        return Component.literal("Wall: ")
                .append(styleBoolean(!this.noWall))
                .append("\n")
                .append(Component.literal("GUI open: ")
                .append(styleBoolean(((IceJarPlayer) player).ij$hasOpenGui())))
                .append("\n")
                .append(Component.literal("Using item: ")
                .append(styleBoolean(player.isUsingItem())))
                .append("\n")
                .append(Component.literal("Blocking: ")
                .append(styleBoolean(player.isBlocking())));
    }
}
