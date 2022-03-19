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

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.util.Vector;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.moving.magic.LostGround;
import fr.neatmonster.nocheatplus.checks.moving.magic.Magic;
import fr.neatmonster.nocheatplus.checks.moving.model.LiftOffEnvelope;
import fr.neatmonster.nocheatplus.checks.moving.model.ModelFlying;
import fr.neatmonster.nocheatplus.checks.moving.model.PlayerMoveData;
import fr.neatmonster.nocheatplus.checks.moving.util.MovingUtil;
import fr.neatmonster.nocheatplus.checks.moving.velocity.AccountEntry;
import fr.neatmonster.nocheatplus.checks.moving.velocity.SimpleEntry;
import fr.neatmonster.nocheatplus.compat.Bridge1_13;
import fr.neatmonster.nocheatplus.compat.Bridge1_9;
import fr.neatmonster.nocheatplus.compat.BridgeEnchant;
import fr.neatmonster.nocheatplus.compat.BridgeMisc;
import fr.neatmonster.nocheatplus.compat.blocks.changetracker.BlockChangeTracker;
import fr.neatmonster.nocheatplus.components.modifier.IAttributeAccess;
import fr.neatmonster.nocheatplus.components.registry.event.IGenericInstanceHandle;
import fr.neatmonster.nocheatplus.players.IPlayerData;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.location.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.location.TrigUtil;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties;


/**
 * A check designed for people that are exposed to particular effects, flying
 * or gliding. The complement to the "SurvivalFly" check, which is for ordinary gameplay.
 */
public class CreativeFly extends Check {

    private final List<String> tags = new LinkedList<String>();
    private final BlockChangeTracker blockChangeTracker;
    private IGenericInstanceHandle<IAttributeAccess> attributeAccess = NCPAPIProvider.getNoCheatPlusAPI().getGenericInstanceHandle(IAttributeAccess.class);


   /**
    * Instantiates a new creative fly check.
    */
    public CreativeFly() {
        super(CheckType.MOVING_CREATIVEFLY);
        blockChangeTracker = NCPAPIProvider.getNoCheatPlusAPI().getBlockChangeTracker();
    }


   /**
    * Checks a player
    *
    * @param player
    * @param from
    * @param to
    * @param data
    * @param cc
    * @param time Milliseconds.
    * @return
    */
    public Location check(final ServerPlayer player, final ServerPlayerLocation from, final ServerPlayerLocation to, 
                          final MovingData data, final MovingConfig cc, final IPlayerData pData,
                          final long time, final int tick,
                          final boolean useBlockChangeTracker) {

        // Reset tags, just in case.
        tags.clear();
        // TODO: Other set back policy for elytra, e.g. not set in narrow spaces?
        final boolean debug = pData.isDebugActive(type);
        final GameMode gameMode = player.getGameMode();
        final ServerPlayerMoveData thisMove = data.playerMoves.getCurrentMove();
        final ServerPlayerMoveData lastMove = data.playerMoves.getFirstPastMove();
        final ModelFlying model = thisMove.modelFlying;
        final double yDistance = thisMove.yDistance;
        final double hDistance = thisMove.hDistance;
        final boolean flying = gameMode == BridgeMisc.GAME_MODE_SPECTATOR || player.isFlying();
        final boolean sprinting = time <= data.timeSprinting + cc.sprintingGrace;
        final long now = System.currentTimeMillis();
        boolean lostGround = false;

        // Lost ground, if set so.
        if (model.getGround()) {
            MovingUtil.prepareFullCheck(from, to, thisMove, Math.max(cc.yOnGround, cc.noFallyOnGround));
            if (!thisMove.from.onGroundOrResetCond) {
                if (from.isSamePos(to)) {
                    if (lastMove.toIsValid && lastMove.hDistance > 0.0 && lastMove.yDistance < -0.3 // Copy and paste from sf.
                        && LostGround.lostGroundStill(player, from, to, hDistance, yDistance, sprinting, lastMove, data, cc, tags)) {
                        lostGround = true;
                    }
                }
                else if (LostGround.lostGround(player, from, to, hDistance, yDistance, sprinting, lastMove, 
                                               data, cc, useBlockChangeTracker ? blockChangeTracker : null, tags)) {
                    lostGround = true;
                }
            }
        }

        // Do not check for nofall if the player has slowfalling active or is gliding
        if (Bridge1_13.hasSlowfalling() && model.getScaleSlowfallingEffect() 
            || Bridge1_9.isGlidingWithElytra(player) && thisMove.yDistance > -0.5) {
            data.clearNoFallData();
        } 
        
        // HACK: when switching model, we need to add some velocity to harmonize the transition and not trggering fps.
        workaroundSwitchingModel(player, thisMove, lastMove, model, data, cc, debug);






        //////////////////////////
        // Horizontal move.
        //////////////////////////

        double[] resH = hDist(player, from, to, hDistance, yDistance, sprinting, flying, thisMove, lastMove, time, model, data, cc);
        double limitH = resH[0];
        double resultH = resH[1];
        double[] rese = hackElytraH(player, from, to, hDistance, yDistance, thisMove, lastMove, lostGround, data, cc, debug); // Related to the elytra
        resultH = Math.max(resultH, rese[1]);

        // Check velocity.
        if (resultH > 0) {
            double hFreedom = data.getHorizontalFreedom();
            if (hFreedom < resultH) {
                // Use queued velocity if possible.
                hFreedom += data.useHorizontalVelocity(resultH - hFreedom);
            }
            if (hFreedom > 0.0) {
                resultH = Math.max(0.0, resultH - hFreedom);
                if (resultH <= 0.0) {
                    limitH = hDistance;
                }
                tags.add("hvel");
            }
        }
        else {
            data.clearActiveHorVel(); // TODO: test/check !
        }

        resultH *= 100.0; // Normalize to % of a block.
        if (resultH > 0.0) {
            tags.add("hdist");
        }






        //////////////////////////
        // Vertical move.
        //////////////////////////

        double limitV = 0.0; // Limit. For debug only, violation handle on resultV
        double resultV = rese[0]; // Violation (normalized to 100 * 1 block, applies if > 0.0).

        // Distinguish checking method by y-direction of the move:
        // Ascend.
        if (yDistance > 0.0) {
            double[] res = vDistAscend(from, to, yDistance, flying, thisMove, lastMove, model, data, cc, debug);
            resultV = Math.max(resultV, res[1]);
            limitV = res[0];
        }
        // Descend.
        else if (yDistance < 0.0) {
            double[] res = vDistDescend(from, to, yDistance, flying, thisMove, lastMove, model, data, cc);
            resultV = Math.max(resultV, res[1]);
            limitV = res[0];
        }
        // Keep altitude.
        else {
            double[] res = vDistZero(from, to, yDistance, flying, thisMove, lastMove, model, data, cc);
            resultV = Math.max(resultV, res[1]);
            limitV = res[0];
        }

        // Velocity.
        if (resultV > 0.0 && (thisMove.verVelUsed != null || data.getOrUseVerticalVelocity(yDistance) != null)) {
            resultV = 0.0;
            tags.add("vvel");
        }
        
        // The antilevitation subcheck
        if (lastMove.toIsValid && !player.isFlying() && model.getScaleLevitationEffect()
            && thisMove.modelFlying == lastMove.modelFlying) { // InLiquid check is alread included in MovingConfig.getModelFlying()

            final double level = Bridge1_9.getLevitationAmplifier(player) + 1;
            final double allowY = (lastMove.yDistance + (0.05D * level - lastMove.yDistance) * 0.2D) * Magic.FRICTION_MEDIUM_AIR;
            // TODO: Wrong friction
            if (allowY * 1.001 >= yDistance) resultV = 0.0;

            if (!from.isHeadObstructed() && !to.isHeadObstructed()
                // Exempt check for 20 seconds after joined
                && !(now > pData.getLastJoinTime() && pData.getLastJoinTime() + 20000 > now)
                && !(thisMove.yDistance < 0.0 && lastMove.yDistance - thisMove.yDistance < 0.0001)
                ) {
 
                if (lastMove.yDistance < 0.0 && thisMove.yDistance < allowY
                    || from.getY() >= to.getY() && !(thisMove.yDistance == 0.0 && allowY < 0.0)
                    ) {
                    resultV = Math.max(resultV, 0.1);
                    tags.add("antilevitate");

                    if (data.getOrUseVerticalVelocity(getBaseV(0.0, yDistance, 0f, 0.0, level, 0.0, false)) != null) {
                        data.addVerticalVelocity(new SimpleEntry(yDistance, 2));
                        resultV = 0.0;
                    }
                }
            }
        }

        // Add tag for maximum height check (silent set back).
        // TODO: Allow use velocity there (would need a flag to signal the actual check below)?
        final double maximumHeight = model.getMaxHeight() + player.getWorld().getMaxHeight();
        if (to.getY() > maximumHeight) {
            tags.add("maxheight");
        }

        resultV *= 100.0; // Normalize to % of a block.
        if (resultV > 0.0) {
            tags.add("vdist");
        }

        final double result = Math.max(0.0, resultH) + Math.max(0.0, resultV);



        //////////////////////////
        // Output debug
        //////////////////////////
        if (debug) {
            outpuDebugMove(player, hDistance, limitH, yDistance, limitV, model, tags, data);
        }



        ///////////////////////
        // Violation handling
        ///////////////////////

        Location setBack = null; // Might get altered below.

        if (result > 0.0) {
            data.creativeFlyVL += result;
            // Execute whatever actions are associated with this check and the violation level and find out if we
            // should cancel the event.
            final ViolationData vd = new ViolationData(this, player, data.creativeFlyVL, result, cc.creativeFlyActions);
            if (vd.needsParameters()) {
                vd.setParameter(ParameterName.LOCATION_FROM, String.format(Locale.US, "%.2f, %.2f, %.2f", from.getX(), from.getY(), from.getZ()));
                vd.setParameter(ParameterName.LOCATION_TO, String.format(Locale.US, "%.2f, %.2f, %.2f", to.getX(), to.getY(), to.getZ()));
                vd.setParameter(ParameterName.DISTANCE, String.format(Locale.US, "%.2f", TrigUtil.distance(from,  to)));
                if (model != null) {
                    vd.setParameter(ParameterName.MODEL, model.getId().toString());
                }
                if (!tags.isEmpty()) {
                    vd.setParameter(ParameterName.TAGS, StringUtil.join(tags, "+"));
                }
            }
            if (executeActions(vd).willCancel()) {
                // Compose a new location based on coordinates of "newTo" and viewing direction of "event.getTo()"
                // to allow the player to look somewhere else despite getting pulled back by NoCheatPlus.
                setBack = data.getSetBack(to); // (OK)
            }
        }
        else {
            // Maximum height check (silent set back).
            if (to.getY() > maximumHeight) {
                setBack = data.getSetBack(to); // (OK)
                if (debug) {
                    debug(player, "Maximum height exceeded, silent set-back.");
                }
            }
            if (setBack == null) {
                // Slowly reduce the violation level with each event.
                data.creativeFlyVL *= 0.97;
            }
        }

        // Return setBack, if set.
        if (setBack != null) {
            // Check for max height of the set back.
            if (setBack.getY() > maximumHeight) {
                // Correct the y position.
                setBack.setY(getCorrectedHeight(maximumHeight, setBack.getWorld()));
                if (debug) {
                    debug(player, "Maximum height exceeded by set back, correct to: " + setBack.getY());
                }
            }
            data.sfJumpPhase = 0;
            return setBack;
        }
        else {
            // Adjust the set back and other last distances.
            data.setSetBack(to);
            // Adjust jump phase.
            if (!thisMove.from.onGroundOrResetCond && !thisMove.to.onGroundOrResetCond) {
                data.sfJumpPhase ++;
            }
            else if (thisMove.touchedGround && !thisMove.to.onGroundOrResetCond) {
                data.sfJumpPhase = 1;
            }
            else {
                data.sfJumpPhase = 0;
            }
            return null;
        }
    }


