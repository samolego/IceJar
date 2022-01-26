package org.samo_lego.icejar.util;


import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.icejar.check.Check;
import org.samo_lego.icejar.check.CheckType;

import java.util.Map;

public interface IceJarPlayer {
    void flag(final Check check);

    <T extends Check> T getCheck(CheckType type);
    <T extends Check> T getCheck(Class<T> type);

    void ij$setOpenGUI(boolean open);
    boolean ij$hasOpenGui();

    boolean ij$nearGround();

    void ij$setOnGround(boolean ij$onGround);

    void ij$updateGroundStatus();

    void ij$setVehicleMovement(ServerboundMoveVehiclePacket packet);
    Vec3 ij$getLastVehicleMovement();
    Vec3 ij$getVehicleMovement();

    void ij$setMovement(ServerboundMovePlayerPacket packet);
    Vec3 ij$getLastMovement();
    Vec3 ij$getMovement();

    void ij$setAboveLiquid(boolean aboveLiquid);
    boolean ij$aboveLiquid();

    void ij$copyFrom(IceJarPlayer oldPlayer);
    Map<Class<?>, Check> getCheckMap();

    double ij$getViolationLevel();
}
