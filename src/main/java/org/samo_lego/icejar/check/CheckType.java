package org.samo_lego.icejar.check;

import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.icejar.check.combat.Critical;
import org.samo_lego.icejar.check.movement.NoFall;

import java.util.HashSet;
import java.util.function.Function;

import static org.samo_lego.icejar.check.CheckCategory.ALL_CHECKS;
import static org.samo_lego.icejar.check.CheckCategory.category2checks;

public enum CheckType {
    COMBAT_CRITICAL(Critical::new, CheckCategory.COMBAT),
    MOVEMENT_NOFALL(NoFall::new, CheckCategory.FIXED_MOVEMENT);

    private final Function<ServerPlayer, Check> checkConstructor;

    CheckType(Function<ServerPlayer, Check> constructor, CheckCategory category) {
        this.checkConstructor = constructor;

        category2checks.computeIfAbsent(category, k -> new HashSet<>()).add(this);
        ALL_CHECKS.computeIfAbsent(category, k -> new HashSet<>()).add(this);
    }

    public Check createCheck(ServerPlayer player) {
        return this.checkConstructor.apply(player);
    }
}