    /**
     * Horizontal distance checking.
     * @param player
     * @param from
     * @param to
     * @param hDistance
     * @param yDistance
     * @param flying
     * @param lastMove
     * @param time
     * @param model
     * @param data
     * @param cc
     * @return limitH, resultH (not normalized).
     */
    private double[] hDist(final ServerPlayer player, final ServerPlayerLocation from, final ServerPlayerLocation to, final double hDistance, 
                           final double yDistance, final boolean sprinting, final boolean flying, final ServerPlayerMoveData thisMove, 
                           final ServerPlayerMoveData lastMove, final long time, final ModelFlying model, final MovingData data, final MovingConfig cc) {

        // Modifiers.
        double fSpeed;
        final boolean ripglide = Bridge1_13.isRiptiding(player) && Bridge1_9.isGlidingWithElytra(player);

        // TODO: Make this configurable ! [Speed effect should not affect flying if not on ground.]
        if (model.getApplyModifiers()) {
            final double speedModifier = mcAccess.getHandle().getFasterMovementAmplifier(player);
            if (Double.isInfinite(speedModifier)) fSpeed = 1.0;
            else fSpeed = 1.0 + 0.2 * (speedModifier + 1.0);
    
            if (flying) {
                // TODO: Consider mechanics for flying backwards.
                fSpeed *= data.flySpeed / Magic.DEFAULT_FLYSPEED;
                if (sprinting) {
                    // TODO: Prevent for pre-1.8?
                    fSpeed *= model.getHorizontalModSprint();
                    tags.add("sprint");
                }
                tags.add("flying");
            }
            else {
                // (Ignore sprinting here).
                final double attrMod = attributeAccess.getHandle().getSpeedAttributeMultiplier(player);
                if (attrMod != Double.MAX_VALUE) fSpeed *= attrMod;
                fSpeed *= data.walkSpeed / Magic.DEFAULT_WALKSPEED;
            }
        }
        else fSpeed = 1.0;
        
        // The horizontal limit is now set. 
        double limitH = model.getHorizontalModSpeed() / 100.0 * ModelFlying.HORIZONTAL_SPEED * fSpeed;
        
        // Do apply dolphinsgrace modifier
        if (from.isInWater() || to.isInWater()) {
            if (!Double.isInfinite(Bridge1_13.getDolphinGraceAmplifier(player))) {
                limitH *= Magic.modDolphinsGrace;
                tags.add("hdolphinsgrace");
            }
        }

        // Moving on stairs with creativefly
        if (Bridge1_9.hasElytra() && from.isAboveStairs() && to.isAboveStairs()) {
            limitH = Math.max(limitH, 0.7 * fSpeed);
        }

        // "Ripglide" (riptiding+gliding phase): allow some additional speed increase
        // Note that the ExtremeMove subcheck is skipped during such phases.
        // TODO: Why not simply skip CreativeFly and let ExtremeMove deal with it? 
        if (lastMove.toIsValid && ripglide && hDistance > limitH) {
            limitH += 9.3;
            tags.add("hripglide");
        }
        
        // Special friction mechanic for levitation
        if (lastMove.toIsValid && model.getScaleLevitationEffect() 
            && (lastMove.hDistance + 0.005) * Magic.FRICTION_MEDIUM_AIR < lastMove.hDistance) {
            limitH = Math.max((lastMove.hDistance + 0.005) * Magic.FRICTION_MEDIUM_AIR, limitH);
            tags.add("hfrict_lev");
        }
        
        // Special friction mechanic for riptiding 
        // Observed: extreme/abrupt acceleration from the last hDistance: 
        // one time hDistance around 3.01 with friction distance being at or slightly lower than last hDistance (0.51/0.52)
        if (lastMove.toIsValid && model.getScaleRiptidingEffect() 
            && lastMove.hDistance * Magic.FRICTION_MEDIUM_AIR <= lastMove.hDistance
            && thisMove.hDistance > 3.0 && thisMove.hDistance < 3.9
            && Bridge1_13.isRiptiding(player) && hDistance > limitH) {
            limitH = Math.max((lastMove.hDistance + 2.9974) * Magic.FRICTION_MEDIUM_AIR, limitH);
            tags.add("hfrict_ript");
        }

        // Ordinary friction
        // TODO: Use last friction (as well)?
        // TODO: Test/adjust more.
        // TODO: Skipping on ripglide phases is not ideal, but at the same time, the speed increase is so much that
        // it doesn't really make much sense checking for friction as well...
        if (lastMove.toIsValid && !ripglide) {
            double frictionDist = lastMove.hDistance * Magic.FRICTION_MEDIUM_AIR;
            limitH = Math.max(frictionDist, limitH);
            tags.add("hfrict");
        }

        // Finally, determine how far the player went beyond the set limits.
        double resultH = Math.max(0.0, hDistance - limitH);

        if (model.getApplyModifiers()) {
            data.bunnyhopDelay--;
            if (!flying && resultH > 0 && resultH < 0.3) {
                // 0: yDistance envelope
                if (yDistance >= 0.0 &&
                    (
                        // 1: Normal jumping.
                        yDistance > 0.0 
                        && yDistance > LiftOffEnvelope.NORMAL.getMinJumpGain(data.jumpAmplifier) - Magic.GRAVITY_SPAN
                        // 1: Too short with head obstructed.
                        || thisMove.headObstructed || lastMove.toIsValid && lastMove.headObstructed && lastMove.yDistance <= 0.0
                        // 1: Hop without y distance increase at moderate h-speed.
                        // TODO: 2nd below: demand next move to jump. Relate to stored past moves. 
                        // TODO: Ensure the gain can only be used once per so and so.
                        //|| (cc.sfGroundHop || yDistance == 0.0 && !lastMove.touchedGroundWorkaround && !lastMove.from.onGround)
                        //&& limitH > 0.0 && hDistance / limitH < 1.5
                        //&& (hDistance / lastMove.hDistance < 1.35 
                        //        || hDistance / limitH < 1.35)
                    )
                    // 0: Ground + jump phase conditions.
                    && (data.sfJumpPhase <= 1 && (thisMove.touchedGroundWorkaround || 
                        lastMove.touchedGround && !lastMove.bunnyHop))
                    // 0: Don't allow bunny to run out of liquid.
                    && (!from.isResetCond() && !to.isResetCond()) // TODO: !to.isResetCond() should be reviewed.
                    ) {

                    tags.add("bunnyhop");
                    data.bunnyhopDelay = 9;
                    thisMove.bunnyHop = true;
                    resultH = 0.0;
                }
                else if (data.bunnyhopDelay <= 0) {
                    resultH = 0.0;
                    tags.add("bunnyhop");
                }
            }
        }
        return new double[] {limitH, resultH};
    }


