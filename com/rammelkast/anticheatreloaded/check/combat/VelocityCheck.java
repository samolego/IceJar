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
package com.rammelkast.anticheatreloaded.check.combat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.check.CheckResult.Result;
import com.rammelkast.anticheatreloaded.config.providers.Checks;
import com.rammelkast.anticheatreloaded.util.Distance;
import com.rammelkast.anticheatreloaded.util.MovementManager;
import com.rammelkast.anticheatreloaded.util.User;
import com.rammelkast.anticheatreloaded.util.Utilities;

public final class VelocityCheck {

	public static final Map<UUID, Integer> VIOLATIONS = new HashMap<UUID, Integer>();
	private static final CheckResult PASS = new CheckResult(Result.PASSED);

	public static CheckResult runCheck(final Player player, final Distance distance) {
		final User user = AntiCheatReloaded.getManager().getUserManager().getUser(player.getUniqueId());
		final MovementManager movementManager = user.getMovementManager();
		final Checks checksConfig = AntiCheatReloaded.getManager().getConfiguration().getChecks();
		final int minimumPercentage = checksConfig.getInteger(CheckType.VELOCITY, "minimumPercentage");
		final int vlBeforeFlag = checksConfig.getInteger(CheckType.VELOCITY, "vlBeforeFlag");
		
		if (movementManager.velocityExpectedMotionY > 0 && !movementManager.onGround) {
			double percentage = (movementManager.motionY / movementManager.velocityExpectedMotionY) * 100;
			if (percentage < 0) {
				percentage = 0;
			}
			// Reset expected Y motion
			movementManager.velocityExpectedMotionY = 0;
			if (percentage < minimumPercentage) {
				final int vl = VIOLATIONS.getOrDefault(player.getUniqueId(), 0) + 1;
				VIOLATIONS.put(player.getUniqueId(), vl);
				if (vl >= vlBeforeFlag) {
					return new CheckResult(Result.FAILED, "ignored server velocity (pct=" + Utilities.roundDouble(percentage, 2) + ")");
				}
			} else {
				VIOLATIONS.remove(player.getUniqueId());
			}
		} else if (movementManager.airTicks > 5 && movementManager.velocityExpectedMotionY > 0) {
			movementManager.velocityExpectedMotionY = 0;
		}
		return PASS;
	}

}
