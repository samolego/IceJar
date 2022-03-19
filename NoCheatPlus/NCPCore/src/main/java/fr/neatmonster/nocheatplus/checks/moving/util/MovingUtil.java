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
package fr.neatmonster.nocheatplus.checks.moving.util;

import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.event.player.PlayerMoveEvent;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.moving.magic.Magic;
import fr.neatmonster.nocheatplus.checks.moving.model.MoveData;
import fr.neatmonster.nocheatplus.checks.moving.model.PlayerMoveData;
import fr.neatmonster.nocheatplus.checks.moving.player.PlayerSetBackMethod;
import fr.neatmonster.nocheatplus.checks.net.NetData;
import fr.neatmonster.nocheatplus.checks.net.model.CountableLocation;
import fr.neatmonster.nocheatplus.compat.Bridge1_13;
import fr.neatmonster.nocheatplus.compat.Bridge1_9;
import fr.neatmonster.nocheatplus.compat.BridgeMisc;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.components.debug.IDebugPlayer;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.players.IPlayerData;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;
import fr.neatmonster.nocheatplus.utilities.location.LocUtil;
import fr.neatmonster.nocheatplus.utilities.location.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.location.RichBoundsLocation;
import fr.neatmonster.nocheatplus.utilities.location.TrigUtil;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.map.MapUtil;

/**
 * Static utility methods.
 * @author asofold
 *
 */
public class MovingUtil {

    /**
     * Always set world to null after use, careful with nested methods. Main thread only.
     */
    private static final Location useLoc = new Location(null, 0, 0, 0);
    //    /** Fast scan flags for 'mostly air'. */
    //    private static final long FLAGS_SCAN_FOR_GROUND_OR_RESETCOND = 
    //            BlockProperties.F_SOLID | BlockProperties.F_GROUND
    //            | BlockProperties.F_LIQUID | BlockProperties.F_COBWEB
    //            | BlockProperties.F_CLIMBABLE
    //            ;


    /**
     * Check if the player is to be checked by the survivalfly check.<br>
     * Primary thread only.
     * 
     * @param player
     * @param fromLoc
     *            The location the player is moving from or just where the
     *            player is.
     * @param toLoc
     *            The location the player has moved to.
     * @param data
     * @param cc
     * @return
     */
    public static final boolean shouldCheckSurvivalFly(final ServerPlayer player, final ServerPlayerLocation fromLocation, final ServerPlayerLocation toLocation,
                                                       final MovingData data, final MovingConfig cc, final IPlayerData pData) {

        final GameMode gameMode = player.getGameMode();
        final double yDistance = data.playerMoves.getCurrentMove().yDistance;
        final boolean toOnground = toLocation != null && toLocation.getWorld() != null && toLocation.isOnGround();
        // (Full activation check - use permission caching for performance rather.)

        return  
                // Sf is active (duh..)
                pData.isCheckActive(CheckType.MOVING_SURVIVALFLY, player)
                // Spectator is handled by Cf
                && gameMode != BridgeMisc.GAME_MODE_SPECTATOR
                // Creative or ignoreCreative is off/flying - let Cf handle those.
                && (cc.ignoreCreative || gameMode != GameMode.CREATIVE) && !player.isFlying()
                // IgnoreAllowFlight is off or the player is allowed to fly (cf).
                && (cc.ignoreAllowFlight || !player.getAllowFlight())
                // Gliding is handled by cf
                && (
                    !Bridge1_9.isGlidingWithElytra(player)
                    || !isGlidingWithElytraValid(player, fromLocation, data, cc)
                )
                // Levitation is handled by Cf, unless the player is in liquid which is handled by Sf.
                && (
                    Double.isInfinite(Bridge1_9.getLevitationAmplifier(player))
                    || fromLocation.isInLiquid() // Can't levitate if in liquid.
                    // Moving up or down will mess with vDistRel detection due to erratic movement (players will fast/slow fall/ascend depending on the level)
                    // so we only check if the move is fully on ground to prevent (too simple) speeding
                    || Bridge1_9.getLevitationAmplifier(player) >= 128 && fromLocation.isOnGround() && toOnground && yDistance == 0.0
                )
                // Actual slowfalling is handled by Cf. Moving up or from/to ground is handled by Sf.
                && (
                    Double.isInfinite(Bridge1_13.getSlowfallingAmplifier(player))
                    || (fromLocation.isOnGround() || yDistance > 0.0 || toOnground)
                )
                // Riptiding is handled by Cf.
                && !Bridge1_13.isRiptiding(player)
            ;
    }