   /**
     * Ascending (yDistance > 0.0) check.
     * @param from
     * @param to
     * @param yDistance
     * @param flying
     * @param lastMove
     * @param model
     * @param data
     * @param cc
     * @return limitV, resultV (not normalized).
     */
    private double[] vDistAscend(final ServerPlayerLocation from, final ServerPlayerLocation to, final double yDistance, 
                                 final boolean flying, final ServerPlayerMoveData thisMove, final ServerPlayerMoveData lastMove, 
                                 final ModelFlying model, final MovingData data, final MovingConfig cc, final boolean debug) {

        final boolean ripglide = Bridge1_13.isRiptiding(from.getPlayer()) && Bridge1_9.isGlidingWithElytra(from.getPlayer());
        final long now = System.currentTimeMillis();
        // Set the vertical limit.
        double limitV = model.getVerticalAscendModSpeed() / 100.0 * ModelFlying.VERTICAL_ASCEND_SPEED; 
        double resultV = 0.0;
        
        // Let fly speed apply with moving upwards.
        if (model.getApplyModifiers() && flying && yDistance > 0.0) {
            limitV *= data.flySpeed / Magic.DEFAULT_FLYSPEED;
        }
        else if (model.getScaleLevitationEffect() && Bridge1_9.hasLevitation()) {
            // Exclude modifiers for now.
            final double levitation = Bridge1_9.getLevitationAmplifier(from.getPlayer());
            if (levitation >= 0.0) {
                // (Double checked.)
                // TODO: Perhaps do with a modifier instead, to avoid confusion.
                limitV += 0.046 * levitation; // (It ends up like 0.5 added extra for some levels of levitation, roughly.)
                final double minJumpGain = LiftOffEnvelope.NORMAL.getMinJumpGain(data.jumpAmplifier) + 0.01;
                // Bug, duplicate motion
                if (yDistance > 0.0 && yDistance < minJumpGain && thisMove.touchedGround) {
                    limitV = minJumpGain;
                    data.addVerticalVelocity(new SimpleEntry(yDistance, 2));
                }
                tags.add("levitation:" + levitation);
            }
        }

        // Related to elytra.
        // TODO: Better detection of an elytra model (extra flags?).
        if (model.getVerticalAscendGliding()) {
            limitV = Math.max(limitV, limitV = hackLytra(yDistance, limitV, thisMove, lastMove, from, data));
        }

        // "Ripglide" (riptiding+gliding phase): allow some additional speed increase
        // Note that the ExtremeMove subcheck is skipped during such phases.
        // TODO: Why not simply skip CreativeFly and let ExtremeMove deal with it? 
        if (lastMove.toIsValid && ripglide && yDistance > limitV) {
            limitV += 5.9;
            tags.add("vripglide");
        }
        
        // Riptiding right onto a bouncy block (2nd time, higher bounce distance)
        // Note that the ExtremeMove subcheck is skipped during such phases.
        if (Bridge1_13.isRiptiding(from.getPlayer()) && (from.getBlockFlags() & BlockProperties.F_BOUNCE25) != 0
            && yDistance > limitV && data.sfJumpPhase <= 2
            && yDistance > 0.0 && yDistance < 7.5  // Cap the distance: observed maximum speed -> 5.536355205897621 (+5.993) / 5.0
            && thisMove.from.onGround && !thisMove.to.onGround) {
            data.addVerticalVelocity(new SimpleEntry(yDistance, 4));
            if (debug) debug(from.getPlayer(), "Riptide bounce: add velocity");
        }
        
        // Gliding in water
        if (Bridge1_9.isGlidingWithElytra(from.getPlayer()) && data.liqtick > 1) {
            limitV = Math.max(limitV, 0.35);
        }
        
        // Friction with gravity.
        if (model.getGravity()) {
            if (yDistance > limitV && lastMove.toIsValid) { 
                // (Disregard gravity.)
                // TODO: Use last friction (as well)?
                double frictionDist = lastMove.yDistance * Magic.FRICTION_MEDIUM_AIR;
                if (!flying) {
                    frictionDist -= 0.019;
                }
                if (frictionDist > limitV) {
                    limitV = frictionDist;
                    tags.add("vfrict_g");
                }
            }
        }

        if (model.getGround()) {
            // Jump lift off gain.
            // NOTE: This assumes SurvivalFly busies about moves with from.onGroundOrResetCond.
            if (yDistance > limitV && !thisMove.to.onGroundOrResetCond && !thisMove.from.onGroundOrResetCond && (
                // Last move touched ground.
                lastMove.toIsValid && lastMove.touchedGround && 
                (lastMove.yDistance <= 0.0 || lastMove.to.extraPropertiesValid && lastMove.to.onGround)
                // This move touched ground by a workaround.
                || thisMove.touchedGroundWorkaround
                )) {
                // Allow normal jumping.
                final double maxGain = LiftOffEnvelope.NORMAL.getMaxJumpGain(data.jumpAmplifier);
                if (maxGain > limitV) {
                    limitV = maxGain;
                    tags.add("jump_gain");
                }
            }
        }

        // Ordinary step up.
        // TODO: Might be within a 'if (model.ground)' block?
        // TODO: sfStepHeight should be a common modeling parameter?
        if (yDistance > limitV && yDistance <= cc.sfStepHeight 
            && (lastMove.toIsValid && lastMove.yDistance < 0.0 || from.isOnGroundOrResetCond() || thisMove.touchedGroundWorkaround)
            && to.isOnGround()) {
            // (Jump effect not checked yet.)
            limitV = cc.sfStepHeight;
            tags.add("step_up");
        }

        // Determine violation amount.
        resultV = Math.max(0.0, yDistance - limitV);
        // Post-violation recovery.
        return new double[] {limitV, resultV};
    }


