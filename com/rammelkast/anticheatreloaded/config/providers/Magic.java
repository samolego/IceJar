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

/**
 * Magic number class. Modifications to values in magic.yml will be accepted here.
 * <p/>
 * Note that although each value is documented, changing it may have unintended side-effects. For instance, setting something to 0 that the plugin then uses as a dividend will result in an error. <br>
 * Also realize that the smaller (more precise) a value is, the less you should change it; it's probably small for a reason. The larger a value is, the safer it is to make larger modifications to it.
 * <p/>
 * <b>How to read the value documentation:</b>
 * <p/>
 * First, you will see a description of the value. Then, you will see a type: <br>
 * <b>Type SYSTEM:</b> This is a millisecond value used to compare past and future events with the current SYSTEM time. Remember, 1000 ms = 1 second. <br>
 * <b>Type INTEGER:</b> This is a regular number. It's typically used as something trivial, such as how many times X event can occur. <br>
 * <b>Type DOUBLE:</b> This is a number that has a decimal in it. It's typically used to evaluate speed or distance.
 * <p/>
 * After the type, you may see a recommendation labeled as 'Leniency'. This means 'In order to add leniency to this value, do X'<br>
 * The suggestion for adding leniency will either be to INCREASE or DECREASE the variable. Doing what it suggests will cause the SYSTEM to not judge the people being checked so vigorously.<br>
 * Some values may not have a leniency recommendation because they are internal numbers used for running checks. Values without these recommendations would be best left alone.
 */

public interface Magic {
    /**
     * Time to exempt a player from moving because of entering/exiting a vehicle; Type=SYSTEM, Leniency=INCREASE.
     */
    public int ENTERED_EXITED_TIME();
    /**
     * Time to exempt a player from moving because of teleporting; Type=SYSTEM, Leniency=INCREASE.
     */
    public int TELEPORT_TIME();
    /**
     * Time to exempt a player from moving because of joining the server; Type=SYSTEM, Leniency=INCREASE.
     */
    public int JOIN_TIME();
    /**
     * Time a player is considered to have a change in velocity; Type=SYSTEM, Leniency=INCREASE.
     */
    public int VELOCITY_TIME();
    /**
     * Minimum travel distance for move to be considered a teleport and subsequently be ignored; Type=INTEGER, Leniency=INCREASE.
     */
    public int TELEPORT_MIN();
    /**
     * The change milliseconds between two keepalives before considering a player to be lagging; TYPE=INTEGER, Leniency=DECREASE
     */
    public int LAG_DETERMINATION();
}
