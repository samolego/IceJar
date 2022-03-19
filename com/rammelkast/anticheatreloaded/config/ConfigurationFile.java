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

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.config.yaml.CommentedConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConfigurationFile {

    private File rawFile;
    private String fileName;

    private CommentedConfiguration configFile;
    private FileConfiguration defaultConfigFile;
    private Configuration config;

    private AntiCheatReloaded plugin;

    private boolean saveDefault;

    protected boolean needsReload;

    public ConfigurationFile(AntiCheatReloaded plugin, Configuration config, String fileName) {
        this(plugin, config, fileName, true);
    }

    public ConfigurationFile(AntiCheatReloaded plugin, Configuration config, String fileName, boolean saveDefault) {
        this(plugin, config, fileName, saveDefault, new File(plugin.getDataFolder(), fileName));
    }

    public ConfigurationFile(AntiCheatReloaded plugin, Configuration config, String fileName, boolean saveDefault, File rawFile) {
        // Can't change with user editing
        this.plugin = plugin;
        this.config = config;
        this.fileName = fileName;
        this.rawFile = rawFile;
        this.saveDefault = saveDefault;

        if (saveDefault) {
        	InputStreamReader reader = new InputStreamReader(plugin.getResource(fileName));
            defaultConfigFile = YamlConfiguration.loadConfiguration(reader);
            try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        load();
    }

    public void load() {
        // Can change with user editing
        if (!rawFile.exists()) {
            if (saveDefault) {
                plugin.saveResource(fileName, true);
            } else {
                if (!rawFile.getParentFile().exists()) {
                    rawFile.getParentFile().mkdir();
                }
                try {
                    rawFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        configFile = CommentedConfiguration.loadConfiguration(rawFile);

        open();
    }

    public void open() {
        // Nothing to do
    }

    public void close() {
        // Nothing to do
    }

    public void save() {
        close();
        try {
            configFile.save(rawFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        save();
        load();
    }

    public File getRawFile() {
        return rawFile;
    }

    public CommentedConfiguration getConfigFile() {
        return configFile;
    }

    public FileConfiguration getDefaultConfigFile() {
        return defaultConfigFile;
    }

    public Configuration getConfiguration() {
        return config;
    }

    public void setNeedsReload(boolean reload) {
        this.needsReload = reload;
    }

    public boolean needsReload() {
        return needsReload;
    }

    public class ConfigValue<T> {

        private String path;

        private Object value = null;

        private boolean didLoadDefault;

        public ConfigValue(String path) {
            this(path, true);
        }

        public ConfigValue(String path, boolean loadDefault) {
            this.path = path;

            if (getConfigFile().contains(path)) {
                value = getConfigFile().get(path);
            } else if (loadDefault) {
                getConfigFile().set(path, getDefaultConfigFile().get(path));
                value = getDefaultConfigFile().get(path);
                getConfigFile().set(path, value);
                save();
                didLoadDefault = true;
            }
        }

        public String getPath() {
            return path;
        }

        @SuppressWarnings("unchecked")
		public T getValue() {
            return (T) value;
        }

        public T setValue(T value) {
            getConfigFile().set(path, value);
            save();
            return value;
        }

        @SuppressWarnings("unchecked")
		public T getDefaultValue() {
            return (T) getDefaultConfigFile().get(path);
        }

        public boolean hasValue() {
            return value != null;
        }

        public boolean didLoadDefault() {
            return didLoadDefault;
        }
    }
}
