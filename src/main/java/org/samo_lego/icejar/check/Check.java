package org.samo_lego.icejar.check;

import net.minecraft.server.level.ServerPlayer;

public abstract class Check {
    private final CheckType checkType;

    public Check(CheckType checkType) {
        this.checkType = checkType;
    }

    public CheckType getType() {
        return checkType;
    }

    public abstract boolean check(ServerPlayer player);
}
