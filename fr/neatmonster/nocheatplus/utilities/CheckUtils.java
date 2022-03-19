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
package fr.neatmonster.nocheatplus.utilities;

import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;

import net.minecraft.server.level.ServerPlayer;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.blockbreak.BlockBreakData;
import fr.neatmonster.nocheatplus.checks.combined.CombinedData;
import fr.neatmonster.nocheatplus.checks.fight.FightData;
import fr.neatmonster.nocheatplus.checks.inventory.InventoryData;
import fr.neatmonster.nocheatplus.components.concurrent.IPrimaryThreadContextTester;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.players.IPlayerData;


/**
 * Random auxiliary gear, some might have general quality. Contents are likely
 * to get moved to other classes. All that is in here should be set up with
 * checks and not be related to early setup stages of the plugin.
 */
public class CheckUtils {

    public static final IPrimaryThreadContextTester primaryServerThreadContextTester = () -> true;

    /**
     * Improper API access: Log once a message with the checkType and the
     * current stack trace. The stack trace is only logged once on repeated access.
     *
     * @param checkType
     *            the check type
     */
    public static void improperAsynchronousAPIAccess(final CheckType checkType) {
        // TODO: Log once + examine stack (which plugins/things are involved).
        final String trace = Arrays.toString(Thread.currentThread().getStackTrace());
        StaticLog.logOnce(Streams.STATUS, Level.SEVERE, "Off primary thread processing for " + checkType, trace);
    }

    /**
     * Kick and log.
     *
     * @param player
     *            the player
     * @param cc
     *            the cc
     */
    public static void kickIllegalMove(final ServerPlayer player, final MovingConfig cc){
        player.disconnect(cc.msgKickIllegalMove);
        StaticLog.logWarning("[NCP] Disconnect " + player.getName() + " due to illegal move!");
    }

    /**
     * Guess some last-action time, likely to be replaced with centralized
     * PlayerData use.
     *
     * @param player
     *            the player
     * @param now
     *            the now
     * @param maxAge
     *            Maximum age in milliseconds.
     * @return Return timestamp or Long.MIN_VALUE if not possible or beyond
     *         maxAge.
     */
    public static final long guessKeepAliveTime(final ServerPlayer player,
            final long now, final long maxAge, final IPlayerData pData){
        final int tick = TickTask.getTick();
        long ref = Long.MIN_VALUE;
        // Estimate last fight action time (important for gode modes).
        final FightData fData = pData.getGenericInstance(FightData.class); 
        ref = Math.max(ref, fData.speedBuckets.lastUpdate());
        ref = Math.max(ref, now - 50L * (tick - fData.lastAttackTick)); // Ignore lag.
        // Health regain (not unimportant).
        ref = Math.max(ref, fData.regainHealthTime);
        // Move time.
        ref = Math.max(ref, pData.getGenericInstance(CombinedData.class).lastMoveTime);
        // Inventory.
        final InventoryData iData = pData.getGenericInstance(InventoryData.class);
        ref = Math.max(ref, iData.lastClickTime);
        ref = Math.max(ref, iData.instantEatInteract);
        // BlcokBreak/interact.
        final BlockBreakData bbData = pData.getGenericInstance(BlockBreakData.class);
        ref = Math.max(ref, bbData.frequencyBuckets.lastUpdate());
        ref = Math.max(ref, bbData.fastBreakfirstDamage);
        // TODO: More, less ...
        if (ref > now || ref < now - maxAge){
            return Long.MIN_VALUE;
        }
        return ref;
    }

    /**
     * Static relay for the check-specific convenience methods, logging with
     * standard format ([check_type] [player_name] ...).
     *
     * @param player
     *            May be null.
     * @param checkType
     *            the check type
     * @param message
     *            the message
     */
    public static void debug(final ServerPlayer player, final CheckType checkType, final String message) {
        NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, getLogMessagePrefix(player, checkType) + message);
    }

    /**
     * Get the standard log message prefix with a trailing space.
     *
     * @param player
     *            May be null.
     * @param checkType
     *            the check type
     * @return the log message prefix
     */
    public static String getLogMessagePrefix(final ServerPlayer player, final CheckType checkType) {
        return getLogMessagePrefix(player == null ? null : player.getName(), checkType);
    }

    /**
     * Get the standard log message prefix with a trailing space.
     *
     * @param playerName
     *            May be null.
     * @param checkType
     *            the check type
     * @return the log message prefix
     */
    public static String getLogMessagePrefix(final String playerName, final CheckType checkType) {
        String base = "[" + checkType + "] ";
        if (playerName != null) {
            base += "[" + playerName + "] ";
        }
        return base;
    }

    /**
     * Convenience method to get a Random instance from the generic registry
     * (NoCheatPlusAPI).
     *
     * @return the random
     */
    // TODO: Move official stuff to some static direct access API.
    public static Random getRandom() {
        return NCPAPIProvider.getNoCheatPlusAPI().getGenericInstance(Random.class);
    }

}
