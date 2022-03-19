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

package com.rammelkast.anticheatreloaded.api;

import java.util.List;

import org.bukkit.entity.Player;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.manage.AntiCheatManager;
import com.rammelkast.anticheatreloaded.manage.CheckManager;
import com.rammelkast.anticheatreloaded.manage.UserManager;
import com.rammelkast.anticheatreloaded.util.Group;

public final class AntiCheatAPI {
	
    private static final CheckManager CHECK_MANAGER = AntiCheatReloaded.getManager().getCheckManager();
    private static final UserManager USER_MANAGER = AntiCheatReloaded.getManager().getUserManager();

    // CheckManager API

    /**
     * Start running a certain check
     *
     * @param type Check to start watching for
     */
    public static void activateCheck(final CheckType type, final Class<?> caller) {
        CHECK_MANAGER.activateCheck(type, caller.getName());
    }

    /**
     * Stop running a certain check
     *
     * @param type Check to stop watching for
     */
    public static void deactivateCheck(final CheckType type, final Class<?> caller) {
        CHECK_MANAGER.deactivateCheck(type, caller.getName());
    }

    /**
     * Find out if a check is currently being watched for
     *
     * @param type Type to check
     * @return true if plugin is watching for this check
     */
    public static boolean isActive(final CheckType type) {
        return CHECK_MANAGER.isActive(type);
    }

    /**
     * Allow a player to skip a certain check
     *
     * @param player Player to stop watching
     * @param type   Check to stop watching for
     */
    public static void exemptPlayer(final Player player, final CheckType type, final Class<?> caller) {
        CHECK_MANAGER.exemptPlayer(player, type, caller.getName());
    }

    /**
     * Stop allowing a player to skip a certain check
     *
     * @param player Player to start watching
     * @param type   Check to start watching for
     */
    public static void unexemptPlayer(final Player player, final CheckType type, final Class<?> caller) {
        CHECK_MANAGER.unexemptPlayer(player, type, caller.getName());
    }

    /**
     * Find out if a player is currently exempt from a certain check
     *
     * @param player Player to check
     * @param type   Type to check
     * @return true if plugin is ignoring this check on this player
     */
    public static boolean isExempt(final Player player, final CheckType type) {
        return CHECK_MANAGER.isExempt(player, type);
    }

    /**
     * Find out if a check will occur for a player. This checks if they are being tracked, the check is active, the player isn't exempt from the check, and the player doesn't have override permission.
     *
     * @param player Player to check
     * @param type   Type to check
     * @return true if plugin will check this player, and that all things allow it to happen.
     */
    public boolean willCheck(final Player player, final CheckType type) {
        return CHECK_MANAGER.willCheck(player, type);
    }

    // PlayerManager API

    /**
     * Reset a player's hack level to 0
     *
     * @param player Player to reset
     */
    public static void resetPlayer(final Player player) {
        USER_MANAGER.getUser(player.getUniqueId()).resetLevel();
    }

    /**
     * Get a user's {@link Group}
     *
     * @param player Player whose group to find
     * @return The player's group
     */
    public static Group getGroup(final Player player) {
        return USER_MANAGER.getUser(player.getUniqueId()).getGroup();
    }

    /**
     * Get all configured Groups
     *
     * @return List of all groups
     * @see Group
     */
    public static List<Group> getGroups() {
        return getManager().getConfiguration().getGroups().getGroups();
    }

    // Advanced Users Only API.

    /**
     * Get access to all the other managers, advanced users ONLY
     *
     * @return the AntiCheat Manager
     */
    public static AntiCheatManager getManager() {
        return AntiCheatReloaded.getManager();
    }

}
