package org.samo_lego.icejar.check;

import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.icejar.check.combat.Critical;
import org.samo_lego.icejar.check.movement.NoFall;

import java.util.function.Function;

public enum CheckType {
    COMBAT_CRITICAL(Critical::new),
    MOVEMENT_NOFALL(NoFall::new);

    private final Function<ServerPlayer, Check> checkConstructor;

    CheckType(Function<ServerPlayer, Check> constructor) {
        this.checkConstructor = constructor;
    }

    public Check createCheck(ServerPlayer player) {
        return this.checkConstructor.apply(player);
    }
}