    /**
     * Elytra gliding model
     * @param from
     * @param to
     * @param hDistance
     * @param yDistance
     * @param thisMove
     * @param lastMove
     * @param lostGround
     * @param data
     * @param player
     * @return resultH, resultV.
     *
     * @author xaw3ep
     */
    private double[] hackElytraH(final ServerPlayer player, final ServerPlayerLocation from, final ServerPlayerLocation to, final double hDistance, 
                                 final double yDistance, final ServerPlayerMoveData thisMove, final ServerPlayerMoveData lastMove, 
                                 final boolean lostGround, final MovingData data, final MovingConfig cc, final boolean debug) {

        /* Known false positives:
         * Still have setback with taking off ?
         * Fly out water with low envelope
         * Head obstructed ?
         */
        final long now = System.currentTimeMillis();
        double resultV = 0.0;
        double resultH = 0.0;
        if (!cc.elytraStrict || !Bridge1_9.isGlidingWithElytra(player) || player.isFlying() || data.timeRiptiding + 2500 > now) return new double[] {0.0, 0.0};
        double allowedElytraHDistance = 0.0;
        double allowedElytraYDistance = 0.0;
        double baseV = 0.0;
        final double speed = Bridge1_13.getSlowfallingAmplifier(player) >= 0.0 ? 0.01 : 0.08;

        if ((lastMove.flyCheck != thisMove.flyCheck || lastMove.modelFlying != thisMove.modelFlying) && !lastMove.elytrafly) {
            //data.sfJumpPhase = 0;
            tags.add("e_pre");
        } 
        else if (!from.isResetCond() && !isCollideWithHB(from)) {
            thisMove.elytrafly = true;

            // Bug, strange 1.17 behavior, start using firework, ending riptide trident?, hit wall?
            if (lastMove.toIsValid && lastMove.elytrafly && speed > 0.05 &&
                !TrigUtil.isSamePos(lastMove.from, lastMove.to) &&
                TrigUtil.isSamePos(from, to)) {
                allowedElytraHDistance = allowedElytraYDistance = 0.0;
                // Next move still use info from second past move not this one
                thisMove.hAllowedDistance = lastMove.hDistance;
                thisMove.yAllowedDistance = lastMove.yAllowedDistance;
                thisMove.elytraNoMove = true;
                if (debug) debug(player, "Elytra no move. Skipping!");
                return new double[] {0.0, 0.0};
            }

            final double lastHdist = lastMove.toIsValid ? lastMove.elytrafly && lastMove.elytraNoMove ? lastMove.hAllowedDistance : lastMove.hDistance : 0.0;
            final Vector lookvec = to.getLocation().getDirection();
            final float radPitch = (float) Math.toRadians(to.getPitch());
            allowedElytraYDistance = lastMove.elytrafly ? lastMove.yAllowedDistance : lastMove.toIsValid ? lastMove.yDistance : 0.0;
            if (Math.abs(allowedElytraYDistance) < 0.003D) allowedElytraYDistance = 0.0D;
            final double xzlength = Math.sqrt(lookvec.getX() * lookvec.getX() + lookvec.getZ() * lookvec.getZ());
            double squaredCos = Math.cos(radPitch); squaredCos = squaredCos * squaredCos;

            baseV = getBaseV(hDistance, yDistance, radPitch, squaredCos, -1.0, speed, to.getPitch() == -90f);

            allowedElytraYDistance += speed * (-1.0D + squaredCos * 0.75D);
            double x = lastMove.to.getX() - lastMove.from.getX();
            double z = lastMove.to.getZ() - lastMove.from.getZ();
            if (lastMove.elytrafly && lastMove.elytraNoMove) {
                final ServerPlayerMoveData secondPastMove = data.playerMoves.getSecondPastMove();
                x = secondPastMove.to.getX() - secondPastMove.from.getX();
                z = secondPastMove.to.getZ() - secondPastMove.from.getZ();
            }
            if (Math.abs(x) < 0.003D) x = 0.0D;
            if (Math.abs(z) < 0.003D) z = 0.0D;

            if (allowedElytraYDistance < 0.0D && xzlength > 0.0) {
                final double d = allowedElytraYDistance * -0.1 * squaredCos;
                x += lookvec.getX() * d / xzlength;
                z += lookvec.getZ() * d / xzlength;
                allowedElytraYDistance += d;
            }

            // Look up
            if (radPitch < 0.0F) {
                // For compatibility
                if (to.getPitch() == -90f
                    && isNear(yDistance, allowedElytraYDistance * Magic.FRICTION_MEDIUM_ELYTRA_AIR, 0.01)) {
                    allowedElytraHDistance += 0.01;
                    if (debug) debug(player, "Add the distance to allowed on look up (hDist/Allowed): " + hDistance +"/"+ allowedElytraHDistance);
                }
                else if (xzlength > 0.0) {
                   final double d = lastHdist * -Math.sin(radPitch) * 0.04;
                   x -= lookvec.getX() * d / xzlength;
                   z -= lookvec.getZ() * d / xzlength;
                   allowedElytraYDistance += d * 3.2;
                }
            }

            if (xzlength > 0.0) {
                x += (lookvec.getX() / xzlength * lastHdist - x) * 0.1D;
                z += (lookvec.getZ() / xzlength * lastHdist - z) * 0.1D;
            }
            
            // Friction
            allowedElytraYDistance *= Magic.FRICTION_MEDIUM_ELYTRA_AIR;

            // Fireworks
            // Can't be more precise due to some problems, still have ~10% faster bypasses :(
            if (data.fireworksBoostDuration > 0) {
                // Handled somewhere else
                // TODO: More strict vertical check
                thisMove.yAllowedDistance = allowedElytraYDistance = yDistance;
                if (Math.round(data.fireworksBoostTickNeedCheck / 4) > data.fireworksBoostDuration
                    && hDistance < Math.sqrt(x*x + z*z)) {
                    thisMove.hAllowedDistance = Math.sqrt(x*x + z*z);
                    if (debug) debug(player, "Set hAllowedDistance for this firework boost phase (hDist/Allowed): " + thisMove.hDistance + "/" + thisMove.hAllowedDistance);
                    return new double[] {0.0, 0.0};
                }
                x *= 0.99;
                z *= 0.99;
                x += lookvec.getX() * 0.1D + (lookvec.getX() * 1.5D - x) * 0.5D;
                z += lookvec.getZ() * 0.1D + (lookvec.getZ() * 1.5D - z) * 0.5D;
                tags.add("fw_speed");
                /* Problem with calculating fireworks duration and it end sooner,
                 * speed after boost might be faster because fw speed lim < actual speed lim without boosting.
                 */
                if (hDistance < lastMove.hAllowedDistance * 0.994) {
                    thisMove.hAllowedDistance = lastMove.hAllowedDistance * 0.994;
                    if (debug) debug(player, "Firework boost phase has ended sooner than expected, but the player is still legitimately boosting (hDist/Allowed): " + thisMove.hDistance + "/" + thisMove.hAllowedDistance);
                    return new double[] {0.0, 0.0};
                }
                else allowedElytraHDistance += 0.2;
            }

            // Adjust false
            allowedElytraHDistance += Math.sqrt(x*x + z*z) + 0.1;
            if (debug) {
                debug(player, "Cumulative elytra hDistance (hDist/Allowed): " + hDistance + "/" + allowedElytraHDistance + " lasthDist:" + lastHdist);
                debug(player, "radiansPitch: " + radPitch + " yDist:" + yDistance + " lastyDist:" + lastMove.yDistance + " allowy:" + allowedElytraYDistance);
            }
            // Difference from vAllowedDistance to yDistance.
            final double yDistDiffEx = yDistance - allowedElytraYDistance;

            if (data.fireworksBoostDuration <= 0) {

                // Workaround, elytra jump 
                if (yDistance > 0.0 && yDistance < 0.42 && thisMove.touchedGround) {
                    allowedElytraYDistance = yDistance;
                    allowedElytraHDistance = Math.max(0.35, allowedElytraHDistance * 1.35);
                    if (debug) debug(player, "Elytra jump (hDist/Allowed): " + thisMove.hDistance +"/"+ allowedElytraHDistance);
                } 
                // Head is obstructed
                else if (from.isHeadObstructed() && lastMove.yDistance > 0.0 && yDistDiffEx < 0.0 && (allowedElytraYDistance > 0.0 || yDistance == 0.0)) {
                    allowedElytraYDistance = yDistance;
                } 
                else if (yDistance < 0.0) {

                    if (
                        // Pos -> neg
                        lastMove.yDistance > 0.0 && yDistance < 0.0 
                        && (
                           lastMove.yDistance < Magic.GRAVITY_MAX + Magic.GRAVITY_MIN && yDistance > - Magic.GRAVITY_MIN
                           || lastMove.yDistance < Magic.GRAVITY_MIN && yDistance > - Magic.GRAVITY_MIN - Magic.GRAVITY_MAX
                        )
                        // For compatibility
                        //|| data.sfJumpPhase < 6 && lastMove.yDistance > yDistance && yDistance - allowedElytraYDistance < 0.0 && yDistance - allowedElytraYDistance > -Magic.GRAVITY_MAX
                        ) {
                        allowedElytraYDistance = yDistance;
                    }
                }

                if (yDistance > 0.0) {
                    if (allowedElytraYDistance < yDistance && !isNear(allowedElytraYDistance, yDistance, 0.001)) {
                        tags.add("e_vasc");
                        resultV = yDistance;
                    }
                } 
                else if (yDistance < 0.0) {
                    if (allowedElytraYDistance > yDistance && !isNear(allowedElytraYDistance, yDistance, Magic.GRAVITY_MAX)) {
                        tags.add("e_vdesc");
                        resultV = Math.abs(yDistance);
                    }
                } 
                else {
                    // TODO: ....
                    //tags.add("e_vzero");
                }

                if (
                    // Touch ground
                    yDistance <= 0.0 && (to.isOnGround() || to.isResetCond() || thisMove.touchedGround)
                    // Less envelope
                    || yDistDiffEx > -Magic.GRAVITY_MAX && yDistDiffEx < 0.0
                    // Slow Falling no move
                    || speed < 0.05 && !TrigUtil.isSamePos(lastMove.from, lastMove.to) && (hDistance == 0.0 && yDistance == 0.0 || yDistance < -Magic.GRAVITY_SPAN)
                    ) {
                    allowedElytraYDistance = yDistance;
                } 
                // TODO: Better
                else if (Math.abs(yDistDiffEx) > (speed < 0.05 ? 0.00001 : 0.03)) {
                    tags.add("e_vdiff");
                    //if (resultV <= 0.0 && yDistDiffEx > 0.0) {
                    //    Location newto = to.getLocation().clone().subtract(0.0, Math.max(yDistDiffEx, 0.5), 0.0);
                    //    final ServerPlayerMoveInfo moveInfo = auxMoving.usePlayerMoveInfo();
                    //    moveInfo.set(player, newto, null, cc.yOnGround);
                    //    moveInfo.from.collectBlockFlags();
                    //    if (moveInfo.from.isPassableBox()) data.setSetBack(moveInfo.from);
                    //    auxMoving.returnPlayerMoveInfo(moveInfo);
                    //}
                    resultV = Math.max(Math.abs(yDistance - allowedElytraYDistance), resultV);
                }
            }

            if (allowedElytraHDistance < hDistance) {
                tags.add("e_hspeed");
                resultH = hDistance - allowedElytraHDistance;
            }
        } 
        // Gliding in water
        // TODO: Add vertical check
        else if(from.isInLiquid()) {

            if (Bridge1_13.isRiptiding(player)) return new double[] {0.0, 0.0};
            allowedElytraHDistance = thisMove.walkSpeed * cc.survivalFlyWalkingSpeed / 100D;
            final int level = BridgeEnchant.getDepthStriderLevel(player);
            
            if (!Double.isInfinite(Bridge1_13.getDolphinGraceAmplifier(player))) {
                allowedElytraHDistance *= Magic.modDolphinsGrace;
                if (level > 0) allowedElytraHDistance *= 1.0 + 0.1 * level;
            }

            if (level > 0) {
                allowedElytraHDistance *= Magic.modDepthStrider[level];
                final double attrMod = attributeAccess.getHandle().getSpeedAttributeMultiplier(player);
                if (attrMod == Double.MAX_VALUE) {
                    final double speedAmplifier = mcAccess.getHandle().getFasterMovementAmplifier(player);
                    if (!Double.isInfinite(speedAmplifier)) {
                        allowedElytraHDistance *= 1.0D + 0.2D * (speedAmplifier + 1);
                    }
                } 
                else allowedElytraHDistance *= attrMod;
            }

            if (lastMove.toIsValid && data.liqtick < 3 && hDistance < lastMove.hAllowedDistance + 0.07) {
                allowedElytraHDistance = lastMove.hAllowedDistance + 0.07;
            }

            // Firework maximum speed
            if (data.fireworksBoostDuration > 0) allowedElytraHDistance = Math.max(allowedElytraHDistance, 1.8);

            // Friction
            if (hDistance < lastMove.hAllowedDistance * (data.liqtick < 5 ? 1.0 : Magic.FRICTION_MEDIUM_WATER)) {
                allowedElytraHDistance = lastMove.hAllowedDistance * (data.liqtick < 5 ? 1.0 : Magic.FRICTION_MEDIUM_WATER);
            }
            
            // Finally, trigger a violation
            if (thisMove.hDistance > allowedElytraHDistance) {
                tags.add("e_hspeed(liq)");
                resultH = hDistance - allowedElytraHDistance;
            }
        }

        if (resultV > 0.0) {
            if (data.getOrUseVerticalVelocity(baseV) != null) {
                allowedElytraYDistance = yDistance;
                resultV = 0.0;
            }
        }

        thisMove.hAllowedDistance = allowedElytraHDistance;
        thisMove.yAllowedDistance = isNear(allowedElytraYDistance, yDistance, 0.001) ? yDistance : allowedElytraYDistance;
        return new double[] {resultV, resultH};
    }


