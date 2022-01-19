package org.samo_lego.icejar.util;


import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.icejar.check.Check;
import org.samo_lego.icejar.check.CheckType;

public interface IceJarPlayer {
    void flag(final Check check);

    Check getCheck(CheckType type);

    void ij$setOpenGUI(boolean open);
    boolean ij$hasOpenGui();

    boolean isNearGround();

    void ij$setOnGround(boolean ij$onGround);

    void updateGroundStatus();

    void setVehicleMovement(ServerboundMoveVehiclePacket packet);
    Vec3 getLastVehicleMovement();
    Vec3 getVehicleMovement();

    void setMovement(ServerboundMovePlayerPacket packet);
    Vec3 getLastMovement();
    Vec3 getMovement();
}
