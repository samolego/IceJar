package org.samo_lego.icejar.check;

import org.jetbrains.annotations.Nullable;
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

import static org.samo_lego.icejar.IceJar.MOD_ID;
import static org.samo_lego.icejar.check.CheckCategory.ALL_CHECKS;
import static org.samo_lego.icejar.check.CheckCategory.COMBAT;
import static org.samo_lego.icejar.check.CheckCategory.FIXED_MOVEMENT;
import static org.samo_lego.icejar.check.CheckCategory.MOVEMENT;
import static org.samo_lego.icejar.check.CheckCategory.VEHICLE_MOVEMENT;
import static org.samo_lego.icejar.check.CheckCategory.category2checks;

public enum CheckType {
    COMBAT_CRITICAL(Critical.class, COMBAT),
    COMBAT_IMPOSSIBLEHIT(ImpossibleHit.class, COMBAT),
    COMBAT_ANGLE(Angle.class, COMBAT),
    COMBAT_NOSWING(NoSwing.class, COMBAT),

    MOVEMENT_NOFALL(NoFall.class, FIXED_MOVEMENT),
    MOVEMENT_DERP(Derp.class, FIXED_MOVEMENT),

    CMOVEMENT_PACKET(PacketMovement.class, MOVEMENT),
    VEHICLE_MOVE_BOATFLY(BoatFly.class, VEHICLE_MOVEMENT),
    CMOVEMENT_TIMER(Timer.class, MOVEMENT),
    SPECIAL_JESUS;


    private final Class<?> checkClass;

    <T extends Check> CheckType(Class<T> checkClass, CheckCategory category, boolean exclude) {
        this.checkClass = checkClass;

        if (!exclude) {
            category2checks.computeIfAbsent(category, k -> new HashSet<>()).add(this);
            ALL_CHECKS.computeIfAbsent(category, k -> new HashSet<>()).add(this);
        }
    }

    <T extends Check> CheckType(Class<T> checkClass, CheckCategory category) {
        this(checkClass, category, false);
    }

    CheckType() {
        this(null, null, true);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> Class<T> getCheckClass() {
        return (Class<T>) this.checkClass;
    }

    public String getBypassPermission() {
        return MOD_ID + ".checks." + this.toString().toLowerCase(Locale.ROOT) + ".bypass";
    }
}
