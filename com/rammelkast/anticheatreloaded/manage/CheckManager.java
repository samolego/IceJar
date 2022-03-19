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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.config.Configuration;
import com.rammelkast.anticheatreloaded.util.VersionUtil;
import net.minecraft.world.entity.player.Player;

/**
 * <p/>
 * The manager that AntiCheat will check with to see if it should watch certain checks and certain players.
 */

public final class CheckManager {
	
    private final AntiCheatManager manager;
    private final Configuration config;
    
    private static final List<CheckType> checkIgnoreList = new ArrayList<CheckType>();
    private static final Map<UUID, List<CheckType>> exemptList = new HashMap<UUID, List<CheckType>>();

    public CheckManager(final AntiCheatManager manager) {
        this.manager = manager;
        this.config = manager.getConfiguration();
        loadCheckIgnoreList(this.config);
    }

    public void loadCheckIgnoreList(final Configuration configuration) {
    	checkIgnoreList.clear();
        for (final CheckType type : CheckType.values()) {
            if (!configuration.getChecks().isEnabled(type)) {
                checkIgnoreList.add(type);
            }
        }
        
        if (!checkIgnoreList.isEmpty()) {
        	Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "ACR " + ChatColor.DARK_GRAY + "> " + ChatColor.GRAY
					+ checkIgnoreList.size() + " check(s) have been disabled");
        }
    }
    
    /**
     * Turn a check on
     *
     * @param type The CheckType to enable
     */
    public void activateCheck(final CheckType type, final String className) {
        if (!isActive(type)) {
            manager.getLoggingManager().logFineInfo("The " + type.toString() + " check was activated by " + className + ".");
            checkIgnoreList.remove(type);
        }
    }

    /**
     * Turn a check off
     *
     * @param type The CheckType to disable
     */
    public void deactivateCheck(final CheckType type, final String className) {
        if (isActive(type)) {
            manager.getLoggingManager().logFineInfo("The " + type.toString() + " check was deactivated by " + className + ".");
            checkIgnoreList.add(type);
        }
    }

    /**
     * Determine whether a check is enabled
     *
     * @param type The CheckType to check
     * @return true if the check is active
     */
    public boolean isActive(final CheckType type) {
        return !checkIgnoreList.contains(type);
    }

    /**
     * Exempt a player from a check
     *
     * @param player The player
     * @param type   The check
     */
    public void exemptPlayer(final Player player, final CheckType type, final String className) {
        if (!isExempt(player, type)) {
            if (!exemptList.containsKey(player.getUUID())) {
                exemptList.put(player.getUUID(), new ArrayList<>());
            }
            manager.getLoggingManager().logFineInfo(player.getName() + " was exempted from the " + type.toString() + " check by " + className + ".");
            exemptList.get(player.getUUID()).add(type);
        }
    }

    /**
     * Unexempt a player from a check
     *
     * @param player The player
     * @param type   The check
     */
    public void unexemptPlayer(final Player player, final CheckType type, final String className) {
        if (isExempt(player, type)) {
            manager.getLoggingManager().logFineInfo(player.getName() + " was unexempted from the " + type.toString() + " check by " + className + ".");
            exemptList.get(player.getUUID()).remove(type);
        }
    }

    /**
     * Determine whether a player is exempt from a check
     *
     * @param player The player
     * @param type   The check
     */
    public boolean isExempt(final Player player, final CheckType type) {
    	if (AntiCheatReloaded.isFloodgateEnabled()) {
    		if (FloodgateApi.getInstance().isFloodgatePlayer(player.getUUID())) {
    			return true;
    		}
    	}
        return exemptList.containsKey(player.getUUID()) ? exemptList.get(player.getUUID()).contains(type) : false;
    }

    /**
     * Determine whether a player is exempt from all checks from op status
     *
     * @param player The player
     */
    public boolean isOpExempt(final Player player) {
        return (this.manager.getConfiguration().getConfig().exemptOp.getValue() && player.isOp());
    }

    /**
     * Determine whether a player should be checked in their world
     *
     * @param player The player
     * @return true if the player's world is enabled
     */
    public boolean checkInWorld(final Player player) {
        return !config.getConfig().disabledWorlds.getValue().contains(player.getWorld().getName());
    }

    /**
     * Run a quick version of the "willCheck" method, using the other non-check-specific methods beforehand
     *
     * @param player The player to check
     * @param type   The check being run
     * @return true if the check should run
     */
    public boolean willCheckQuick(final Player player, final CheckType type) {
        return
                isActive(type)
                        && !isExempt(player, type)
                        && !type.checkPermission(player);
    }

    /**
     * Determine whether a check should run on a player
     *
     * @param player The player to check
     * @param type   The check being run
     * @return true if the check should run
     */
    public boolean willCheck(final Player player, final CheckType type) {
    	final boolean check = isActive(type)
                && checkInWorld(player)
                && !isExempt(player, type)
                && !type.checkPermission(player)
                && !isOpExempt(player);
        if ((type == CheckType.FLIGHT || type == CheckType.SPEED) && VersionUtil.isFlying(player)) {
        	return false;
        }
        return check;
    }

    /**
     * Determine whether a player is actually online; not an NPC
     *
     * @param player Player to check
     * @return true if the player is a real person
     */
    public boolean isOnline(final Player player) {
        // Check if the player is on the user list, e.g. is not an NPC
        for (final Player iterated : Bukkit.getOnlinePlayers()) {
            if (iterated.getUUID().equals(player.getUUID())) {
                return true;
            }
        }
        return false;
    }
}