    /** 
     * Collect the F_STICKY block flag. Clear NoFall's data upon side collision.
     * @param from
     * @param to
     * @param data
     */
    public static boolean isCollideWithHB(PlayerLocation from, PlayerLocation to, MovingData data) {

        final boolean isFlagCollected = (to.getBlockFlags() & BlockProperties.F_STICKY) != 0;
        // Moving on side block, remove nofall data
        if (isFlagCollected && !to.isOnGround() && BlockProperties.collides(to.getBlockCache(),
                                                         to.getMinX() - 0.01, to.getMinY(), to.getMinZ() - 0.01, 
                                                         to.getMaxX() + 0.01, to.getMaxY(), to.getMaxZ() + 0.01, 
                                                         BlockProperties.F_STICKY)
        ) {
            data.clearNoFallData();
        }
        return isFlagCollected;
    }


    /**
     * Consistency / cheat check. Prerequisite is
     * Bridge1_9.isGlidingWithElytra(player) having returned true.
     * 
     * @param player
     * @param fromLocation
     * @param data
     * @param cc
     * @return
     */
    public static boolean isGlidingWithElytraValid(final ServerPlayer player, final ServerPlayerLocation fromLocation,
                                                   final MovingData data, final MovingConfig cc) {

        // TODO: Configuration for which/if to check on either lift-off / unknown / gliding.
        // TODO: Item durability?
        // TODO: TEST LAVA (ordinary and boost, lift off and other).
        /*
         * TODO: Allow if last move not touched ground (+-) after all the
         * onGround check isn't much needed, if we can test for the relevant stuff (web?).
         */

        // Check start glide conditions.
        final ServerPlayerMoveData firstPastMove = data.playerMoves.getFirstPastMove();
        if (
                // Skip lift-off conditions if the EntityToggleGlideEvent is present (checked there).
                !Bridge1_9.hasEntityToggleGlideEvent()
                // Otherwise only treat as lift-off, if not already gliding.
                && !firstPastMove.toIsValid || firstPastMove.modelFlying == null 
                || !MovingConfig.ID_JETPACK_ELYTRA.equals(firstPastMove.modelFlying.getId())) {
            // Treat as a lift off.
            // TODO: Past map states might allow lift off (...).
            return canLiftOffWithElytra(player, fromLocation, data);
        }

        /*
         * TODO: Test / verify it gets turned off if depleted during gliding,
         * provided the client doesn't help knowing. (Only shortly tested with
         * grep -r "ItemElytra.d" <- looks good.)
         */
        //        // Test late, as lift-off check also tests for this.
        //        if (InventoryUtil.isItemBroken(player.getInventory().getChestplate())) {
        //            return false;
        //        }

        /*
         * TODO: Rather focus on abort conditions (in-medium stay time for
         * special blocks, sleeping / dead / ...)?
         */

        // Only the web can stop a player who isn't propelled by a rocket.
        return data.fireworksBoostDuration > 0 || !BlockProperties.collides(fromLocation.getBlockCache(), 
                fromLocation.getMinX(), fromLocation.getMinY(), fromLocation.getMinZ(), 
                fromLocation.getMaxX(), fromLocation.getMinY() + 0.6, fromLocation.getMaxZ(),
                BlockProperties.F_COBWEB);
    }


