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

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.check.CheckResult.Result;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.config.providers.Checks;
import com.rammelkast.anticheatreloaded.util.MovementManager;
import com.rammelkast.anticheatreloaded.util.User;
import com.rammelkast.anticheatreloaded.util.Utilities;
import com.rammelkast.anticheatreloaded.util.VelocityTracker;
import com.rammelkast.anticheatreloaded.util.VersionUtil;

/**
 * 
 * @author Rammelkast
 *
 */
public final class StrafeCheck {

	private static final CheckResult PASS = new CheckResult(Result.PASSED);

	public static CheckResult runCheck(final Player player, final double x, final double z, final Location from,
			final Location to) {
		if (!Utilities.cantStandAtExp(from) || !Utilities.cantStandAtExp(to) || Utilities.isNearWater(player)
				|| Utilities.isNearClimbable(player) || VersionUtil.isFlying(player) || player.isDead()
				|| Utilities.isHalfblock(to.getBlock().getRelative(BlockFace.DOWN)) || Utilities.isNearHalfblock(to)) {
			return PASS;
		}

		final User user = AntiCheatReloaded.getManager().getUserManager()
				.getUser(player.getUniqueId());
		final MovementManager movementManager = user.getMovementManager();
		final VelocityTracker velocityTracker = user.getVelocityTracker();
		if (velocityTracker.isVelocitized()) {
			return PASS;
		}
		
		final Checks checksConfig = AntiCheatReloaded.getManager().getConfiguration().getChecks();

		if (System.currentTimeMillis() - movementManager.lastTeleport <= checksConfig.getInteger(CheckType.STRAFE,
				"accountForTeleports") || movementManager.elytraEffectTicks >= 20
				|| movementManager.halfMovementHistoryCounter >= 20 || Utilities.couldBeOnBoat(player, 0.5d, false)) {
			return PASS;
		}

		final Vector oldAcceleration = new Vector(movementManager.lastDistanceX, 0, movementManager.lastDistanceZ);
		final Vector newAcceleration = new Vector(x, 0, z);

		final float angle = newAcceleration.angle(oldAcceleration);
		final double distance = newAcceleration.lengthSquared();
		if (angle > checksConfig.getDouble(CheckType.STRAFE, "maxAngleChange")
				&& distance > checksConfig.getDouble(CheckType.STRAFE, "minActivationDistance")
				&& Utilities.cantStandFar(to.getBlock())) {
			return new CheckResult(Result.FAILED, "switched angle in air (angle=" + angle + ", dist=" + distance + ")");
		}
		return PASS;
	}

}
