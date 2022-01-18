package org.samo_lego.icejar.mixin;

import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.icejar.check.Check;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.util.AdditionalData;
import org.samo_lego.icejar.util.IceJarPlayer;
import org.spongepowered.asm.mixin.Mixin;

import java.util.HashMap;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements IceJarPlayer {


    private final AdditionalData additionalData = new AdditionalData();
    private final HashMap<CheckType, Check> playerChecks = new HashMap<>();

    private final ServerPlayer player = (ServerPlayer) (Object) this;

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
        this.player.getServer().getPlayerList().broadcastMessage(new TextComponent(this.player.getGameProfile().getName() + " was flagged for " + check.getType()),  ChatType.SYSTEM, Util.NIL_UUID);
    }

    @Override
    public AdditionalData getAdditionalData() {
        return this.additionalData;
    }

    @Override
    public boolean isNearGround() {
        return this.additionalData.onGround() ||
                this.additionalData.wasOnGround() ||
                this.additionalData.wasLastOnGround();
    }

    @Override
    public boolean isAboveFluid() {
        return !this.player.getFeetBlockState().getFluidState().isEmpty();
    }
}
