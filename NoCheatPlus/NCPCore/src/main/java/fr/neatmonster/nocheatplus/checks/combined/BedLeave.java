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
package fr.neatmonster.nocheatplus.checks.combined;

import net.minecraft.server.level.ServerPlayer;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.players.IPlayerData;

public class BedLeave extends Check {

    public BedLeave() {
        super(CheckType.COMBINED_BEDLEAVE);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @return If to prevent action (use the set back location of survivalfly).
     */
    public boolean checkBed(final ServerPlayer player, final IPlayerData pData) {
        final CombinedData data = pData.getGenericInstance(CombinedData.class);

        boolean cancel = false;
        // Check if the player had been in bed at all.
        if (!data.wasInBed) {
            // Violation ...
            data.bedLeaveVL += 1D;

            // TODO: add tag

            // And return if we need to do something or not.
            if (executeActions(player, data.bedLeaveVL, 1D,
                    pData.getGenericInstance(CombinedConfig.class).bedLeaveActions).willCancel()){
                cancel = true;
            }
        } else{
            // He has, everything is allright.
            data.wasInBed = false;
            // TODO: think about decreasing the vl ?
        }
        return cancel;
    }

}
