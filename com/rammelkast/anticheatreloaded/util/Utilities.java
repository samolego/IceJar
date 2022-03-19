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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.NumberConversions;

import com.cryptomorin.xseries.XMaterial;

public final class Utilities {
	private static final List<Material> INSTANT_BREAK = new ArrayList<Material>();
	private static final List<Material> FOOD = new ArrayList<Material>();
	private static final List<Material> CLIMBABLE = new ArrayList<Material>();
	private static final Map<Material, Material> COMBO = new HashMap<Material, Material>();

	public static final double JUMP_MOTION_Y = 0.41999998688697815;

	/**
	 * Check if only the block beneath them is standable (includes water + lava)
	 * 
	 * @param block the block to check (under)
	 * @return true if they cannot stand there
	 */
	public static boolean cantStandAtSingle(final Block block) {
		final Block otherBlock = block.getLocation().add(0, -0.5, 0).getBlock();
		final boolean center = otherBlock.getType() == Material.AIR;
		return center;
	}

	/**
	 * Determine whether a player cannot stand on or around the given block
	 *
	 * @param block the block to check
	 * @return true if the player should be unable to stand here
	 */
	public static boolean cantStandAt(final Block block) {
		return !canStand(block) && cantStandClose(block) && cantStandFar(block);
	}

	/**
	 * Determine whether a player should be unable to stand at a given location
	 *
	 * @param location the location to check
	 * @return true if the player should be unable to stand here
	 */
	public static boolean cantStandAtExp(final Location location) {
		return cantStandAt(new Location(location.getWorld(), fixXAxis(location.getX()), location.getY() - 0.01D,
				location.getBlockZ()).getBlock());
	}

	/**
	 * Determine whether cannot stand on the block's immediately surroundings
	 * (North, East, South, West)
	 *
	 * @param block the block to check
	 * @return true if a player cannot stand in the immediate vicinity
	 */
	public static boolean cantStandClose(final Block block) {
		return !canStand(block.getRelative(BlockFace.NORTH)) && !canStand(block.getRelative(BlockFace.EAST))
				&& !canStand(block.getRelative(BlockFace.SOUTH)) && !canStand(block.getRelative(BlockFace.WEST));
	}

	/**
	 * Determine whether cannot stand on the block's outer surroundings
	 *
	 * @param block the block to check
	 * @return true if a player cannot stand in areas further away from the block
	 */
	public static boolean cantStandFar(final Block block) {
		return !canStand(block.getRelative(BlockFace.NORTH_WEST)) && !canStand(block.getRelative(BlockFace.NORTH_EAST))
				&& !canStand(block.getRelative(BlockFace.SOUTH_WEST))
				&& !canStand(block.getRelative(BlockFace.SOUTH_EAST));
	}

	/**
	 * Determine whether a player can stand on the given block
	 *
	 * @param block the block to check
	 * @return true if the player can stand here
	 */
	public static boolean canStand(final Block block) {
		return !(block.isLiquid() || block.getType() == Material.AIR);
	}

	public static boolean isNotNearSlime(final Block block) {
		return !isSlime(block.getRelative(BlockFace.NORTH)) && !isSlime(block.getRelative(BlockFace.EAST))
				&& !isSlime(block.getRelative(BlockFace.SOUTH)) && !isSlime(block.getRelative(BlockFace.WEST))
				&& !isSlime(block.getRelative(BlockFace.DOWN));
	}

	/**
	 * Determine whether a block is a type of slime
	 *
	 * @param block block to check
	 * @return true if block is a type of slime
	 */
	public static boolean isSlime(final Block block) {
		final Material type = block.getType();
		return type.equals(XMaterial.SLIME_BLOCK.parseMaterial());
	}
	
	public static boolean couldBeOnBoat(final Player player) {
		return couldBeOnBoat(player, 0.35, false);
	}

