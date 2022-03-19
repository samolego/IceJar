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
package fr.neatmonster.nocheatplus.checks.blockbreak;

import org.bukkit.GameMode;
import net.minecraft.server.level.ServerPlayer;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.players.IPlayerData;
import fr.neatmonster.nocheatplus.utilities.TickTask;

/**
 * This checks just limits the number of blocks broken per time frame.
 * @author mc_dev
 *
 */
public class Frequency extends Check {

    public Frequency() {
        super(CheckType.BLOCKBREAK_FREQUENCY);
    }

    public boolean check(final ServerPlayer player, final int tick,
            final BlockBreakConfig cc, final BlockBreakData data,
            final IPlayerData pData){

        final float interval = (float) ((player.getGameMode() == GameMode.CREATIVE)?(cc.frequencyIntervalCreative):(cc.frequencyIntervalSurvival));
        data.frequencyBuckets.add(System.currentTimeMillis(), interval);

        // Full period frequency.
        final float fullScore = data.frequencyBuckets.score(cc.frequencyBucketFactor);
        final long fullTime = cc.frequencyBucketDur * cc.frequencyBuckets;
        final boolean lag = pData.getCurrentWorldData().shouldAdjustToLag(type);

        // Short term arrivals.
        if (tick < data.frequencyShortTermTick){
            // Tick task got reset.
            data.frequencyShortTermTick = tick;
            data.frequencyShortTermCount = 1;
        }
        else if (tick - data.frequencyShortTermTick < cc.frequencyShortTermTicks){
            // Account for server side lag.
            final float stLag = lag ? TickTask.getLag(50L * (tick - data.frequencyShortTermTick), true) : 1f;
            if (stLag < 1.5){
                // Within range, add.
                data.frequencyShortTermCount ++;
            }
            else{
                // Too much lag, reset.
                data.frequencyShortTermTick = tick;
                data.frequencyShortTermCount = 1;
            }
        }
        else{
            data.frequencyShortTermTick = tick;
            data.frequencyShortTermCount = 1;
        }

        // Account for server side lag.
        final float fullLag = lag ? TickTask.getLag(fullTime, true) : 1f;

        // Find if one of both or both are violations:
        final float fullViolation = (fullScore > fullTime * fullLag) ? (fullScore - fullTime * fullLag) : 0;
        final float shortTermWeight = 50f * cc.frequencyShortTermTicks / (float) cc.frequencyShortTermLimit; 
        final float shortTermViolation = (data.frequencyShortTermCount > cc.frequencyShortTermLimit) 
                ? (data.frequencyShortTermCount - cc.frequencyShortTermLimit) * shortTermWeight : 0; 
                // TODO: AUTO INDENT?
                final float violation = Math.max(fullViolation, shortTermViolation);

                boolean cancel = false;
                if (violation > 0f){

                    // TODO: account for lag spikes !

                    final double change = violation / 1000;
                    data.frequencyVL += change;
                    cancel = executeActions(player, data.frequencyVL, change, cc.frequencyActions).willCancel();
                }
                else if (data.frequencyVL > 0d && fullScore < fullTime * .75)
                    data.frequencyVL *= 0.95;

                return cancel;
    }

}
