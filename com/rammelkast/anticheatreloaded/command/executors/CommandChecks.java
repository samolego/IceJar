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

package com.rammelkast.anticheatreloaded.command.executors;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.command.CommandBase;
import com.rammelkast.anticheatreloaded.manage.CheckManager;
import com.rammelkast.anticheatreloaded.util.Permission;

public class CommandChecks extends CommandBase {

	private static final String NAME = "AntiCheatReloaded Checks Management";
	private static final String COMMAND = "checks";
	private static final String USAGE = "anticheat checks";
	private static final Permission PERMISSION = Permission.SYSTEM_CHECK;
	private static final String[] HELP = new String[2];

	static {
		HELP[0] = GRAY + "Use: " + AQUA + "/anticheat checks" + GRAY + " for a list of checks";
		StringBuilder builder = new StringBuilder();
		CheckManager checkManager = AntiCheatReloaded.getManager().getCheckManager();
		builder.append(GRAY + "Checks: ");
		for (int i = 0; i < CheckType.values().length; i++) {
			CheckType type = CheckType.values()[i];
			builder.append((checkManager.isActive(type) ? ChatColor.GREEN : ChatColor.RED) + type.getName());
			if (i < CheckType.values().length - 1) {
				builder.append(", ");
			}
		}
		HELP[1] = builder.toString();
	}

	public CommandChecks() {
		super(NAME, COMMAND, USAGE, HELP, PERMISSION);
	}

	@Override
	protected void execute(CommandSender cs, String[] args) {
		sendHelp(cs);
	}
}
