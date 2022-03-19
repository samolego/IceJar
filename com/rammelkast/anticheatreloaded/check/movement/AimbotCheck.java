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

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.Backend;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.config.providers.Checks;
import com.rammelkast.anticheatreloaded.util.MovementManager;
import com.rammelkast.anticheatreloaded.util.User;
import com.rammelkast.anticheatreloaded.util.Utilities;

/*
 * In development - expect issues
 */
public final class AimbotCheck {

	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);
	
	public static CheckResult runCheck(final Player player, final EntityDamageByEntityEvent event) {
		final Backend backend = AntiCheatReloaded.getManager().getBackend();
		if (backend.isMovingExempt(player)) {
			return PASS;
		}
		
		final User user = AntiCheatReloaded.getManager().getUserManager()
				.getUser(player.getUniqueId());
		final MovementManager movementManager = user.getMovementManager();
		final Checks checksConfig = AntiCheatReloaded.getManager().getConfiguration().getChecks();
		final float deltaPitch = movementManager.deltaPitch;
		final float deltaYaw = movementManager.deltaYaw;
		final float pitchAcceleration = Math.abs(deltaPitch - movementManager.lastDeltaPitch);

		final double gcd = Utilities.computeGcd(deltaPitch, movementManager.lastDeltaPitch);
		final double mod = Math.abs(player.getLocation().getPitch() % gcd);
		final double minAcceleration = checksConfig.getDouble(CheckType.AIMBOT, "minAcceleration");
		final double maxMod = checksConfig.getDouble(CheckType.AIMBOT, "maxMod");
		if (mod < maxMod && pitchAcceleration > minAcceleration
				&& (deltaPitch == 0.0f || (deltaPitch > 45.0f && deltaYaw > 45.0f))) {
			return new CheckResult(CheckResult.Result.FAILED,
					"failed computational check (gcd=" + Utilities.roundDouble(gcd, 4) + ", mod="
							+ Utilities.roundDouble(mod, 4) + ", accel=" + Utilities.roundDouble(pitchAcceleration, 4)
							+ ")");
		}
		return PASS;
	}

}