    /**
     * Get vertical velocity stand behind this move 
     * @param lasthDistance
     * @param yDistance
     * @param radPitch pitch in Radians (elytra)
     * @param squaredCos squared of cos(radPitch) (elytra)
     * @param levitation (levitation level)
     * @param speed (elytra)
     * @param up (elytra)
     * @return baseV.
     */
    private double getBaseV(double lasthDistance, double yDistance, float radPitch, double squaredCos, double levitation, double speed, boolean up) { 

        double baseV = yDistance;

        if (levitation >= 0.0) {
            baseV /= Magic.FRICTION_MEDIUM_AIR;
            return (baseV - 0.01 * levitation) / 0.8 + 0.221;
        } 
        if (radPitch < 0.0 && !up) baseV -= lasthDistance * -Math.sin(radPitch) * 0.128;
        if (baseV < 0.0)  baseV /= (1.0 - (0.1 * squaredCos));
        baseV -= speed * (-1.0 + squaredCos * 0.75);
        return baseV;
    }
    
    private static boolean isNear(double a, double b, double c) {
        if (c < 0.0) return false;
        return Math.abs(a-b) <= c;
    }
    
    /**
     * 
     * @param yDistance
     * @param limitV
     * @param thisMove
     * @param lastMove
     * @param from
     * @param data
     * @return limitV
     */
    private double hackLytra(final double yDistance, final double limitV, final ServerPlayerMoveData thisMove, 
                             final ServerPlayerMoveData lastMove, final ServerPlayerLocation from, 
                             final MovingData data) {

        // TODO: Hack, move / config / something.
        // TODO: Confine more. hdist change relates to ydist change
        // TODO: Further: jumpphase vs. y-distance to set back. Problem: velocity
        // TODO: Further: record max h and descend speeds and relate to those.
        // TODO: Demand total speed to decrease.

        // Elytra jump, let hackElytraH hande it
        if (yDistance > 0.0 && yDistance < 0.42 && thisMove.touchedGround) {
            tags.add("e_jump");
            return yDistance;
        } 
        // Ignore slowfalling here
        else if (Bridge1_13.getSlowfallingAmplifier(from.getPlayer()) >= 0.0) {
            tags.add("e_slowfall");
            return yDistance;
        }
        // Do ignore riptiding.
        else if (Bridge1_13.isRiptiding(from.getPlayer())) {
            tags.add("e_riptide");
            return yDistance;
        }

        if (yDistance > Magic.GLIDE_DESCEND_PHASE_MIN && yDistance < 34.0 * Magic.GRAVITY_MAX 
            && (
                // Normal envelope.
                lastMove.hDistance < 3.3 && yDistance - lastMove.yDistance < lastMove.hDistance / 11.0
                // Inversion (neg -> pos).
                || lastMove.yDistance < -Magic.GRAVITY_SPAN 
                && yDistance < Magic.GRAVITY_MAX + Magic.GRAVITY_ODD && yDistance > Magic.GRAVITY_SPAN
            )
            && thisMove.hDistance < lastMove.hDistance
            && (lastMove.yDistance > 0.0 || lastMove.hDistance > 0.55) // Demand some speed on the transition.
            // Demand total speed to decrease somehow, unless for the very transition.
            //&& (thisMove.distanceSquared / lastMove.distanceSquared < 0.99
            //        || lastMove.yDistance < 0.0) // Might confine the latter something to be tested.
            ) {

            // (Increasing y-distance.)
            if (lastMove.hDistance > 0.51) {
                tags.add("e_asc1");
                return yDistance;
            }
            // (Decreasing y-distance.)
            else if (thisMove.hDistance > Magic.GRAVITY_MIN && yDistance < lastMove.yDistance) {

                final ServerPlayerMoveData pastMove1 = data.playerMoves.getSecondPastMove();
                if (pastMove1.toIsValid && pastMove1.to.extraPropertiesValid) {
                    // Demand this being the first one, or decreasing by a decent amount with past two moves.
                    if (
                        // First move rather decreasing.
                        pastMove1.yDistance < lastMove.yDistance 
                        // Decreasing by a reasonable (?) amount.
                        || yDistance - pastMove1.yDistance < -0.001) {
                        tags.add("e_asc2");
                        return yDistance;
                    }
                }
            }
        }

        // Elytra boost with fireworks rockets.
        if (yDistance > limitV && data.fireworksBoostDuration > 0 && lastMove.toIsValid
            && (
                yDistance >= lastMove.yDistance 
                || yDistance - lastMove.yDistance < Magic.GRAVITY_MAX
                // TODO: Head blocked -> friction does it?
            )
            && (
                yDistance - lastMove.yDistance < 0.79 // TODO
                || lastMove.yDistance < 0.0 && yDistance < 1.54
            )
            && yDistance < 1.67) {
            /*
             * TODO: Do cross check item consumption (do other events fire?).
             * [?on tick: expectations framework, check before tick and before
             * other inventory events, once set]
             */
            // TODO: Remove fumbling with magic constants.
            // TODO: Relate horizontal to vertical + relate to looking direction.
            // TODO: More invalidation conditions, like total age (checked elsewhere?).
            tags.add("fw_boost_asc");
            return yDistance;
        }

        return limitV;
    }

