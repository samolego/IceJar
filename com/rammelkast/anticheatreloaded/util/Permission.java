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

package com.rammelkast.anticheatreloaded.util;

import org.bukkit.command.CommandSender;

public enum Permission {
	/* Check Nodes */
	CHECK_FLIGHT, CHECK_ELYTRAFLY, CHECK_WATERWALK, CHECK_FASTPLACE, CHECK_CHATSPAM, CHECK_COMMANDSPAM, CHECK_SPRINT,
	CHECK_SNEAK, CHECK_SPEED, CHECK_VCLIP, CHECK_SPIDER, CHECK_NOFALL, CHECK_FASTBOW, CHECK_FASTEAT, CHECK_FASTHEAL,
	CHECK_KILLAURA, CHECK_UNICODE, CHECK_FASTPROJECTILE, CHECK_ITEMSPAM, CHECK_FASTINVENTORY,
	CHECK_MOREPACKETS, CHECK_BADPACKETS, CHECK_VELOCITY, CHECK_CRITICALS, CHECK_ILLEGALINTERACT, CHECK_FASTLADDER,
	CHECK_AIMBOT, CHECK_STRAFE, CHECK_NOSLOW, CHECK_BOATFLY,

	/* System Nodes */
	SYSTEM_LOG, SYSTEM_RESET, SYSTEM_HELP, SYSTEM_VERSION, SYSTEM_REPORT, SYSTEM_NOTICE, SYSTEM_CHECK, SYSTEM_RELOAD,
	SYSTEM_MUTE;

	private static final String PERMISSION_ALL = "anticheat.*";
	private static final String PERMISSION_SPAM = "anticheat.check.spam";

	public static boolean getCommandExempt(CommandSender cs, String commandLabel) {
		// Check permission, base, and all will have already been checked
		return cs.hasPermission(CHECK_COMMANDSPAM.toString() + commandLabel);
	}

	public boolean get(CommandSender cs) {
		if ((this == CHECK_CHATSPAM || this == CHECK_COMMANDSPAM) && cs.hasPermission(PERMISSION_SPAM))
			return true;
		return cs.hasPermission(toString()) || cs.hasPermission(getBase()) || cs.hasPermission(PERMISSION_ALL);
	}

	public String getBase() {
		return "anticheat." + this.name().toLowerCase().split("_")[0] + ".*";
	}

	public String whichPermission(CommandSender cs) {
		for (String s : new String[] { PERMISSION_ALL, getBase(), toString() }) {
			if (cs.hasPermission(s)) {
				return s;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "anticheat." + this.name().toLowerCase().replace("_", ".");
	}

}
