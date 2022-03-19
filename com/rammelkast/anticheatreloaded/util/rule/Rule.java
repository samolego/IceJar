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

package com.rammelkast.anticheatreloaded.util.rule;

import org.bukkit.GameMode;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.util.Group;
import com.rammelkast.anticheatreloaded.util.User;
import com.rammelkast.anticheatreloaded.util.Utilities;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A rule is a scriptable function used to customize the functions of AntiCheat<br />
 * Rules are executed whenever a user's level rises.<br /><br />
 * <p/>
 * There are various variables you can use to interact with AntiCheat through your rules<br />
 * Variables are denoted with the type followed by a _ followed by the variable name.<br />
 * For instance, a variable coming from a user who has used FLY 5 times and is checked by a rule containing 'Check_FLY' will produce a value of 5.
 * <br /><br />
 * <p/>
 * <b>Types of variables:</b><br />
 * <ul>
 *   <li>Check
 *     <ul>
 *       <li>Contains all valid checks as listed in {@link CheckType}</li>
 *       <li>Will return the number of times this user has failed the given check</li>
 *       <li><b>Example:</b> Check_SPRINT</li>
 *     </ul>
 *   </li>
 *   <li>Player
 *     <ul>
 *       <li>Contains NAME, the name of the player</li>
 *       <li>Contains LEVEL*, the player's current level</li>
 *       <li>Contains GROUP*, the name of the player's current hack group</li>
 *       <li>Contains CHECK, the check that was just failed</li>
 *       <li>Contains GAMEMODE*, the player's current Game Mode (Survival, Creative, Adventure)/li>
 *       <li>Contains WORLD, the name of the world the player is in/li>
 *       <li>Contains HEALTH*, the player's current health/li>
 *       <li>A * denotes that this value can be set, for example Player_HEALTH = 20.0/li>
 *     </ul>
 *   </li>
 * </ul>
 * <p/>
 * There are also functions you can use to execute an action within AntiCheat<br />
 * Functions are denoted with the type followed by a period followed by the function name.<br />
 * For instance, a rule containing Player.KICK will result in the user being kicked.<br />
 * <br /><br />
 * <p/>
 * <b>Types of functions:</b><br />
 * - Player: RESET, KICK, BAN, COMMAND[command] OR COMMAND[command1;command2]
 * <i>- when using commands <b>%player%</b> will be replaced by the player name, <b>&world</b> will be replaced by the player's world,
 * and <b>&check</b> will be replaced by the check that caused this rule to be run</i><br />
 * <br /><br />
 * <p/>
 * The Rule class itself is not an functional rule setup,
 * it is inherited and made functional by different implementations of the rule parser.<br />
 * The only current Rule implementation is the {@link ConditionalRule}
 */
public class Rule {

    private static final String VARIABLE_SET_REGEX = ".*(_).*=.*";
    private static final String FUNCTION_REGEX = ".*\\..*";
    private String string;
    private Type type;

    public enum Type {
        CONDITIONAL(".*[?]*.*:.*", "com.rammelkast.anticheatreloaded.util.rule.ConditionalRule");

        private String regex;
        private String c;

        private Type(String regex, String c) {
            this.regex = regex;
            this.c = c;
        }

        public boolean matches(String s) {
            return s.matches(regex);
        }

        public String getInstance() {
            return c;
        }
    }

    public Rule(String string, Type type) {
        this.string = Utilities.removeWhitespace(string).toLowerCase();
        this.type = type;
    }

    /**
     * Check if the rule passed or failed
     *
     * @param user the user to check
     * @return true if the rule has passed, false if failed
     */
    public boolean check(User user, CheckType type) {
        // Default value
        return true;
    }

    /**
     * Load a rule by its string value
     *
     * @param string the string value to load
     * @return an instance of Rule if an implementation exists to handle this rule, null if none are found
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static Rule load(String string) {
        for (Type type : Type.values()) {
            if (type.matches(string)) {
                try {
                    Class c = Class.forName(type.getInstance());
                    Constructor con = c.getConstructor(String.class);
                    return (Rule) con.newInstance(string);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Get the type of rule this is
     *
     * @return a {@link Type}
     */
    public Type getType() {
        return type;
    }

    protected String getString() {
        return string;
    }

    protected SortedMap<String, Object> getVariables(User user, CheckType type) {
        SortedMap<String, Object> map = new TreeMap<String, Object>();
        map.put("player_check", type.name().toLowerCase());
        map.put("player_level", user.getLevel());
        map.put("player_group", user.getGroup() != null ? user.getGroup().getName().toLowerCase() : "low");
        map.put("player_uuid", user.getUUID().toString());
        map.put("player_gamemode", user.getPlayer().getGameMode().toString());
        map.put("player_world", user.getPlayer().getWorld().getName());
        map.put("player_health", user.getPlayer().getHealth());
        for (CheckType t : CheckType.values()) {
            map.put("check_" + t.name().toLowerCase(), t.getUses(user.getUUID()));
        }
        return map;
    }

    protected void setVariable(String variable, String value, User user) {
        if (variable.equals("player_level") && Utilities.isInt(value)) {
            user.setLevel(Integer.parseInt(value));
        } else if (variable.equals("player_group") && Utilities.isInt(value)) {
            for (Group group : AntiCheatReloaded.getManager().getConfiguration().getGroups().getGroups()) {
                if (group.getName().equalsIgnoreCase(value)) {
                    user.setLevel(group.getLevel());
                }
            }
        } else if (variable.equals("player_gamemode")) {
            try {
                GameMode mode = GameMode.valueOf(value);
                user.getPlayer().setGameMode(mode);
            } catch (IllegalArgumentException ex) {
            }
        } else if (variable.equals("player_health") && Utilities.isDouble(value)) {
            user.getPlayer().setHealth(Double.parseDouble(value));
        }
    }

    protected void doFunction(String text, CheckType type, User user) {
        if (text.toLowerCase().startsWith("player")) {
            text = text.split("\\.")[1];
            List<String> action = new ArrayList<String>();
            action.add(text);
            AntiCheatReloaded.getManager().getUserManager().execute(user, action, type);
        }
    }

    protected boolean isFunction(String string) {
        return string.matches(FUNCTION_REGEX);
    }

    protected boolean isVariableSet(String string) {
        return string.matches(VARIABLE_SET_REGEX);
    }

    @Override
    public String toString() {
        return type + "{" + string + "}";
    }
}