    /**
     * Descending phase vDist check
     * @param from
     * @param to
     * @param yDistance
     * @param flying
     * @param thisMove
     * @param lastMove
     * @param model
     * @param data
     * @param cc
     * @return limitV, resultV
     */
    private double[] vDistDescend(final ServerPlayerLocation from, final ServerPlayerLocation to, final double yDistance, final boolean flying, 
                                  final ServerPlayerMoveData thisMove, final ServerPlayerMoveData lastMove, final ModelFlying model, 
                                  final MovingData data, final MovingConfig cc) {
        double limitV = 0.0;
        double resultV = 0.0;
        
        if (model.getScaleSlowfallingEffect() && lastMove.modelFlying == thisMove.modelFlying 
            && data.liqtick <= 0 && !from.isOnClimbable() && !to.isOnClimbable()) {

            if (!thisMove.touchedGround && !to.isResetCond()) {

                final ServerPlayerMoveData pastmove2 = data.playerMoves.getSecondPastMove();
                final double allowY = lastMove.toIsValid ? lastMove.yDistance : 0.0;
                if (isCollideWithHB(from)) limitV = -0.05; 
                else if (from.isInBerryBush()) limitV = -0.085;
                else limitV = allowY * Magic.FRICTION_MEDIUM_AIR - 0.0097;// -0.0098
                if (!pastmove2.toIsValid && allowY < -0.035) limitV = -0.035;
                if (limitV != 0.0 && yDistance > limitV) resultV = Math.abs(limitV);
            }
        }
        // Note that 'extreme moves' are covered by the extreme move check.
        // TODO: if gravity: friction + gravity.
        // TODO: min-max envelope (elytra).
        // TODO: ordinary flying (flying: enforce maximum speed at least)
        return new double[] {limitV, resultV};
    }


