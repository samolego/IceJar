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
import java.util.List;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.config.Configuration;
import com.rammelkast.anticheatreloaded.config.ConfigurationFile;

public class YamlLangHolder extends ConfigurationFile implements InvocationHandler {

    public static final String FILENAME = "lang.yml";

    public YamlLangHolder(AntiCheatReloaded plugin, Configuration config) {
        super(plugin, config, FILENAME);
    }

    @Override
    public void open() {
        if (new ConfigValue<String>("chat.ban_broadcast").getValue() != null) {
            update();
        }
    }

    private void update() {
        new ConfigValue<List<String>>("ALERT").setValue(new ConfigValue<List<String>>("alert").getValue());
        new ConfigValue<List<String>>("WARNING").setValue(new ConfigValue<List<String>>("warning.player_warning").getValue());
        new ConfigValue<List<String>>("alert").setValue(null);
        new ConfigValue<List<String>>("warning.player_warning").setValue(null);
        updateValue("ban.ban_reason", "BAN_REASON");
        updateValue("ban.ban_broadcast", "BAN_BROADCAST");
        updateValue("kick.kick_reason", "KICK_REASON");
        updateValue("kick.kick_broadcast", "KICK_BROADCAST");
        updateValue("chat.warning", "SPAM_WARNING");
        updateValue("chat.kick_reason", "SPAM_KICK_REASON");
        updateValue("chat.kick_broadcast", "SPAM_KICK_BROADCAST");
        updateValue("chat.ban_reason", "SPAM_BAN_REASON");
        updateValue("chat.ban_broadcast", "SPAM_BAN_BROADCAST");
    }

    private void updateValue(String oldValue, String newValue) {
        new ConfigValue<String>(newValue).setValue(new ConfigValue<String>(oldValue).getValue());
        new ConfigValue<String>(oldValue).setValue(null);
    }

    public Object invoke(Object proxy, Method method, Object[] args) {
        String key = method.getName();

        if (method.getReturnType() == List.class) {
            ConfigValue<List<String>> value = new ConfigValue<List<String>>(key);
            if (value.getValue() != null) {
                return value.getValue();
            }
        } else {
            ConfigValue<String> value = new ConfigValue<String>(key);
            if (value.getValue() != null) {
                return value.getValue();
            }
        }
        AntiCheatReloaded.getPlugin().getLogger().severe("The lang value " + key + " couldn't be found.");
        return "Language error. See console for details.";
    }
}
