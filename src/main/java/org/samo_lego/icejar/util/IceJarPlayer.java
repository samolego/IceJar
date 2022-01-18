package org.samo_lego.icejar.util;


import org.samo_lego.icejar.check.Check;
import org.samo_lego.icejar.check.CheckType;

public interface IceJarPlayer {
    void flag(final Check check);

    AdditionalData getAdditionalData();

    boolean isNearGround();

    boolean isAboveFluid();

    Check getCheck(CheckType type);
}
