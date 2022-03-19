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

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.cryptomorin.xseries.XMaterial;
import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.check.CheckResult.Result;
import com.rammelkast.anticheatreloaded.config.providers.Checks;
import com.rammelkast.anticheatreloaded.util.MovementManager;
import com.rammelkast.anticheatreloaded.util.Utilities;
import com.rammelkast.anticheatreloaded.util.VersionUtil;

public final class BoatFlyCheck {

	public static final Map<UUID, Integer> VIOLATIONS = new HashMap<UUID, Integer>();
	private static final CheckResult PASS = new CheckResult(Result.PASSED);

	public static CheckResult runCheck(final Player player, final MovementManager movementManager, final Location to) {
		if (movementManager.distanceXZ <= 0 || movementManager.motionY <= 1E-3
				|| (System.currentTimeMillis() - movementManager.lastTeleport <= 50) || VersionUtil.isFlying(player)
				|| !player.isInsideVehicle()) {
			return PASS;
		}

		final UUID uuid = player.getUniqueId();
		final Checks checksConfig = AntiCheatReloaded.getManager().getConfiguration().getChecks();
		if (player.getVehicle().getType() == EntityType.BOAT) {
			final Block bottom = player.getWorld().getBlockAt(to.getBlockX(), to.getBlockY() - 1, to.getBlockZ());
			if (!Utilities.cantStandAt(bottom) || bottom.getType() == XMaterial.WATER.parseMaterial()) {
				return PASS;
			}
			
			int violations = VIOLATIONS.getOrDefault(uuid, 1);
			if (violations++ >= checksConfig.getInteger(CheckType.BOATFLY, "vlBeforeFlag")) {
				violations = 0;
				return new CheckResult(Result.FAILED, "tried to fly in a boat (mY=" + movementManager.motionY
						+ ", bottom=" + bottom.getType().name().toLowerCase() + ")");
			}
			VIOLATIONS.put(uuid, violations);
		}
		return PASS;
	}

}