    /**
     * Keep the altitude
     * @param from
     * @param to
     * @param yDistance
     * @param flying
     * @param thisMove
     * @param lastMove
     * @param model
     * @param data
     * @param cc
     * @return limitV, resultV
     */
    private double[] vDistZero(final ServerPlayerLocation from, final ServerPlayerLocation to, final double yDistance, final boolean flying, 
                               final ServerPlayerMoveData thisMove, final ServerPlayerMoveData lastMove, final ModelFlying model, 
                               final MovingData data, final MovingConfig cc) {

        double limitV = 0.0;
        double resultV = 0.0;

        if (model.getScaleSlowfallingEffect() && lastMove.modelFlying == thisMove.modelFlying && !to.isInWeb()) {
            if (thisMove.touchedGround || thisMove.from.onClimbable || data.liqtick > 0) {
                // Allow normal jumping.
                final double maxGain = LiftOffEnvelope.NORMAL.getMinJumpGain(data.jumpAmplifier) + 0.01;
                if (maxGain > limitV) limitV = maxGain;
            }
            if (limitV <= 0.0 && lastMove.yDistance == 0.0) resultV = 0.1;
        }

        // TODO: Deny on enforcing mingain.
        return new double[] {limitV, resultV};
    }


   /**
    * 
    * @param maximumHeight
    * @param world
    * @return 
    */
    private double getCorrectedHeight(final double maximumHeight, final World world) {
        return Math.max(maximumHeight - 10.0, world.getMaxHeight());
    }


   /**
    * Adds velocity to a player upon switching movement model, in order to
    * workaround false positives
    * @param player
    * @param thisMove
    * @param lastMove
    * @param model
    * @param data
    * @param cc
    * @author xaw3ep
    */
    private void workaroundSwitchingModel(final ServerPlayer player, final ServerPlayerMoveData thisMove, final ServerPlayerMoveData lastMove, 
                                          final ModelFlying model, final MovingData data, final MovingConfig cc, final boolean debug) {

        if (lastMove.toIsValid && lastMove.modelFlying != thisMove.modelFlying) {

            // Other modelflying -> levitation
            if (model.getScaleLevitationEffect()) {
                final double amount = lastMove.hAllowedDistance > 0.0 ? lastMove.hAllowedDistance : lastMove.hDistance;
                if (thisMove.touchedGround) data.addHorizontalVelocity(new AccountEntry(amount, 2, MovingData.getHorVelValCount(amount)));
                if (debug) debug(player, lastMove.modelFlying.getId().toString() + " -> potion.levitation: add velocity");
                return;
            }

            // Gliding -> Other modelflying
            if (lastMove.modelFlying != null && lastMove.modelFlying.getVerticalAscendGliding()) {
                final double amount = guessVelocityAmount(player, thisMove, lastMove, data);
                if (thisMove.touchedGround || model.getId().equals("gamemode.creative")) {
                    data.addHorizontalVelocity(new AccountEntry(amount, 3, MovingData.getHorVelValCount(amount)));
                    if (debug) debug(player, "Jetpack.elytra -> " + (thisMove.touchedGround ? "touchedGround" : "gamemode.creative") + ": add velocity");
                }

                if (model.getId().equals("gamemode.creative")) {
                    data.addVerticalVelocity(new SimpleEntry(0.0, 2));
                    if (debug) debug(player, "Jetpack.elytra -> gamemode.creative: add velocity");
                }
                return;
            }
            // TODO: Levitation -> Slow_falling
            // A ripglide phase has ended, smoothen the transition.
            if (lastMove.modelFlying != null && lastMove.modelFlying.getScaleRiptidingEffect() && thisMove.modelFlying.getVerticalAscendGliding()) {

                final double amount = guessVelocityAmount(player, thisMove, lastMove, data);
                if (!thisMove.from.onGround && !thisMove.to.onGround) {
                    data.addVerticalVelocity(new SimpleEntry(thisMove.yDistance, cc.velocityActivationCounter));
                    data.addVerticalVelocity(new SimpleEntry(0.0, cc.velocityActivationCounter));
                    data.addHorizontalVelocity(new AccountEntry(amount, 4, MovingData.getHorVelValCount(amount)));
                    if (debug) debug(player, "Effect.riptiding -> Jetpack.elytra: add velocity");
                }
                return;
            }
        }

        final ServerPlayerMoveData secondPastMove = data.playerMoves.getSecondPastMove();
        // Quick change between models, reset friction, invalid
        if (secondPastMove.modelFlying != null && lastMove.modelFlying != null
            && secondPastMove.modelFlying == model && model != lastMove.modelFlying) {
            if (debug) debug(player, "Invalidate this move on too fast model switch: " + (secondPastMove.modelFlying.getId().toString() + " -> " + lastMove.modelFlying.getId().toString() + " -> " + model.getId().toString()));
            thisMove.toIsValid = false;
            // thisMove.invalidate();
        }
    }


