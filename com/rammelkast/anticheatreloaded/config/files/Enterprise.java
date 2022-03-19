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

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.config.Configuration;
import com.rammelkast.anticheatreloaded.config.ConfigurationFile;
import com.rammelkast.anticheatreloaded.util.enterprise.Database;

public class Enterprise extends ConfigurationFile {

    public static final String FILENAME = "enterprise.yml";

    public ConfigValue<String> serverName;

    public ConfigValue<Boolean> loggingEnabled;
    public ConfigValue<String> loggingLife;
    public ConfigValue<String> loggingInterval;

    public ConfigValue<Boolean> syncLevels;
    public ConfigValue<String> syncInterval;

    public ConfigValue<Boolean> configGroups;
    public ConfigValue<Boolean> configRules;
    public ConfigValue<Boolean> configMagic;
    public ConfigValue<Boolean> configLang;

    public Database database;

    public Enterprise(AntiCheatReloaded plugin, Configuration config) {
        super(plugin, config, FILENAME);
    }

    @Override
    public void open() {
        serverName = new ConfigValue<String>("server.name");

        loggingEnabled = new ConfigValue<Boolean>("log.enable");
        loggingLife = new ConfigValue<String>("log.life");
        loggingInterval = new ConfigValue<String>("log.interval");

        syncLevels = new ConfigValue<Boolean>("sync.levels");
        syncInterval = new ConfigValue<String>("sync.interval");

        configGroups = new ConfigValue<Boolean>("config.groups");
        configRules = new ConfigValue<Boolean>("config.rules");
        configMagic = new ConfigValue<Boolean>("config.magic");
        configLang = new ConfigValue<Boolean>("config.lang");

        ConfigValue<String> databaseType = new ConfigValue<String>("database.type");
        ConfigValue<String> databaseHostname = new ConfigValue<String>("database.hostname");
        ConfigValue<String> databaseUsername = new ConfigValue<String>("database.username");
        ConfigValue<String> databasePassword = new ConfigValue<String>("database.password");
        ConfigValue<String> databasePrefix = new ConfigValue<String>("database.prefix");
        ConfigValue<String> databaseSchema = new ConfigValue<String>("database.database");
        ConfigValue<Integer> databasePort = new ConfigValue<Integer>("database.port");

        if (getConfiguration().getConfig().enterprise.getValue()) {
            // Convert database values to Database
            database = new Database(
                    Database.DatabaseType.valueOf(databaseType.getValue()),
                    databaseHostname.getValue(),
                    databasePort.getValue(),
                    databaseUsername.getValue(),
                    databasePassword.getValue(),
                    databasePrefix.getValue(),
                    databaseSchema.getValue(),
                    serverName.getValue(),
                    loggingInterval.getValue(),
                    loggingLife.getValue(),
                    syncLevels.getValue(),
                    syncInterval.getValue()
            );

            database.connect();
        }
    }
}
