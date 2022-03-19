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
import com.rammelkast.anticheatreloaded.config.ConfigurationFile;
import com.rammelkast.anticheatreloaded.util.Permission;

public class CommandLog extends CommandBase {

    private static final String NAME = "AntiCheatReloaded Logging";
    private static final String COMMAND = "log";
    private static final String USAGE = "anticheat log [file/console] [on/off]";
    private static final Permission PERMISSION = Permission.SYSTEM_LOG;
    private static final String[] HELP = {
            GRAY + "Use: " + AQUA + "/anticheat log console on" + GRAY + " to enable console logging",
            GRAY + "Use: " + AQUA + "/anticheat log file off" + GRAY + " to disable file logging",
    };

    public CommandLog() {
        super(NAME, COMMAND, USAGE, HELP, PERMISSION);
    }

    @Override
    protected void execute(CommandSender cs, String[] args) {
        if (args.length == 2) {
            ConfigurationFile.ConfigValue<Boolean> value;
            String name;
            if (args[0].equalsIgnoreCase("file")) {
                value = CONFIG.getConfig().logToFile;
                name = "File logging";
            } else if (args[0].equalsIgnoreCase("console")) {
                value = CONFIG.getConfig().logToConsole;
                name = "Console logging";
            } else {
                sendHelp(cs);
                return;
            }

            boolean newValue;
            if (args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("enable")) {
                newValue = true;
            } else if (args[1].equalsIgnoreCase("off") || args[1].equalsIgnoreCase("disable")) {
                newValue = false;
            } else {
                sendHelp(cs);
                return;
            }

            String strValue = (newValue ? " enabled" : " disabled");
            if (value.getValue() == newValue) {
                cs.sendMessage(GREEN + name + " is already " + strValue + "!");
            } else {
                value.setValue(newValue);
                cs.sendMessage(GREEN + name + strValue + ".");
                CONFIG.getConfig().reload();
            }
        } else {
            sendHelp(cs);
        }
    }
}
