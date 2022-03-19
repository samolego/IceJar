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

package com.rammelkast.anticheatreloaded.event;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import com.cryptomorin.xseries.XMaterial;
import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.check.combat.VelocityCheck;
import com.rammelkast.anticheatreloaded.check.movement.BoatFlyCheck;
import com.rammelkast.anticheatreloaded.check.movement.ElytraCheck;
import com.rammelkast.anticheatreloaded.check.movement.FastLadderCheck;
import com.rammelkast.anticheatreloaded.check.movement.FlightCheck;
import com.rammelkast.anticheatreloaded.check.movement.SpeedCheck;
import com.rammelkast.anticheatreloaded.check.movement.StrafeCheck;
import com.rammelkast.anticheatreloaded.check.movement.WaterWalkCheck;
import com.rammelkast.anticheatreloaded.check.player.IllegalInteract;
import com.rammelkast.anticheatreloaded.manage.AntiCheatManager;
import com.rammelkast.anticheatreloaded.util.Distance;
import com.rammelkast.anticheatreloaded.util.Permission;
import com.rammelkast.anticheatreloaded.util.User;
import com.rammelkast.anticheatreloaded.util.Utilities;
import com.rammelkast.anticheatreloaded.util.VersionUtil;

public final class PlayerListener extends EventListener {

	private static final AntiCheatManager MANAGER = AntiCheatReloaded.getManager();
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		final Player player = event.getPlayer();
		if (getCheckManager().willCheck(player, CheckType.COMMAND_SPAM)
				&& !Permission.getCommandExempt(player, event.getMessage().split(" ")[0])) {
			final CheckResult result = getBackend().checkCommandSpam(player, event.getMessage());
			if (result.failed()) {
				event.setCancelled(!silentMode());
				if (!silentMode())
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', result.getMessage()));
				getBackend().processCommandSpammer(player);
				log(null, player, CheckType.COMMAND_SPAM, result.getSubCheck());
			}
		}

