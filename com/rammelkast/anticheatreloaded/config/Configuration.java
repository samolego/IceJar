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

package com.rammelkast.anticheatreloaded.config;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;

import org.bukkit.configuration.file.YamlConfiguration;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.config.files.Config;
import com.rammelkast.anticheatreloaded.config.files.Enterprise;
import com.rammelkast.anticheatreloaded.config.holders.mysql.MySQLGroupsHolder;
import com.rammelkast.anticheatreloaded.config.holders.mysql.MySQLLangHolder;
import com.rammelkast.anticheatreloaded.config.holders.mysql.MySQLLevelsHolder;
import com.rammelkast.anticheatreloaded.config.holders.mysql.MySQLMagicHolder;
import com.rammelkast.anticheatreloaded.config.holders.mysql.MySQLRulesHolder;
import com.rammelkast.anticheatreloaded.config.holders.yaml.YamlChecksHolder;
import com.rammelkast.anticheatreloaded.config.holders.yaml.YamlGroupsHolder;
import com.rammelkast.anticheatreloaded.config.holders.yaml.YamlLangHolder;
import com.rammelkast.anticheatreloaded.config.holders.yaml.YamlLevelsHolder;
import com.rammelkast.anticheatreloaded.config.holders.yaml.YamlMagicHolder;
import com.rammelkast.anticheatreloaded.config.holders.yaml.YamlRulesHolder;
import com.rammelkast.anticheatreloaded.config.providers.Checks;
import com.rammelkast.anticheatreloaded.config.providers.Groups;
import com.rammelkast.anticheatreloaded.config.providers.Lang;
import com.rammelkast.anticheatreloaded.config.providers.Levels;
import com.rammelkast.anticheatreloaded.config.providers.Magic;
import com.rammelkast.anticheatreloaded.config.providers.Rules;
import com.rammelkast.anticheatreloaded.manage.AntiCheatManager;

public class Configuration {

    private AntiCheatManager manager;
    private Config config;
    private Enterprise enterprise;

    private Lang lang;
    private Magic magic;
    private Groups groups;
    private Levels levels;
    private Rules rules;
    private Checks checks;

    private ArrayList<ConfigurationFile> flatfiles;
    private ArrayList<ConfigurationTable> dbfiles;

    public Configuration(AntiCheatReloaded plugin, AntiCheatManager manager) {
        removeOldFiles();
        this.manager = manager;
        config = new Config(plugin, this);
        plugin.setVerbose(config.verboseStartup.getValue());
        // Now load others
        enterprise = new Enterprise(plugin, this);

		flatfiles = new ArrayList<ConfigurationFile>() {
			private static final long serialVersionUID = 4497798966909062852L;
			{
				add(config);
				add(enterprise);
			}
		};

        dbfiles = new ArrayList<ConfigurationTable>();

        // The following values can be configuration from a database, or from flatfile.
        if (config.enterprise.getValue() && enterprise.configGroups.getValue()) {
            groups = new MySQLGroupsHolder(this);
            dbfiles.add((MySQLGroupsHolder) groups);
        } else {
            groups = new YamlGroupsHolder(plugin, this);
            flatfiles.add((YamlGroupsHolder) groups);
        }

        if (config.enterprise.getValue() && enterprise.configRules.getValue()) {
            rules = new MySQLRulesHolder(this);
            dbfiles.add((MySQLRulesHolder) rules);
        } else {
            rules = new YamlRulesHolder(plugin, this);
            flatfiles.add((YamlRulesHolder) rules);
        }
        
        checks = new YamlChecksHolder(plugin, this);
        flatfiles.add((YamlChecksHolder) checks);

        InvocationHandler handler;
        if (config.enterprise.getValue() && enterprise.configMagic.getValue()) {
            handler = new MySQLMagicHolder(this);
            magic = (Magic) Proxy.newProxyInstance(Magic.class.getClassLoader(),
                    new Class[] { Magic.class },
                    handler);
            dbfiles.add((MySQLMagicHolder) handler);
        } else {
            handler = new YamlMagicHolder(plugin, this);
            magic = (Magic) Proxy.newProxyInstance(Magic.class.getClassLoader(),
                    new Class[] { Magic.class },
                    handler);
            flatfiles.add((YamlMagicHolder) handler);
        }

        if (config.enterprise.getValue() && enterprise.configLang.getValue()) {
            handler = new MySQLLangHolder(this);
            lang = (Lang) Proxy.newProxyInstance(Lang.class.getClassLoader(),
                    new Class[] { Lang.class },
                    handler);
            dbfiles.add((MySQLLangHolder) handler);
        } else {
            handler = new YamlLangHolder(plugin, this);
            lang = (Lang) Proxy.newProxyInstance(Lang.class.getClassLoader(),
                    new Class[] { Lang.class },
                    handler);
            flatfiles.add((YamlLangHolder) handler);
        }

        if (config.enterprise.getValue() && enterprise.syncLevels.getValue()) {
            levels = new MySQLLevelsHolder(this);
            dbfiles.add((MySQLLevelsHolder) levels);
        } else {
            levels = new YamlLevelsHolder(plugin, this);
            flatfiles.add((YamlLevelsHolder) levels);
        }
        // End

        for (ConfigurationFile file : flatfiles) {
            file.save();
            checkReload(file);
        }
    }

    public void load() {
        for (ConfigurationFile file : flatfiles) {
            file.load();
            checkReload(file);
        }
        
        for (ConfigurationTable table : dbfiles)
            table.load();
        
        if (manager.getBackend() != null)
            manager.getBackend().updateConfig(this);
        
        if (manager.getCheckManager() != null)
        	manager.getCheckManager().loadCheckIgnoreList(this);
    }

    private void checkReload(ConfigurationFile file) {
        if (file.needsReload()) {
            file.reload();
            file.setNeedsReload(false);
        }
    }

    private void removeOldFiles() {
        ArrayList<String> removed = new ArrayList<String>();
        File configFile = new File(AntiCheatReloaded.getPlugin().getDataFolder(), "config.yml");
        if (configFile.exists() && YamlConfiguration.loadConfiguration(configFile).getString("System.Auto update") != null) {
            configFile.renameTo(new File(AntiCheatReloaded.getPlugin().getDataFolder(), "config.old"));
            removed.add("config.yml has been renamed to config.old and replaced with the new config.yml");
        }
        File eventsFile = new File(AntiCheatReloaded.getPlugin().getDataFolder(), "events.yml");
        if (eventsFile.exists()) {
            eventsFile.renameTo(new File(AntiCheatReloaded.getPlugin().getDataFolder(), "events.old"));
            removed.add("events.yml has been renamed to events.old and replaced with groups.yml and rules.yml");
        }
        if (removed.size() > 0) {
            AntiCheatReloaded.getPlugin().getLogger().info("You are upgrading from an old version of AntiCheat. Due to configuration changes, the following files have been modified:");
            for (String s : removed) {
                AntiCheatReloaded.getPlugin().getLogger().info(s);
            }
        }
    }

    public Config getConfig() {
        return config;
    }

    public Groups getGroups() {
        return groups;
    }

    public Rules getRules() {
        return rules;
    }

    public Lang getLang() {
        return lang;
    }

    public Enterprise getEnterprise() {
        return enterprise;
    }

    public Levels getLevels() {
        return levels;
    }

    public Magic getMagic() {
        return magic;
    }
    
    public Checks getChecks() {
    	return checks;
    }
}