    /**
     * Check lift-off (CB: on ground is done wrongly, inWater probably is
     * correct, web is not checked).
     * 
     * @param fromLocation
     * @param data
     * @return
     */
    public static boolean canLiftOffWithElytra(final ServerPlayer player, final ServerPlayerLocation loc, final MovingData data) {
        // TODO: Item durability here too?
        // TODO: this/firstPast- Move not touching or not explicitly on ground would be enough?
        return 
                loc.isPassableBox() // Full box as if standing for lift-off.
                && !loc.isInWeb()
                // Durability is checked within PlayerConnection (toggling on).
                // && !InventoryUtil.isItemBroken(player.getInventory().getChestplate())
                /*
                 * TODO: Could be a problem with too high yOnGround. Actual
                 * cheating rather would have to be prevented by monitoring
                 * jumping more tightly (low jump = set back, needs
                 * false-positive-free checking (...)).
                 */
                && !loc.isOnGround(0.001)
                && !loc.isInBerryBush() 
                // Assume water is checked correctly.
                //                && (
                //                        !fromLocation.isInLiquid() // (Needs to check for actual block bounds).
                //                        /*
                //                         * Observed with head free, but feet clearly in water:
                //                         * lift off from water (not 100% easy to do).
                //                         */
                //                        || !BlockProperties.isLiquid(fromLocation.getTypeId())
                //                        || !BlockProperties.isLiquid(fromLocation.getTypeIdAbove())
                //                        )
                ;
    }


    /**
     * Workaround for getEyeHeight not accounting for special conditions like
     * gliding with elytra. (Sleeping is not checked.)
     * 
     * @param player
     * @return
     */
    public static double getEyeHeight(final ServerPlayer player) {
        // TODO: Need a variant/method to test legitimate state transitions?
        // TODO: EntityToggleGlideEvent
        return Bridge1_9.isGlidingWithElytra(player) ? 0.4 : player.getEyeHeight();
    }


    /**
     * Initialize pLoc with edge data specific to gliding with elytra.
     * 
     * @param player
     * @param pLoc
     * @param loc
     * @param yOnGround
     * @param mcAccess
     */
    public static void setElytraProperties(final ServerPlayer player, final ServerPlayerLocation pLoc, final Location loc,
                                           final double yOnGround, final MCAccess mcAccess) {
        pLoc.set(loc, player, mcAccess.getWidth(player), 0.4, 0.6, 0.6, yOnGround);
    }


    /**
     * Handle an illegal move by a player, attempt to restore a valid location.
     * <br>
     * NOTE: event.setTo is used to not leave a gap.
     * 
     * @param event
     * @param player
     * @param data
     * @param cc
     */
    public static void handleIllegalMove(final ServerPlayerMoveEvent event, final ServerPlayer player,
                                         final MovingData data, final MovingConfig cc) {

        // This might get extended to a check-like thing.
        boolean restored = false;
        final ServerPlayerLocation pLoc = new PlayerLocation(NCPAPIProvider.getNoCheatPlusAPI().getGenericInstanceHandle(MCAccess.class), null);
        // (Mind that we don't set the block cache here).
        final Location loc = player.getLocation();
        if (!restored && data.hasSetBack()) {
            /*
             * TODO: Harmonize with MovingUtil.getApplicableSetBackLocation
             * (somehow include the desired set back type / loc / context).
             */
            final Location setBack = data.getSetBack(loc); // TODO
            pLoc.set(setBack, player);
            if (!pLoc.hasIllegalCoords() && (cc.ignoreStance || !pLoc.hasIllegalStance())) {
                event.setFrom(setBack);
                event.setTo(setBack);
                restored = true;
            }
            else {
                data.resetSetBack();
            }
        } 
        if (!restored) {
            pLoc.set(loc, player);
            if (!pLoc.hasIllegalCoords() && (cc.ignoreStance || !pLoc.hasIllegalStance())) {
                event.setFrom(loc);
                event.setTo(loc);
                restored = true;
            }
        }
        pLoc.cleanup();
        if (!restored) {
            // TODO: reset the bounding box of the player ?
            if (cc.tempKickIllegal) {
                NCPAPIProvider.getNoCheatPlusAPI().denyLogin(player.getName(), 24L * 60L * 60L * 1000L);
                StaticLog.logSevere("[NoCheatPlus] could not restore location for " + player.getName() + ", kicking them and deny login for 24 hours");
            } else {
                StaticLog.logSevere("[NoCheatPlus] could not restore location for " + player.getName() + ", kicking them.");
            }
            CheckUtils.kickIllegalMove(player, cc);
        }
    }


