package org.samo_lego.icejar.check;

import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.icejar.check.combat.Angle;
import org.samo_lego.icejar.check.combat.Critical;
import org.samo_lego.icejar.check.combat.ImpossibleHit;
import org.samo_lego.icejar.check.combat.NoSwing;
import org.samo_lego.icejar.check.movement.Derp;
import org.samo_lego.icejar.check.movement.NoFall;
import org.samo_lego.icejar.check.movement.cancellable.BoatFly;
import org.samo_lego.icejar.check.movement.cancellable.PacketMovement;
import org.samo_lego.icejar.check.movement.cancellable.Timer;

import java.util.HashSet;
import java.util.Locale;
import java.util.function.Function;

import static org.samo_lego.icejar.IceJar.MOD_ID;
import static org.samo_lego.icejar.check.CheckCategory.ALL_CHECKS;
import static org.samo_lego.icejar.check.CheckCategory.COMBAT;
import static org.samo_lego.icejar.check.CheckCategory.FIXED_MOVEMENT;
import static org.samo_lego.icejar.check.CheckCategory.MOVEMENT;
import static org.samo_lego.icejar.check.CheckCategory.VEHICLE_MOVEMENT;
import static org.samo_lego.icejar.check.CheckCategory.category2checks;

public enum CheckType {
    COMBAT_CRITICAL(Critical::new, COMBAT),
    COMBAT_IMPOSSIBLEHIT(ImpossibleHit::new, COMBAT),
    COMBAT_ANGLE(Angle::new, COMBAT),
    COMBAT_NOSWING(NoSwing::new, COMBAT),

    MOVEMENT_NOFALL(NoFall::new, FIXED_MOVEMENT),
    MOVEMENT_DERP(Derp::new, FIXED_MOVEMENT),

    CMOVEMENT_PACKET(PacketMovement::new, MOVEMENT),
    VEHICLE_MOVE_BOATFLY(BoatFly::new, VEHICLE_MOVEMENT),
    CMOVEMENT_TIMER(Timer::new, MOVEMENT);

    private final Function<ServerPlayer, Check> checkConstructor;

    CheckType(Function<ServerPlayer, Check> constructor, CheckCategory category) {
        this.checkConstructor = constructor;

        category2checks.computeIfAbsent(category, k -> new HashSet<>()).add(this);
        ALL_CHECKS.computeIfAbsent(category, k -> new HashSet<>()).add(this);
    }

    public Check createCheck(ServerPlayer player) {
        return this.checkConstructor.apply(player);
    }

    public String getBypassPermission() {
        return MOD_ID + ".bypass." + this.toString().toLowerCase(Locale.ROOT);
    }
}
