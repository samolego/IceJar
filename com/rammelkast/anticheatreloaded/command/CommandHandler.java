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

package com.rammelkast.anticheatreloaded.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.rammelkast.anticheatreloaded.command.executors.CommandChecks;
import com.rammelkast.anticheatreloaded.command.executors.CommandHelp;
import com.rammelkast.anticheatreloaded.command.executors.CommandLog;
import com.rammelkast.anticheatreloaded.command.executors.CommandMute;
import com.rammelkast.anticheatreloaded.command.executors.CommandReload;
import com.rammelkast.anticheatreloaded.command.executors.CommandReport;
import com.rammelkast.anticheatreloaded.command.executors.CommandReset;
import com.rammelkast.anticheatreloaded.command.executors.CommandVersion;

public class CommandHandler implements CommandExecutor {

    private List<CommandBase> commands = new ArrayList<CommandBase>();

    public CommandHandler() {
        commands.add(new CommandHelp());
        commands.add(new CommandChecks());
        commands.add(new CommandLog());
        commands.add(new CommandReload());
        commands.add(new CommandReport());
        commands.add(new CommandMute());
        commands.add(new CommandReset());
        commands.add(new CommandVersion());
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
        if (args.length >= 1) {
            String command = args[0];
            // Shift args down
            String[] newArgs = new String[args.length-1];
            for (int i=1;i<args.length;i++) {
                newArgs[i-1] = args[i];
            }
            for (CommandBase base : commands) {
                if (base.getCommand().equalsIgnoreCase(command)) {
                    base.run(cs, newArgs);
                    return true;
                }
            }
            // Command not found, send help
            commands.get(0).run(cs, null);
        } else {
            // Send help
            commands.get(0).run(cs, null);
        }
        return true;
    }
}
