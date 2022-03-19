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
package fr.neatmonster.nocheatplus.permissions;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import fr.neatmonster.nocheatplus.command.CommandUtil;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.utilities.ColorUtil;

public class PermissionUtil {

    public static final String AUTO_GENERATED = "Auto-generated permission (NoCheatPlus).";

    /**
     * Entry for what the old state of a command was.
     * @author mc_dev
     *
     */
    public static class CommandProtectionEntry{
        public final  Command command;
        public final String label;
        public final String permission;
        public final PermissionDefault permissionDefault;
        public final  String permissionMessage;
        /**
         * 
         * @param command
         * @param label trim + lower case.
         * @param permission
         * @param permissionDefault
         * @param permissionMessage
         */
        public CommandProtectionEntry(Command command, String label, String permission, PermissionDefault permissionDefault, String permissionMessage) {
            this.command = command;
            this.label = label;
            this.permission = permission;
            this.permissionDefault = permissionDefault;
            this.permissionMessage = permissionMessage;
        }

        public void restore() {
            // (Don't skip resetting, as there could be fall-back aliases.)
            //			Command registered = CommandUtil.getCommand(label);
            //			if (registered == null || registered != command) return;
            if (!label.equalsIgnoreCase(command.getLabel().trim().toLowerCase())) {
                command.setLabel(label);
            }
            command.setPermission(permission);
            if (permission != null && permissionDefault != null) {
                Permission perm = Bukkit.getPluginManager().getPermission(permission);
                if (perm != null && perm.getDefault() != permissionDefault) {
                    perm.setDefault(permissionDefault);
                }
            }
            command.setPermissionMessage(permissionMessage);
        }
    }

    /**
     * 
     * @param commands Command white-list.
     * @param permissionBase
     * @param ops
     * @return
     */
    public static List<CommandProtectionEntry> protectCommands(Collection<String> commands, String permissionBase, boolean ops) {
        return protectCommands(permissionBase, commands, true, ops);
    }

    /**
     * Set up the command protection with the "unknown command" message for the permission message (attempt to hide the command).
     * @param permissionBase
     * @param ignoredCommands
     * @param invertIgnored
     * @param ops
     * @return
     */
    public static List<CommandProtectionEntry> protectCommands(String permissionBase, Collection<String> ignoredCommands, boolean invertIgnored, boolean ops) {
        return protectCommands(permissionBase, ignoredCommands, invertIgnored, ops, ColorUtil.replaceColors(ConfigManager.getConfigFile().getString(ConfPaths.PROTECT_PLUGINS_HIDE_NOCOMMAND_MSG)));
    }

    /**
     * Set up the command protection with the given permission message.
     * @param permissionBase
     * @param ignoredCommands
     * @param invertIgnored
     * @param ops
     * @param permissionMessage
     * @return
     */
    public static List<CommandProtectionEntry> protectCommands(final String permissionBase, final Collection<String> ignoredCommands, final boolean invertIgnored, final boolean ops, final String permissionMessage) {
        final Set<String> checked = new HashSet<String>();
        for (String label : ignoredCommands) {
            checked.add(CommandUtil.getCommandLabel(label, false));
        }
        final PluginManager pm = Bukkit.getPluginManager();
        Permission rootPerm = pm.getPermission(permissionBase);
        if (rootPerm == null) {
            rootPerm = new Permission(permissionBase);
            pm.addPermission(rootPerm);
        }
        final List<CommandProtectionEntry> changed = new LinkedList<CommandProtectionEntry>();
        // Apply protection based on white-list or black-list.
        for (final Command command : CommandUtil.getCommands()) {
            final String lcLabel = command.getLabel().trim().toLowerCase();
            if (checked.contains(lcLabel) || containsAnyAliases(checked, command)) {
                if (!invertIgnored) {
                    continue;
                }
            }
            else if (invertIgnored) {
                continue;
            }
            // Set the permission for the command.
            String cmdPermName = command.getPermission();
            final boolean cmdHadPerm;
            if (cmdPermName == null) {
                // Set a permission.
                cmdPermName = permissionBase + "." + lcLabel;
                command.setPermission(cmdPermName);
                cmdHadPerm = false;
            }
            else{
                cmdHadPerm = true;
            }
            // Set permission default behavior.
            Permission cmdPerm = pm.getPermission(cmdPermName);
            final boolean permRegistered = cmdPerm != null;
            if (!permRegistered) {
                cmdPerm = new Permission(cmdPermName);
                if (!cmdHadPerm) {
                    // NCP added the permission, allow root.
                    cmdPerm.addParent(rootPerm, true);
                } // else: permission was present, but not registered.
                pm.addPermission(cmdPerm);
            }
            // Create change history entry.
            if (cmdHadPerm && permRegistered) {
                changed.add(new CommandProtectionEntry(command, lcLabel, cmdPermName, cmdPerm.getDefault(), command.getPermissionMessage()));
            }
            else {
                // (New Permission instances will not be touched on restore.)
                changed.add(new CommandProtectionEntry(command, lcLabel, null, null, command.getPermissionMessage()));
            }
            // Change 
            cmdPerm.setDefault(ops ? PermissionDefault.OP : PermissionDefault.FALSE);
            command.setPermissionMessage(permissionMessage);
        }
        return changed;
    }

