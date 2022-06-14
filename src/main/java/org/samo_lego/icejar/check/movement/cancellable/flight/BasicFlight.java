package org.samo_lego.icejar.check.movement.cancellable.flight;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.check.movement.cancellable.CancellableMovementCheck;

public class BasicFlight extends CancellableMovementCheck {
    private double lastDiffY;
    private short airTicks;
    private short airTicksBeforeReset;

    boolean lastTickOnGround;
    boolean lLastTickOnGround;

    public BasicFlight(ServerPlayer player) {
        super(CheckType.CMOVEMENT_BASIC_FLIGHT, player);
        this.lastTickOnGround = player.isOnGround();
        this.lLastTickOnGround = player.isOnGround();
    }

    @Override
    public boolean checkMovement(ServerboundMovePlayerPacket packet) {
        if (!player.isFallFlying() &&
            !this.lastTickOnGround && !this.lLastTickOnGround && !player.isOnGround() &&  // Checking ground status
            !player.isPassenger() &&
            !player.getAbilities().flying &&
            player.getEffect(MobEffects.LEVITATION) == null) {

            this.lLastTickOnGround = this.lastTickOnGround;
            this.lastTickOnGround = player.isOnGround();

            // Get Y velocity
            final double diffY = packet.getY(player.getY()) - player.getY();
            final double lastDiff = this.lastDiffY;
            this.lastDiffY = diffY;

            if (diffY < 0.0) {
                // Player is falling

                // Check if difference is greater than before
                if (diffY < lastDiff && lastDiff != 0.0) {
                    return false;
                }
            } else if (diffY > 0.0) {
                // Check if diffY is smaller than the last tick diffY
                if (diffY > lastDiff && lastDiff != 0.0) {
                    return false;
                }
            } else {
                ++this.airTicks;
                if (this.airTicks > IceJar.getInstance().getConfig().movement.maxAirTicks) {
                    if (this.trainModeActive()) {
                        IceJar.getInstance().getConfig().movement.maxAirTicks = this.airTicks;
                        return true;
                    }

                    this.airTicksBeforeReset = this.airTicks;
                    this.airTicks = 0;
                    return false;
                }
            }

        }
        if (this.airTicks > 0) {
            --this.airTicks;
        }
        this.lastDiffY = 0.0;

        this.lLastTickOnGround = this.lastTickOnGround;
        this.lastTickOnGround = player.isOnGround();

        return true;
    }


    @Override
    public MutableComponent getAdditionalFlagInfo() {
        return Component.translatable("Y difference: %s\nAir ticks: %s",
                Component.literal(String.format("%.2f", this.lastDiffY)).withStyle(ChatFormatting.RED),
                Component.literal(String.format("%d", this.airTicksBeforeReset)).withStyle(ChatFormatting.RED));
    }
}