	public static boolean couldBeOnBoat(final Player player, final double range, final boolean checkY) {
		for (final Entity entity : player.getNearbyEntities(range, range, range)) {
			if (entity instanceof Boat) {
				if (((Boat) entity).getLocation().getY() < player.getLocation().getY() + 0.35) {
					return true;
				} else if (!checkY) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Determine whether a player could be standing on ice
	 *
	 * @param location the location to check
	 * @return true if the player could be standing on ice
	 */
	public static boolean couldBeOnIce(final Location location) {
		return isNearIce(new Location(location.getWorld(), fixXAxis(location.getX()), location.getY() - 0.01D,
				location.getBlockZ()))
				|| isNearIce(new Location(location.getWorld(), fixXAxis(location.getX()), location.getY() - 0.26D,
						location.getBlockZ()));
	}

	public static boolean isNearIce(final Location location) {
		return isCollisionPoint(location, material -> material.name().endsWith("ICE"));
	}

	public static boolean isNearShulkerBox(final Location location) {
		// Shulkers are 1.12+
		if (MinecraftVersion.getCurrentVersion().isAtLeast(MinecraftVersion.COLOR_UPDATE)) {
			return false;
		}
		return isCollisionPoint(location, material -> material.name().endsWith("SHULKER_BOX"));
	}

	/**
	 * Determine whether a player could be standing on a halfblock
	 *
	 * @param location the location to check
	 * @return true if the player could be standing on a halfblock
	 */
	public static boolean couldBeOnHalfblock(final Location location) {
		return isNearHalfblock(
				new Location(location.getWorld(), location.getX(), location.getY() - 0.01D, location.getBlockZ()))
				|| isNearHalfblock(new Location(location.getWorld(), location.getX(), location.getY() - 0.51D,
						location.getBlockZ()));
	}

	public static boolean isNearHalfblock(final Location location) {
		return isHalfblock(location.getBlock()) || isHalfblock(location.getBlock().getRelative(BlockFace.NORTH))
				|| isHalfblock(location.getBlock().getRelative(BlockFace.SOUTH))
				|| isHalfblock(location.getBlock().getRelative(BlockFace.EAST))
				|| isHalfblock(location.getBlock().getRelative(BlockFace.WEST))
				|| isHalfblock(location.getBlock().getRelative(BlockFace.NORTH_EAST))
				|| isHalfblock(location.getBlock().getRelative(BlockFace.NORTH_WEST))
				|| isHalfblock(location.getBlock().getRelative(BlockFace.SOUTH_EAST))
				|| isHalfblock(location.getBlock().getRelative(BlockFace.SOUTH_WEST));
	}

	public static boolean isHalfblock(final Block block) {
		// getBoundingBox() is only available on 1.14+
		if (MinecraftVersion.getCurrentVersion().isAtLeast(MinecraftVersion.VILLAGE_UPDATE)) {
			final BoundingBox box = block.getBoundingBox();
			final double height = box.getMaxY() - box.getMinY();
			if (height > 0.42 && height <= 0.6 && block.getType().isSolid()) {
				return true;
			}
		}
		return isSlab(block) || isStair(block) || isWall(block) || block.getType() == XMaterial.SNOW.parseMaterial()
				|| block.getType().name().endsWith("HEAD");
	}

	/**
	 * Determine whether a player could be standing on slime
	 *
	 * @param location the location to check
	 * @return true if the player could be standing on slime
	 */
	public static boolean couldBeOnSlime(final Location location) {
		return isNearSlime(new Location(location.getWorld(), fixXAxis(location.getX()), location.getY() - 0.01D,
				location.getBlockZ()))
				|| isNearSlime(new Location(location.getWorld(), fixXAxis(location.getX()), location.getY() - 0.51D,
						location.getBlockZ()));
	}

	public static boolean isNearSlime(final Location location) {
		return isCollisionPoint(location, material -> material == XMaterial.SLIME_BLOCK.parseMaterial());
	}
	
	/**
	 * Determine whether a player could be standing on soil
	 *
	 * @param location the location to check
	 * @return true if the player could be standing on soil
	 */
	public static boolean couldBeOnSoil(final Location location) {
		return isNearSoil(new Location(location.getWorld(), fixXAxis(location.getX()), location.getY() - 0.01D,
				location.getBlockZ()))
				|| isNearSoil(new Location(location.getWorld(), fixXAxis(location.getX()), location.getY() - 0.51D,
						location.getBlockZ()));
	}

	public static boolean isNearSoil(final Location location) {
		return isCollisionPoint(location, material -> material == XMaterial.SOUL_SOIL.parseMaterial() || material == XMaterial.SOUL_SAND.parseMaterial());
	}

	/**
	 * Determine whether a player is fully submerged in water
	 *
	 * @param player the player's location
	 * @return true if the player is fully in the water
	 */
	public static boolean isFullyInWater(Location player) {
		double touchedX = fixXAxis(player.getX());

		// Yes, this doesn't make sense, but it's supposed to fix some false positives
		// in water walk.
		// Think of it as 2 negatives = a positive :)
		if (!(new Location(player.getWorld(), touchedX, player.getY(), player.getBlockZ()).getBlock()).isLiquid()
				&& !(new Location(player.getWorld(), touchedX, Math.round(player.getY()), player.getBlockZ())
						.getBlock()).isLiquid()) {
			return true;
		}

		return (new Location(player.getWorld(), touchedX, player.getY(), player.getBlockZ()).getBlock()).isLiquid()
				&& (new Location(player.getWorld(), touchedX, Math.round(player.getY()), player.getBlockZ()).getBlock())
						.isLiquid();
	}

	/**
	 * Fixes a player's X position to determine the block they are on, even if
	 * they're on the edge
	 *
	 * @param x player's x position
	 * @return fixed x position
	 */
	public static double fixXAxis(double x) {
		/* For Z axis, just use Math.round(xaxis); */
		double touchedX = x;
		double rem = touchedX - Math.round(touchedX) + 0.01D;
		if (rem < 0.30D) {
			touchedX = NumberConversions.floor(x) - 1;
		}
		return touchedX;
	}

	/**
	 * Determine if the player is hovering over water with the given limit
	 *
	 * @param player the player's location
	 * @param blocks max blocks to check
	 * @return true if the player is hovering over water
	 */
	public static boolean isHoveringOverWater(Location player, int blocks) {
		for (int i = player.getBlockY(); i > player.getBlockY() - blocks; i--) {
			Block newloc = (new Location(player.getWorld(), player.getBlockX(), i, player.getBlockZ())).getBlock();
			if (newloc.getType() != Material.AIR) {
				return newloc.isLiquid();
			}
		}

		return false;
	}

	/**
	 * Determine if the player is hovering over water with a hard limit of 25 blocks
	 *
	 * @param player the player's location
	 * @return true if the player is hovering over water
	 */
	public static boolean isHoveringOverWater(Location player) {
		return isHoveringOverWater(player, 25);
	}

	/**
	 * Determine whether a material will break instantly when hit
	 *
	 * @param m the material to check
	 * @return true if the material is instant break
	 */
	public static boolean isInstantBreak(Material m) {
		return INSTANT_BREAK.contains(m);
	}

	/**
	 * Determine whether a material is edible
	 *
	 * @param m the material to check
	 * @return true if the material is food
	 */
	public static boolean isFood(Material m) {
		return FOOD.contains(m);
	}

	/**
	 * Determine whether a block is a slab
	 *
	 * @param block block to check
	 * @return true if slab
	 */
	public static boolean isSlab(final Block block) {
		final Material type = block.getType();
		return type.name().endsWith("SLAB");
	}

	public static boolean isNearBed(final Location location) {
		return isCollisionPoint(location, material -> material.name().endsWith("BED"));
	}

	/**
	 * Determine whether a block is a stair
	 *
	 * @param block block to check
	 * @return true if stair
	 */
	public static boolean isStair(final Block block) {
		Material type = block.getType();
		return type.name().endsWith("STAIRS");
	}

	/**
	 * Determine whether a block is a wall
	 *
	 * @param block block to check
	 * @return true if wall
	 */
	public static boolean isWall(final Block block) {
		Material type = block.getType();
		return type.name().endsWith("WALL") || type.name().endsWith("FENCE") || type.name().endsWith("FENCE_GATE");
	}
	
	public static boolean isNearWall(final Location location) {
		return isWall(location.getBlock()) || isWall(location.getBlock().getRelative(BlockFace.NORTH))
				|| isWall(location.getBlock().getRelative(BlockFace.SOUTH))
				|| isWall(location.getBlock().getRelative(BlockFace.EAST))
				|| isWall(location.getBlock().getRelative(BlockFace.WEST))
				|| isWall(location.getBlock().getRelative(BlockFace.NORTH_EAST))
				|| isWall(location.getBlock().getRelative(BlockFace.NORTH_WEST))
				|| isWall(location.getBlock().getRelative(BlockFace.SOUTH_EAST))
				|| isWall(location.getBlock().getRelative(BlockFace.SOUTH_WEST));
	}

	/**
	 * Determine whether a player is standing on a lily pad
	 *
	 * @param player player to check
	 * @return true if on lily pad
	 */
	public static boolean isOnLilyPad(final Player player) {
		final Block block = player.getLocation().getBlock();
		return block.getType() == XMaterial.LILY_PAD.parseMaterial()
				|| block.getRelative(BlockFace.NORTH).getType() == XMaterial.LILY_PAD.parseMaterial()
				|| block.getRelative(BlockFace.SOUTH).getType() == XMaterial.LILY_PAD.parseMaterial()
				|| block.getRelative(BlockFace.EAST).getType() == XMaterial.LILY_PAD.parseMaterial()
				|| block.getRelative(BlockFace.WEST).getType() == XMaterial.LILY_PAD.parseMaterial();
	}
	
	/**
	 * Determine whether a player is standing on a carpet
	 *
	 * @param player player to check
	 * @return true if on carpet
	 */
	public static boolean isOnCarpet(final Player player) {
		final Block block = player.getLocation().getBlock();
		return block.getType().name().endsWith("CARPET")
				|| block.getRelative(BlockFace.NORTH).getType().name().endsWith("CARPET")
				|| block.getRelative(BlockFace.SOUTH).getType().name().endsWith("CARPET")
				|| block.getRelative(BlockFace.EAST).getType().name().endsWith("CARPET")
				|| block.getRelative(BlockFace.WEST).getType().name().endsWith("CARPET");
	}

	/**
	 * Determine whether a player is fully submersed in liquid
	 *
	 * @param player player to check
	 * @return true if submersed
	 */
	public static boolean isSubmersed(final Player player) {
		return player.getLocation().getBlock().isLiquid()
				&& player.getLocation().getBlock().getRelative(BlockFace.UP).isLiquid();
	}

	/**
	 * Determine whether a player is in water
	 *
	 * @param player player to check
	 * @return true if in water
	 */
	public static boolean isInWater(Player player) {
		return player.getLocation().getBlock().isLiquid()
				|| player.getLocation().getBlock().getRelative(BlockFace.DOWN).isLiquid()
				|| player.getLocation().getBlock().getRelative(BlockFace.UP).isLiquid()
				|| (MinecraftVersion.getCurrentVersion().isAtLeast(MinecraftVersion.AQUATIC_UPDATE)
						&& (player.getLocation().getBlock().getType() == XMaterial.KELP_PLANT.parseMaterial()
								|| player.getLocation().getBlock().getRelative(BlockFace.UP)
										.getType() == XMaterial.KELP_PLANT.parseMaterial()
								|| player.getLocation().getBlock().getRelative(BlockFace.DOWN)
										.getType() == XMaterial.KELP_PLANT.parseMaterial())
						&& isNearWater(player));
	}

	/**
	 * Determine whether a player is near a liquid block
	 *
	 * @param player player to check
	 * @return true if near liquid block
	 */
	public static boolean isNearWater(Player player) {
		return player.getLocation().getBlock().isLiquid()
				|| player.getLocation().getBlock().getRelative(BlockFace.UP).isLiquid()
				|| player.getLocation().getBlock().getRelative(BlockFace.DOWN).isLiquid()
				|| player.getLocation().getBlock().getRelative(BlockFace.NORTH).isLiquid()
				|| player.getLocation().getBlock().getRelative(BlockFace.SOUTH).isLiquid()
				|| player.getLocation().getBlock().getRelative(BlockFace.EAST).isLiquid()
				|| player.getLocation().getBlock().getRelative(BlockFace.WEST).isLiquid()
				|| player.getLocation().getBlock().getRelative(BlockFace.NORTH_EAST).isLiquid()
				|| player.getLocation().getBlock().getRelative(BlockFace.NORTH_WEST).isLiquid()
				|| player.getLocation().getBlock().getRelative(BlockFace.SOUTH_EAST).isLiquid()
				|| player.getLocation().getBlock().getRelative(BlockFace.SOUTH_WEST).isLiquid();
	}

	/**
	 * Determine whether a location is near a liquid block
	 *
	 * @param location Location to check
	 * @return true if near liquid block
	 */
	public static boolean isNearWater(Location location) {
		return location.getBlock().isLiquid() || location.getBlock().getRelative(BlockFace.UP).isLiquid()
				|| location.getBlock().getRelative(BlockFace.DOWN).isLiquid()
				|| location.getBlock().getRelative(BlockFace.NORTH).isLiquid()
				|| location.getBlock().getRelative(BlockFace.SOUTH).isLiquid()
				|| location.getBlock().getRelative(BlockFace.EAST).isLiquid()
				|| location.getBlock().getRelative(BlockFace.WEST).isLiquid()
				|| location.getBlock().getRelative(BlockFace.NORTH_EAST).isLiquid()
				|| location.getBlock().getRelative(BlockFace.NORTH_WEST).isLiquid()
				|| location.getBlock().getRelative(BlockFace.SOUTH_EAST).isLiquid()
				|| location.getBlock().getRelative(BlockFace.SOUTH_WEST).isLiquid();
	}

	/**
	 * Determine whether a player is surrounded by liquid blocks
	 *
	 * @param player player to check
	 * @return true if surrounded by liquid blocks
	 */
	public static boolean isSurroundedByWater(Player player) {
		final Location location = player.getLocation().clone().subtract(0, 0.1, 0);
		return location.getBlock().isLiquid() && location.getBlock().getRelative(BlockFace.NORTH).isLiquid()
				&& location.getBlock().getRelative(BlockFace.SOUTH).isLiquid()
				&& location.getBlock().getRelative(BlockFace.EAST).isLiquid()
				&& location.getBlock().getRelative(BlockFace.WEST).isLiquid()
				&& location.getBlock().getRelative(BlockFace.NORTH_EAST).isLiquid()
				&& location.getBlock().getRelative(BlockFace.NORTH_WEST).isLiquid()
				&& location.getBlock().getRelative(BlockFace.SOUTH_EAST).isLiquid()
				&& location.getBlock().getRelative(BlockFace.SOUTH_WEST).isLiquid();
	}

	/**
	 * Determine whether a player is near a web
	 *
	 * @param player player to check
	 * @return true if near a web
	 */
	public static boolean isNearWeb(final Player player) {
		return isCollisionPoint(player.getLocation(), material -> material == XMaterial.COBWEB.parseMaterial());
	}

	/**
	 * Determine whether a player is in a web
	 *
	 * @param player player to check
	 * @return true if in web
	 */
	public static boolean isInWeb(Player player) {
		return player.getLocation().getBlock().getType() == XMaterial.COBWEB.parseMaterial()
				|| player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == XMaterial.COBWEB
						.parseMaterial()
				|| player.getLocation().getBlock().getRelative(BlockFace.UP).getType() == XMaterial.COBWEB
						.parseMaterial();
	}

	/**
	 * Determine whether a player is near a climbable block
	 *
	 * @param player player to check
	 * @return true if near climbable block
	 */
	public static boolean isNearClimbable(final Player player) {
		return isCollisionPoint(player.getLocation(), material -> CLIMBABLE.contains(material));
	}

	/**
	 * Determine whether a location is near a climbable block
	 *
	 * @param location location to check
	 * @return true if near climbable block
	 */
	public static boolean isNearClimbable(final Location location) {
		return isCollisionPoint(location, material -> CLIMBABLE.contains(material));
	}

	/**
	 * Determine whether a block is climbable
	 *
	 * @param block block to check
	 * @return true if climbable
	 */
	public static boolean isClimbableBlock(final Block block) {
		return CLIMBABLE.contains(block.getType());
	}

	/**
	 * Determine whether a player is on a vine (can be free hanging)
	 *
	 * @param player to check
	 * @return true if on vine
	 */
	public static boolean isOnVine(final Player player) {
		return player.getLocation().getBlock().getType() == XMaterial.VINE.parseMaterial();
	}

	/**
	 * Determine whether a String can be cast to an Integer
	 *
	 * @param string text to check
	 * @return true if int
	 */
	public static boolean isInt(String string) {
		try {
			Integer.parseInt(string);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * Determine whether a String can be cast to a Double
	 *
	 * @param string text to check
	 * @return true if double
	 */
	public static boolean isDouble(String string) {
		try {
			Double.parseDouble(string);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * Determine if a block ISN'T one of the specified types
	 *
	 * @param block     block to check
	 * @param materials array of possible materials
	 * @return true if the block isn't any of the materials
	 */
	public static boolean blockIsnt(Block block, Material[] materials) {
		Material type = block.getType();
		for (Material m : materials) {
			if (m == type) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Determine if a block ISN'T one of the specified types
	 *
	 * @param block    block to check
	 * @param endTypes array of possible name endings
	 * @return true if the block isn't any of the materials
	 */
	public static boolean blockIsnt(Block block, String[] endTypes) {
		Material type = block.getType();
		for (String s : endTypes) {
			if (type.name().endsWith(s)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Parse a COMMAND[] input to a set of commands to execute
	 *
	 * @param command input string
	 * @return parsed commands
	 */
	public static String[] getCommands(String command) {
		return command.replaceAll("COMMAND\\[", "").replaceAll("]", "").split(";");
	}

	/**
	 * Remove all whitespace from the given string to ready it for parsing
	 *
	 * @param string the string to parse
	 * @return string with whitespace removed
	 */
	public static String removeWhitespace(String string) {
		return string.replaceAll(" ", "");
	}

	/**
	 * Determine if a player has the given enchantment on their armor
	 *
	 * @param player player to check
	 * @param e      enchantment to check
	 * @return true if the armor has this enchantment
	 */
	public static boolean hasArmorEnchantment(Player player, Enchantment e) {
		for (ItemStack is : player.getInventory().getArmorContents()) {
			if (is != null && is.containsEnchantment(e)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Create a list with the given string for execution
	 *
	 * @param string the string to parse
	 * @return ArrayList with string
	 */
	public static ArrayList<String> stringToList(final String string) {
		return new ArrayList<String>() {
			private static final long serialVersionUID = 364115444874638230L;
			{
				add(string);
			}
		};
	}

	/**
	 * Create a comma-delimited string from a list
	 *
	 * @param list the list to parse
	 * @return the list in a string format
	 */
	public static String listToCommaString(List<String> list) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			b.append(list.get(i));
			if (i < list.size() - 1) {
				b.append(",");
			}
		}
		return b.toString();
	}

	/**
	 * Parse a string in the format of "XdXhXmXs" to seconds
	 * 
	 * @param string The string to parse
	 * @return seconds
	 */
	public static long lifeToSeconds(String string) {
		if (string.equals("0") || string.equals(""))
			return 0;
		String[] lifeMatch = new String[] { "d", "h", "m", "s" };
		int[] lifeInterval = new int[] { 86400, 3600, 60, 1 };
		long seconds = 0L;

		for (int i = 0; i < lifeMatch.length; i++) {
			Matcher matcher = Pattern.compile("([0-9]*)" + lifeMatch[i]).matcher(string);
			while (matcher.find()) {
				seconds += Integer.parseInt(matcher.group(1)) * lifeInterval[i];
			}

		}
		return seconds;
	}

	/**
	 * Rounds a float value to a scale
	 * 
	 * @param value Value to round
	 * @param scale Scale
	 * @return rounded value
	 */
	public static float roundFloat(float value, int scale) {
		return new BigDecimal(value).setScale(scale, RoundingMode.HALF_UP).floatValue();
	}

	/**
	 * Rounds a double value to a scale
	 * 
	 * @param value Value to round
	 * @param scale Scale
	 * @return rounded value
	 */
	public static double roundDouble(double value, int scale) {
		return new BigDecimal(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
	}

	public static int floor(double value) {
		int rounded = (int) value;
		return value < rounded ? rounded - 1 : rounded;
	}
	
	/**
	 * Computes the difference between two angles
	 * @param a Angle A
	 * @param b Angle B
	 * @return the difference between two angles
	 */
	public static float computeAngleDifference(final float a, final float b) {
		float diff = Math.abs(a - b);
		final float altDiff = b + 360 - a;
		final float altAltDiff = a + 360 - b;
        if (altDiff < diff) diff = altDiff;
        if (altAltDiff < diff) diff = altAltDiff;
        return diff;
    }

	/**
	 * Calculates the greatest common divider
	 * @param a value a
	 * @param b value b
	 * @return the greatest common divider of a and b
	 */
	public static double computeGcd(final double a, final double b) {
		if (a < b) {
			return computeGcd(b, a);
		}

		if (Math.abs(b) < 0.001) {
			return a;
		} else {
			return computeGcd(b, a - Math.floor(a / b) * b);
		}
    }
	
	public static boolean isCollisionPoint(final Location location, final Predicate<Material> predicate) {
		final ArrayList<Material> materials = new ArrayList<>();
		for (double x = -0.3; x <= 0.3; x += 0.3) {
			for (double y = -0.3; y <= 0.3; y += 0.3) {
				for (double z = -0.3; z <= 0.3; z += 0.3) {
					final Material material = location.clone().add(x, y, z).getBlock().getType();
					if (material != null) {
						materials.add(material);
					}
				}
			}
		}
		return materials.stream().anyMatch(predicate);
	}
	
	static {
		MinecraftVersion currentVersion = MinecraftVersion.getCurrentVersion();

		// Start instant break materials
		INSTANT_BREAK.add(XMaterial.COMPARATOR.parseMaterial());
		INSTANT_BREAK.add(XMaterial.REPEATER.parseMaterial());
		INSTANT_BREAK.add(XMaterial.TORCH.parseMaterial());
		INSTANT_BREAK.add(XMaterial.REDSTONE_TORCH.parseMaterial());
		INSTANT_BREAK.add(XMaterial.REDSTONE_WIRE.parseMaterial());
		INSTANT_BREAK.add(XMaterial.TRIPWIRE.parseMaterial());
		INSTANT_BREAK.add(XMaterial.TRIPWIRE_HOOK.parseMaterial());
		INSTANT_BREAK.add(XMaterial.FIRE.parseMaterial());
		INSTANT_BREAK.add(XMaterial.FLOWER_POT.parseMaterial());
		INSTANT_BREAK.add(XMaterial.INFESTED_CHISELED_STONE_BRICKS.parseMaterial());
		INSTANT_BREAK.add(XMaterial.INFESTED_COBBLESTONE.parseMaterial());
		INSTANT_BREAK.add(XMaterial.INFESTED_CRACKED_STONE_BRICKS.parseMaterial());
		INSTANT_BREAK.add(XMaterial.INFESTED_MOSSY_STONE_BRICKS.parseMaterial());
		INSTANT_BREAK.add(XMaterial.INFESTED_STONE.parseMaterial());
		INSTANT_BREAK.add(XMaterial.INFESTED_STONE_BRICKS.parseMaterial());
		INSTANT_BREAK.add(XMaterial.TNT.parseMaterial());
		INSTANT_BREAK.add(XMaterial.SLIME_BLOCK.parseMaterial());
		INSTANT_BREAK.add(XMaterial.CARROTS.parseMaterial());
		INSTANT_BREAK.add(XMaterial.DEAD_BUSH.parseMaterial());
		INSTANT_BREAK.add(XMaterial.FERN.parseMaterial());
		INSTANT_BREAK.add(XMaterial.LARGE_FERN.parseMaterial());
		INSTANT_BREAK.add(XMaterial.CHORUS_FLOWER.parseMaterial());
		INSTANT_BREAK.add(XMaterial.SUNFLOWER.parseMaterial());
		INSTANT_BREAK.add(XMaterial.LILY_PAD.parseMaterial());
		INSTANT_BREAK.add(XMaterial.MELON_STEM.parseMaterial());
		INSTANT_BREAK.add(XMaterial.ATTACHED_MELON_STEM.parseMaterial());
		INSTANT_BREAK.add(XMaterial.BROWN_MUSHROOM.parseMaterial());
		INSTANT_BREAK.add(XMaterial.RED_MUSHROOM.parseMaterial());
		INSTANT_BREAK.add(XMaterial.NETHER_WART.parseMaterial());
		INSTANT_BREAK.add(XMaterial.POTATOES.parseMaterial());
		INSTANT_BREAK.add(XMaterial.PUMPKIN_STEM.parseMaterial());
		INSTANT_BREAK.add(XMaterial.ATTACHED_PUMPKIN_STEM.parseMaterial());
		INSTANT_BREAK.add(XMaterial.ACACIA_SAPLING.parseMaterial());
		INSTANT_BREAK.add(XMaterial.BIRCH_SAPLING.parseMaterial());
		INSTANT_BREAK.add(XMaterial.DARK_OAK_SAPLING.parseMaterial());
		INSTANT_BREAK.add(XMaterial.JUNGLE_SAPLING.parseMaterial());
		INSTANT_BREAK.add(XMaterial.OAK_SAPLING.parseMaterial());
		INSTANT_BREAK.add(XMaterial.SPRUCE_SAPLING.parseMaterial());
		INSTANT_BREAK.add(XMaterial.SUGAR_CANE.parseMaterial());
		INSTANT_BREAK.add(XMaterial.TALL_GRASS.parseMaterial());
		INSTANT_BREAK.add(XMaterial.TALL_SEAGRASS.parseMaterial());
		INSTANT_BREAK.add(XMaterial.WHEAT.parseMaterial());
		// Start 1.14 objects
		if (currentVersion.isAtLeast(MinecraftVersion.VILLAGE_UPDATE)) {
			INSTANT_BREAK.add(XMaterial.BAMBOO_SAPLING.parseMaterial());
			INSTANT_BREAK.add(XMaterial.CORNFLOWER.parseMaterial());
		}
		// End 1.14 objects
		// Start 1.15 objects
		if (currentVersion.isAtLeast(MinecraftVersion.BEE_UPDATE)) {
			INSTANT_BREAK.add(XMaterial.HONEY_BLOCK.parseMaterial());
		}
		// End 1.15 objects
		// End instant break materials

		// Start food
		FOOD.add(XMaterial.APPLE.parseMaterial());
		FOOD.add(XMaterial.BAKED_POTATO.parseMaterial());
		FOOD.add(XMaterial.BEETROOT.parseMaterial());
		FOOD.add(XMaterial.BEETROOT_SOUP.parseMaterial());
		FOOD.add(XMaterial.BREAD.parseMaterial());
		FOOD.add(XMaterial.CAKE.parseMaterial());
		FOOD.add(XMaterial.CARROT.parseMaterial());
		FOOD.add(XMaterial.CHORUS_FRUIT.parseMaterial());
		FOOD.add(XMaterial.COOKED_BEEF.parseMaterial());
		FOOD.add(XMaterial.COOKED_CHICKEN.parseMaterial());
		FOOD.add(XMaterial.COOKED_COD.parseMaterial());
		FOOD.add(XMaterial.COOKED_MUTTON.parseMaterial());
		FOOD.add(XMaterial.COOKED_PORKCHOP.parseMaterial());
		FOOD.add(XMaterial.COOKED_RABBIT.parseMaterial());
		FOOD.add(XMaterial.COOKED_SALMON.parseMaterial());
		FOOD.add(XMaterial.COOKIE.parseMaterial());
		FOOD.add(XMaterial.DRIED_KELP.parseMaterial());
		FOOD.add(XMaterial.ENCHANTED_GOLDEN_APPLE.parseMaterial());
		FOOD.add(XMaterial.GOLDEN_APPLE.parseMaterial());
		FOOD.add(XMaterial.GOLDEN_CARROT.parseMaterial());
		FOOD.add(XMaterial.MELON_SLICE.parseMaterial());
		FOOD.add(XMaterial.MUSHROOM_STEW.parseMaterial());
		FOOD.add(XMaterial.POISONOUS_POTATO.parseMaterial());
		FOOD.add(XMaterial.POTATO.parseMaterial());
		FOOD.add(XMaterial.PUFFERFISH.parseMaterial());
		FOOD.add(XMaterial.PUMPKIN_PIE.parseMaterial());
		FOOD.add(XMaterial.RABBIT_STEW.parseMaterial());
		FOOD.add(XMaterial.BEEF.parseMaterial());
		FOOD.add(XMaterial.CHICKEN.parseMaterial());
		FOOD.add(XMaterial.COD.parseMaterial());
		FOOD.add(XMaterial.MUTTON.parseMaterial());
		FOOD.add(XMaterial.PORKCHOP.parseMaterial());
		FOOD.add(XMaterial.RABBIT.parseMaterial());
		FOOD.add(XMaterial.SALMON.parseMaterial());
		FOOD.add(XMaterial.ROTTEN_FLESH.parseMaterial());
		FOOD.add(XMaterial.SPIDER_EYE.parseMaterial());
		FOOD.add(XMaterial.TROPICAL_FISH.parseMaterial());
		// Start 1.14 objects
		if (currentVersion.isAtLeast(MinecraftVersion.VILLAGE_UPDATE)) {
			FOOD.add(XMaterial.SUSPICIOUS_STEW.parseMaterial());
			FOOD.add(XMaterial.SWEET_BERRIES.parseMaterial());
		}
		// End 1.14 objects
		// Start 1.15 objects
		if (currentVersion.isAtLeast(MinecraftVersion.BEE_UPDATE)) {
			FOOD.add(XMaterial.HONEY_BOTTLE.parseMaterial());
		}
		// End 1.15 objects
		// End food

		// Start combos
		COMBO.put(XMaterial.SHEARS.parseMaterial(), XMaterial.BLACK_WOOL.parseMaterial());
		COMBO.put(XMaterial.SHEARS.parseMaterial(), XMaterial.BLUE_WOOL.parseMaterial());
		COMBO.put(XMaterial.SHEARS.parseMaterial(), XMaterial.BROWN_WOOL.parseMaterial());
		COMBO.put(XMaterial.SHEARS.parseMaterial(), XMaterial.CYAN_WOOL.parseMaterial());
		COMBO.put(XMaterial.SHEARS.parseMaterial(), XMaterial.GRAY_WOOL.parseMaterial());
		COMBO.put(XMaterial.SHEARS.parseMaterial(), XMaterial.GREEN_WOOL.parseMaterial());
		COMBO.put(XMaterial.SHEARS.parseMaterial(), XMaterial.LIGHT_BLUE_WOOL.parseMaterial());
		COMBO.put(XMaterial.SHEARS.parseMaterial(), XMaterial.LIGHT_GRAY_WOOL.parseMaterial());
		COMBO.put(XMaterial.SHEARS.parseMaterial(), XMaterial.LIME_WOOL.parseMaterial());
		COMBO.put(XMaterial.SHEARS.parseMaterial(), XMaterial.MAGENTA_WOOL.parseMaterial());
		COMBO.put(XMaterial.SHEARS.parseMaterial(), XMaterial.MAGENTA_WOOL.parseMaterial());
		COMBO.put(XMaterial.SHEARS.parseMaterial(), XMaterial.ORANGE_WOOL.parseMaterial());
		COMBO.put(XMaterial.SHEARS.parseMaterial(), XMaterial.PINK_WOOL.parseMaterial());
		COMBO.put(XMaterial.SHEARS.parseMaterial(), XMaterial.PURPLE_WOOL.parseMaterial());
		COMBO.put(XMaterial.SHEARS.parseMaterial(), XMaterial.RED_WOOL.parseMaterial());
		COMBO.put(XMaterial.SHEARS.parseMaterial(), XMaterial.WHITE_WOOL.parseMaterial());
		COMBO.put(XMaterial.SHEARS.parseMaterial(), XMaterial.YELLOW_WOOL.parseMaterial());

		COMBO.put(XMaterial.IRON_SWORD.parseMaterial(), XMaterial.COBWEB.parseMaterial());
		COMBO.put(XMaterial.DIAMOND_SWORD.parseMaterial(), XMaterial.COBWEB.parseMaterial());
		COMBO.put(XMaterial.STONE_SWORD.parseMaterial(), XMaterial.COBWEB.parseMaterial());
		COMBO.put(XMaterial.WOODEN_SWORD.parseMaterial(), XMaterial.COBWEB.parseMaterial());
		// End combos

		// Start climbable
		CLIMBABLE.add(XMaterial.VINE.parseMaterial());
		CLIMBABLE.add(XMaterial.LADDER.parseMaterial());
		CLIMBABLE.add(XMaterial.WATER.parseMaterial());
		// Start 1.14 objects
		if (currentVersion.isAtLeast(MinecraftVersion.VILLAGE_UPDATE)) {
			CLIMBABLE.add(XMaterial.SCAFFOLDING.parseMaterial());
			CLIMBABLE.add(XMaterial.SWEET_BERRY_BUSH.parseMaterial());
		}
		// End 1.14 objects

		// Start 1.15 objects
		if (currentVersion.isAtLeast(MinecraftVersion.BEE_UPDATE)) {
			CLIMBABLE.add(XMaterial.HONEY_BLOCK.parseMaterial());
		}
		// End 1.15 objects
		
		// Start 1.16 objects
		if (currentVersion.isAtLeast(MinecraftVersion.NETHER_UPDATE)) {
			CLIMBABLE.add(XMaterial.TWISTING_VINES.parseMaterial());
			CLIMBABLE.add(XMaterial.TWISTING_VINES_PLANT.parseMaterial());
			CLIMBABLE.add(XMaterial.WEEPING_VINES.parseMaterial());
			CLIMBABLE.add(XMaterial.WEEPING_VINES_PLANT.parseMaterial());
		}
		// End 1.16 objects

		// Start 1.17 objects
		if (currentVersion.isAtLeast(MinecraftVersion.CAVES_CLIFFS_1)) {
			CLIMBABLE.add(XMaterial.CAVE_VINES.parseMaterial());
			CLIMBABLE.add(XMaterial.CAVE_VINES_PLANT.parseMaterial());
			CLIMBABLE.add(XMaterial.GLOW_BERRIES.parseMaterial());
			CLIMBABLE.add(XMaterial.POWDER_SNOW.parseMaterial());
		}
		// End 1.17 objects
		// End climbable
	}
}
