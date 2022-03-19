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
package fr.neatmonster.nocheatplus.checks.blockplace;

import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.generic.block.AbstractBlockDirectionCheck;

/**
 * The Direction check will find out if a player tried to interact with
 * something that's not in their field of view.
 */
public class Direction extends AbstractBlockDirectionCheck<BlockPlaceData, BlockPlaceConfig> {

    /**
     * Instantiates a new direction check.
     */
    public Direction() {
        super(CheckType.BLOCKPLACE_DIRECTION);
    }

    @Override
    protected double addVL(Player player, double distance, BlockPlaceData data, BlockPlaceConfig cc) {
        data.directionVL += distance;
        return data.directionVL;
    }

    @Override
    protected ActionList getActions(BlockPlaceConfig cc) {
        return cc.directionActions;
    }

    @Override
    protected void cooldown(Player player, BlockPlaceData data, BlockPlaceConfig cc) {
        data.directionVL *= 0.9D;
    }

}
