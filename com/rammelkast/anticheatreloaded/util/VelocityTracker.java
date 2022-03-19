package com.rammelkast.anticheatreloaded.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.util.Vector;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class VelocityTracker {

	private final long velocitizedTime;
	private final List<VelocityWrapper> velocities = new ArrayList<>();

	public void registerVelocity(final Vector vector) {
		this.velocities.add(new VelocityWrapper(vector.getX(), vector.getY(), vector.getZ(),
				Math.hypot(vector.getX(), vector.getZ()), Math.abs(vector.getY()), System.currentTimeMillis()));
	}

	public void tick() {
		this.velocities
				.removeIf(velocity -> (velocity.getTimestamp() + this.velocitizedTime < System.currentTimeMillis()));
	}
	
	public double getHorizontal() {
		return Math
				.sqrt(this.velocities.parallelStream().mapToDouble(VelocityWrapper::getHorizontal).max().orElse(0.0D));
	}
	
	public double getVertical() {
		return Math
				.sqrt(this.velocities.parallelStream().mapToDouble(VelocityWrapper::getVertical).max().orElse(0.0D));
	}
	
	public boolean isVelocitized() {
		return this.velocities.size() != 0;
	}

	@RequiredArgsConstructor
	@Getter
	private class VelocityWrapper {
		private final double motionX, motionY, motionZ;
		private final double horizontal, vertical;
		private final long timestamp;
	}
	
}
