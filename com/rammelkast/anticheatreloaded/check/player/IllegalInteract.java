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
package com.rammelkast.anticheatreloaded.check.player;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.check.combat.KillAuraCheck;
import com.rammelkast.anticheatreloaded.config.providers.Checks;
import com.rammelkast.anticheatreloaded.util.User;
import com.rammelkast.anticheatreloaded.util.Utilities;
import com.rammelkast.anticheatreloaded.util.VersionUtil;

public final class IllegalInteract {

	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);

	public static CheckResult performCheck(final Player player, final Event event) {
		final Checks checksConfig = AntiCheatReloaded.getManager().getConfiguration().getChecks();
		if (event instanceof BlockPlaceEvent && checksConfig.isSubcheckEnabled(CheckType.ILLEGAL_INTERACT, "place")) {
			return checkBlockPlace(player, (BlockPlaceEvent) event);
		} else if (event instanceof BlockBreakEvent
				&& checksConfig.isSubcheckEnabled(CheckType.ILLEGAL_INTERACT, "break")) {
			return checkBlockBreak(player, (BlockBreakEvent) event);
		} else if (event instanceof PlayerInteractEvent
				&& checksConfig.isSubcheckEnabled(CheckType.ILLEGAL_INTERACT, "interact")) {
			return checkInteract(player, (PlayerInteractEvent) event);
		}
		return PASS;
	}

	private static CheckResult checkInteract(final Player player, final PlayerInteractEvent event) {
		final User user = AntiCheatReloaded.getManager().getUserManager().getUser(player.getUniqueId());
		final Checks checksConfig = AntiCheatReloaded.getManager().getConfiguration().getChecks();
		final double distance = player.getEyeLocation().toVector()
				.distance(event.getClickedBlock().getLocation().toVector());
		double maxDistance = player.getGameMode() == GameMode.CREATIVE
				? checksConfig.getDouble(CheckType.ILLEGAL_INTERACT, "interact", "creativeRange")
				: checksConfig.getDouble(CheckType.ILLEGAL_INTERACT, "interact", "survivalRange");

		maxDistance += user.isLagging() ? 0.12 : 0;
		maxDistance += user.getPing()
				* checksConfig.getInteger(CheckType.ILLEGAL_INTERACT, "interact", "pingCompensation");
		maxDistance += player.getVelocity().length()
				* checksConfig.getDouble(CheckType.ILLEGAL_INTERACT, "interact", "velocityMultiplier");
		if (distance > maxDistance) {
			return new CheckResult(CheckResult.Result.FAILED, "Interact",
					"tried to interact out of range (dist=" + distance + ", max=" + maxDistance + ")");
		}
		return PASS;
	}

	private static CheckResult checkBlockBreak(final Player player, final BlockBreakEvent event) {
		if (!isValidTarget(player, event.getBlock())) {
			return new CheckResult(CheckResult.Result.FAILED, "Break", "tried to break a block which was out of view");
		}
		return PASS;
	}

	private static CheckResult checkBlockPlace(final Player player, final BlockPlaceEvent event) {
		final BlockFace face = event.getBlock().getFace(event.getBlockAgainst());
		if (face == null) {
			return PASS;
		}
		
		final Vector vector = new Vector(face.getModX(), face.getModY(), face.getModZ());
		if (event.getBlock().getType().isSolid()
				&& player.getLocation().getDirection().angle(vector) > Math.toRadians(90)) {
			return new CheckResult(CheckResult.Result.FAILED, "Place",
					"tried to place a block out of their view (angle="
							+ Utilities.roundFloat(player.getLocation().getDirection().angle(vector), 1)
							+ ", max=90.0");
		}
		return PASS;
	}

	private static boolean isValidTarget(final Player player, final Block block) {
		final Checks checksConfig = AntiCheatReloaded.getManager().getConfiguration().getChecks();
		final double distance = player.getGameMode() == GameMode.CREATIVE ? 6.0
				: player.getLocation().getDirection().getY() > 0.9 ? 6.0 : 5.5;
		final Block targetBlock = VersionUtil.getTargetBlock(player, ((int) Math.ceil(distance)));
		if (targetBlock == null) {
			// TODO better check here
			return true;
		}

		if (Utilities.isClimbableBlock(targetBlock)) {
			if (targetBlock.getLocation().distance(player.getLocation()) <= distance) {
				return true;
			}
		}

		if (targetBlock.equals(block)) {
			return true;
		}

		final Location eyeLocation = player.getEyeLocation();
		final double yawDifference = KillAuraCheck.calculateYawDifference(eyeLocation, block.getLocation());
		final double playerYaw = player.getEyeLocation().getYaw();
		final double angleDifference = Math.abs(180 - Math.abs(Math.abs(yawDifference - playerYaw) - 180));
		if (Math.round(angleDifference) > checksConfig.getInteger(CheckType.ILLEGAL_INTERACT, "maxAngleDifference")) {
			return false;
		}
		return true;
	}

}
