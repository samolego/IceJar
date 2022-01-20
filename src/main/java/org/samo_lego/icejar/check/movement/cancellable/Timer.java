package org.samo_lego.icejar.check.movement.cancellable;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.config.IceConfig;

public class Timer extends CancellableMovementCheck {
    private long lastPacketTime;
    private long packetRate;

    public Timer(ServerPlayer player) {
        super(CheckType.CMOVEMENT_TIMER, player);
    }

    @Override
    public boolean checkMovement(ServerboundMovePlayerPacket packet) {
        if(packet instanceof ServerboundMovePlayerPacket.PosRot ||
            packet instanceof ServerboundMovePlayerPacket.Rot ||
            packet.getX(player.getX()) != player.getX() ||
            packet.getY(player.getY()) != player.getY() ||
            packet.getZ(player.getZ()) != player.getZ()) {

            final IceConfig config = IceJar.getInstance().getConfig();

            final long currentPacketTime = System.currentTimeMillis();
            final long lastTime = this.lastPacketTime;
            this.lastPacketTime = currentPacketTime;

            if(lastTime != 0) {
                this.packetRate += (50 + lastTime - currentPacketTime);
                boolean valid = this.packetRate <= config.movement.timerThreshold;

                if (!valid) {
                    this.packetRate = 0;
                    return false;
                }
            }
            if (config.trainMode) {
                config.movement.timerThreshold = Math.max(config.movement.timerThreshold, this.packetRate);
            }
        } else {
            this.packetRate = 0;
            this.lastPacketTime = 0;
        }
        return true;
    }

    public void rebalance() {
        this.packetRate -= 50;
    }
}
