/*
 * AntiCheatReloaded for Bukkit and Spigot.
 * Copyright (c) 2012-2015 AntiCheat Team
 * Copyright (c) 2016-2022 Rammelkast
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.rammelkast.anticheatreloaded.check.movement;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.config.providers.Checks;
import com.rammelkast.anticheatreloaded.util.Distance;
import com.rammelkast.anticheatreloaded.util.MovementManager;
import com.rammelkast.anticheatreloaded.util.User;
import com.rammelkast.anticheatreloaded.util.Utilities;
import com.rammelkast.anticheatreloaded.util.VelocityTracker;
import com.rammelkast.anticheatreloaded.util.VersionUtil;

/**
 * @author Rammelkast
 */
public final class FlightCheck {

	public static final Map<UUID, Long> MOVING_EXEMPT = new HashMap<UUID, Long>();
	public static final Map<UUID, Float> GRAVITY_VIOLATIONS = new HashMap<UUID, Float>();

	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);
	private static final double GRAVITY_FRICTION = 0.9800000190734863D;

	public static CheckResult runCheck(final Player player, final Distance distance) {
		if (distance.getYDifference() >= AntiCheatReloaded.getManager().getBackend().getMagic().TELEPORT_MIN()
				|| VersionUtil.isFlying(player) || player.getVehicle() != null
				|| AntiCheatReloaded.getManager().getBackend().isMovingExempt(player)) {
			// This was a teleport or user is flying/using elytra/in a vehicle, so we don't
			// care about it.
			return PASS;
		}

		final User user = AntiCheatReloaded.getManager().getUserManager().getUser(player.getUniqueId());
		final MovementManager movementManager = user.getMovementManager();
		final VelocityTracker velocityTracker = user.getVelocityTracker();
		final Checks checksConfig = AntiCheatReloaded.getManager().getConfiguration().getChecks();

		if (movementManager.nearLiquidTicks > 0 || movementManager.halfMovement || Utilities.isNearClimbable(player)
				|| movementManager.riptideTicks > 0) {
			return PASS;
		}

		int minAirTicks = 13;
		if (player.hasPotionEffect(PotionEffectType.JUMP)) {
			minAirTicks += VersionUtil.getPotionLevel(player, PotionEffectType.JUMP) * 3;
		}

		if (movementManager.halfMovementHistoryCounter > 25) {
			minAirTicks += 5;
		}

		// Start AirFlight
		if (checksConfig.isSubcheckEnabled(CheckType.FLIGHT, "airFlight") && movementManager.airTicks > minAirTicks
				&& movementManager.elytraEffectTicks <= 25) {
			// Config default base is 1200ms
			// Ping clamped to max. 1000 to prevent spoofing for an advantage
			int blockPlaceAccountingTime = (int) (checksConfig.getInteger(CheckType.FLIGHT, "airFlight",
					"accountForBlockPlacement") + (0.25 * (user.getPing() > 1000 ? 1000 : user.getPing())));
			// Config default account is 250ms
			if (AntiCheatReloaded.getPlugin().getTPS() < 18.0) {
				blockPlaceAccountingTime += checksConfig.getInteger(CheckType.FLIGHT, "airFlight",
						"accountForTpsDrops");
			}

			final long lastPlacedBlock = AntiCheatReloaded.getManager().getBackend().placedBlock
					.containsKey(player.getUniqueId())
							? AntiCheatReloaded.getManager().getBackend().placedBlock.get(player.getUniqueId())
							: (blockPlaceAccountingTime + 1);
			double maxMotionY = System.currentTimeMillis() - lastPlacedBlock > blockPlaceAccountingTime ? 0 : 0.42;
			// Fixes snow false positive
			if (movementManager.motionY < 0.004 && Utilities
					.isNearHalfblock(distance.getFrom().getBlock().getRelative(BlockFace.DOWN).getLocation())) {
				maxMotionY += 0.004D;
			}

			// Fixes water false positive
			if (Utilities.isNearWater(player)
					|| Utilities.isNearWater(distance.getFrom().clone().subtract(0, 0.51, 0))) {
				maxMotionY += 0.05;
			}

			// Velocity adjustment
			maxMotionY += velocityTracker.getVertical();

			if (movementManager.motionY > maxMotionY && movementManager.slimeInfluenceTicks <= 0
					&& !Utilities.isNearClimbable(distance.getTo().clone().subtract(0, 1.25, 0))
					&& !Utilities.isNearClimbable(distance.getTo().clone().subtract(0, 0.75, 0))
					&& (!Utilities.isNearWater(distance.getTo().clone().subtract(0, 1.5, 0))
							&& distance.getTo().clone().subtract(0, 0.5, 0).getBlock().getType() != Material.AIR)) {
				return new CheckResult(CheckResult.Result.FAILED, "AirFlight",
						"tried to fly on the Y-axis (mY=" + movementManager.motionY + ", max=" + maxMotionY + ")");
			}

			// TODO falses when falling large distances
			if (Math.abs(movementManager.motionY
					- movementManager.lastMotionY) < (movementManager.airTicks >= 115 ? 1E-3 : 5E-3)
					&& !Utilities.couldBeOnBoat(player)
					&& (System.currentTimeMillis() - movementManager.lastTeleport >= checksConfig
							.getInteger(CheckType.FLIGHT, "airFlight", "accountForTeleports"))
					&& !VersionUtil.isSlowFalling(player) && !Utilities.isNearWeb(player)
					&& movementManager.elytraEffectTicks <= 25
					&& !Utilities.isNearClimbable(distance.getFrom().clone().subtract(0, 0.51D, 0))
					&& !Utilities.isNearWater(player)
					&& !Utilities.isNearWater(distance.getFrom().clone().subtract(0, 0.51, 0))) {
				return new CheckResult(CheckResult.Result.FAILED, "AirFlight", "had too little Y dropoff (diff="
						+ Math.abs(movementManager.motionY - movementManager.lastMotionY) + ")");
			}
		}
		// End AirFlight

		// Start AirClimb
		if (checksConfig.isSubcheckEnabled(CheckType.FLIGHT, "airClimb") && movementManager.lastMotionY > 0
				&& movementManager.motionY > 0 && movementManager.airTicks == 2
				&& Math.round(movementManager.lastMotionY * 1000) != 420
				&& Math.round(movementManager.motionY * 1000) != 248
				&& !(Math.round(movementManager.motionY * 1000) == 333
						&& Math.round(movementManager.lastMotionY * 1000) != 333)
				&& !velocityTracker.isVelocitized() && !player.hasPotionEffect(PotionEffectType.JUMP)
				&& (System.currentTimeMillis() - movementManager.lastTeleport >= checksConfig
						.getInteger(CheckType.FLIGHT, "airClimb", "accountForTeleports"))
				&& (!Utilities.isNearBed(distance.getTo()) || ((Utilities.isNearBed(distance.getTo())
						|| Utilities.isNearBed(distance.getTo().clone().add(0, -0.51, 0)))
						&& movementManager.motionY > 0.15))
				&& movementManager.slimeInfluenceTicks == 0 && movementManager.elytraEffectTicks <= 25
				&& !Utilities.couldBeOnBoat(player, 0.8d, false))
			return new CheckResult(CheckResult.Result.FAILED, "AirClimb",
					"tried to climb air (mY=" + movementManager.motionY + ")");

		if (checksConfig.isSubcheckEnabled(CheckType.FLIGHT, "airClimb") && movementManager.motionY > 0.42
				&& movementManager.airTicks > 2 && !velocityTracker.isVelocitized()
				&& !player.hasPotionEffect(PotionEffectType.JUMP)
				&& !(Math.round(movementManager.motionY * 1000) == 425 && movementManager.airTicks == 11)
				&& (System.currentTimeMillis() - movementManager.lastTeleport >= checksConfig
						.getInteger(CheckType.FLIGHT, "airClimb", "accountForTeleports"))
				&& movementManager.slimeInfluenceTicks == 0 && movementManager.elytraEffectTicks <= 25) {
			return new CheckResult(CheckResult.Result.FAILED, "AirClimb",
					"tried to climb air (mY=" + movementManager.motionY + ", at=" + movementManager.airTicks + ")");
		}

		if (checksConfig.isSubcheckEnabled(CheckType.FLIGHT, "airClimb") && movementManager.airTicks >= minAirTicks
				&& movementManager.lastMotionY < 0 && movementManager.motionY > 0
				&& movementManager.motionY > velocityTracker.getVertical() && movementManager.elytraEffectTicks <= 25
				&& (System.currentTimeMillis() - movementManager.lastTeleport >= checksConfig
						.getInteger(CheckType.FLIGHT, "airClimb", "accountForTeleports"))
				&& !(Math.round(movementManager.motionY * 1000) == 396) && movementManager.airTicks == 15) {
			return new CheckResult(CheckResult.Result.FAILED, "AirClimb",
					"tried to climb air (mY=" + movementManager.motionY + ", at=" + movementManager.airTicks + ")");
		}

		if (checksConfig.isSubcheckEnabled(CheckType.FLIGHT, "airClimb") && movementManager.airTicks >= minAirTicks
				&& !velocityTracker.isVelocitized() && movementManager.slimeInfluenceTicks <= 0
				&& movementManager.elytraEffectTicks <= 25
				&& (System.currentTimeMillis() - movementManager.lastTeleport >= checksConfig
						.getInteger(CheckType.FLIGHT, "airClimb", "accountForTeleports"))) {
			// Config default base is 1200ms
			// Ping clamped to max. 1000 to prevent spoofing for an advantage
			int blockPlaceAccountingTime = (int) (checksConfig.getInteger(CheckType.FLIGHT, "airFlight",
					"accountForBlockPlacement") + (0.25 * (user.getPing() > 1000 ? 1000 : user.getPing())));
			// Config default account is 250ms
			if (AntiCheatReloaded.getPlugin().getTPS() < 18.0) {
				blockPlaceAccountingTime += checksConfig.getInteger(CheckType.FLIGHT, "airFlight",
						"accountForTpsDrops");
			}

			final long lastPlacedBlock = AntiCheatReloaded.getManager().getBackend().placedBlock
					.containsKey(player.getUniqueId())
							? AntiCheatReloaded.getManager().getBackend().placedBlock.get(player.getUniqueId())
							: (blockPlaceAccountingTime + 1);
			final double maxMotionY = System.currentTimeMillis() - lastPlacedBlock > blockPlaceAccountingTime ? 0
					: 0.42;
			if (movementManager.motionY > maxMotionY) {
				return new CheckResult(CheckResult.Result.FAILED, "AirClimb",
						"tried to climb air (mY=" + movementManager.motionY + ", at=" + movementManager.airTicks + ")");
			}
		}
		// End AirClimb

		// Start GroundFlight
		if (checksConfig.isSubcheckEnabled(CheckType.FLIGHT, "groundFlight") && movementManager.onGround
				&& Utilities.cantStandAt(distance.getTo().getBlock().getRelative(BlockFace.DOWN))
				&& Utilities.cantStandAt(distance.getFrom().getBlock().getRelative(BlockFace.DOWN))
				&& Utilities.cantStandAt(distance.getTo().getBlock())) {
			return new CheckResult(CheckResult.Result.FAILED, "GroundFlight",
					"faked ground to fly (mY=" + movementManager.motionY + ", gt=" + movementManager.groundTicks + ")");
		}
		// End GroundFlight

		// Start Gravity
		if (checksConfig.isSubcheckEnabled(CheckType.FLIGHT, "gravity") && !movementManager.onGround
				&& movementManager.motionY < 0 && Math.abs(movementManager.motionY) > velocityTracker.getVertical()
				&& (System.currentTimeMillis() - movementManager.lastTeleport >= checksConfig
						.getInteger(CheckType.FLIGHT, "gravity", "accountForTeleports"))
				&& !Utilities.isNearWeb(player) && movementManager.elytraEffectTicks <= 25
				&& !VersionUtil.isSlowFalling(player)) {
			final double gravitatedY = (movementManager.lastMotionY - 0.08) * GRAVITY_FRICTION;
			final double offset = Math.abs(gravitatedY - movementManager.motionY);
			double maxOffset = checksConfig.getDouble(CheckType.FLIGHT, "gravity", "maxOffset");
			if (Utilities.isNearClimbable(distance.getFrom().clone().subtract(0, 0.51D, 0))
					|| Utilities.isNearClimbable(distance.getFrom()) || Utilities.isNearWater(player)
					|| (!Utilities.isNearWater(distance.getTo().clone().subtract(0, 1.5, 0))
							&& distance.getTo().clone().subtract(0, 0.5, 0).getBlock().getType() != Material.AIR))
				maxOffset += 0.15D;
			if (offset > maxOffset && movementManager.airTicks > 2) {
				float vl = GRAVITY_VIOLATIONS.getOrDefault(player.getUniqueId(), 0f) + 1;
				GRAVITY_VIOLATIONS.put(player.getUniqueId(), vl);
				int vlBeforeFlag = checksConfig.getInteger(CheckType.FLIGHT, "gravity", "vlBeforeFlag");
				if (vl >= vlBeforeFlag) {
					GRAVITY_VIOLATIONS.put(player.getUniqueId(), Math.max(0, vl - 2));
					return new CheckResult(CheckResult.Result.FAILED, "Gravity",
							"ignored gravity (offset=" + offset + ", at=" + movementManager.airTicks + ")");
				}
			} else {
				if (GRAVITY_VIOLATIONS.containsKey(player.getUniqueId())) {
					final float vl = GRAVITY_VIOLATIONS.getOrDefault(player.getUniqueId(), 0f);
					GRAVITY_VIOLATIONS.put(player.getUniqueId(), Math.max(0, vl - 0.5f));
				}
			}
		}
		// End Gravity

		return PASS;
	}

}
