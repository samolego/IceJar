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

package com.rammelkast.anticheatreloaded.check;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.rammelkast.anticheatreloaded.api.CheckFailEvent;
import com.rammelkast.anticheatreloaded.util.Permission;
import com.rammelkast.anticheatreloaded.util.User;
import net.minecraft.world.entity.player.Player;

/**
 * <p/>
 * All the types of checks and their corresponding permission nodes.
 */
public enum CheckType {
    FLIGHT(Permission.CHECK_FLIGHT, "Flight"),
    ELYTRAFLY(Permission.CHECK_ELYTRAFLY, "ElytraFly"),
    WATER_WALK(Permission.CHECK_WATERWALK, "WaterWalk"),
    FAST_PLACE(Permission.CHECK_FASTPLACE, "FastPlace"),
    CHAT_SPAM(Permission.CHECK_CHATSPAM, "ChatSpam"),
    COMMAND_SPAM(Permission.CHECK_COMMANDSPAM, "CommandSpam"),
    SPRINT(Permission.CHECK_SPRINT, "Sprint"),
    SNEAK(Permission.CHECK_SNEAK, "Sneak"),
    SPEED(Permission.CHECK_SPEED, "Speed"),
    VCLIP(Permission.CHECK_VCLIP, "vClip"),
    SPIDER(Permission.CHECK_SPIDER, "Spider"),
    NOFALL(Permission.CHECK_NOFALL, "NoFall"),
    FAST_BOW(Permission.CHECK_FASTBOW, "FastBow"),
    FAST_EAT(Permission.CHECK_FASTEAT, "FastEat"),
    FAST_HEAL(Permission.CHECK_FASTHEAL, "FastHeal"),
    KILLAURA(Permission.CHECK_KILLAURA, "KillAura"),
    FAST_PROJECTILE(Permission.CHECK_FASTPROJECTILE, "FastProjectile"),
    ITEM_SPAM(Permission.CHECK_ITEMSPAM, "ItemSpam"),
    FAST_INVENTORY(Permission.CHECK_FASTINVENTORY, "FastInventory"),
    MOREPACKETS(Permission.CHECK_MOREPACKETS, "MorePackets"),
    BADPACKETS(Permission.CHECK_BADPACKETS, "BadPackets"),
	VELOCITY(Permission.CHECK_VELOCITY, "Velocity"),
	CRITICALS(Permission.CHECK_CRITICALS, "Criticals"),
	CHAT_UNICODE(Permission.CHECK_UNICODE, "ChatUnicode"),
	ILLEGAL_INTERACT(Permission.CHECK_ILLEGALINTERACT, "IllegalInteract"),
	FASTLADDER(Permission.CHECK_FASTLADDER, "FastLadder"),
	AIMBOT(Permission.CHECK_AIMBOT, "Aimbot"),
	STRAFE(Permission.CHECK_STRAFE, "Strafe"),
	NOSLOW(Permission.CHECK_NOSLOW, "NoSlow"),
	BOATFLY(Permission.CHECK_BOATFLY, "BoatFly");
	
    private final Permission permission;
    private final String displayName;
    private final Map<UUID, Integer> level = new HashMap<UUID, Integer>();

    /**
     * Initialize a CheckType
     *
     * @param permission Permission that applies to this check
     * @peram displayName The chat display name
     */
    CheckType(Permission permission, String displayName) {
        this.permission = permission;
        this.displayName = displayName;
    }

    /**
     * Determine whether a player has permission to bypass this check
     *
     * @param player Player to check
     * @return true if the player can bypass
     */
    public boolean checkPermission(Player player) {
        return permission.get(player);
    }

    /**
     * Log the failure of this check
     *
     * @param user User who failed the check
     */
    public void logUse(User user) {
        int amount = level.get(user.getUUID()) == null ? 1 : level.get(user.getUUID()) + 1;
        level.put(user.getUUID(), amount);
        Bukkit.getServer().getPluginManager().callEvent(new CheckFailEvent(user, this));
    }

    /**
     * Clear failure history of this check for a user
     *
     * @param uuid User's UUID to clear
     */
    public void clearUse(UUID uuid) {
        level.put(uuid, 0);
    }

    /**
     * Get how many times a user has failed this check
     *
     * @param uuid User's UUID
     * @return number of times failed
     */
    public int getUses(UUID uuid) {
        return level.get(uuid) != null ? level.get(uuid) : 0;
    }

    /**
     * Get the reference name of a check
     *
     * @return reference name
     */
    public String getName() {
    	return this.displayName;
    }
}
