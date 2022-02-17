package org.samo_lego.icejar.check.movement;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.mixin.accessor.AServerboundMovePlayerPacket;

public class Derp extends MovementCheck {
    private float xRot;

    public Derp(ServerPlayer player) {
        super(CheckType.MOVEMENT_DERP, player);
    }

    @Override
    public boolean checkMovement(ServerboundMovePlayerPacket packet) {
        this.xRot = Math.abs(packet.getXRot(player.getXRot()));
        if (packet.hasRotation() && this.xRot > 90) {
            ((AServerboundMovePlayerPacket) packet).setXRot(player.getXRot());
            return false;
        }
        return true;
    }


    @Override
    public MutableComponent getAdditionalFlagInfo() {
        return new TranslatableComponent("Pitch: ")
                .append(new TextComponent(String.format("%.2f", this.xRot)).withStyle(ChatFormatting.RED));
    }
}
