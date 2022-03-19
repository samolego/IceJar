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
package fr.neatmonster.nocheatplus.checks.net;

import net.minecraft.server.level.ServerPlayer;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;

public class WrongTurn extends Check {
	
	public WrongTurn() {
		super(CheckType.NET_WRONGTURN);
	}
	
	public boolean check(final ServerPlayer player, final float pitch, final NetData data, final NetConfig cc) {
		boolean cancel = false;
		
		if (Math.abs(pitch) > 90.0 || pitch < -90.0) {
			data.wrongTurnVL++;
			
			if (executeActions(player, data.wrongTurnVL, 1, cc.wrongTurnActions).willCancel()) {
				cancel = true;
			}
			
		}
		
		return cancel;
	}

}