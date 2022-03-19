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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.command.CommandBase;
import com.rammelkast.anticheatreloaded.util.Group;
import com.rammelkast.anticheatreloaded.util.Permission;
import com.rammelkast.anticheatreloaded.util.User;
import com.rammelkast.anticheatreloaded.util.Utilities;

public class CommandReport extends CommandBase {

	private static final String NAME = "AntiCheatReloaded Reports";
	private static final String COMMAND = "report";
	private static final String USAGE = "anticheat report [group/user]";
	private static final Permission PERMISSION = Permission.SYSTEM_REPORT;
	private static final String[] HELP = {
			GRAY + "Use: " + GOLD + "/anticheat report [group]" + GRAY + " to see all users in a given group",
			GRAY + "Use: " + GOLD + "/anticheat report [user]" + GRAY + " to see a single user's report",
			GRAY + "Use: " + GOLD + "/anticheat report [user/group] [num]" + GRAY + " to see pages of a report", };

	public CommandReport() {
		super(NAME, COMMAND, USAGE, HELP, PERMISSION);
	}

	@Override
	protected void execute(CommandSender cs, String[] args) {
		if (args.length >= 1) {
			int page = 1;
			if (args.length == 2) {
				if (Utilities.isInt(args[1])) {
					page = Integer.parseInt(args[1]);
				} else {
					cs.sendMessage(RED + "Not a valid page number: " + WHITE + args[1]);
				}
			}

			if ("low".equalsIgnoreCase(args[0])) {
				groupReport(cs, null, page);
			} else if ("all".equalsIgnoreCase(args[0])) {
				cs.sendMessage(GREEN + "Low: " + WHITE + USER_MANAGER.getUsersInGroup(null).size() + " players");
				for (Group g : CONFIG.getGroups().getGroups()) {
					int numPlayers = USER_MANAGER.getUsersInGroup(g).size();
					cs.sendMessage(g.getColor() + g.getName() + WHITE + ": " + numPlayers + " players");
				}
				cs.sendMessage(GRAY + "Use " + GOLD + "/anticheat report [group]" + GRAY
						+ " for a list of players in each group.");
			} else {
				// Test groups
				for (Group group : CONFIG.getGroups().getGroups()) {
					if (group.getName().equalsIgnoreCase(args[0]) || "low".equalsIgnoreCase(args[0])
							|| "all".equalsIgnoreCase(args[0])) {
						groupReport(cs, group, page);
						return;
					}
				}

				@SuppressWarnings("deprecation")
				OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[0]);
				if (targetPlayer == null) {
					cs.sendMessage(RED + "Not a valid group or user: " + WHITE + args[0]);
					return;
				}

				User user = AntiCheatReloaded.getManager().getUserManager().getUser(targetPlayer.getUniqueId());
				if (user != null) {
					playerReport(cs, user, page);
					return;
				}

				cs.sendMessage(RED + "Not a valid group or user: " + WHITE + args[0]);
			}
		} else {
			sendHelp(cs);
		}
	}

	private void groupReport(CommandSender cs, Group group, int page) {
		List<User> users = USER_MANAGER.getUsersInGroup(group);
		ChatColor color = group == null ? GREEN : group.getColor();
		String groupName = group == null ? "Low" : group.getName();
		int pages = (int) Math.ceil(((float) users.size()) / 7);
		if (page <= pages && page > 0) {
			cs.sendMessage(GOLD + "----------------------[" + GRAY + "Page " + page + "/" + pages + "" + GOLD
					+ "]----------------------");
			cs.sendMessage(GRAY + "Group: " + color + groupName);
			for (int x = 0; x < 7; x++) {
				int index = ((page - 1) * 6) + (x + ((page - 1) * 1));
				if (index < users.size()) {
					String player = users.get(index).getName();
					cs.sendMessage(GRAY + player);
				}
			}
			cs.sendMessage(MENU_END);
		} else {
			if (pages == 0) {
				cs.sendMessage(GOLD + "----------------------[" + GRAY + "Page 1/1" + GOLD + "]----------------------");
				cs.sendMessage(GRAY + "Group: " + color + groupName);
				cs.sendMessage(GRAY + "There are no users in this group.");
				cs.sendMessage(MENU_END);
			} else {
				cs.sendMessage(RED + "Page not found. Requested " + WHITE + page + RED + ", Max " + WHITE + pages);
			}
		}
	}

	private void playerReport(CommandSender cs, User user, int page) {
		List<CheckType> types = new ArrayList<CheckType>();
		for (CheckType type : CheckType.values()) {
			if (type.getUses(user.getUUID()) > 0) {
				types.add(type);
			}
		}

		UUID uuid = user.getUUID();
		String name = user.getName();
		int pages = (int) Math.ceil(((float) types.size()) / 6);

		Group group = user.getGroup();
		String groupString = GREEN + "Low";

		if (group != null) {
			groupString = group.getColor() + group.getName();
		}
		groupString += " (" + user.getLevel() + ")";

		if (page <= pages && page > 0) {
			cs.sendMessage(GOLD + "----------------------[" + GRAY + "Page " + page + "/" + pages + "" + GOLD
					+ "]----------------------");
			cs.sendMessage(GRAY + "Player: " + WHITE + name);
			int ping = user.getPing();
			cs.sendMessage(GRAY + "Ping: " + ((ping == -1) ? (RED + "Offline")
					: (WHITE + "" + ping + "ms "
							+ (user.isLagging() ? ChatColor.RED + "(lagging)" : ChatColor.GREEN + "(not lagging)"))));
			cs.sendMessage(GRAY + "Group: " + groupString);
			for (int x = 0; x < 6; x++) {
				int index = ((page - 1) * 5) + (x + ((page - 1)));
				if (index < types.size()) {
					CheckType type = types.get(index);
					int use = type.getUses(uuid);
					ChatColor color = WHITE;
					if (use >= 20) {
						color = YELLOW;
					} else if (use > 50) {
						color = RED;
					}
					cs.sendMessage(GRAY + type.getName() + ": " + color + use);
				}
			}
			cs.sendMessage(MENU_END);
		} else {
			if (pages == 0 && page == 1) {
				cs.sendMessage(GOLD + "----------------------[" + GRAY + "Page 1/1" + GOLD + "]----------------------");
				cs.sendMessage(GRAY + "Player: " + WHITE + name);
				int ping = user.getPing();
				cs.sendMessage(GRAY + "Ping: " + ((ping == -1) ? (RED + "Offline")
						: (WHITE + "" + ping + "ms "
								+ (user.isLagging() ? ChatColor.RED + "(lagging)" : ChatColor.GREEN + "(not lagging)"))));
				cs.sendMessage(GRAY + "Group: " + groupString);
				cs.sendMessage(GRAY + "This user has not failed any checks.");
				cs.sendMessage(MENU_END);
			} else {
				cs.sendMessage(RED + "Page not found. Requested " + WHITE + page + RED + ", Max " + WHITE + pages + 1);
			}
		}
	}
}