		MANAGER.addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerToggleFlight(final PlayerToggleFlightEvent event) {
		if (!event.isFlying()) {
			getBackend().logEnterExit(event.getPlayer());
		}

		MANAGER.addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerGameModeChange(final PlayerGameModeChangeEvent event) {
		if (event.getNewGameMode() != GameMode.CREATIVE) {
			getBackend().logEnterExit(event.getPlayer());
		}

		MANAGER.addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onProjectileLaunch(final ProjectileLaunchEvent event) {
		if (event.getEntity().getShooter() instanceof Player) {
			final Player player = (Player) event.getEntity().getShooter();

			if (event.getEntity() instanceof Arrow) {
				return;
			}

			if (getCheckManager().willCheck(player, CheckType.FAST_PROJECTILE)) {
				final CheckResult result = getBackend().checkProjectile(player);
				if (result.failed()) {
					event.setCancelled(!silentMode());
					log(result.getMessage(), player, CheckType.FAST_PROJECTILE, result.getSubCheck());
				}
			}
		}

		MANAGER.addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(final PlayerTeleportEvent event) {
		getBackend().logTeleport(event.getPlayer());
		MANAGER.addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerChangeWorlds(final PlayerChangedWorldEvent event) {
		getBackend().logTeleport(event.getPlayer());

		MANAGER.addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerToggleSneak(final PlayerToggleSneakEvent event) {
		final Player player = event.getPlayer();
		if (event.isSneaking()) {
			if (getCheckManager().willCheck(player, CheckType.SNEAK)) {
				final CheckResult result = getBackend().checkSneakToggle(player);
				if (result.failed()) {
					event.setCancelled(!silentMode());
					log(result.getMessage(), player, CheckType.SNEAK, result.getSubCheck());
				}
			}
		}

		MANAGER.addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerVelocity(final PlayerVelocityEvent event) {
		final Player player = event.getPlayer();
		final Vector velocity = event.getVelocity();
		final User user = MANAGER.getUserManager().getUser(player.getUniqueId());
		final boolean debugMode = MANAGER.getConfiguration().getConfig().debugMode.getValue();
		user.getVelocityTracker().registerVelocity(velocity);
		if (debugMode) {
			player.sendMessage(AntiCheatReloaded.PREFIX + "Registered velocity [" + velocity.toString() + "]");
		}

		// Part of Velocity check
		if (!user.getMovementManager().onGround) {
			return;
		}
		
		final double motionY = velocity.getY();
		user.getMovementManager().velocityExpectedMotionY = motionY;
		// End part of Velocity check
		
		MANAGER.addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(final AsyncPlayerChatEvent event) {
		final Player player = event.getPlayer();
		
		if (getCheckManager().willCheck(player, CheckType.CHAT_SPAM)) {
			final CheckResult result = getBackend().checkChatSpam(player, event.getMessage());
			if (result.failed()) {
				event.setCancelled(!silentMode());
				if (!result.getMessage().equals("") && !silentMode()) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', result.getMessage()));
				}
				getBackend().processChatSpammer(player);
				AntiCheatReloaded.sendToMainThread(new Runnable() {
					@Override
					public void run() {
						log(null, player, CheckType.CHAT_SPAM, result.getSubCheck());
					}
				});
			}
		}

		if (getCheckManager().willCheck(player, CheckType.CHAT_UNICODE)) {
			final CheckResult result = getBackend().checkChatUnicode(player, event.getMessage());
			if (result.failed()) {
				event.setCancelled(true);
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', result.getMessage()));
				AntiCheatReloaded.sendToMainThread(new Runnable() {
					@Override
					public void run() {
						log(null, player, CheckType.CHAT_UNICODE, result.getSubCheck());
					}
				});
			}
		}

		MANAGER.addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerKick(PlayerKickEvent event) {
		getBackend().cleanup(event.getPlayer());

		MANAGER.addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(final PlayerQuitEvent event) {
		final Player player = event.getPlayer();
		try {
			final User user = AntiCheatReloaded.getExecutor().submit(new Callable<User>() {
				@Override
				public User call() throws Exception {
					getBackend().cleanup(player);
					final User user = getUserManager().getUser(player.getUniqueId());
					getConfig().getLevels().saveLevelFromUser(user);
					return user;
				}
			}).get();
			getUserManager().removeUser(user);
		} catch (final InterruptedException | ExecutionException exception) {
			AntiCheatReloaded.getPlugin().getLogger().log(Level.SEVERE, "Failed to destroy user object async", exception);
			return;
		}

		MANAGER.addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerVehicleCollide(final VehicleEntityCollisionEvent event) {
		if (!(event.getVehicle() instanceof Boat) || !(event.getEntity() instanceof Player)) {
			return;
		}

		final Player player = (Player) event.getEntity();
		getBackend().logBoatCollision(player);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
		final Player player = event.getPlayer();
		if (getCheckManager().willCheck(player, CheckType.SPRINT)) {
			final CheckResult result = getBackend().checkSprintHungry(event);
			if (result.failed()) {
				event.setCancelled(!silentMode());
				log(result.getMessage(), player, CheckType.SPRINT, result.getSubCheck());
			} else {
				decrease(player);
			}
		}

		MANAGER.addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(final PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		final PlayerInventory inv = player.getInventory();
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			final ItemStack itemInHand = ((event.getHand() == EquipmentSlot.HAND) ? inv.getItemInMainHand() : inv.getItemInOffHand());

			if (itemInHand.getType() == Material.BOW) {
				getBackend().logBowWindUp(player);
			} else if (Utilities.isFood(itemInHand.getType()) || Utilities.isFood(itemInHand.getType())) {
				getBackend().logEatingStart(player);
			}

			if (itemInHand.getType() == XMaterial.FIREWORK_ROCKET.parseMaterial()) {
				ElytraCheck.JUMP_Y_VALUE.remove(player.getUniqueId());
				if (player.isGliding()) {
					ElytraCheck.JUMP_Y_VALUE.put(player.getUniqueId(), 9999.99D);
				}
			}
		}

		final Block block = event.getClickedBlock();
		if (block != null
				&& (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
			if (getCheckManager().willCheck(player, CheckType.ILLEGAL_INTERACT)) {
				final CheckResult result = IllegalInteract.performCheck(player, event);
				if (result.failed()) {
					event.setCancelled(!silentMode());
					log(result.getMessage(), player, CheckType.ILLEGAL_INTERACT, result.getSubCheck());
				}
			}
		}

		MANAGER.addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		final Player player = event.getPlayer();
		if (getCheckManager().willCheck(player, CheckType.ITEM_SPAM)) {
			CheckResult result = getBackend().checkFastDrop(player);
			if (result.failed()) {
				event.setCancelled(!silentMode());
				log(result.getMessage(), player, CheckType.ITEM_SPAM, result.getSubCheck());
			}
		}

		MANAGER.addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerEnterBed(PlayerBedEnterEvent event) {
		if (event.getBed().getType().name().endsWith("BED")) {
			return;
		}
		getBackend().logEnterExit(event.getPlayer());

		MANAGER.addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerExitBed(PlayerBedLeaveEvent event) {
		if (event.getBed().getType().name().endsWith("BED")) {
			return;
		}
		getBackend().logEnterExit(event.getPlayer());

		MANAGER.addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		getBackend().logJoin(player);

		try {
			final User user = AntiCheatReloaded.getExecutor().submit(new Callable<User>() {
				@Override
				public User call() throws Exception {
					final User user = new User(player.getUniqueId());
					user.setIsWaitingOnLevelSync(true);
					getConfig().getLevels().loadLevelToUser(user);
					return user;
				}
			}).get();
			getUserManager().addUser(user);
		} catch (final InterruptedException | ExecutionException exception) {
			AntiCheatReloaded.getPlugin().getLogger().log(Level.SEVERE, "Failed to create user object async", exception);
			return;
		}
		
		MANAGER.addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());

		if (player.hasPermission("anticheat.admin") && !AntiCheatReloaded.getUpdateManager().isLatest()) {
			player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "ACR " + ChatColor.GRAY
					+ "Your version of AntiCheatReloaded is outdated! You can download "
					+ AntiCheatReloaded.getUpdateManager().getLatestVersion()
					+ " from the Spigot forums or DevBukkit.");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerMove(final PlayerMoveEvent event) {
		final Player player = event.getPlayer();
		if (getCheckManager().checkInWorld(player) && !getCheckManager().isOpExempt(player)) {
			final Location from = event.getFrom();
			final Location to = event.getTo();

			final Distance distance = new Distance(from, to);
			final double y = distance.getYDifference();

			final User user = getUserManager().getUser(player.getUniqueId());
			user.setTo(to.getX(), to.getY(), to.getZ());
			user.getMovementManager().handle(player, from, to, distance);

			if (getCheckManager().willCheckQuick(player, CheckType.FLIGHT) && !VersionUtil.isFlying(player)) {
				final CheckResult result = FlightCheck.runCheck(player, distance);
				if (result.failed()) {
					if (!silentMode()) {
						event.setTo(user.getGoodLocation(from.clone()));
					}
					log(result.getMessage(), player, CheckType.FLIGHT, result.getSubCheck());
				}
			}
			if (getCheckManager().willCheckQuick(player, CheckType.ELYTRAFLY)) {
				final CheckResult result = ElytraCheck.runCheck(player, distance);
				if (result.failed()) {
					log(result.getMessage(), player, CheckType.ELYTRAFLY, result.getSubCheck());
				}
			}
			if (getCheckManager().willCheckQuick(player, CheckType.BOATFLY)) {
				final CheckResult result = BoatFlyCheck.runCheck(player, user.getMovementManager(), to);
				if (result.failed()) {
					if (!silentMode()) {
						player.eject();
						event.setTo(user.getGoodLocation(from.clone()));
					}
					log(result.getMessage(), player, CheckType.BOATFLY, result.getSubCheck());
				}
			}
			if (getCheckManager().willCheckQuick(player, CheckType.VCLIP)
					&& event.getFrom().getY() > event.getTo().getY()) {
				final CheckResult result = getBackend().checkVClip(player, new Distance(event.getFrom(), event.getTo()));
				if (result.failed()) {
					if (!silentMode()) {
						int data = result.getData() > 3 ? 3 : result.getData();
						Location newloc = new Location(player.getWorld(), event.getFrom().getX(),
								event.getFrom().getY() + data, event.getFrom().getZ());
						if (newloc.getBlock().getType() == Material.AIR) {
							event.setTo(newloc);
						} else {
							event.setTo(user.getGoodLocation(from.clone()));
						}
						player.damage(3);
					}
					log(result.getMessage(), player, CheckType.VCLIP, result.getSubCheck());
				}
			}
			if (getCheckManager().willCheckQuick(player, CheckType.NOFALL)
					&& getCheckManager().willCheck(player, CheckType.FLIGHT)
					&& !Utilities.isClimbableBlock(player.getLocation().getBlock())
					&& event.getFrom().getY() > event.getTo().getY()) {
				final CheckResult result = getBackend().checkNoFall(player, y);
				if (result.failed()) {
					if (!silentMode()) {
						event.setTo(user.getGoodLocation(from.clone()));
					}
					log(result.getMessage(), player, CheckType.NOFALL, result.getSubCheck());
				}
			}

			if (event.getTo() != event.getFrom()) {
				final double x = distance.getXDifference();
				final double z = distance.getZDifference();
				if (getCheckManager().willCheckQuick(player, CheckType.SPEED)
						&& getCheckManager().willCheck(player, CheckType.FLIGHT)) {
					if (event.getFrom().getY() < event.getTo().getY()) {
						final CheckResult result = SpeedCheck.checkVerticalSpeed(player, distance);
						if (result.failed()) {
							if (!silentMode()) {
								event.setTo(user.getGoodLocation(from.clone()));
							}
							log(result.getMessage(), player, CheckType.SPEED, result.getSubCheck());
						}
					}
					final CheckResult result = SpeedCheck.checkXZSpeed(player, x, z, event.getTo());
					if (result.failed()) {
						if (!silentMode()) {
							event.setTo(user.getGoodLocation(from.clone()));
						}
						log(result.getMessage(), player, CheckType.SPEED, result.getSubCheck());
					}
				}
				if (getCheckManager().willCheckQuick(player, CheckType.WATER_WALK)) {
					final CheckResult result = WaterWalkCheck.runCheck(player, x, y, z);
					if (result.failed()) {
						if (!silentMode()) {
							player.teleport(player.getLocation().clone().subtract(0, 0.42, 0));
						}
						log(result.getMessage(), player, CheckType.WATER_WALK, result.getSubCheck());
					}
				}
				if (getCheckManager().willCheckQuick(player, CheckType.SPIDER)) {
					final CheckResult result = getBackend().checkSpider(player, y);
					if (result.failed()) {
						if (!silentMode()) {
							event.setTo(user.getGoodLocation(from.clone()));
						}
						log(result.getMessage(), player, CheckType.SPIDER, result.getSubCheck());
					}
				}
				if (getCheckManager().willCheckQuick(player, CheckType.FASTLADDER)) {
					// Does not use y value created before because that value is absolute
					final CheckResult result = FastLadderCheck.runCheck(player,
							event.getTo().getY() - event.getFrom().getY());
					if (result.failed()) {
						if (!silentMode()) {
							event.setTo(user.getGoodLocation(from.clone()));
						}
						log(result.getMessage(), player, CheckType.FASTLADDER, result.getSubCheck());
					}
				}
				if (getCheckManager().willCheckQuick(player, CheckType.STRAFE)) {
					final CheckResult result = StrafeCheck.runCheck(player, x, z, event.getFrom(), event.getTo());
					if (result.failed()) {
						if (!silentMode()) {
							event.setTo(user.getGoodLocation(from.clone()));
						}
						log(result.getMessage(), player, CheckType.STRAFE, result.getSubCheck());
					}
				}
			}
			if (getCheckManager().willCheckQuick(player, CheckType.VELOCITY)) {
				final CheckResult result = VelocityCheck.runCheck(player, distance);
				if (result.failed()) {
					log(result.getMessage(), player, CheckType.VELOCITY, result.getSubCheck());
				}
			}
		}

		MANAGER.addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onRespawn(final PlayerRespawnEvent event) {
		getBackend().logTeleport(event.getPlayer());
	}

}