    /**
     * Check if the checked set contains any trim+lower-case alias of the command.
     * @param checked
     * @param command
     * @return
     */
    private static final boolean containsAnyAliases(final Set<String> checked, final Command command) {
        final Collection<String> aliases = command.getAliases();
        if (aliases != null) {
            for (final String alias : aliases) {
                if (checked.contains(alias.trim().toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Set a permission as child for all the other permissions given in a
     * Collection.
     * 
     * @param permissions
     *            Not expected to exist.
     * @param childPermissionName
     * @param permissionDefault
     *            Default for all generated permissions (child and parent
     *            alike).
     */
    public static void addChildPermission(final Collection<RegisteredPermission> registeredPermissions, 
            final RegisteredPermission registeredChild, final PermissionDefault permissionDefault) {
        final String childPermissionName = registeredChild.getStringRepresentation();
        final PluginManager pm = Bukkit.getPluginManager();
        Permission childPermission = pm.getPermission(childPermissionName);
        if (childPermission == null) {
            childPermission = new Permission(childPermissionName, AUTO_GENERATED, permissionDefault);
            pm.addPermission(childPermission);
        }
        for (final RegisteredPermission registeredPermission : registeredPermissions) {
            final String permissionName = registeredPermission.getStringRepresentation();
            Permission permission = pm.getPermission(permissionName);
            if (permission == null) {
                permission = new Permission(permissionName, AUTO_GENERATED, permissionDefault);
                pm.addPermission(permission);
            }
            if (!permission.getChildren().containsKey(childPermissionName)) {
                childPermission.addParent(permission, true);
            }
        }
    }

    /**
     * 
     * @param permissionName
     *            A permission already registered with Bukkit.
     * @param childPermissionSuffix
     *            The suffix to add, the resulting permission will b registered
     *            with the permissionRegistry and Bukkit, if not yet done. No
     *            leading dot, results in: permissionName.childPermissionSuffix
     * @param permissionDefault
     *            Default for child permissions.
     * @throws NullPointerException
     *             If no permission is registered for permissionName.
     */
    public static void addChildPermissionBySuffix(final RegisteredPermission registeredPermission, 
            final String childPermissionSuffix, final PermissionRegistry permissionRegistry,
            final PermissionDefault permissionDefault, final boolean childValue) {
        final String permissionName = registeredPermission.getStringRepresentation();
        final String childPermissionName = permissionName + "." + childPermissionSuffix;
        permissionRegistry.getOrRegisterPermission(childPermissionName); // Ensure it s registered like this.
        final PluginManager pm = Bukkit.getPluginManager();
        final Permission permission = pm.getPermission(permissionName);
        if (permission == null) {
            throw new NullPointerException("Permission is not registered: " + permissionName);
        }
        Permission childPermission = pm.getPermission(childPermissionName);
        if (childPermission == null) {
            childPermission = new Permission(childPermissionName, AUTO_GENERATED, permissionDefault);
            pm.addPermission(childPermission);
        }
        if (!permission.getChildren().containsKey(childPermissionName)) {
            childPermission.addParent(permission, childValue);
        }
    }

}
