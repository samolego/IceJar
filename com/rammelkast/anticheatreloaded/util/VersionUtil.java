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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;

public class VersionUtil {

	public static final MinecraftVersion CURRENT_VERSION;
	private static final List<String> SUPPORTED_VERSIONS;

	public static String getVersion() {
		return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
	}

	public static boolean isSupported() {
		for (String versionId : SUPPORTED_VERSIONS) {
			if (getVersion().startsWith(versionId)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isOfVersion(final String versionId) {
		if (getVersion().startsWith(versionId)) {
			return true;
		}
		return false;
	}

	public static boolean isFlying(final Player player) {
		return player.isFlying() || player.getGameMode() == GameMode.SPECTATOR || player.isGliding() || player.hasPotionEffect(PotionEffectType.LEVITATION)
				|| AntiCheatReloaded.getManager().getBackend().justLevitated(player);
	}

	public static boolean isSlowFalling(final Player player) {
		if (!CURRENT_VERSION.isAtLeast(MinecraftVersion.AQUATIC_UPDATE)) {
			return false;
		}
		return player.hasPotionEffect(PotionEffectType.SLOW_FALLING);
	}

	public static boolean isFrostWalk(final Player player) {
		if (player.getInventory().getBoots() == null) {
			return false;
		}
		return player.getInventory().getBoots().containsEnchantment(Enchantment.FROST_WALKER);
	}
	
	public static boolean isSoulSpeed(final ItemStack boots) {
		if (!CURRENT_VERSION.isAtLeast(MinecraftVersion.NETHER_UPDATE)) {
			return false;
		}
		return boots.containsEnchantment(Enchantment.SOUL_SPEED);
	}

	public static ItemStack getItemInHand(final Player player) {
		return player.getInventory().getItemInMainHand();
	}

	public static int getPlayerPing(final Player player) {
		// May be called with offline (null) player
		if (player == null) {
			return -1;
		}
		
		if (!CURRENT_VERSION.isAtLeast(MinecraftVersion.CAVES_CLIFFS_1)) {
			try {
				final Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
				final int ping = (int) entityPlayer.getClass().getField("ping").get(entityPlayer);
				return ping;
			} catch (Exception e) {
				return -1;
			}
		} else {
			return player.getPing();
		}
	}

	public static Block getTargetBlock(final Player player, final int distance) {
		if (!CURRENT_VERSION.isAtLeast(MinecraftVersion.AQUATIC_UPDATE)) {
			return player.getTargetBlock((Set<Material>) null, distance);
		}
		return player.getTargetBlockExact(distance);
	}

	public static boolean isGliding(final Player player) {
		return player.isGliding();
	}

	public static boolean isLevitationEffect(final PotionEffect effect) {
		return effect.getType().equals(PotionEffectType.LEVITATION);
	}

	public static int getPotionLevel(final Player player, final PotionEffectType type) {
		if (player.hasPotionEffect(type)) {
			return player.getPotionEffect(type).getAmplifier() + 1;
		}
		return 0;
	}

	public static boolean isSwimming(final Player player) {
		if (!CURRENT_VERSION.isAtLeast(MinecraftVersion.AQUATIC_UPDATE)) {
			return false;
		}
		return player.isSwimming();
	}

	static {
		SUPPORTED_VERSIONS = Arrays.asList(new String[] { "v1_18", "v1_17", "v1_16", "v1_15", "v1_14", "v1_13", "v1_12" });
		CURRENT_VERSION = MinecraftVersion.getCurrentVersion();
	}
}