    /**
     * Used for a workaround that resets the set back for the case of jumping on just placed blocks.
     * @param id
     * @return
     */
    public static boolean canJumpOffTop(final Material blockType) {
        return BlockProperties.isGround(blockType) || BlockProperties.isSolid(blockType);
    }


    /**
     * Check the context-independent pre-conditions for checking for untracked
     * locations (not the world spawn, location is not passable, passable is
     * enabled for the player).
     * 
     * @param player
     * @param loc
     * @return
     */
    public static boolean shouldCheckUntrackedLocation(final ServerPlayer player,
            final Location loc, final IPlayerData pData) {
        return !TrigUtil.isSamePos(loc, loc.getWorld().getSpawnLocation()) 
                && !BlockProperties.isPassable(loc)
                && pData.isCheckActive(CheckType.MOVING_PASSABLE, player);
    }


    /**
     * Detect if the given location is an untracked spot. This is spots for
     * which a player is at the location, but the moving data has another
     * "last to" position set for that player. Note that one matching player
     * with "last to" being consistent is enough to let this return null, world spawn is exempted.
     * <hr>
     * Pre-conditions:<br>
     * <li>Context-specific (e.g. activation flags for command, teleport).</li>
     * <li>See MovingUtils.shouldCheckUntrackedLocation.</li>
     * 
     * @param loc
     * @return Corrected location, if loc is an "untracked location".
     */
    public static Location checkUntrackedLocation(final Location loc) {
        // TODO: More efficient method to get entities at the same position (might use MCAccess).
        final Chunk toChunk = loc.getChunk();
        final Entity[] entities = toChunk.getEntities();
        MovingData untrackedData = null;
        for (int i = 0; i < entities.length; i++) {
            final Entity entity = entities[i];
            if (entity.getType() != EntityType.PLAYER) {
                continue;
            }
            final Location refLoc = entity.getLocation(useLoc);
            // Exempt world spawn.
            // TODO: Exempt other warps -> HASH based exemption (expire by time, keep high count)?
            if (TrigUtil.isSamePos(loc, refLoc) && (entity instanceof Player)) {
                final ServerPlayer other = (Player) entity;
                final IPlayerData otherPData = DataManager.getPlayerData(other);
                final MovingData otherData = otherPData.getGenericInstance(MovingData.class);
                final ServerPlayerMoveData otherLastMove = otherData.playerMoves.getFirstPastMove();
                if (!otherLastMove.toIsValid) {
                    // Data might have been removed.
                    // TODO: Consider counting as tracked?
                    continue;
                }
                else if (TrigUtil.isSamePos(refLoc, otherLastMove.to.getX(), otherLastMove.to.getY(), otherLastMove.to.getZ())) {
                    // Tracked.
                    return null;
                }
                else {
                    // Untracked location.
                    // TODO: Discard locations in the same block, if passable.
                    // TODO: Sanity check distance?
                    // More leniency: allow moving inside of the same block.
                    if (TrigUtil.isSameBlock(loc, otherLastMove.to.getX(), otherLastMove.to.getY(), otherLastMove.to.getZ()) && !BlockProperties.isPassable(refLoc.getWorld(), otherLastMove.to.getX(), otherLastMove.to.getY(), otherLastMove.to.getZ())) {
                        continue;
                    }
                    untrackedData = otherData;
                }
            }
        }
        useLoc.setWorld(null); // Cleanup.
        if (untrackedData == null) {
            return null;
        }
        else {
            // TODO: Count and log to TRACE_FILE, if multiple locations would match (!).
            final ServerPlayerMoveData lastMove = untrackedData.playerMoves.getFirstPastMove();
            return new Location(loc.getWorld(), lastMove.to.getX(), lastMove.to.getY(), lastMove.to.getZ(), loc.getYaw(), loc.getPitch());
        }
    }


