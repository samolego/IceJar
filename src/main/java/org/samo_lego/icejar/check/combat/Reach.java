package org.samo_lego.icejar.check.combat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.check.CheckType;

public class Reach extends CombatCheck {
    private float victimDistance;

    public Reach(ServerPlayer player) {
        super(CheckType.COMBAT_REACH, player);
    }

    @Override
    public boolean checkCombat(Level world, InteractionHand hand, Entity targetEntity, EntityHitResult hitResult) {
        this.victimDistance = targetEntity.distanceTo(player);
        final double maxDist = player.isCreative() ?
                CREATIVE_DISTANCE :
                getMaxDist(targetEntity);

        return this.victimDistance <= maxDist;
    }


    @Override
    public MutableComponent getAdditionalFlagInfo() {
        return new TextComponent("Distance: ")
                .append(new TextComponent(String.format("%.2f", this.victimDistance)).withStyle(ChatFormatting.RED));
    }

    /**
     * Gets max hit distance for the given entity.
     * Method taken from NoCheatPlus.
     *
     * @param damaged damaged entity.
     * @return max distance.
     */
    static double getMaxDist(final Entity damaged) {
        // Handle the EnderDragon differently.
        if (damaged instanceof EnderDragon)
            return 6.5D;
        else if (damaged instanceof Giant)
            return 1.5D;

        return IceJar.getInstance().getConfig().combat.maxSurvivalDistance;
    }
}
