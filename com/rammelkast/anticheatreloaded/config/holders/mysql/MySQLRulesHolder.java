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
import java.util.List;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.config.Configuration;
import com.rammelkast.anticheatreloaded.config.ConfigurationTable;
import com.rammelkast.anticheatreloaded.config.providers.Rules;
import com.rammelkast.anticheatreloaded.util.rule.Rule;

public class MySQLRulesHolder extends ConfigurationTable implements Rules {

    private static final String TABLE = "rules";

    private List<Rule> rules;

    public MySQLRulesHolder(Configuration config) {
        super(config, TABLE);
    }

    @Override
    public void open() {
        String sqlCreate = "CREATE TABLE " + getFullTable() + "(" +
                "  `id` INT NOT NULL AUTO_INCREMENT," +
                "  `rule` VARCHAR(256) NOT NULL," +
                "  PRIMARY KEY (`id`));";
        String sqlPopulate = "INSERT INTO " + getFullTable() +
                "  VALUES (1, 'Check_SPIDER < 0 ? Player.KICK : null')";

        String sqlLoad = "SELECT * FROM " + getFullTable();

        try {
            if (!tableExists()) {
                getConnection().prepareStatement(sqlCreate).executeUpdate();
                getConnection().prepareStatement(sqlPopulate).executeUpdate();
                getConnection().commit();
            }

            this.rules = new ArrayList<Rule>();
            ResultSet set = getConnection().prepareStatement(sqlLoad).executeQuery();
            while (set.next()) {
                String string = set.getString("rule");
                Rule rule = Rule.load(string);
                if (rule != null) {
                    this.rules.add(rule);
                } else {
                    AntiCheatReloaded.getPlugin().getLogger().warning("Couldn't load rule '" + string + "' from the database. Improper format used.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Rule> getRules() {
        return rules;
    }
}