    /**
     * Convenience method for the case that the server has already reset the
     * fall distance, e.g. with micro moves.
     * 
     * @param player
     * @param fromY
     * @param toY
     * @param data
     * @return
     */
    public static double getRealisticFallDistance(final ServerPlayer player, final double fromY, final double toY,
                                                  final MovingData data, final IPlayerData pData) {

        if (pData.isCheckActive(CheckType.MOVING_NOFALL, player)) {
            // (NoFall will not be checked, if this method is called.)
            if (data.noFallMaxY >= fromY ) {
                return Math.max(0.0, data.noFallMaxY - toY);
            } else {
                return Math.max(0.0, fromY - toY); // Skip to avoid exploits: + player.getFallDistance()
            }
        } else {
            // TODO: This would ignore the first split move, if this is the second one.
            return (double) player.getFallDistance() + Math.max(0.0, fromY - toY);
        }
    }


    /**
     * Ensure we have a set back location set, plus allow moving from upwards
     * with respawn/login. Intended for MovingListener (pre-checks).
     * 
     * @param player
     * @param from
     * @param data
     */
    public static void checkSetBack(final ServerPlayer player, final ServerPlayerLocation from,
                                    final MovingData data, final IPlayerData pData, final IDebugPlayer idp) {

        if (!data.hasSetBack()) {
            data.setSetBack(from);
        }
        else if (data.joinOrRespawn && from.getY() > data.getSetBackY() && 
                TrigUtil.isSamePos(from.getX(), from.getZ(), data.getSetBackX(), data.getSetBackZ()) &&
                (from.isOnGround() || from.isResetCond())) {
            // TODO: Move most to a method?
            // TODO: Is a margin needed for from.isOnGround()? [bukkitapionly]
            if (pData.isDebugActive(CheckType.MOVING)) {
                // TODO: Should this be info?
                idp.debug(player, "Adjust set back after join/respawn: " + from.getLocation());
            }
            data.setSetBack(from);
            data.resetPlayerPositions(from);
        }
    }


    public static double getJumpAmplifier(final ServerPlayer player, final MCAccess mcAccess) {
        final double amplifier = mcAccess.getJumpAmplifier(player);
        if (Double.isInfinite(amplifier)) {
            return 0.0;
        }
        else {
            return 1.0 + amplifier;
        }
    }


    public static void prepareFullCheck(final RichBoundsLocation from, final RichBoundsLocation to, final MoveData thisMove, final double yOnGround) {
        // Collect block flags.
        from.collectBlockFlags(yOnGround);
        if (from.isSamePos(to)) {
            // TODO: Could consider pTo = pFrom, set pitch / yaw elsewhere.
            // Sets all properties, but only once.
            to.prepare(from);
        }
        else {
            // Might collect block flags for small distances with the containing bounds for both. 
            to.collectBlockFlags(yOnGround);
        }

        // Set basic properties for past move bookkeeping.
        thisMove.setExtraProperties(from, to);
    }


