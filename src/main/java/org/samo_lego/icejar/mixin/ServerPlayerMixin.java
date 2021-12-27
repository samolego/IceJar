package org.samo_lego.icejar.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.icejar.check.Check;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.util.AdditionalData;
import org.samo_lego.icejar.util.IceJarPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements IceJarPlayer {
    @Shadow public abstract ServerLevel getLevel();

    private AdditionalData additionalData;


    private final ServerPlayer player = (ServerPlayer) (Object) this;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void constructor(MinecraftServer minecraftServer, ServerLevel serverLevel, GameProfile gameProfile, CallbackInfo ci) {
        this.additionalData = new AdditionalData();
    }

    @Override
    public void flag(Check check) {
        this.flag(check.getType());
    }

    @Override
    public void flag(CheckType check) {
        this.player.getServer().getPlayerList().broadcastMessage(new TextComponent(this.player.getGameProfile().getName() + " was flagged for " + check.name()),  ChatType.SYSTEM, Util.NIL_UUID);
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
        return this.player.getFeetBlockState().getFluidState().isEmpty();
    }
}
