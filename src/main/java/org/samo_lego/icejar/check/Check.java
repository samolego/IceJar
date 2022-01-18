package org.samo_lego.icejar.check;

import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.icejar.util.IceJarPlayer;

public abstract class Check {

    private final CheckType checkType;
    protected final ServerPlayer player;
    private final long lastFlagTime;

    public Check(CheckType checkType, ServerPlayer player) {
        this.checkType = checkType;
        this.lastFlagTime = 0;
        this.player = player;
    }

    public abstract boolean check(Object ... params);

    public CheckType getType() {
        return checkType;
    }

    public long getCooldown() {
        return 0;
    }

    public void flag() {
        ((IceJarPlayer) this.player).flag(this);
    }

    public long getLastFlag() {
        return this.lastFlagTime;
    }
}
