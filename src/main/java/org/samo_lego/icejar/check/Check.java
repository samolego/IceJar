package org.samo_lego.icejar.check;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.casts.IceJarPlayer;
import org.samo_lego.icejar.config.IceConfig;

public abstract class Check {

    protected final CheckType checkType;
    protected ServerPlayer player;
    protected IceJarPlayer ijp;
    private long lastFlagTime;
    protected double violationLevel;
    protected int cheatAttempts;

    public Check(CheckType checkType, ServerPlayer player) {
        this.checkType = checkType;
        this.player = player;
        this.ijp = (IceJarPlayer) this.player;
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

    public double getViolationLevel() {
        return this.violationLevel;
    }

    /**
     * Gets additional information about check circumstances as a {@link MutableComponent}.
     * @return additional information about check circumstances.
     */
    public MutableComponent getAdditionalFlagInfo() {
        return Component.empty();
    }

    public double increaseViolationLevel() {
        this.violationLevel += this.getViolationIncrease();
        return this.violationLevel;
    }

    public double getMaxViolationLevel() {
        return this.getOptions().maxViolationLevel;
    }

    public void executeAction() {
        this.getOptions().action.execute(this.player, this);
    }

    public IceConfig.CheckConfig getOptions() {
        return IceConfig.getCheckOptions(this);
    }

    public boolean trainModeActive() {
        return this.getOptions().trainMode || IceJar.getInstance().getConfig().trainMode;
    }

    public int increaseCheatAttempts() {
        if (++this.cheatAttempts > this.getMaxAttemptsBeforeFlag()) {
            this.cheatAttempts = this.getMaxAttemptsBeforeFlag() + 1;
        }
        return this.cheatAttempts;
    }

    public void decreaseCheatAttempts() {
        if(--this.cheatAttempts < 0) {
            this.cheatAttempts = 0;
        }
    }

    public int getCheatAttempts() {
        return this.cheatAttempts;
    }

    public void setCheatAttempts(int attempts) {
        this.cheatAttempts = attempts;
    }

    public void setPlayer(ServerPlayer player) {
        this.player = player;
    }
}
