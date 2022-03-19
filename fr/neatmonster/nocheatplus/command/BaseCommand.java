/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.command;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.permissions.RegisteredPermission;


/**
 * Just an interface for sub commands, for future use.
 * @author mc_dev
 *
 */
public abstract class BaseCommand extends AbstractCommand<JavaPlugin>{
	

    /** The prefix of every message sent by NoCheatPlus. */
    public static final String TAG = ChatColor.GRAY +""+ ChatColor.BOLD + "[" + ChatColor.RED + "NC+" + ChatColor.GRAY +""+ ChatColor.BOLD + "] " + ChatColor.GRAY;
    public static final String CTAG = "[NoCheatPlus] ";
	
	public BaseCommand(JavaPlugin plugin, String label, RegisteredPermission permission){
		this(plugin, label, permission, null);
	}

	public BaseCommand(JavaPlugin access, String label, RegisteredPermission permission, String[] aliases){
		super(access, label, permission, aliases);
	}

}
