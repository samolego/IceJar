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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.rammelkast.anticheatreloaded.config.Configuration;
import com.rammelkast.anticheatreloaded.config.ConfigurationTable;
import com.rammelkast.anticheatreloaded.config.providers.Groups;
import com.rammelkast.anticheatreloaded.util.Group;

public class MySQLGroupsHolder extends ConfigurationTable implements Groups {

    private static final String TABLE = "groups";

    private List<Group> groups;
    private int highestLevel;

    public MySQLGroupsHolder(Configuration config) {
        super(config, TABLE);
    }

    @Override
    public void open() {
        highestLevel = 0;
        groups = new ArrayList<Group>();

        String sqlCreate = "CREATE TABLE " + getFullTable() + "(" +
                "  `id` INT NOT NULL AUTO_INCREMENT," +
                "  `name` VARCHAR(45) NOT NULL," +
                "  `level` INT NOT NULL," +
                "  `color` VARCHAR(45) NOT NULL," +
                "  `actions` VARCHAR(45) NOT NULL," +
                "  PRIMARY KEY (`id`));";
        String sqlPopulate = "INSERT INTO " + getFullTable() +
                "  SELECT t.*" +
                "  FROM (" +
                "    (SELECT 1 as id, 'Medium' as name, 20 as level, 'YELLOW' as color, 'WARN' as actions)" +
                "    UNION ALL " +
                "    (SELECT 2 as id, 'High' as name, 50 as level, 'RED' as color, 'KICK' as actions)" +
                "  ) t;";
        String sqlLoad = "SELECT * FROM " + getFullTable();

        try {
            if (!tableExists()) {
                getConnection().prepareStatement(sqlCreate).executeUpdate();
                getConnection().prepareStatement(sqlPopulate).executeUpdate();
                getConnection().commit();
            }

            ResultSet set = getConnection().prepareStatement(sqlLoad).executeQuery();
            while (set.next()) {
                String name = set.getString("name");
                int level = set.getInt("level");
                String color = set.getString("color");
                List<String> actions = Arrays.asList(set.getString("actions").split(","));

                groups.add(new Group(name, level, color, actions));
                if (level > highestLevel) {
                    highestLevel = level;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Group> getGroups() {
        return groups;
    }

    @Override
    public int getHighestLevel() {
        return highestLevel;
    }
}
