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

import java.util.*;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.config.Configuration;
import com.rammelkast.anticheatreloaded.config.ConfigurationFile;
import com.rammelkast.anticheatreloaded.config.providers.Groups;
import com.rammelkast.anticheatreloaded.util.Group;

public class YamlGroupsHolder extends ConfigurationFile implements Groups {

    public static final String FILENAME = "groups.yml";

    private List<Group> groups;

    private int highestLevel;

    public YamlGroupsHolder(AntiCheatReloaded plugin, Configuration config) {
        super(plugin, config, FILENAME);
    }

    @Override
    public List<Group> getGroups() {
        return groups;
    }

    @Override
    public int getHighestLevel() {
        return highestLevel;
    }

    @Override
    public void open() {
        ConfigValue<List<String>> groups = new ConfigValue<List<String>>("groups");

        // Convert groups list to Levels
        this.groups = new ArrayList<Group>();
        for (String string : groups.getValue()) {
            Group group = Group.load(string);
            if (group == null || group.getLevel() < 0) {
                continue;
            }

            this.groups.add(group);
            highestLevel = group.getLevel() > highestLevel ? group.getLevel() : highestLevel;
        }

        // Sort groups
        Collections.sort(this.groups, new Comparator<Group>() {
            public int compare(Group l1, Group l2) {
                if (l1.getLevel() == l2.getLevel()) {
                    return 0;
                } else if (l1.getLevel() < l2.getLevel()) {
                    return -1;
                }
                return 1;
            }
        });
    }
}
