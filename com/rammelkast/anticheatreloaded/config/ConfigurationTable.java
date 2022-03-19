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

import org.bukkit.Bukkit;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.util.enterprise.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ConfigurationTable {

	private final String table;
	private final Configuration config;

	private Database database;
	private String prefix;
	private String serverName;

	public ConfigurationTable(Configuration config, String table) {
		this.config = config;
		this.table = table;

		load();
	}

	public void load() {
		this.database = config.getEnterprise().database;
		this.prefix = database.getPrefix();
		this.serverName = config.getEnterprise().serverName.getValue();

		open();
	}

	public void open() {
		// Nothing to do
	}

	public void reload() {
		// For after sql inserts have been made
		Bukkit.getScheduler().runTask(AntiCheatReloaded.getPlugin(), new Runnable() {
			@Override
			public void run() {
				load();
			}
		});
	}

	public Database getDatabase() {
		return database;
	}

	public String getFullTable() {
		return prefix + table;
	}

	public boolean tableExists() {
		try {
			return getConnection().getMetaData().getTables(null, null, getFullTable(), null).next();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean isUUIDFormatted(String colName) {
		String sqlSelect = "SELECT " + colName + " FROM " + getFullTable() + " LIMIT 1";
		String user;

		try {
			ResultSet res = getConnection().prepareStatement(sqlSelect).executeQuery();

			if (res.next() && (user = res.getString(colName)) != null) {
				try {
					return UUID.fromString(user) != null;
				} catch (IllegalArgumentException e) {}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	@SuppressWarnings("deprecation")
	public void convertToUUID(String colName) {
		String sqlSelect = "SELECT " + colName + " FROM " + getFullTable();
		String sqlUpdate = "UPDATE " + getFullTable() + " SET " + colName + " = ? WHERE " + colName + " = ?";

		try {
			ResultSet res = getConnection().prepareStatement(sqlSelect).executeQuery();

			while (res.next()) {
				PreparedStatement stmt = getConnection().prepareStatement(sqlUpdate);
				stmt.setString(1, (Bukkit.getOfflinePlayer(res.getString(1)).getUniqueId().toString()));
				stmt.setString(2, res.getString(1));

				stmt.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public String getServerName() {
		return serverName;
	}

	public Connection getConnection() {
		return database.getConnection();
	}
}
