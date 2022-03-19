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
package fr.neatmonster.nocheatplus.checks;

import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.components.debug.IDebugPlayer;
import fr.neatmonster.nocheatplus.components.registry.event.IGenericInstanceHandle;
import fr.neatmonster.nocheatplus.hooks.NCPHookManager;
import fr.neatmonster.nocheatplus.penalties.IPenaltyList;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.players.ExecutionHistory;
import fr.neatmonster.nocheatplus.players.IPlayerData;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;
import fr.neatmonster.nocheatplus.worlds.IWorldData;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

/**
 * The parent class of all checks. Don't let this implement Listener without
 * knowing that this might be registered as component with NCP before the
 * check-listeners.
 * <hr>
 * Note on enabling and bypassing:<br>
 * <li>The full Check.isEnabled test will check the config flag
 * (config.isEnabled) and test Check.hasBypass (permissions, exemption).</li>
 * <li>Currently the code for isEnabled and hasBypasss is in
 * fr.neatmonster.nocheatplus.utilities.CheckUtils .</li>
 * <li>Currently the hasBypass check will test for a permission if in the main
 * thread, or test for a cached permission if off the main thread, then check
 * for exemption with fr.neatmonster.nocheatplus.hooks.NCPExemptionManager, for
 * the case that no bypass permission is present.</li>
 * <li>For checks run off the primary thread, permissions are cached. Updates
 * must be requested with the TickTask explicitly, e.g. in the listener. This
 * depends on the definition of which checks might run asynchronously, as given
 * in fr.neatmonster.nocheatplus.hooks.APIUtils and the default permissions
 * defined in ICheckConfig implementations.</li>
 * <li>At present exemption checking is only thread-safe for the checks that are
 * set to run off main thread (APIUtils).</li>
 * <br>
 * Note on performance:
 * <li>You might check the configuration flag directly with a given
 * configuration, which is equivalent to but supposedly faster than calling
 * CheckType.isEnabled(player) or CheckConfig.isEnabled(player). Then call
 * hasBypass extra to that.</li>
 * <li>For very simple checks, you might skip checking hasBypass for the normal
 * case, and check it lazily only in case of a violation.</li>
 * <li>The method signatures that take ICheckData and ICheckConfig as extra
 * arguments will perform better, they also allow to pass null for config and
 * data (also see fr.neatmonster.nocheatplus.utilities.CheckUtils).</li>
 * <li>In case simplicity of code is demanded, just check isEnabled(player) or
 * for better performance isEnabled(player, data, config), before running the
 * actual check.</li>
 * 
 */
// TODO: javadocs redo (above)
public abstract class Check implements IDebugPlayer {

    // TODO: Do these get cleaned up ?
    /** The execution histories of each check. */
    protected static Map<String, ExecutionHistory> histories = new HashMap<String, ExecutionHistory>();

    /**
     * Gets the player's history.
     * 
     * @param player
     *            the player
     * @return the history
     */
    protected static ExecutionHistory getHistory(final ServerPlayer player) {
        String name = player.getGameProfile().getName();
        if (!histories.containsKey(name)) {
            histories.put(name, new ExecutionHistory());
        }
        return histories.get(name);
    }

    /** The type. */
    protected final CheckType type;

    protected final IGenericInstanceHandle<MCAccess> mcAccess;

    /**
     * Instantiates a new check.
     * 
     * @param type
     *            the type
     */
    @SuppressWarnings("deprecation")
    public Check(final CheckType type) {
        this.type = type;
        mcAccess = NCPAPIProvider.getNoCheatPlusAPI().getGenericInstanceHandle(MCAccess.class);
        ViolationHistory.checkTypeMap.put(getClass().getName(), type);
        DataManager.registerExecutionHistory(type, histories);
    }

    /**
     * Execute actions, thread-safe.<br>
     * 
     * @param player
     *            The player who failed a check.
     * @param vL
     *            The total check-specific violation level.
     * @param addedVL
     *            The violation level, that has been added with this violation.
     * @param actions
     *            The ActionList to use for this violation.
     * @param penaltyList
     *            IPenaltyList instance to fill in penalties for specific input
     *            other than Player.
     * @return The ViolationData instance that is created for execution of
     *         actions.
     */
    public ViolationData executeActions(final ServerPlayer player, final double vL, final double addedVL, final ActionList actions, final IPenaltyList penaltyList) {
        return executeActions(new ViolationData(this, player, vL, addedVL, actions, penaltyList));
    }

    /**
     * Execute actions, thread-safe.<br>
     * 
     * @param player
     *            The player who failed a check.
     * @param vL
     *            The total check-specific violation level.
     * @param addedVL
     *            The violation level, that has been added with this violation.
     * @param actions
     *            The ActionList to use for this violation.
     * @return The ViolationData instance that is created for execution of
     *         actions.
     */
    public ViolationData executeActions(final ServerPlayer player, final double vL, final double addedVL, final ActionList actions) {
        return executeActions(new ViolationData(this, player, vL, addedVL, actions));
    }

    /**
     * Execute actions, thread safe.
     * 
     * @param violationData
     *            the violation data
     * @return The ViolationData instanced passed to this method.
     */
    public ViolationData executeActions(final ViolationData violationData) {

        // Dispatch the VL processing to the hook manager (now thread safe).
        if (NCPHookManager.shouldCancelVLProcessing(violationData)) {
            // One of the hooks has decided to cancel the VL processing, return false.
            violationData.preventCancel();
        } else {
            violationData.executeActions();
        }


        // (Permission checks are moved to cached permissions, lazily updated.)
        return violationData;
    }

    /**
     * Gets the type of the check.
     * 
     * @return the type
     */
    public CheckType getType() {
        return type;
    }

    /**
     * Full activation check (configuration, exemption, permission).
     * 
     * @param player
     * @param pData
     * @param worldData
     * @return
     */
    public boolean isEnabled(final ServerPlayer player, final IPlayerData pData,
            final IWorldData worldData) {
        return pData.isCheckActive(type, player, worldData);
    }

    /**
     * Full activation check (configuration, exemption, permission).
     * 
     * @param player
     * @param pData
     * @return
     */
    public boolean isEnabled(final ServerPlayer player, final IPlayerData pData) {
        return pData.isCheckActive(type, player);
    }

    /**
     * Full activation check (configuration, exemption, permission).
     * 
     * @param player
     *            the player
     * @return true, if the check is enabled
     */
    public boolean isEnabled(final ServerPlayer player) {
        return isEnabled(player, DataManager.getPlayerData(player));
    }

    @Override
    public void debug(final ServerPlayer player, final String message) {
        CheckUtils.debug(player, type, message);
    }

}
