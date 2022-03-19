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

import java.util.UUID;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.check.CheckResult.Result;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.config.providers.Checks;
import com.rammelkast.anticheatreloaded.util.MovementManager;
import com.rammelkast.anticheatreloaded.util.User;
import com.rammelkast.anticheatreloaded.util.Utilities;
import com.rammelkast.anticheatreloaded.util.VersionUtil;

public final class WaterWalkCheck {

	private static final CheckResult PASS = new CheckResult(Result.PASSED);

	public static CheckResult runCheck(final Player player, final double x, final double y, final double z) {
		final UUID uuid = player.getUniqueId();
		final User user = AntiCheatReloaded.getManager().getUserManager().getUser(uuid);
		final MovementManager movementManager = user.getMovementManager();

		if (movementManager.distanceXZ <= 0 || player.getVehicle() != null || Utilities.isOnLilyPad(player) || Utilities.isOnCarpet(player)
				|| movementManager.riptideTicks > 0 || VersionUtil.isSwimming(player) || VersionUtil.isFlying(player) || user.getVelocityTracker().isVelocitized()) {
			return PASS;
		}

		final Checks checksConfig = AntiCheatReloaded.getManager().getConfiguration().getChecks();
		final Block blockBeneath = player.getLocation().clone().subtract(0, 0.1, 0).getBlock();
		if (checksConfig.isSubcheckEnabled(CheckType.WATER_WALK, "walk") && blockBeneath.isLiquid()
				&& Utilities.isSurroundedByWater(player)
				&& ((movementManager.motionY == 0 && movementManager.lastMotionY == 0)
						|| movementManager.motionY == Utilities.JUMP_MOTION_Y)
				&& movementManager.distanceXZ > checksConfig.getDouble(CheckType.WATER_WALK, "walk", "minimumDistXZ")
				&& !movementManager.topSolid && !Utilities.couldBeOnBoat(player, 3, false)) {
			return new CheckResult(Result.FAILED, "Walk",
					"tried to walk on water (xz=" + Utilities.roundDouble(movementManager.distanceXZ, 5) + ")");
		}

		if (checksConfig.isSubcheckEnabled(CheckType.WATER_WALK, "hop") && blockBeneath.isLiquid()
				&& Utilities.isSurroundedByWater(player) && movementManager.onGround
				&& Math.abs(movementManager.motionY) < checksConfig.getDouble(CheckType.WATER_WALK, "hop", "maxMotionY")
				&& !Utilities.couldBeOnBoat(player, 0.3, false)) {
			return new CheckResult(Result.FAILED, "Hop",
					"tried to hop on water (mY=" + Utilities.roundDouble(movementManager.motionY, 5) + ")");
		}

		final double minAbsMotionY = 0.12D + (VersionUtil.getPotionLevel(player, PotionEffectType.SPEED) * 0.05D);
		if (checksConfig.isSubcheckEnabled(CheckType.WATER_WALK, "lunge") && blockBeneath.isLiquid()
				&& Utilities.isSurroundedByWater(player)
				&& Math.abs(movementManager.lastMotionY - movementManager.motionY) > minAbsMotionY
				&& movementManager.distanceXZ > checksConfig.getDouble(CheckType.WATER_WALK, "lunge", "minimumDistXZ")
				&& movementManager.lastMotionY > -0.25
				&& !Utilities.couldBeOnBoat(player, 0.3, false)) {
			return new CheckResult(Result.FAILED, "Lunge", "tried to lunge in water (xz="
					+ Utilities.roundDouble(movementManager.distanceXZ, 5) + ", absMotionY="
					+ Utilities.roundDouble(Math.abs(movementManager.lastMotionY - movementManager.motionY), 5) + ")");
		}
		return PASS;
	}

}
