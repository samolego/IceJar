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

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.config.Configuration;
import com.rammelkast.anticheatreloaded.util.rule.Rule;

import lombok.Getter;

public final class User {
	private final UUID uuid;
	private final String name;
	private final int id;
	private int level = 0;
	private Location goodLocation;
	private Configuration config = AntiCheatReloaded.getManager().getConfiguration();
	private int toX, toY, toZ;
	private String[] messages = new String[2];
	private Long[] messageTimes = new Long[2];
	private String[] commands = new String[2];
	private Long[] commandTimes = new Long[2];

	private boolean isWaitingOnLevelSync;
	private Timestamp levelSyncTimestamp;

	private long lastServerPing, lastClientPong;
	private int ping = -1;
	private int lastPing = -1;

	@Getter
	private final MovementManager movementManager;
	@Getter
	private final VelocityTracker velocityTracker;

	/**
	 * Initialize an AntiCheat user
	 *
	 * @param uuid Player's UUID
	 */
	public User(final UUID uuid) {
		this.uuid = uuid;
		this.name = getPlayer() != null && getPlayer().isOnline() ? getPlayer().getName() : "";
		this.id = getPlayer() != null && getPlayer().isOnline() ? getPlayer().getEntityId() : -1;
		this.ping = getPlayer() != null && getPlayer().isOnline() ? VersionUtil.getPlayerPing(getPlayer()) : -1;
		
		this.movementManager = new MovementManager();
		this.velocityTracker = new VelocityTracker(this.config.getMagic().VELOCITY_TIME());
		
        setIsWaitingOnLevelSync(true);
        config.getLevels().loadLevelToUser(this);
	}

	/**
	 * Get the player's UUID
	 *
	 * @return Player UUID
	 */
	public UUID getUUID() {
		return uuid;
	}

	/**
	 * Get the player's name
	 *
	 * @return Player name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the player's entity ID
	 *
	 * @return Entity id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Get the Bukkit player
	 *
	 * @return Player
	 */
	public Player getPlayer() {
		return Bukkit.getPlayer(uuid);
	}

	/**
	 * Get the user's level value
	 *
	 * @return level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * Get the user's group
	 *
	 * @return group
	 */
	public Group getGroup() {
		final List<Group> groups = config.getGroups().getGroups();
		for (int i = 0; i < groups.size(); i++) {
			if (i == 0 && level < groups.get(i).getLevel()) {
				break;
			} else if (i == groups.size() - 1) {
				return groups.get(i);
			} else if (level >= groups.get(i).getLevel() && level < groups.get(i + 1).getLevel()) {
				return groups.get(i);
			}
		}
		return null;
	}

