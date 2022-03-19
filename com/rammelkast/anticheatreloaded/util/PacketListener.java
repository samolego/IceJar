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

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.server.TemporaryPlayer;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerDigType;
import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.movement.NoSlowCheck;
import com.rammelkast.anticheatreloaded.check.packet.BadPacketsCheck;
import com.rammelkast.anticheatreloaded.check.packet.MorePacketsCheck;

public final class PacketListener {

	public static void load(final ProtocolManager manager) {
		// Used for MorePackets and BadPackets
		manager.addPacketListener(new PacketAdapter(AntiCheatReloaded.getPlugin(), ListenerPriority.LOWEST,
				new PacketType[] { PacketType.Play.Client.POSITION, PacketType.Play.Client.POSITION_LOOK,
						PacketType.Play.Server.POSITION }) {
			@Override
			public void onPacketReceiving(final PacketEvent event) {
				final Player player = event.getPlayer();
				if (player == null || !player.isOnline()) {
					return;
				}

				// Run MorePackets check
				MorePacketsCheck.runCheck(player, event);

				if (!event.isCancelled()) {
					// Run BadPackets check
					BadPacketsCheck.runCheck(player, event);
				}
			}

			@Override
			public void onPacketSending(final PacketEvent event) {
				final Player player = event.getPlayer();
				// Check if we have an actual player object
				if (player instanceof TemporaryPlayer) {
					return;
				}
				// Compensate for teleport
				MorePacketsCheck.compensate(player);
			}
		});
		
		// Used for accurate ping
		manager.addPacketListener(new PacketAdapter(AntiCheatReloaded.getPlugin(), ListenerPriority.LOWEST,
				new PacketType[] { PacketType.Play.Client.KEEP_ALIVE, PacketType.Play.Server.KEEP_ALIVE }) {
			@Override
			public void onPacketSending(final PacketEvent event) {
				final User user = AntiCheatReloaded.getManager().getUserManager()
						.getUser(event.getPlayer().getUniqueId());
				user.onServerPing();
			}

			@Override
			public void onPacketReceiving(final PacketEvent event) {
				final User user = AntiCheatReloaded.getManager().getUserManager()
						.getUser(event.getPlayer().getUniqueId());
				user.onClientPong();
			}
		});
		
		// Used for NoSlow
		manager.addPacketListener(new PacketAdapter(AntiCheatReloaded.getPlugin(),
				ListenerPriority.LOWEST, new PacketType[] { PacketType.Play.Client.BLOCK_DIG }) {
			@Override
			public void onPacketReceiving(final PacketEvent event) {
				if (event.getPacket().getPlayerDigTypes().read(0) == PlayerDigType.RELEASE_USE_ITEM) {
					NoSlowCheck.runCheck(event.getPlayer(), event);
				}
			}
		});
	}

}