    /**
     * Ensure nearby chunks are loaded so that the move can be processed at all.
     * Assume too big moves to be cancelled anyway and/or checks like passable
     * accounting for chunk load. Further skip chunk loading if the latest
     * stored past move has extra properties set and is close by.
     * 
     * @param from
     * @param to
     * @param lastMove
     * @param tag
     *            The type of context/event for debug logging.
     * @param data
     * @param cc
     */
    public static void ensureChunksLoaded(final ServerPlayer player,final Location from, final Location to, final ServerPlayerMoveData lastMove,
                                          final String tag, final MovingConfig cc, final IPlayerData pData) {

        // (Worlds must be equal. Ensured in player move handling.)
        final double x0 = from.getX();
        final double z0 = from.getZ();
        final double x1 = to.getX();
        final double z1 = to.getZ();
        if (TrigUtil.distanceSquared(x0, z0, x1, z1) > 2.0 * Magic.CHUNK_LOAD_MARGIN_MIN) {
            // Assume extreme move to trigger.
            return;
        }
        boolean loadFrom = true;
        boolean loadTo = true;
        double margin = Magic.CHUNK_LOAD_MARGIN_MIN;
        // Heuristic for if loading may be necessary at all.
        if (lastMove.toIsValid && lastMove.to.extraPropertiesValid) {
            if (TrigUtil.distanceSquared(lastMove.to, x0, z0) < 1.0) {
                loadFrom = false;
            }
            if (TrigUtil.distanceSquared(lastMove.to, x1, z1) < 1.0) {
                loadTo = false;
            }
        }
        else if (lastMove.valid && lastMove.from.extraPropertiesValid
                && cc.loadChunksOnJoin) {
            // TODO: Might need to distinguish join/teleport/world-change later.
            if (TrigUtil.distanceSquared(lastMove.from, x0, z0) < 1.0) {
                loadFrom = false;
            }
            if (TrigUtil.distanceSquared(lastMove.from, x1, z1) < 1.0) {
                loadTo = false;
            }
        }
        int loaded = 0;
        if (loadFrom) {
            loaded += MapUtil.ensureChunksLoaded(from.getWorld(), x0, z0, margin);
            if (TrigUtil.distanceSquared(x0, z0, x1, z1) < 1.0) {
                loadTo = false;
            }
        }
        if (loadTo) {
            loaded += MapUtil.ensureChunksLoaded(to.getWorld(), x1, z1, margin);
        }
        if (loaded > 0 && pData.isDebugActive(CheckType.MOVING)) {
            StaticLog.logInfo("Player " + tag + ": Loaded " + loaded + " chunk" + (loaded == 1 ? "" : "s") + " for the world " + from.getWorld().getName() +  " for player: " + player.getName());
        }
    }


    /**
     * Ensure nearby chunks are loaded. Further skip chunk loading if the latest
     * stored past move has extra properties set and is close by.
     * 
     * @param player
     * @param loc
     * @param tag
     *            The type of context/event for debug logging.
     * @param data
     * @param cc
     */
    public static void ensureChunksLoaded(final ServerPlayer player, final Location loc, final String tag,
                                          final MovingData data, final MovingConfig cc, final IPlayerData pData) {

        final ServerPlayerMoveData lastMove = data.playerMoves.getFirstPastMove();
        final double x0 = loc.getX();
        final double z0 = loc.getZ();
        // Heuristic for if loading may be necessary at all.
        if (lastMove.toIsValid && lastMove.to.extraPropertiesValid) {
            if (TrigUtil.distanceSquared(lastMove.to, x0, z0) < 1.0) {
                return;
            }
        }
        else if (lastMove.valid && lastMove.from.extraPropertiesValid
                && cc.loadChunksOnJoin) {
            // TODO: Might need to distinguish join/teleport/world-change later.
            if (TrigUtil.distanceSquared(lastMove.from, x0, z0) < 1.0) {
                return;
            }
        }
        int loaded = MapUtil.ensureChunksLoaded(loc.getWorld(), loc.getX(), loc.getZ(), Magic.CHUNK_LOAD_MARGIN_MIN);
        if (loaded > 0 && pData.isDebugActive(CheckType.MOVING)) {
            StaticLog.logInfo("Player " + tag + ": Loaded " + loaded + " chunk" + (loaded == 1 ? "" : "s") + " for the world " + loc.getWorld().getName() +  " for player: " + player.getName());
        }
    }


    /**
     * Test if a set back is set in data and scheduled. <br>
     * Primary thread only.
     * 
     * @param player
     * @return
     */
    public static boolean hasScheduledPlayerSetBack(final ServerPlayer player) {
        return hasScheduledPlayerSetBack(player.getUniqueId(),
                DataManager.getGenericInstance(player, MovingData.class));
    }


