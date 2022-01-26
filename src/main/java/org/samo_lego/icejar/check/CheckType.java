package org.samo_lego.icejar.check;

import org.jetbrains.annotations.Nullable;
import org.samo_lego.icejar.check.combat.Angle;
import org.samo_lego.icejar.check.combat.Critical;
import org.samo_lego.icejar.check.combat.ImpossibleHit;
import org.samo_lego.icejar.check.combat.NoSwing;
import org.samo_lego.icejar.check.combat.Reach;
import org.samo_lego.icejar.check.movement.Derp;
import org.samo_lego.icejar.check.movement.NoFall;
import org.samo_lego.icejar.check.movement.cancellable.BoatFly;
import org.samo_lego.icejar.check.movement.cancellable.PacketMovement;
import org.samo_lego.icejar.check.movement.cancellable.Timer;
import org.samo_lego.icejar.check.world.block.AutoSign;
import org.samo_lego.icejar.check.world.block.ReachBlock;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.samo_lego.icejar.IceJar.MOD_ID;
import static org.samo_lego.icejar.check.CheckCategory.ALL_CHECKS;
import static org.samo_lego.icejar.check.CheckCategory.COMBAT;
import static org.samo_lego.icejar.check.CheckCategory.ENTITY_INTERACT;
import static org.samo_lego.icejar.check.CheckCategory.FIXED_MOVEMENT;
import static org.samo_lego.icejar.check.CheckCategory.MOVEMENT;
import static org.samo_lego.icejar.check.CheckCategory.VEHICLE_MOVEMENT;
import static org.samo_lego.icejar.check.CheckCategory.WORLD_BLOCK_BREAK;
import static org.samo_lego.icejar.check.CheckCategory.WORLD_BLOCK_INTERACT;
import static org.samo_lego.icejar.check.CheckCategory.category2checks;

public enum CheckType {
    COMBAT_CRITICAL(Critical.class, COMBAT),
    COMBAT_IMPOSSIBLEHIT(ImpossibleHit.class, COMBAT),
    COMBAT_ANGLE(Angle.class, COMBAT),
    COMBAT_NOSWING(NoSwing.class, COMBAT),
    COMBAT_REACH(Reach.class, Set.of(COMBAT, ENTITY_INTERACT)),

    MOVEMENT_NOFALL(NoFall.class, FIXED_MOVEMENT),
    MOVEMENT_DERP(Derp.class, FIXED_MOVEMENT),

    CMOVEMENT_PACKET(PacketMovement.class, MOVEMENT),
    VEHICLE_MOVE_BOATFLY(BoatFly.class, VEHICLE_MOVEMENT),
    CMOVEMENT_TIMER(Timer.class, MOVEMENT),
    SPECIAL_JESUS,
    WORLD_BLOCK_REACH(ReachBlock.class, Set.of(WORLD_BLOCK_BREAK, WORLD_BLOCK_INTERACT)),
    WORLD_BLOCK_AUTOSIGN(AutoSign.class, WORLD_BLOCK_INTERACT);


    private final Class<?> checkClass;

    <T extends Check> CheckType(Class<T> checkClass, Iterable<CheckCategory> categories, boolean exclude) {
        this.checkClass = checkClass;

        if (!exclude && categories != null) {
            categories.forEach(category -> {
                category2checks.computeIfAbsent(category, k -> new HashSet<>()).add(this);
                ALL_CHECKS.computeIfAbsent(category, k -> new HashSet<>()).add(this);
            });
        }
    }

    <T extends Check> CheckType(Class<T> checkClass, CheckCategory category) {
        this(checkClass, Set.of(category), false);
    }

    CheckType() {
        this(null, null, true);
    }

    <T extends Check> CheckType(Class<T> reachBlockClass, Set<CheckCategory> categories) {
        this(reachBlockClass, categories, false);
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
