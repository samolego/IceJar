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
package com.rammelkast.anticheatreloaded.event;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.Backend;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.config.Configuration;
import com.rammelkast.anticheatreloaded.manage.AntiCheatManager;
import com.rammelkast.anticheatreloaded.manage.CheckManager;
import com.rammelkast.anticheatreloaded.manage.UserManager;
import com.rammelkast.anticheatreloaded.util.User;

public class EventListener implements Listener {
	private static final Map<CheckType, Integer> USAGE_LIST = new EnumMap<CheckType, Integer>(CheckType.class);
	private static final Map<UUID, Integer> DECREASE_LIST = new HashMap<UUID, Integer>();
	private static final CheckManager CHECK_MANAGER = AntiCheatReloaded.getManager().getCheckManager();
	private static final Backend BACKEND = AntiCheatReloaded.getManager().getBackend();
	private static final AntiCheatReloaded PLUGIN = AntiCheatReloaded.getManager().getPlugin();
	private static final UserManager USER_MANAGER = AntiCheatReloaded.getManager().getUserManager();
	private static final Configuration CONFIG = AntiCheatReloaded.getManager().getConfiguration();
	private static final DecimalFormat TPS_FORMAT = new DecimalFormat("##.##");
	
	static {
		// What the hell, Java..
		TPS_FORMAT.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
	}
	
	public static void log(String message, Player player, CheckType type, String subcheck) {
		User user = getUserManager().getUser(player.getUniqueId());
		if (user == null)
			return;
		
		boolean debugMode = CONFIG.getConfig().debugMode.getValue();
		int vlForType = type.getUses(player.getUniqueId()) + 1;
		int notifyEveryVl = CONFIG.getConfig().notifyEveryVl.getValue();
		String prefix = ChatColor.translateAlternateColorCodes('&', CONFIG.getLang().ALERT_PREFIX());
		if (message == null || message.equals("")) {
			if (debugMode) {
				message = prefix + player.getName() + " failed "
						+ type.getName();
			} else {
				message = prefix + player.getName() + " failed "
						+ type.getName() + ChatColor.GOLD + " (x" + vlForType + ")";
			}
		} else {
			if (debugMode) {
				message = prefix + player.getName() + " failed "
						+ type.getName() + ChatColor.DARK_GRAY + " | " + ChatColor.GRAY + message
						+ ChatColor.DARK_GRAY + " | " + ChatColor.GRAY + "ping: " + user.getPing() + "ms"
						+ ", tps: " + TPS_FORMAT.format(AntiCheatReloaded.getPlugin().getTPS());
			} else {
				message = prefix
						+ player.getName() + " failed " + type.getName() + ChatColor.GOLD + " (x" + vlForType + ")"
						+ ChatColor.DARK_GRAY + " | " + ChatColor.GRAY
						+ (subcheck != null ? ("type: " + subcheck.toLowerCase() + ", ") : "") + "ping: " + user.getPing() + "ms"
						+ ", tps: " + TPS_FORMAT.format(AntiCheatReloaded.getPlugin().getTPS());
			}
		}
		
		logCheat(type, user);
		if (user.increaseLevel(type) && (!debugMode && vlForType % notifyEveryVl == 0)) {
			AntiCheatReloaded.getManager().log(message);
		}
		removeDecrease(user);

		if (debugMode) {
			player.sendMessage(message);
			return;
		}
		
		if (vlForType % notifyEveryVl == 0) {
			AntiCheatReloaded.getPlugin().sendToStaff(message);
		}
	}

	private static void logCheat(CheckType type, User user) {
		USAGE_LIST.put(type, getCheats(type) + 1);
		// Ignore plugins that are creating NPCs with no names (why the hell)
		if (user != null && user.getUUID() != null) {
			type.logUse(user);
			if (CONFIG.getConfig().enterprise.getValue() && CONFIG.getEnterprise().loggingEnabled.getValue()) {
				CONFIG.getEnterprise().database.logEvent(user, type);
			}
		}
	}

	public void resetCheck(CheckType type) {
		USAGE_LIST.put(type, 0);
	}

	public static int getCheats(CheckType type) {
		int x = 0;
		if (USAGE_LIST.get(type) != null) {
			x = USAGE_LIST.get(type);
		}
		return x;
	}

	private static void removeDecrease(User user) {
		int x = 0;
		// Ignore plugins that are creating NPCs with no names
		if (user.getUUID() != null) {
			if (DECREASE_LIST.get(user.getUUID()) != null) {
				x = DECREASE_LIST.get(user.getUUID());
				x -= 2;
				if (x < 0) {
					x = 0;
				}
			}
			DECREASE_LIST.put(user.getUUID(), x);
		}
	}

	public static void decrease(Player player) {
		User user = getUserManager().getUser(player.getUniqueId());
		// Ignore plugins that are creating NPCs with no names
		if (user.getUUID() != null) {
			int x = 0;

			if (DECREASE_LIST.get(user.getUUID()) != null) {
				x = DECREASE_LIST.get(user.getUUID());
			}

			x += 1;
			DECREASE_LIST.put(user.getUUID(), x);

			if (x >= 10) {
				user.decreaseLevel();
				DECREASE_LIST.put(user.getUUID(), 0);
			}
		}
	}

	public static CheckManager getCheckManager() {
		return CHECK_MANAGER;
	}

	public static AntiCheatManager getManager() {
		return AntiCheatReloaded.getManager();
	}

	public static Backend getBackend() {
		return BACKEND;
	}

	public static UserManager getUserManager() {
		return USER_MANAGER;
	}

	public static AntiCheatReloaded getPlugin() {
		return PLUGIN;
	}

	public static Configuration getConfig() {
		return CONFIG;
	}

	public static boolean silentMode() {
		return CONFIG.getConfig().silentMode.getValue();
	}
}