    /**
     * Test if a set back is set in data and scheduled. <br>
     * Primary thread only.
     * @param playerId
     * @param data
     * @return
     */
    public static boolean hasScheduledPlayerSetBack(final UUID playerId, final MovingData data) {
        return data.hasTeleported() && isPlayersetBackScheduled(playerId);
    }


    private static boolean isPlayersetBackScheduled(final UUID playerId) {
        final IPlayerData pd = DataManager.getPlayerData(playerId);
        return pd != null  && pd.isPlayerSetBackScheduled();
    }


    /**
     * 
     * @param player
     * @param debugMessagePrefix
     * @return True, if the teleport has been successful.
     */
    public static boolean processStoredSetBack(final ServerPlayer player, final String debugMessagePrefix, final IPlayerData pData) {
        final MovingData data = pData.getGenericInstance(MovingData.class);
        final boolean debug = pData.isDebugActive(CheckType.MOVING);
        if (!data.hasTeleported()) {
            if (debug) {
                CheckUtils.debug(player, CheckType.MOVING, debugMessagePrefix + "No stored location available.");
            }
            return false;
        }
        // (teleported is set.).

        final Location loc = player.getLocation(useLoc);
        if (data.isTeleportedPosition(loc)) {
            // Skip redundant teleport.
            if (debug) {
                CheckUtils.debug(player, CheckType.MOVING, debugMessagePrefix + "Skip teleport, player is there, already.");
            }
            data.resetTeleported(); // Not necessary to keep.
            useLoc.setWorld(null);
            return false;
        }
        useLoc.setWorld(null);
        // (player is somewhere else.)

        // Post-1.9 packet level workaround.
        final MovingConfig cc = pData.getGenericInstance(MovingConfig.class);
        // TODO: Consider to skip checking for packet level, if not available (plus optimize access).
        // TODO: Consider a config flag, so this can be turned off (set back method).
        final ServerPlayerSetBackMethod method = cc.playerSetBackMethod;
        if (!method.shouldNoRisk() 
                && (method.shouldCancel() || method.shouldSetTo()) && method.shouldUpdateFrom()) {
            /*
             * Another leniency option: Skip, if we have already received an ACK
             * for this position on packet level - typically the next move would
             * confirm the set-back, but a redundant teleport would freeze the
             * player for a slightly longer time. This could happen with the set
             * back being at the coordinates the player had just been at, but
             * between set back and on-tick there has been a micro move (not
             * firing a PlayerMoveEvent) - similarly observed on a local test
             * server once, HOWEVER there the micro move had been a look-only
             * packet, not explaining why the position of the player wasn't
             * reflecting the outgoing position. So here remains the uncertainty
             * concerning the question if a (silent) Minecraft entity teleport
             * always follows a cancelled PlayerMoveEvent (!), and a thinkable
             * potential for abuse.
             */
            // (CANCEL + UPDATE_FROM mean a certain teleport to the set back, still could be repeated tp.)
            // TODO: Better method, full sync reference?
            final CountableLocation cl = pData.getGenericInstance(NetData.class).teleportQueue.getLastAck();
            if (data.isTeleportedPosition(cl)) {
                if (debug) {
                    CheckUtils.debug(player, CheckType.MOVING, debugMessagePrefix + "Skip teleport, having received an ACK for the teleport on packet level. Player is at: " + LocUtil.simpleFormat(loc));
                }
                // Keep teleported in data. Subject to debug logs and/or discussion.
                return false;
            }
        }
        // (No ACK received yet.)

        // Attempt to teleport.
        final Location teleported = data.getTeleported();
        // (Data resetting is done during PlayerTeleportEvent handling.)
        if (player.teleport(teleported, BridgeMisc.TELEPORT_CAUSE_CORRECTION_OF_POSITION)) {
            return true;
        }
        else {
            if (debug) {
                CheckUtils.debug(player, CheckType.MOVING, "Player set back on tick: Teleport failed.");
            }
            return false;
        }
    }


