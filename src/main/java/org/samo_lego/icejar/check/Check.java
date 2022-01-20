package org.samo_lego.icejar.check;

import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.icejar.config.IceConfig;
import org.samo_lego.icejar.util.IceJarPlayer;

public abstract class Check {

    private final CheckType checkType;
    protected final ServerPlayer player;
    private long lastFlagTime;
    private double violationLevel;
    private double cheatAttempts;

    public Check(CheckType checkType, ServerPlayer player) {
        this.checkType = checkType;
        this.player = player;
    }

    public abstract boolean check(Object ... params);

    public CheckType getType() {
        return checkType;
    }

    public long getCooldown() {
        return this.getOptions().cooldown;
    }

    public void flag() {
        ((IceJarPlayer) this.player).flag(this);
    }

    public long getLastFlagTime() {
        return this.lastFlagTime;
    }

    public void setLastFlagTime(long now) {
        this.lastFlagTime = now;
    }

    public int getMaxAttemptsBeforeFlag() {
        return this.getOptions().attemptsToFlag;
    }

    public double getViolationIncrease() {
        return this.getOptions().violationIncrease;
    }

    public double increaseViolationLevel() {
        this.violationLevel += this.getViolationIncrease();
        return this.violationLevel;
    }

    public double getMaxViolationLevel() {
        return this.getOptions().maxViolationLevel;
    }

    public void executeAction() {
        this.getOptions().action.execute(this.player);
    }

    public IceConfig.CheckConfig getOptions() {
        return IceConfig.getCheckOptions(this);
    }

    public double increaseCheatAttempts() {
        return ++this.cheatAttempts;
    }

    public void decreaseCheatAttempts() {
        --this.cheatAttempts;
    }

    public double getCheatAttempts() {
        return this.cheatAttempts;
    }

    public void setCheatAttempts(double attempts) {
        this.cheatAttempts = attempts;
    }
}
