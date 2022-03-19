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

package com.rammelkast.anticheatreloaded.config.providers;

import java.util.List;

public interface Lang {

    /**
     * Get the alert to send when a player's hack group changes.
     *
     * @return Alert
     */
    public List<String> ALERT();

    /**
     * Get the warning to send players when they enter a group.
     *
     * @return Warning.
     */
    public List<String> WARNING();

    /**
     * Get the warning to send players for spamming.
     *
     * @return Spam warning.
     */
    public String SPAM_WARNING();

    /**
     * Get the reason for kicking a player for spam.
     *
     * @return Spam kicking reason.
     */
    public String SPAM_KICK_REASON();

    /**
     * Get the reason for banning a player for spam.
     *
     * @return Spam banning reason.
     */
    public String SPAM_BAN_REASON();

    /**
     * Get the broadcast for kicking a player for spam.
     *
     * @return Spam kicking broadcast.
     */
    public String SPAM_KICK_BROADCAST();

    /**
     * Get the broadcast for banning a player for spam.
     *
     * @return Spam banning broadcast.
     */
    public String SPAM_BAN_BROADCAST();

    /**
     * Get the reason for banning a player.
     *
     * @return Banning reason.
     */
    public String BAN_REASON();

    /**
     * Get the broadcast for banning a player.
     *
     * @return Banning broadcast.
     */
    public String BAN_BROADCAST();

    /**
     * Get the reason for kicking a player.
     *
     * @return Kicking reason.
     */
    public String KICK_REASON();

    /**
     * Get the broadcast for kicking a player.
     *
     * @return Kicking broadcast.
     */
    public String KICK_BROADCAST();
    

    /**
     * Get the alert prefix for staff and debug.
     *
     * @return Alert prefix.
     */
    public String ALERT_PREFIX();
}
