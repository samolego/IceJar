package org.samo_lego.icejar.check.combat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.samo_lego.icejar.check.CheckType;

public class NoSwing extends CombatCheck {
    private boolean hasSwingedHand;

    public NoSwing(ServerPlayer player) {
        super(CheckType.COMBAT_NOSWING, player);
        this.hasSwingedHand = true;
    }

    @Override
    public boolean checkCombat(Level world, InteractionHand hand, Entity targetEntity, EntityHitResult hitResult) {
        boolean canHit = this.hasSwingedHand;
        this.hasSwingedHand = false;
        return canHit;
    }

    public void onSwing() {
        this.hasSwingedHand = true;
    }
}
