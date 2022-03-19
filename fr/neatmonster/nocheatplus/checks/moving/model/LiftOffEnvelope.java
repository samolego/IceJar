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
package fr.neatmonster.nocheatplus.checks.moving.model;

/**
 * Basic preset envelopes for moving off one medium.
 * 
 * @author asofold
 *
 */
public enum LiftOffEnvelope {
    /** Normal in-air lift off without any restrictions/specialties. */
    NORMAL(0.42, 1.35, 1.15, 6, true),
    /** Weak or no limit moving off liquid near ground. */
    LIMIT_NEAR_GROUND(0.42, 1.35, 1.15, 6, false), // TODO: 0.385 / not jump on top of 1 high wall from water.
    /** Simple calm water surface. */
    LIMIT_LIQUID(0.1, 0.27, 0.07, 3, false),
    //    /** Flowing water / strong(-est) limit. */
    //    LIMIT_LIQUID_STRONG(...), // TODO
    /** No jumping at all (web). */
    NO_JUMP(0.0, 0.0, 0.0, 0, false),
    /** Like NO_JUMP, just to distinguish from being in web. */
    UNKNOWN(0.0, 0.0, 0.0, 0, false),
    /** Halfed jump gain, meant for the honey block, rather. */
    // NOTE: Jump height: 0.3 would trigger false positives. While 0.45 is too much
    HALF_JUMP(0.21, 0.4, 0.2, 4, true), 
    /** Nearly ordinary jumping gain (meant for berry bushes)*/
    // TEST: Jumping height is random (but higher than the honeyblock), needs testing to be more strict.
    BERRY_JUMP(0.35, 0.54, 0.34, 0, true), 
    // Powder snow is considered as reset condition so we don't care about the jump phase.
    /** Special liftoff handling for powder snow: higher than ordinary despite not reaching actual full block height */
    POWDER_SNOW(0.63, 0.63, 0.43, 0, true),
    /** Jumping up stairs. Not ordinary ground-to-ground stepping because this game's movement mechanics are trash. */
    // TODO: Get rid of the F_GROUND_HEIGHT flag for stairs and handle them with their own liftoff envelope (vDistRel or separated handling)
    STAIRS(0.5, 1.35, 1.15, 3, false) // Jump height 0.5 as well?
    ;

    private double maxJumpGain;
    private double maxJumpHeight;
    private double minJumpHeight;
    private int maxJumpPhase;
    private boolean jumpEffectApplies;

    private LiftOffEnvelope(double maxJumpGain, double maxJumpHeight, double minJumpHeight, int maxJumpPhase, boolean jumpEffectApplies) {
        this.maxJumpGain = maxJumpGain; //(Lift-off speed gain)
        this.maxJumpHeight = maxJumpHeight; //(Actual jump height)
        this.minJumpHeight = minJumpHeight;
        this.maxJumpPhase = maxJumpPhase;
        this.jumpEffectApplies = jumpEffectApplies;
    }

    /**
     * Minimal distance expected with lift-off.
     * 
     * @param jumpAmplifier
     * @return The minimum yDistance players can achieve for this envelope
     */
    public double getMinJumpGain(double jumpAmplifier) {
        if (jumpEffectApplies && jumpAmplifier != 0.0) {
            return Math.max(0.0, maxJumpGain + 0.1 * jumpAmplifier);
        }
        else {
            return maxJumpGain;
        }
    }

    /**
     * Minimal distance expected with lift-off, with a custom factor for the jump amplifier.
     * @param jumpAmplifier
     * @param factor
     * @return
     */
    public double getMinJumpGain(double jumpAmplifier, double factor) {
        if (jumpEffectApplies && jumpAmplifier != 0.0) {
            return Math.max(0.0, maxJumpGain + 0.1 * jumpAmplifier * factor);
        }
        else {
            return maxJumpGain;
        }
    }

    /**
     * Maximum distance expected with lift-off.
     * 
     * @param jumpAmplifier
     * @return The maximum yDistance players can achieve for this envelope
     */
    public double getMaxJumpGain(double jumpAmplifier) {
        if (jumpEffectApplies && jumpAmplifier != 0.0) {
            return Math.max(0.0, maxJumpGain + 0.2 * jumpAmplifier);
        }
        else {
            return maxJumpGain;
        }
    }

    /**
     * Minimal jump height in blocks.
     * @param jumpAmplifier
     * @param factor
     * @return
     */
    public double getMinJumpHeight(double jumpAmplifier) {
        if (jumpEffectApplies && jumpAmplifier != 0.0) {
            return minJumpHeight + (0.5 * jumpAmplifier);
        }
        else {
            return minJumpHeight;
        }
    }
    
    /**
     * Maximum jump height in blocks.
     * 
     * @param jumpAmplifier
     * @return The maximum jump height for this envelope
     */
    public double getMaxJumpHeight(double jumpAmplifier) {
        if (jumpEffectApplies && jumpAmplifier > 0.0) {
            // Note: The jumpAmplifier value is one higher than the MC level.
            if (jumpAmplifier < 10.0) {
                // Classic.
                // TODO: Can be confined more.
                return maxJumpHeight + 0.6 + jumpAmplifier - 1.0;
            }
            else if (jumpAmplifier < 19){
                // Quadratic, without accounting for gravity.
                return 0.6 + (jumpAmplifier + 3.2) * (jumpAmplifier + 3.2) / 16.0;
            }
            else {
                // Quadratic, with some amount of gravity counted in.
                return 0.6 + (jumpAmplifier + 3.2) * (jumpAmplifier + 3.2) / 16.0 - (jumpAmplifier * (jumpAmplifier - 1.0) / 2.0) * (0.0625 / 2.0);
            }
        } // TODO: < 0.0 ?
        else {
            return maxJumpHeight;
        }
    }
    
    /**
     * How many in-air events players can achieve before losing
     * altitude.
     * 
     * @param jumpAmplifier
     * @return The maximum jump phase for this envelope.
     */
    public int getMaxJumpPhase(double jumpAmplifier) {
        if (jumpEffectApplies && jumpAmplifier > 0.0) {
            return (int) Math.round((0.5 + jumpAmplifier) * (double) maxJumpPhase);
        } // TODO: < 0.0 ?
        else {
            return maxJumpPhase;
        }
    }
   
    public boolean jumpEffectApplies() {
        return jumpEffectApplies;
    }
}
