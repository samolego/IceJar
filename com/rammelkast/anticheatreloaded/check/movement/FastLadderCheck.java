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

import org.bukkit.entity.Player;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.check.CheckResult.Result;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.config.providers.Checks;
import com.rammelkast.anticheatreloaded.util.Utilities;
import com.rammelkast.anticheatreloaded.util.VersionUtil;

public final class FastLadderCheck {

	public static final Map<UUID, Integer> VIOLATIONS = new HashMap<UUID, Integer>();
	private static final CheckResult PASS = new CheckResult(Result.PASSED);

	public static CheckResult runCheck(Player player, double y) {
		// Only check if fully on ladder and not flying
		// Liquids are climbable blocks, so we have to add a seperate check
		if (!Utilities.isClimbableBlock(player.getLocation().getBlock())
				|| !Utilities.isClimbableBlock(player.getEyeLocation().getBlock())
				|| player.getLocation().getBlock().isLiquid() || VersionUtil.isFlying(player)) {
			return PASS;
		}

		int vlCount = VIOLATIONS.getOrDefault(player.getUniqueId(), 0);
		Checks checksConfig = AntiCheatReloaded.getManager().getConfiguration().getChecks();
		int vlBeforeFlag = checksConfig.getInteger(CheckType.FASTLADDER, "vlBeforeFlag");
		if (y > 0) {
			// Check moving up
			double max = checksConfig.getDouble(CheckType.FASTLADDER, "speedUpMax");
			// Check if going faster than max Y climbing speed
			if (y - max > 0) {
				if (vlCount++ > vlBeforeFlag) {
					vlCount = 0;
					return new CheckResult(Result.FAILED,
							"tried to climb up too fast (speed=" + y + ", max=" + max + ")");
				}
			} else {
				if (vlCount > 0)
					vlCount--;
			}
		} else if (y < 0) {
			// Check moving down
			double max = checksConfig.getDouble(CheckType.FASTLADDER, "speedDownMax");
			if (Math.abs(y) > max + 6E-9) {
				if (vlCount++ > vlBeforeFlag) {
					vlCount = 0;
					return new CheckResult(Result.FAILED, "tried to climb down too fast (speed=" + Math.abs(y)
							+ ", max=" + max + ")");
				}
			} else {
				if (vlCount > 0)
					vlCount--;
			}
		} else {
			if (vlCount > 0)
				vlCount--;
		}

		VIOLATIONS.put(player.getUniqueId(), vlCount);
		return PASS;
	}

}
