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
package fr.neatmonster.nocheatplus.compat;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Entity;
import net.minecraft.server.level.ServerPlayer;

import fr.neatmonster.nocheatplus.components.entity.IEntityAccessDimensions;
import fr.neatmonster.nocheatplus.components.map.IGetBlockCache;
import fr.neatmonster.nocheatplus.utilities.map.BlockCache;


/**
 * Compatibility interface to get properties for Bukkit instances that need
 * access of CraftBukkit or Minecraft classes.<br>
 * NOTE: All methods returning AlmostBoolean must never return null, unless
 * stated otherwise.<br>
 * NOTE: Expect API changes in the near future!<br>
 * NOTE: If an instance implements BlockPropertiesSetup, the setup method will
 * be called after basic initialization but before configuration is applied.<br>
 * <hr>
 * TODO: Make minimal.
 * 
 * @author asofold
 *
 */
public interface MCAccess extends IGetBlockCache, IEntityAccessDimensions {

    /**
     * Simple/rough version information, separate several individual versions by
     * '|' ranges with '-', potential future support with '?', e.g.
     * "1.x.1|1.x.2|2.3.4-3.4.5|?". For large ranges, don't expect all versions
     * between to be supported.
     * 
     * @return
     */
    public String getMCVersion();

    /**
     * Server version tag, like CB 2511.
     * @return
     */
    public String getServerVersionTag();

    /**
     * Get the servers command map.
     * @return May return null if not supported.
     */
    public CommandMap getCommandMap();

    /**
     * Retrieve a new BlockCache instance with access set to null.
     */
    @Override
    public BlockCache getBlockCache();

    /**
     * Get a new BlockCache instance.
     * 
     * @param world
     *            May be null to store an instance of BlockCache for future use.
     * @return
     */
    public BlockCache getBlockCache(World world);

    @Override
    public double  getHeight(Entity entity);

    @Override
    public double getWidth(Entity entity);

    /**
     * NMS Block static.
     * @param id
     * @return MAYBE if undecided, YES or NO if decided.
     */
    public AlmostBoolean isBlockSolid(Material id);

    /**
     * NMS Block static..
     * @param id
     * @return MAYBE if undecided, YES or NO if decided.
     */
    public AlmostBoolean isBlockLiquid(Material id);

    /**
     * Does only check y bounds, returns false if dead. This is called by
     * PlayerLocation.hasIllegalStance(), PlayerLocation.hasIllegalCoords()
     * should always be checked first.
     * 
     * @param player
     * @return MAYBE if undecided, YES or NO if decided.
     */
    public AlmostBoolean isIllegalBounds(Player player);

    /**
     * Potion effect jump amplifier.
     * 
     * @param player
     * @return Double.NEGATIVE_INFINITY if not present.
     */
    public double getJumpAmplifier(Player player);

    /**
     * Potion effect speed amplifier.
     * 
     * @return Double.NEGATIVE_INFINITY if not present.
     */
    public double getFasterMovementAmplifier(Player player);

    /**
     * 
     * @param player
     * @return Integer.MAX_VALUE if not available (!).
     */
    public int getInvulnerableTicks(Player player);

    /**
     * 
     * @param player
     * @param ticks
     */
    public void setInvulnerableTicks(Player player, int ticks);

    /**
     * Deal damage with DamageCause.FALL as cause.
     * 
     * @param player
     * @param damage
     */
    public void dealFallDamage(Player player, double damage);

    /**
     * If dealFallDamage(Player, double) will fire a damage event.
     * @return
     */
    public AlmostBoolean dealFallDamageFiresAnEvent();

    /**
     * This may well be removed, if possible to check with Bukkit.
     * @param damaged
     * @return
     */
    public boolean isComplexPart(Entity damaged);

    /**
     * Tests if player is not set to dead but has no health.
     * @param player
     * @return
     */
    public boolean shouldBeZombie(Player player);

    /**
     * Ensure the player is really taken out: Set flag + death ticks.
     * 
     * TODO: Check if still necessary + make knowledge-base entries for what to check.
     * 
     * @param player
     * @param deathTicks
     */
    public void setDead(Player player, int deathTicks);

    /**
     * Usually sand and gravel. Not for fastest access.
     * @param type
     * @return
     */
    public boolean hasGravity(Material type);

    //	/**
    //	 * Correct the direction (yaw + pitch). If this can't be done lightly it should just do nothing. Check pitch and yaw before calling, use auxiliary methods from LocUtil.
    //	 * @param player
    //	 */
    //	public void correctDirection(Player player);

    /**
     * Reset active item
     * @param player
     * @return true if can reset and vice versa
     */
    public boolean resetActiveItem(Player player);
}