   /**
    * @param player
    * @param thisMove
    * @param lastMove
    * @param data
    * @return
    */
    private static double guessVelocityAmount(final ServerPlayer player, final ServerPlayerMoveData thisMove, final ServerPlayerMoveData lastMove, final MovingData data) {

        // Default margin: Allow slightly less than the previous speed.
        final double defaultAmount = lastMove.hDistance * (1.0 + Magic.FRICTION_MEDIUM_AIR) / 2.0;

        // Test for exceptions.
        if (thisMove.hDistance > defaultAmount && Bridge1_9.isWearingElytra(player) 
            && lastMove.modelFlying != null 
            && lastMove.modelFlying.getId().equals(MovingConfig.ID_JETPACK_ELYTRA)) {
            // Allowing the same speed won't always work on elytra (still increasing, differing modeling on client side with motXYZ).
            // (Doesn't seem to be overly effective.)

            final ServerPlayerMoveData secondPastMove = data.playerMoves.getSecondPastMove();
            if (secondPastMove.modelFlying != null
                && Magic.glideEnvelopeWithHorizontalGain(thisMove, lastMove, secondPastMove)) {
                return lastMove.hDistance + 0.1468;
            }
        }
        return defaultAmount;
    }
    
    public static double[] guessElytraVelocityAmount(final ServerPlayer player, final ServerPlayerMoveData thisMove, final ServerPlayerMoveData lastMove, final MovingData data) {
        final Location useLoc = new Location(null, 0, 0, 0);
        useLoc.setYaw(thisMove.to.getYaw());
        useLoc.setPitch(thisMove.to.getPitch());
        final double speed = Bridge1_13.getSlowfallingAmplifier(player) >= 0.0 ? 0.01 : 0.08;
        double allowedElytraYDistance = 0.0;
        double allowedElytraHDistance = 0.0;
        final double lastHdist = lastMove.toIsValid ? lastMove.hDistance : 0.0;
        final Vector lookvec = useLoc.getDirection();
        final float radPitch = (float) Math.toRadians(thisMove.to.getPitch());
        allowedElytraYDistance = lastMove.elytrafly ? lastMove.yAllowedDistance : lastMove.toIsValid ? lastMove.yDistance : 0.0;
        if (Math.abs(allowedElytraYDistance) < 0.003D) allowedElytraYDistance = 0.0D;
        final double xzlength = Math.sqrt(lookvec.getX() * lookvec.getX() + lookvec.getZ() * lookvec.getZ());
        double f4 = Math.cos(radPitch); f4 = f4 * f4;

        allowedElytraYDistance += speed * (-1.0D + f4 * 0.75D);
        double x = lastMove.to.getX() - lastMove.from.getX();
        double z = lastMove.to.getZ() - lastMove.from.getZ();
        
        if (allowedElytraYDistance < 0.0D && xzlength > 0.0) {
            final double d = allowedElytraYDistance * -0.1 * f4;
            x += lookvec.getX() * d / xzlength;
            z += lookvec.getZ() * d / xzlength;
            allowedElytraYDistance += d;
        }
        
        // Look up
        if (radPitch < 0.0F) {
            // For compatibility
            if (thisMove.to.getPitch() == -90f
                && isNear(thisMove.yDistance, allowedElytraYDistance * Magic.FRICTION_MEDIUM_ELYTRA_AIR, 0.01)) {
                allowedElytraHDistance += 0.01;
            }
            else if (xzlength > 0.0) {
                final double d = lastHdist * -Math.sin(radPitch) * 0.04;
                x -= lookvec.getX() * d / xzlength;
                z -= lookvec.getZ() * d / xzlength;
                allowedElytraYDistance += d * 3.2;
            } 
        }

        if (xzlength > 0.0) {
            x += (lookvec.getX() / xzlength * lastHdist - x) * 0.1D;
            z += (lookvec.getZ() / xzlength * lastHdist - z) * 0.1D;
        }
        
        if (data.fireworksBoostDuration > 0) {
            allowedElytraYDistance = Math.abs(thisMove.yDistance) < 2.0 ?
                    thisMove.yDistance : lastMove.toIsValid ? lastMove.yDistance : 0;
            if (Math.round(data.fireworksBoostTickNeedCheck / 4) > data.fireworksBoostDuration 
                && thisMove.hDistance < Math.sqrt(x*x + z*z)) {
                return new double[] {Math.sqrt(x*x + z*z), allowedElytraYDistance};
            }

            x *= 0.99;
            z *= 0.99;
            x += lookvec.getX() * 0.1D + (lookvec.getX() * 1.5D - x) * 0.5D;
            z += lookvec.getZ() * 0.1D + (lookvec.getZ() * 1.5D - z) * 0.5D;

            if (thisMove.hDistance < lastMove.hAllowedDistance * 0.994) {
                return new double[] {lastMove.hAllowedDistance * 0.994, allowedElytraYDistance};
            } 
            else allowedElytraHDistance += 0.2;
        }

        // Adjust false
        allowedElytraHDistance += Math.sqrt(x*x + z*z) + 0.1;
        return new double[] {allowedElytraHDistance, allowedElytraYDistance};
    }


  /**
    * @param from
    * @return
    */
    private boolean isCollideWithHB(PlayerLocation from) {
        return (from.getBlockFlags() & BlockProperties.F_STICKY) != 0;
    }


  /**
    * Output debug
    * @param player
    * @param hDistance
    * @param limitH
    * @param yDistance
    * @param limitV
    * @param model
    * @param tags
    * @param data
    * @return
    */
    private void outpuDebugMove(final ServerPlayer player, final double hDistance, final double limitH, 
                                final double yDistance, final double limitV, final ModelFlying model, final List<String> tags, 
                                final MovingData data) {

        final ServerPlayerMoveData lastMove = data.playerMoves.getFirstPastMove();
        StringBuilder builder = new StringBuilder(350);
        final String dHDist = lastMove.toIsValid ? " (" + StringUtil.formatDiff(hDistance, lastMove.hDistance) + ")" : "";
        final String dYDist = lastMove.toIsValid ? " (" + StringUtil.formatDiff(yDistance, lastMove.yDistance)+ ")" : "";
        builder.append("hDist: " + hDistance + dHDist + " / " + limitH + " , vDist: " + yDistance + dYDist + " / " + limitV);
        final ServerPlayerMoveData thisMove = data.playerMoves.getCurrentMove();
        if (lastMove.toIsValid) {
            builder.append(" , fdsq: " + StringUtil.fdec3.format(thisMove.distanceSquared / lastMove.distanceSquared));
        }
        if (thisMove.verVelUsed != null) {
            builder.append(" , vVelUsed: " + thisMove.verVelUsed);
        }
        if (data.fireworksBoostDuration > 0 && MovingConfig.ID_JETPACK_ELYTRA.equals(model.getId())) {
            builder.append(" , boost: " + data.fireworksBoostDuration);
        }
        if (thisMove.elytrafly) {
            builder.append(", elytraFly");
        }
        builder.append(" , model: " + model.getId());
        if (!tags.isEmpty()) {
            builder.append(" , tags: ");
            builder.append(StringUtil.join(tags, "+"));
        }
        builder.append(" , jumpphase: " + data.sfJumpPhase);
        thisMove.addExtraProperties(builder, " , ");
        debug(player, builder.toString());
    }
}
