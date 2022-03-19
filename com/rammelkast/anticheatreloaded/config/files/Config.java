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

package com.rammelkast.anticheatreloaded.config.files;

import java.util.List;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.config.Configuration;
import com.rammelkast.anticheatreloaded.config.ConfigurationFile;

public class Config extends ConfigurationFile {

    public static final String FILENAME = "config.yml";

    public ConfigValue<Boolean> logToConsole;
    public ConfigValue<Boolean> verboseStartup;
    public ConfigValue<Boolean> silentMode;
    public ConfigValue<Boolean> exemptOp;
    public ConfigValue<Boolean> eventChains;
    public ConfigValue<Boolean> enterprise;
    public ConfigValue<Boolean> debugMode;
    public ConfigValue<Boolean> disableBroadcast;

    public ConfigValue<Boolean> blockChatSpamSpeed;
    public ConfigValue<Boolean> blockCommandSpamSpeed;
    public ConfigValue<Boolean> blockChatSpamRepetition;
    public ConfigValue<Boolean> blockCommandSpamRepetition;
    public ConfigValue<String> chatSpamActionOne;
    public ConfigValue<String> chatSpamActionTwo;
    public ConfigValue<String> commandSpamActionOne;
    public ConfigValue<String> commandSpamActionTwo;

    public ConfigValue<Boolean> logToFile;

    public ConfigValue<List<String>> disabledWorlds;
    
    public ConfigValue<Integer> notifyEveryVl;
    public ConfigValue<Integer> maxSetbackDistance;

    public Config(AntiCheatReloaded plugin, Configuration config) {
        super(plugin, config, FILENAME);
    }

    @Override
    public void open() {
        logToConsole = new ConfigValue<Boolean>("system.log-to-console");

        verboseStartup = new ConfigValue<Boolean>("system.verbose-startup");
        silentMode = new ConfigValue<Boolean>("system.silent-mode");
        exemptOp = new ConfigValue<Boolean>("system.exempt-op");
        eventChains = new ConfigValue<Boolean>("system.event-chains");
        enterprise = new ConfigValue<Boolean>("system.enterprise");
        debugMode = new ConfigValue<Boolean>("system.debug-mode");
        disableBroadcast = new ConfigValue<Boolean>("system.disable-broadcast");

        blockChatSpamSpeed = new ConfigValue<Boolean>("spam.chat.block-speed");
        blockChatSpamRepetition = new ConfigValue<Boolean>("spam.chat.block-repetition");
        chatSpamActionOne = new ConfigValue<String>("spam.chat.action-one");
        chatSpamActionTwo = new ConfigValue<String>("spam.chat.action-two");

        blockCommandSpamSpeed = new ConfigValue<Boolean>("spam.command.block-speed");
        blockCommandSpamRepetition = new ConfigValue<Boolean>("spam.command.block-repetition");
        commandSpamActionOne = new ConfigValue<String>("spam.command.action-one");
        commandSpamActionTwo = new ConfigValue<String>("spam.command.action-two");

        logToFile = new ConfigValue<Boolean>("system.log-to-file");

        disabledWorlds = new ConfigValue<List<String>>("disable-in");
        
        notifyEveryVl = new ConfigValue<Integer>("system.notify-every-vl");
        if (notifyEveryVl.getValue() < 1) {
        	notifyEveryVl.setValue(5);
        }
        maxSetbackDistance = new ConfigValue<Integer>("system.max-setback-distance");
        if (maxSetbackDistance.getValue() < 5) {
        	maxSetbackDistance.setValue(50);
        }
    }
}