    /**
     * Get the applicable set-back location at this moment.
     * <hr>
     * <ul>
     * <li>The idea is that this method call remains side effect free.</li>
     * <li>Because set-back policies may need scanning for ground down to the
     * void, calling this method can have an impact on performance, if called
     * excessively.</li>
     * </ul>
     * 
     * @param player
     * @param refYaw
     * @param refPitch
     * @param from
     *            Safe reference location, for scanning for ground/void.
     *            Typically a move start point. Not used for a return value
     *            directly.
     * @param data
     * @param cc
     * @return The applicable set back location
     */
    public static Location getApplicableSetBackLocation(final ServerPlayer player, final float refYaw, final float refPitch,
                                                        final ServerPlayerLocation from, final MovingData data, final MovingConfig cc) {
        /*
         * TODO: Consider returning a context object (include if to deal fall
         * damage, otherwise / if possible use a utility method checking the
         * config and ground properties).
         */

        // TODO: Implement down-to-ground.
        boolean scanForVoid = false; // If set, scan for void and teleport there (dedo).
        // Void to void.
        if (cc.sfSetBackPolicyVoid) {
            if (from.getY() < 0.0) {
                // Set back into the void.
                // TODO: Assume realistic falling speed somehow?
                return new Location(from.getWorld(),
                        from.getX(), 
                        from.getY() - Magic.GRAVITY_MAX, // Safer than sorry.
                        from.getZ(), 
                        from.getYaw(), from.getPitch());
            }
            else {
                scanForVoid = true;
            }
        }

        if (scanForVoid) { // || scanForGround
            // TODO: Chunk section scanning?
            // TODO: Rather precise scanForGround method for down-to-ground policy.
            final double[] groundSpec = scanForGroundOrResetCond(player, from);
            if (groundSpec == null) {
                // Teleport to void
                return new Location(from.getWorld(),
                        from.getX(), 
                        - 2.0, // Safer than sorry.
                        from.getZ(), 
                        from.getYaw(), from.getPitch());
            }
            // TODO: else if (scanForGround) { // Teleport there, ensure fall damage (MovingListener/override). 
        }

        // Ordinary handling.
        if (data.hasSetBack()) {
            return data.getSetBack(refYaw, refPitch); // (OK)
        }

        // Nothing appropriate found.
        // (If no set back is set, should be checked before the actual check is run.)
        return null;
    }


    /**
     * 
     * @param player
     * @param from
     * @return {rough position of ground or -1.0 if none, maximum error}
     */
    private static final double[] scanForGroundOrResetCond(final ServerPlayer player, final ServerPlayerLocation from) {
        // Re-check for ground - who knows where this will get called from.
        if (from.isOnGroundOrResetCond()) {
            // TODO: + inside blocks?
            return new double[] {0.0, from.getyOnGround()};
        }
        // TODO: Actually scan for ground (BlockProperties).
        // TODO: standsOnEntity is not covered yet (idk flying boats etc).
        final double distToVoid = from.getY();
        if (distToVoid <= 0.0) {
            return null;
        }
        // TODO: Strictness flags and/or 
        // collectFlagsSimple: Just allow air - more safe concerning false positives.
        // collides: Scan for stuff that can be stood on.
        if (BlockProperties.collectFlagsSimple(from.getBlockCache(), 
                from.getMinX(), 0.0, from.getMinZ(), 
                from.getMaxX(), from.getMinY(), from.getMaxZ()) != 0L
                // (... & FLAGS_SCAN_FOR_GROUND_OR_RESETCOND) != 0L // Allow blocks one can't stand on.
                ) {
            //        if (BlockProperties.collides(from.getBlockCache(), 
            //                from.getMinX(), 0.0, from.getMinZ(), 
            //                from.getMaxX(), from.getMinY(), from.getMaxZ(), 
            //                FLAGS_SCAN_FOR_GROUND_OR_RESETCOND)) {
            return new double[] {0.0, distToVoid};
        }
        else {
            return null;
        }
    }
}
