package org.samo_lego.icejar.check.movement;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.icejar.casts.IceJarPlayer;
import org.samo_lego.icejar.check.Check;
import org.samo_lego.icejar.check.CheckType;

import java.util.Set;

import static org.samo_lego.icejar.check.CheckCategory.MOVEMENT_IMMUTABLE;
import static org.samo_lego.icejar.check.CheckCategory.category2checks;

public abstract class MovementCheck extends Check {

    public MovementCheck(CheckType checkType, ServerPlayer player) {
        super(checkType, player);
    }

    @Override
    public boolean check(Object ... params) {
        if (params.length != 1)
            throw new IllegalArgumentException("MovementCheck.check() requires 1 parameter");
        return this.checkMovement((ServerboundMovePlayerPacket) params[0]);
    }

    public abstract boolean checkMovement(ServerboundMovePlayerPacket packet);

    public static boolean performCheck(ServerPlayer player, ServerboundMovePlayerPacket packet) {
        // Loop through all movement checks
        final Set<CheckType> checks = category2checks.get(MOVEMENT_IMMUTABLE);
        if (checks != null) {
            for (CheckType type : checks) {
                if (Permissions.check(player, type.getBypassPermission(), false)) continue;

                final MovementCheck check = ((IceJarPlayer) player).getCheck(type);

                // Check movement
                if (!check.checkMovement(packet) && check.increaseCheatAttempts() > check.getMaxAttemptsBeforeFlag()) {
                    check.flag();

                    // Jesus ruberband
                    if (check instanceof NoFall nf && nf.hasJesus())
                        return false;
                }
            }
        }
        return true;
    }
}
