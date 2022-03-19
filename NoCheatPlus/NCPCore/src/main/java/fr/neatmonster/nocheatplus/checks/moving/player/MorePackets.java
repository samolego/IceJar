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
package fr.neatmonster.nocheatplus.checks.moving.player;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import net.minecraft.server.level.ServerPlayer;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.net.NetStatic;
import fr.neatmonster.nocheatplus.players.IPlayerData;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.location.PlayerLocation;

/**
 * The MorePackets check will try to identify players that send more than the usual
 * amount of move-packets to the server to be able to move faster than normal, without getting caught by the other
 * checks (flying/running).
 */
public class MorePackets extends Check {

    private final List<String> tags = new ArrayList<String>();

    /**
     * Instantiates a new more packets check.
     */
    public MorePackets() {
        super(CheckType.MOVING_MOREPACKETS);
    }

    /**
     * Check for speeding by sending too many packets. We assume 22 packets per
     * second to be legitimate, while 20 would be ideal. See
     * PlayerData.morePacketsFreq for the monitored amount of time and the
     * resolution. See NetStatic for the actual check code.
     * 
     * @param player
     * @param from
     * @param to
     * @param allowSetSetBack
     *            If to allow setting the set back location.
     * @param data
     * @param cc
     * @return
     */
    public Location check(final ServerPlayer player, final ServerPlayerLocation from, final ServerPlayerLocation to,
            final boolean allowSetSetBack, final MovingData data, final MovingConfig cc,
            final IPlayerData pData) {
        // Take time once, first:
        final long time = System.currentTimeMillis();
        final boolean debug = pData.isDebugActive(type);

        //    	if (from.isSamePos(to)) {
        //    		// Ignore moves with "just look" for now.
        //    		// TODO: Extra ActionFrequency for "just look" + use to burn, maybe also check individually.
        //    		return null;
        //    	}

        // Ensure we have a set back location.
        if (allowSetSetBack && !data.hasMorePacketsSetBack()){
            // TODO: Check if other set back is appropriate or if to set/reset on other events.
            if (data.hasSetBack()) {
                data.setMorePacketsSetBackFromSurvivalfly();
            }
            else {
                data.setMorePacketsSetBack(from);
            }
        }

        // Check for a violation of the set limits.
        tags.clear();
        final double violation = NetStatic.morePacketsCheck(data.morePacketsFreq, time, 1f, cc.morePacketsEPSMax, cc.morePacketsEPSIdeal, data.morePacketsBurstFreq, cc.morePacketsBurstPackets, cc.morePacketsBurstDirect, cc.morePacketsBurstEPM, tags);

        // Process violation result.
        if (violation > 0.0) {
            // Increment violation level.
            data.morePacketsVL = violation; // TODO: Accumulate somehow [e.g. always += 1, decrease with continuous moving without violation]?

            // Violation handling.
            final ViolationData vd = new ViolationData(this, player, data.morePacketsVL, violation, cc.morePacketsActions);
            if (debug || vd.needsParameters()) {
                vd.setParameter(ParameterName.PACKETS, Integer.toString(new Double(violation).intValue()));
                vd.setParameter(ParameterName.TAGS, StringUtil.join(tags, "+"));
            }
            if (executeActions(vd).willCancel()) {
                // Set to cancel the move.
                /*
                 * TODO: Harmonize with MovingUtil.getApplicableSetBackLocation
                 * (somehow include the desired set back type / loc / context).
                 */
                return data.hasMorePacketsSetBack() ? data.getMorePacketsSetBack() : data.getSetBack(to); // TODO
            }
        } 
        else if (allowSetSetBack && data.getMorePacketsSetBackAge() > cc.morePacketsSetBackAge) {
            // Update the set back location. (CHANGED to only update, if not a violation.)
            // (Might update whenever newTo == null)
            data.setMorePacketsSetBack(from);
            if (debug) {
                debug(player, "Update set back (morepackets) to from.");
            }
        }

        // No set back.
        return null;

    }

}
