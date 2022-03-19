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

package com.rammelkast.anticheatreloaded.manage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.config.Configuration;
import com.rammelkast.anticheatreloaded.util.Group;
import com.rammelkast.anticheatreloaded.util.User;
import com.rammelkast.anticheatreloaded.util.Utilities;

public final class UserManager {
	private final List<User> users = new ArrayList<User>();
	private final AntiCheatManager manager;
	private final Configuration config;

	/**
	 * Initialize the user manager
	 *
	 * @param manager The AntiCheat Manager
	 */
	public UserManager(final AntiCheatManager manager) {
		this.manager = manager;
		this.config = manager.getConfiguration();
	}

	/**
	 * Get a user with the given UUID
	 *
	 * @param uuid UUID
	 * @return User with UUID
	 */
	public User getUser(final UUID uuid) {
		return this.users.parallelStream().filter(user -> user.getUUID().equals(uuid)).findFirst()
				.orElseGet(() -> new User(uuid));
	}

	/**
	 * Get all users
	 *
	 * @return List of users
	 */
	public List<User> getUsers() {
		return users;
	}

	/**
	 * Add a user to the list
	 *
	 * @param user User to add
	 */
	public void addUser(final User user) {
		users.add(user);
	}

	/**
	 * Remove a user from the list
	 *
	 * @param user User to remove
	 */
	public void removeUser(final User user) {
		users.remove(user);
	}

	/**
	 * Save a user's level
	 *
	 * @param user User to save
	 */
	public void saveLevel(final User user) {
		config.getLevels().saveLevelFromUser(user);
	}

	/**
	 * Get users in group
	 *
	 * @param group Group to find users of
	 */
	public List<User> getUsersInGroup(final Group group) {
		return this.users.parallelStream().filter(user -> user.getGroup() == group).collect(Collectors.toList());
	}

	/**
	 * Get a user's level, or 0 if the player isn't found
	 *
	 * @param uuid UUID of the player
	 * @return player level
	 */
	public int safeGetLevel(final UUID uuid) {
		final User user = getUser(uuid);
		if (user == null) {
			return 0;
		} else {
			return user.getLevel();
		}
	}

	/**
	 * Set a user's level
	 *
	 * @param uuid  UUID of the player
	 * @param level Group to set
	 */
	public void safeSetLevel(final UUID uuid, final int level) {
		final User user = getUser(uuid);
		if (user != null) {
			user.setLevel(level);
		}
	}

	/**
	 * Reset a user
	 *
	 * @param uuid UUID of the user
	 */
	public void safeReset(final UUID uuid) {
		final User user = getUser(uuid);
		if (user != null) {
			user.resetLevel();
		}
	}

	/**
	 * Fire an alert
	 *
	 * @param user  The user to alert
	 * @param group The user's group
	 * @param type  The CheckType that triggered the alert
	 */
	public void alert(final User user, final Group group, final CheckType type) {
		execute(user, group.getActions(), type);
	}

	/**
	 * Execute configuration actions for an alert
	 *
	 * @param user    The user
	 * @param actions The list of actions to execute
	 * @param type    The CheckType that triggered the alert
	 */
	public void execute(final User user, final List<String> actions, final CheckType type) {
		execute(user, actions, type, config.getLang().KICK_REASON(), config.getLang().WARNING(),
				config.getLang().BAN_REASON());
	}

	/**
	 * Execute configuration actions for an alert
	 *
	 * @param user       The user
	 * @param actions    The list of actions to execute
	 * @param type       The CheckType that triggered the alert
	 * @param kickReason The config's kick reason
	 * @param warning    The config's warning format
	 * @param banReason  The config's ban reason
	 */
	public void execute(final User user, final List<String> actions, final CheckType type, final String kickReason,
			final List<String> warning, final String banReason) {
		// Execute synchronously for thread safety when called from AsyncPlayerChatEvent
		Bukkit.getScheduler().scheduleSyncDelayedTask(AntiCheatReloaded.getPlugin(), new Runnable() {
			@Override
			public void run() {
				if (user.getPlayer() == null) {
					return;
				}
				final String name = user.getName();
				for (String event : actions) {
					event = event.replaceAll("%player%", name)
							.replaceAll("%world%", user.getPlayer().getWorld().getName())
							.replaceAll("%check%", type.name());

					if (event.startsWith("COMMAND[")) {
						for (String cmd : Utilities.getCommands(event)) {
							Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
						}
					} else if (event.equalsIgnoreCase("KICK")) {
						user.getPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&', kickReason));
						AntiCheatReloaded.getPlugin().onPlayerKicked();
						String msg = ChatColor.translateAlternateColorCodes('&',
								config.getLang().KICK_BROADCAST().replaceAll("%player%", name) + " (" + type.getName()
										+ ")");
						if (!msg.equals("")) {
							manager.log(msg);
							manager.playerLog(msg);
						}
					} else if (event.equalsIgnoreCase("WARN")) {
						List<String> message = warning;
						for (String string : message) {
							if (!string.equals("")) {
								user.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', string));
							}
						}
					} else if (event.equalsIgnoreCase("BAN")) {
						Bukkit.getBanList(Type.NAME).addBan(user.getPlayer().getName(),
								ChatColor.translateAlternateColorCodes('&', banReason), null, null);
						user.getPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&', banReason));
						String msg = ChatColor.translateAlternateColorCodes('&',
								config.getLang().BAN_BROADCAST().replaceAll("%player%", name) + " (" + type.getName()
										+ ")");
						if (!msg.equals("")) {
							manager.log(msg);
							manager.playerLog(msg);
						}
					} else if (event.equalsIgnoreCase("RESET")) {
						user.resetLevel();
					}
				}
			}
		});
	}

}
