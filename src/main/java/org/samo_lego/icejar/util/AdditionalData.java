package org.samo_lego.icejar.util;

public class AdditionalData {
    private boolean wasOnGround;
    private boolean wasLastOnGround;
    private boolean onGround;
    private double fallDistance;


    public boolean onGround() {
        return this.onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public boolean wasOnGround() {
        return this.wasOnGround;
    }

    public boolean wasLastOnGround() {
        return this.wasLastOnGround;
    }

    public void updateGroundStatus() {
        this.wasLastOnGround = this.wasOnGround();
        this.wasOnGround = this.onGround();
    }

    public void setFallDistance(double fallDistance) {
        this.fallDistance = fallDistance;
    }

    public double getFallDistance() {
        return this.fallDistance;
    }
}