	/**
	 * Increase the player's level (from failing a check)
	 *
	 * @param type The check failed
	 * @return true if the level was increased
	 */
	public boolean increaseLevel(final CheckType type) {
		if (getPlayer() != null && getPlayer().isOnline()) {
			if (silentMode() && type.getUses(uuid) % 4 != 0) {
				// Prevent silent mode from increasing the level way too fast
				return false;
			} else {
				if (level < config.getGroups().getHighestLevel()) {
					level++;

					// Check levels
					for (final Group group : getLevels()) {
						if (group.getLevel() == level) {
							AntiCheatReloaded.getManager().getUserManager().alert(this, group, type);
							if (group.getLevel() == config.getGroups().getHighestLevel()) {
								level = group.getLevel() - 10;
							}
						}
					}

					// Execute rules
					for (final Rule rule : config.getRules().getRules()) {
						rule.check(this, type);
					}
					return true;
				}
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Decrease the player's level by one value
	 */
	public void decreaseLevel() {
		level = level != 0 ? level - 1 : 0;
	}

	/**
	 * Set the users level to a specific value. Must be 0 < level < config-defined
	 * highest level
	 *
	 * @param level level to set
	 * @return true if the level was valid and set properly
	 */
	public boolean setLevel(final int level) {
		isWaitingOnLevelSync = false;
		if (level >= 0) {
			if (level <= config.getGroups().getHighestLevel()) {
				this.level = level;
				return true;
			} else {
				this.level = config.getGroups().getHighestLevel();
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Reset the user's level to zero and clear all check failures
	 */
	public void resetLevel() {
		level = 0;
		for (final CheckType type : CheckType.values()) {
			type.clearUse(uuid);
		}
	}

	/**
	 * Get the last known valid location of the player for use in resetting
	 *
	 * @param location default value to be returned if no known good location exists
	 * @return last known valid location
	 */
	public Location getGoodLocation(final Location location) {
		if (goodLocation == null || !location.getWorld().equals(goodLocation.getWorld())
				|| location.distance(goodLocation) > config.getConfig().maxSetbackDistance.getValue()) {
			return location;
		}

		final Location moveTo = goodLocation;
		if (location != null) {
			moveTo.setPitch(location.getPitch());
			moveTo.setYaw(location.getYaw());
		}
		return moveTo;
	}

	/**
	 * Set the last known valid location of the player
	 *
	 * @param location location to be set
	 * @return true if the location was valid and set properly
	 */
	public boolean setGoodLocation(final Location location) {
		if (Utilities.cantStandAtExp(location)) {
			return false;
		}

		goodLocation = location;
		return true;
	}

	/**
	 * Set the player's to location
	 *
	 * @param x X value
	 * @param y Y value
	 * @param z Z value
	 */
	public void setTo(double x, double y, double z) {
		toX = (int) x;
		toY = (int) y;
		toZ = (int) z;
	}

	/**
	 * Check if a given to value is the same as the stored value
	 *
	 * @param x X value
	 * @param y Y value
	 * @param z Z value
	 * @return true if coordinates are all equal
	 */
	public boolean checkTo(double x, double y, double z) {
		return (int) x == toX && (int) y == toY && (int) z == toZ;
	}

	/**
	 * Log a player's chat message
	 *
	 * @param message Message to log
	 */
	public void addMessage(String message) {
		addToSpamLog(message, messages, messageTimes);
	}

	/**
	 * Log a player's command
	 *
	 * @param command Command to log
	 */
	public void addCommand(String command) {
		addToSpamLog(command, commands, commandTimes);
	}

	private void addToSpamLog(String string, String[] messages, Long[] times) {
		messages[1] = messages[0];
		messages[0] = string;

		times[1] = times[0];
		times[0] = System.currentTimeMillis();
	}

	/**
	 * Get a stored chat message
	 *
	 * @param index Index of the chat in storage
	 * @return the message
	 */
	public String getMessage(int index) {
		return messages[index];
	}

	/**
	 * Get a stored command
	 *
	 * @param index Index of the command in storage
	 * @return the command
	 */
	public String getCommand(int index) {
		return commands[index];
	}

	/**
	 * Get the time a message in storage was sent
	 *
	 * @param index Index of the chat in storage
	 * @return time the message was sent
	 */
	public Long getMessageTime(int index) {
		return messageTimes[index];
	}

	/**
	 * Get the time a command in storage was sent
	 *
	 * @param index Index of the command in storage
	 * @return time the command was sent
	 */
	public Long getCommandTime(int index) {
		return commandTimes[index];
	}

	/**
	 * Clear all stored messages
	 */
	public void clearMessages() {
		messages = new String[2];
		messageTimes = new Long[2];
	}

	/**
	 * Clear all stored commands
	 */
	public void clearCommands() {
		commands = new String[2];
		commandTimes = new Long[2];
	}

	/**
	 * Get the time the very last stored message was sent
	 *
	 * @return the last message's time
	 */
	public Long getLastMessageTime() {
		return getMessageTime(0) == null ? -1 : getMessageTime(0);
	}

	/**
	 * Get the time the very last stored command was sent
	 *
	 * @return the last command's time
	 */
	public Long getLastCommandTime() {
		return getCommandTime(0) == null ? -1 : getCommandTime(0);
	}

	private List<Group> getLevels() {
		return config.getGroups().getGroups();
	}

	private boolean silentMode() {
		return config.getConfig().silentMode.getValue();
	}

	public void setIsWaitingOnLevelSync(boolean b) {
		this.isWaitingOnLevelSync = b;
	}

	public boolean isWaitingOnLevelSync() {
		return isWaitingOnLevelSync;
	}

	public void setLevelSyncTimestamp(Timestamp timestamp) {
		levelSyncTimestamp = timestamp;
	}

	public Timestamp getLevelSyncTimestamp() {
		return levelSyncTimestamp;
	}

	public void onServerPing() {
		this.lastServerPing = System.currentTimeMillis();
	}

	public void onClientPong() {
		this.lastClientPong = System.currentTimeMillis();
		this.lastPing = this.ping;
		this.ping = (int) (this.lastClientPong - this.lastServerPing);
	}

	public int getPing() {
		final int ping = this.ping;
		if (ping < 0) {
			return VersionUtil.getPlayerPing(getPlayer());
		}
		return ping;
	}

	public int getLastPing() {
		final int lastPing = this.lastPing;
		if (lastPing < 0) {
			return this.getPing();
		}
		return lastPing;
	}

	public boolean isLagging() {
		return Math.abs(this.ping - this.lastPing) > config.getMagic().LAG_DETERMINATION()
				&& this.ping > config.getMagic().LAG_DETERMINATION();
	}

	@Override
	public String toString() {
		return "User {name = " + getName() + ", level = " + level + "}";
	}

}
