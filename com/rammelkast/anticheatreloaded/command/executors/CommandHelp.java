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

import org.bukkit.command.CommandSender;

import com.rammelkast.anticheatreloaded.command.CommandBase;
import com.rammelkast.anticheatreloaded.util.Permission;

public class CommandHelp extends CommandBase {

    private static final String NAME = "AntiCheatReloaded Help";
    private static final String COMMAND = "help";
    private static final String USAGE = "anticheat help";
    private static final Permission PERMISSION = Permission.SYSTEM_HELP;
    private static final String[] HELP = {
    		GOLD + "/acr " + GRAY + "help",
    		GOLD + "/acr " + GRAY + "reload",
            GOLD + "/acr " + GRAY + "version",
            GOLD + "/acr " + GRAY + "mute",
    		GOLD + "/acr " + GRAY + "checks",
            GOLD + "/acr " + GRAY + "log " + WHITE + "[file/console] [on/off]",
            GOLD + "/acr " + GRAY + "report " + WHITE + "[group/user]",
            GOLD + "/acr " + GRAY + "reset " + WHITE + "[user]",
    };

    public CommandHelp() {
        super(NAME, COMMAND, USAGE, HELP, PERMISSION);
    }

    @Override
    protected void execute(CommandSender cs, String[] args) {
        sendHelp(cs);
    }
}
