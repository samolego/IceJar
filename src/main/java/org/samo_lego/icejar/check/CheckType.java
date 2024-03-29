package org.samo_lego.icejar.check;

import org.jetbrains.annotations.Nullable;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.check.combat.Angle;
import org.samo_lego.icejar.check.combat.Critical;
import org.samo_lego.icejar.check.combat.ImpossibleHit;
import org.samo_lego.icejar.check.combat.NoSwing;
import org.samo_lego.icejar.check.combat.Reach;
import org.samo_lego.icejar.check.inventory.ImpossibleUse;
import org.samo_lego.icejar.check.movement.Derp;
import org.samo_lego.icejar.check.movement.NoFall;
import org.samo_lego.icejar.check.movement.cancellable.FastLadder;
import org.samo_lego.icejar.check.movement.cancellable.NoDeceleration;
import org.samo_lego.icejar.check.movement.cancellable.Timer;
import org.samo_lego.icejar.check.movement.cancellable.WrongRotation;
import org.samo_lego.icejar.check.movement.cancellable.flight.BasicFlight;
import org.samo_lego.icejar.check.movement.cancellable.vehicle.BoatFly;
import org.samo_lego.icejar.check.movement.cancellable.vehicle.EntityControl;
import org.samo_lego.icejar.check.world.block.AirPlace;
import org.samo_lego.icejar.check.world.block.AutoSign;
import org.samo_lego.icejar.check.world.block.BlockDirection;
import org.samo_lego.icejar.check.world.block.BlockFace;
import org.samo_lego.icejar.check.world.block.FakeBlockPos;
import org.samo_lego.icejar.check.world.block.ImpossibleBlockAction;
import org.samo_lego.icejar.check.world.block.ReachBlock;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.samo_lego.icejar.IceJar.MOD_ID;
import static org.samo_lego.icejar.check.CheckCategory.ALL_CHECKS;
import static org.samo_lego.icejar.check.CheckCategory.COMBAT;
import static org.samo_lego.icejar.check.CheckCategory.ENTITY_INTERACT;
import static org.samo_lego.icejar.check.CheckCategory.INVENTORY;
import static org.samo_lego.icejar.check.CheckCategory.MOVEMENT_IMMUTABLE;
import static org.samo_lego.icejar.check.CheckCategory.MOVEMENT_MUTABLE;
import static org.samo_lego.icejar.check.CheckCategory.VEHICLE_MOVEMENT;
import static org.samo_lego.icejar.check.CheckCategory.WORLD_BLOCK_BREAK;
import static org.samo_lego.icejar.check.CheckCategory.WORLD_BLOCK_INTERACT;
import static org.samo_lego.icejar.check.CheckCategory.category2checks;

public enum CheckType {
    CMOVEMENT_FAST_LADDER(FastLadder.class, MOVEMENT_MUTABLE),
    CMOVEMENT_NO_DECELERATION(NoDeceleration.class, MOVEMENT_MUTABLE),
    CMOVEMENT_TIMER(Timer.class, MOVEMENT_MUTABLE),
    COMBAT_ANGLE(Angle.class, COMBAT),

    COMBAT_CRITICAL(Critical.class, COMBAT),
    COMBAT_IMPOSSIBLEHIT(ImpossibleHit.class, COMBAT),

    COMBAT_NOSWING(NoSwing.class, COMBAT),
    COMBAT_REACH(Reach.class, Set.of(COMBAT, ENTITY_INTERACT)),
    INVENTORY_IMPOSSIBLE_ITEM_USE(ImpossibleUse.class, INVENTORY),
    MOVEMENT_DERP(Derp.class, MOVEMENT_IMMUTABLE),
    MOVEMENT_NOFALL(NoFall.class, MOVEMENT_IMMUTABLE),
    SPECIAL_JESUS,
    VEHICLE_MOVE_BOATFLY(BoatFly.class, VEHICLE_MOVEMENT),
    WORLD_BLOCK_AUTOSIGN(AutoSign.class, WORLD_BLOCK_INTERACT),
    WORLD_BLOCK_DIRECTION(BlockDirection.class, Set.of(WORLD_BLOCK_INTERACT, WORLD_BLOCK_BREAK)),
    WORLD_BLOCK_IMPOSSIBLE_ACTION(ImpossibleBlockAction.class, Set.of(WORLD_BLOCK_BREAK, WORLD_BLOCK_INTERACT)), WORLD_BLOCK_PLACE_AIR(AirPlace.class, WORLD_BLOCK_INTERACT),
    WORLD_BLOCK_REACH(ReachBlock.class, Set.of(WORLD_BLOCK_BREAK, WORLD_BLOCK_INTERACT)),
    CMOVEMENT_ROTATION(WrongRotation.class, MOVEMENT_MUTABLE),
    VEHICLE_MOVE_ENTITY_CONTROL(EntityControl.class, VEHICLE_MOVEMENT),
    CMOVEMENT_BASIC_FLIGHT(BasicFlight.class, MOVEMENT_MUTABLE),
    WORLD_BLOCK_FACE(BlockFace.class, Set.of(WORLD_BLOCK_BREAK, WORLD_BLOCK_INTERACT)),
    WORLD_BLOCK_FAKEPOS(FakeBlockPos.class, WORLD_BLOCK_BREAK);

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

    <T extends Check> CheckType(Class<T> checkClass, CheckCategory category, boolean exclude) {
        this(checkClass, Set.of(category), exclude);
    }

    CheckType() {
        this(null, (Iterable<CheckCategory>) null, true);
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
        return MOD_ID + ".checks.bypass." + this.toString().toLowerCase(Locale.ROOT);
    }

    public String getReportPermission() {
        return MOD_ID + ".checks.get_report." + this.toString().toLowerCase(Locale.ROOT);
    }

    public String getTrainPermission() {
        return MOD_ID + ".checks.train." + this.toString().toLowerCase(Locale.ROOT);
    }

    public boolean isEnabled() {
        var cfg = IceJar.getInstance().getConfig().checkConfigs.get(this);

        return cfg != null ? cfg.enabled : IceJar.getInstance().getConfig().DEFAULT.enabled;
    }
}
