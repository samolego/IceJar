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

package com.rammelkast.anticheatreloaded.util;

import org.bukkit.Location;

public class Distance {
	private final double l1Y;
	private final double l2Y;

	private final double XDiff;
	private final double YDiff;
	private final double ZDiff;
	
	private final Location from;
	private final Location to;

	public Distance(Location from, Location to) {
		l1Y = to.getY();
		l2Y = from.getY();

		XDiff = Math.abs(to.getX() - from.getX());
		ZDiff = Math.abs(to.getZ() - from.getZ());
		YDiff = Math.abs(l1Y - l2Y);
		
		this.from = from;
		this.to = to;
	}
	
	public Distance() {
		l1Y = 0;
		l2Y = 0;

		XDiff = 0;
		ZDiff = 0;
		YDiff = 0;
		
		this.from = null;
		this.to = null;
	}

	public double fromY() {
		return l2Y;
	}

	public double toY() {
		return l1Y;
	}

	public double getXDifference() {
		return XDiff;
	}

	public double getZDifference() {
		return ZDiff;
	}

	public double getYDifference() {
		return YDiff;
	}
	
	public Location getFrom() {
		return this.from;
	}
	
	public Location getTo() {
		return this.to;
	}
}
