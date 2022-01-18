package org.samo_lego.icejar.check.movement;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.icejar.check.Check;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.util.IceJarPlayer;

import static org.samo_lego.icejar.check.CheckCategory.FIXED_MOVEMENT;
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
        if (category2checks.get(FIXED_MOVEMENT) != null) {
            for (CheckType type : category2checks.get(FIXED_MOVEMENT)) {
                final MovementCheck check = (MovementCheck) ((IceJarPlayer) player).getCheck(type);

                // Check movement
                if (!check.checkMovement(packet)) {
                    check.flag();
                }
            }
        }
        return true;
    }
}
