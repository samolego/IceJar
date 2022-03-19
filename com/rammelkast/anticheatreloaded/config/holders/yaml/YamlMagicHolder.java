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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.config.Configuration;
import com.rammelkast.anticheatreloaded.config.ConfigurationFile;

public class YamlMagicHolder extends ConfigurationFile implements InvocationHandler {

    public static final String FILENAME = "magic.yml";

    public YamlMagicHolder(AntiCheatReloaded plugin, Configuration config) {
        super(plugin, config, FILENAME);
    }

    public Object invoke(Object proxy, Method method, Object[] args) {
        String key = method.getName();

        if (method.getReturnType().getSimpleName().equals("int")) {
            return new ConfigValue<Integer>(key).getValue();
        } else if (method.getReturnType().getSimpleName().equals("double")) {
            return new ConfigValue<Double>(key).getValue();
        } else {
            AntiCheatReloaded.getPlugin().getLogger().severe("The magic value " + key + " couldn't be found.");
            return -1;
        }
    }
}
