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
 * GNU General Public License for more details.-
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.rammelkast.anticheatreloaded.manage;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.bukkit.plugin.RegisteredListener;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.Backend;
import com.rammelkast.anticheatreloaded.config.Configuration;

/**
 * The internal hub for all managers.
 */
public final class AntiCheatManager {
	
    private static AntiCheatReloaded plugin;
    private static Configuration configuration;
    private static UserManager userManager;
    private static CheckManager checkManager;
    private static LoggingManager loggingManager;
    private static Backend backend;
    private static Map<String, RegisteredListener[]> eventchains = new ConcurrentHashMap<String, RegisteredListener[]>();
    private static Map<String, Long> eventcache = new ConcurrentHashMap<String, Long>();

    public AntiCheatManager(final AntiCheatReloaded instance, final Logger logger) {
        plugin = instance;
        configuration = new Configuration(plugin, this);
        loggingManager = new LoggingManager(plugin, logger, configuration);
        userManager = new UserManager(this);
        checkManager = new CheckManager(this);
        backend = new Backend(this);
    }

    public void log(String message) {
        loggingManager.log(message);
    }

    public void debugLog(String message) {
        loggingManager.debugLog(message);
    }

    public void playerLog(String message) {
        loggingManager.logToPlayers(message);
    }

    public void addEvent(String e, RegisteredListener[] arr) {
        if (!configuration.getConfig().eventChains.getValue())
            return;
        if (!eventcache.containsKey(e) || eventcache.get(e) > 30000L) {
            eventchains.put(e, arr);
            eventcache.put(e, System.currentTimeMillis());
        }
    }

    public String getEventChainReport() {
        String gen = "";
        if (!configuration.getConfig().eventChains.getValue()) {
            return "Event Chains is disabled by the configuration." + '\n';
        }

        if (eventchains.entrySet().size() == 0) {
            return "No event chains found." + '\n';
        }

        for (Entry<String, RegisteredListener[]> e : eventchains.entrySet()) {
            String toadd = "";
            String ename = e.getKey();

            toadd += ename + ":" + '\n';

            RegisteredListener[] reg = e.getValue();
            for (RegisteredListener plug : reg) {
                String pluginname = plug.getPlugin().getName();
                if (pluginname.equals("AntiCheat"))
                    pluginname = "self";

                toadd += "- " + pluginname + '\n';
            }

            gen += toadd + '\n';
        }

        return gen;
    }

    public AntiCheatReloaded getPlugin() {
        return plugin;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public CheckManager getCheckManager() {
        return checkManager;
    }

    public Backend getBackend() {
        return backend;
    }

    public LoggingManager getLoggingManager() {
        return loggingManager;
    }

    public static void close() {
        loggingManager.closeHandler();

        if (configuration.getConfig().enterprise.getValue()) {
            configuration.getEnterprise().database.shutdown();
        }
        
        eventchains.clear();
        eventcache.clear();
        
        // Nullify
        plugin = null;
        configuration = null;
        loggingManager = null;
        userManager = null;
        checkManager = null;
        backend = null;
        eventchains = null;
        eventcache = null;
    }
}
