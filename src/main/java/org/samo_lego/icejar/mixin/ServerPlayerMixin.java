package org.samo_lego.icejar.mixin;

import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.icejar.check.Check;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.util.IceJarPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements IceJarPlayer {

    @Unique
    private final HashMap<CheckType, Check> playerChecks = new HashMap<>();
    @Unique
    private final ServerPlayer player = (ServerPlayer) (Object) this;
    @Unique
    private boolean guiOpen = false;

    @Override
    public Check getCheck(CheckType checkType) {
        Check check = this.playerChecks.get(checkType);
        if (check == null) {
            // Create new check from type
            check = checkType.createCheck(this.player);
            this.playerChecks.put(checkType, check);
        }

        return check;
    }

    @Override
    public void flag(final Check check) {
        // See if cooldown is still active on check
        final long now  = System.currentTimeMillis();
        final long timeDelta = now - check.getLastFlagTime();

        if (check.getCooldown() < timeDelta) {
            final double newLvl = check.increaseViolationLevel();
            final double max = check.getMaxViolationLevel();
            if (newLvl > max && max > 0) {
                check.executeAction();
            }
            this.player.getServer().getPlayerList().broadcastMessage(new TextComponent(this.player.getGameProfile().getName() + " was flagged for " + check.getType()),  ChatType.SYSTEM, Util.NIL_UUID);
            check.setLastFlagTime(now);
        }
    }

    @Override
    public void setOpenGUi(boolean open) {
        this.guiOpen = true;
    }

    @Override
    public boolean hasOpenGui() {
        return this.guiOpen;
    }
}
