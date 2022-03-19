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

package com.rammelkast.anticheatreloaded.config.holders.mysql;

import org.bukkit.Bukkit;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.config.Configuration;
import com.rammelkast.anticheatreloaded.config.ConfigurationTable;
import com.rammelkast.anticheatreloaded.config.providers.Levels;
import com.rammelkast.anticheatreloaded.util.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class MySQLLevelsHolder extends ConfigurationTable implements Levels {

    private static final String TABLE = "levels";
    private static final String MANUAL_EDIT_TAG = "MANUAL";

    private String sqlSave;
    private String sqlLoad;
    private String sqlUpdate;

    public MySQLLevelsHolder(Configuration config) {
        super(config, TABLE);
    }

    @Override
    public void open() {
        sqlSave = "INSERT INTO " + getFullTable() +
                " (user, level, last_update_server) " +
                "VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "level = IF(last_update = ?, ?, level+?), " +
                "last_update=CURRENT_TIMESTAMP, " +
                "last_update_server=?";

        sqlLoad = "SELECT level, last_update FROM " + getFullTable() + " " +
                "WHERE user = ?";

        sqlUpdate = "SELECT level, last_update FROM " + getFullTable() + " " +
                "WHERE user = ? AND last_update <> ? AND last_update_server = ?";

        String sqlCreate = "CREATE TABLE " + getFullTable() + "(" +
                "  `user` VARCHAR(45) NOT NULL," +
                "  `level` INT NOT NULL," +
                "  `last_update` TIMESTAMP NOT NULL DEFAULT NOW()," +
                "  `last_update_server` VARCHAR(45) NOT NULL," +
                "  PRIMARY KEY (`user`));";

        try {
            if (!tableExists()) {
                getConnection().prepareStatement(sqlCreate).executeUpdate();
                getConnection().commit();
            } else if (!isUUIDFormatted("user")) {
            	convertToUUID("user");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveLevelFromUser(final User user) {
        Bukkit.getScheduler().runTaskAsynchronously(AntiCheatReloaded.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (!user.isWaitingOnLevelSync()) {
                    AntiCheatReloaded.debugLog("Saving level from " + user.getUUID() + ". Value: " + user.getLevel());
                    try {
                        PreparedStatement statement = getConnection().prepareStatement(sqlSave);
                        statement.setString(1, user.getUUID().toString());
                        statement.setInt(2, user.getLevel());
                        statement.setString(3, getServerName());
                        statement.setTimestamp(4, user.getLevelSyncTimestamp());
                        statement.setInt(5, user.getLevel());
                        statement.setInt(6, user.getLevel());
                        statement.setString(7, getServerName());

                        statement.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                AntiCheatReloaded.getManager().getUserManager().removeUser(user);
            }
        });
    }

    @Override
    public void loadLevelToUser(final User user) {
        Bukkit.getScheduler().runTaskAsynchronously(AntiCheatReloaded.getPlugin(), new Runnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement statement = getConnection().prepareStatement(sqlLoad);
                    statement.setString(1, user.getUUID().toString());

                    ResultSet set = statement.executeQuery();

                    boolean has = false;
                    while (set.next()) {
                        has = true;
                        AntiCheatReloaded.debugLog("Syncing level to " + user.getUUID() + ". Value: " + set.getInt("level"));
                        user.setLevel(set.getInt("level"));
                        user.setLevelSyncTimestamp(set.getTimestamp("last_update"));
                    }
                    if (!has) user.setLevel(0);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void saveLevelsFromUsers(List<User> users) {
        try {
            PreparedStatement statement = getConnection().prepareStatement(sqlSave);
            for (User user : users) {
                if (!user.isWaitingOnLevelSync()) {
                    try {
                        statement.setString(1, user.getUUID().toString());
                        statement.setInt(2, user.getLevel());
                        statement.setString(3, getServerName());
                        statement.setTimestamp(4, user.getLevelSyncTimestamp());
                        statement.setInt(5, user.getLevel());
                        statement.setInt(6, user.getLevel());
                        statement.setString(7, getServerName());

                        statement.addBatch();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            statement.executeBatch();
            getConnection().commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateLevelToUser(final User user) {
        Bukkit.getScheduler().runTaskAsynchronously(AntiCheatReloaded.getPlugin(), new Runnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement statement = getConnection().prepareStatement(sqlUpdate);
                    statement.setString(1, user.getUUID().toString());
                    statement.setTimestamp(2, user.getLevelSyncTimestamp());
                    statement.setString(3, MANUAL_EDIT_TAG);

                    ResultSet set = statement.executeQuery();

                    while (set.next()) {
                        AntiCheatReloaded.debugLog("Syncing level to " + user.getUUID() + ". Value: " + set.getInt("level"));
                        user.setLevel(set.getInt("level"));
                        user.setLevelSyncTimestamp(set.getTimestamp("last_update"));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
