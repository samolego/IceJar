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
package com.rammelkast.anticheatreloaded.config.holders.yaml;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.config.Configuration;
import com.rammelkast.anticheatreloaded.config.ConfigurationFile;
import com.rammelkast.anticheatreloaded.config.providers.Checks;

public class YamlChecksHolder extends ConfigurationFile implements Checks {

    public static final String FILENAME = "checks.yml";

    public YamlChecksHolder(AntiCheatReloaded plugin, Configuration config) {
        super(plugin, config, FILENAME);
    }

    @Override
    public void open() {
 
    }

	@Override
	public boolean getBoolean(CheckType checkType, String subcheck, String name) {
		final ConfigurationSection section = this.getConfigFile().getConfigurationSection(checkType.getName().toLowerCase()).getConfigurationSection(subcheck);
		if (!section.contains(name)) {
			AntiCheatReloaded.getPlugin().getLogger().warning("Configuration is missing option '" + section.getCurrentPath() + "." + name + "'");
			return false;
		}
		return section.getBoolean(name);
	}

	@Override
	public boolean isSubcheckEnabled(CheckType checkType, String subcheck) {
		return this.getBoolean(checkType, subcheck, "enabled");
	}

	@Override
	public double getDouble(CheckType checkType, String subcheck, String name) {
		final ConfigurationSection section = this.getConfigFile().getConfigurationSection(checkType.getName().toLowerCase()).getConfigurationSection(subcheck);
		if (!section.contains(name)) {
			AntiCheatReloaded.getPlugin().getLogger().warning("Configuration is missing option '" + section.getCurrentPath() + "." + name + "'");
			return 0.0D;
		}
		return section.getDouble(name);
	}

	@Override
	public int getInteger(CheckType checkType, String subcheck, String name) {
		final ConfigurationSection section = this.getConfigFile().getConfigurationSection(checkType.getName().toLowerCase()).getConfigurationSection(subcheck);
		if (!section.contains(name)) {
			AntiCheatReloaded.getPlugin().getLogger().warning("Configuration is missing option '" + section.getCurrentPath() + "." + name + "'");
			return 0;
		}
		return section.getInt(name);
	}

	@Override
	public boolean getBoolean(CheckType checkType, String name) {
		final ConfigurationSection section =  this.getConfigFile().getConfigurationSection(checkType.getName().toLowerCase());
		if (!section.contains(name)) {
			AntiCheatReloaded.getPlugin().getLogger().warning("Configuration is missing option '" + section.getCurrentPath() + "." + name + "'");
			return false;
		}
		return section.getBoolean(name);
	}

	@Override
	public double getDouble(CheckType checkType, String name) {
		final ConfigurationSection section =  this.getConfigFile().getConfigurationSection(checkType.getName().toLowerCase());
		if (!section.contains(name)) {
			AntiCheatReloaded.getPlugin().getLogger().warning("Configuration is missing option '" + section.getCurrentPath() + "." + name + "'");
			return 0.0D;
		}
		return section.getDouble(name);
	}

	@Override
	public int getInteger(CheckType checkType, String name) {
		final ConfigurationSection section =  this.getConfigFile().getConfigurationSection(checkType.getName().toLowerCase());
		if (!section.contains(name)) {
			AntiCheatReloaded.getPlugin().getLogger().warning("Configuration is missing option '" + section.getCurrentPath() + "." + name + "'");
			return 0;
		}
		return section.getInt(name);
	}

	@Override
	public boolean isEnabled(CheckType checkType) {
		// If the root of a check has no enabled or disabled option, it has to be configured on a subcheck basis
		if (!this.getConfigFile().isConfigurationSection(checkType.getName().toLowerCase())) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "ACR " + ChatColor.RED
					+ "Missing configuration settings for " + checkType.getName() + "!");
			return true;
		}
		if (!this.getConfigFile().getConfigurationSection(checkType.getName().toLowerCase()).contains("enabled"))
			return true;
		return this.getConfigFile().getConfigurationSection(checkType.getName().toLowerCase()).getBoolean("enabled");
	}
}
